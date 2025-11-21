package web.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import web.model.dto.game.GameDto;
import web.model.dto.game.GameLogDto;
import web.model.dto.point.PointRecordDto;
import web.model.entity.game.GameEntity;
import web.model.entity.game.GameLogEntity;
import web.model.mapper.PointMapper;
import web.model.mapper.UserMapper;
import web.repository.GameLogRepository;
import web.repository.GameRepository;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final GameLogRepository gameLogRepository;
    private final UserMapper userMapper;
    private final PointMapper pointMapper;

    // 게임 포인트 정책 번호
    private static final int GAME_POINT_NO = 5;

    /**
     * [GL-01] 게임 기록 생성
     * - 게임 결과를 저장하고 성공 시 포인트 적립
     *
     * @param gameLogDto 게임 기록 DTO
     * @return 저장된 게임 기록 DTO
     */
    public GameLogDto createGameLog(GameLogDto gameLogDto) {
        try {
            log.info("🎮 게임 기록 저장 시작 - gameNo: {}, userNo: {}, score: {}, result: {}",
                    gameLogDto.getGameNo(),
                    gameLogDto.getUserNo(),
                    gameLogDto.getGameScore(),
                    gameLogDto.getGameResult());

            // 1. 게임 엔티티 존재 확인 (영속 상태로 조회)
            GameEntity gameEntity = gameRepository.findById(gameLogDto.getGameNo())
                    .orElseThrow(() -> {
                        log.error("❌ 존재하지 않는 게임 - gameNo: {}", gameLogDto.getGameNo());
                        return new RuntimeException("존재하지 않는 게임입니다. gameNo: " + gameLogDto.getGameNo());
                    });

            log.info("✅ 게임 엔티티 조회 성공 - gameTitle: {}", gameEntity.getGameTitle());

            // 2. GameLogEntity 생성 (영속 상태의 GameEntity 참조)
            GameLogEntity gameLogEntity = GameLogEntity.builder()
                    .gameResult(gameLogDto.getGameResult())
                    .gameScore(gameLogDto.getGameScore())
                    .userNo(gameLogDto.getUserNo())
                    .gameEntity(gameEntity)  // ✅ 영속 상태의 엔티티 참조
                    .build();

            // 3. 게임 기록 저장
            GameLogEntity savedEntity = gameLogRepository.save(gameLogEntity);
            log.info("✅ 게임 기록 저장 완료 - gameLogNo: {}", savedEntity.getGameLogNo());

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 4. 게임 결과에 따른 포인트 적립
            // gameResult: 0=실패, 1=성공, 2=대성공
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            try {
                if (savedEntity.getGameResult() >= 1) {
                    log.info("🎁 포인트 적립 시작 - userNo: {}, gameResult: {}",
                            savedEntity.getUserNo(),
                            savedEntity.getGameResult());

                    PointRecordDto pointRecord = new PointRecordDto();
                    pointRecord.setPointNo(GAME_POINT_NO);
                    pointRecord.setUserNo(savedEntity.getUserNo());

                    // 포인트 적립 실행
                    int insertResult = pointMapper.insertPointRecord(pointRecord);

                    if (insertResult > 0) {
                        log.info("✅ 포인트 적립 완료 - userNo: {}, pointNo: {}",
                                savedEntity.getUserNo(),
                                GAME_POINT_NO);
                    } else {
                        log.warn("⚠️ 포인트 적립 실패 - insertResult: {}", insertResult);
                    }
                }
            } catch (Exception pointError) {
                // 포인트 적립 실패해도 게임 기록은 유지
                log.error("⚠️ 포인트 적립 중 오류 발생 (게임 기록은 저장됨)", pointError);
            }

            // 5. 저장된 엔티티를 DTO로 변환하여 반환
            return savedEntity.toDto();

        } catch (Exception e) {
            log.error("❌ 게임 기록 저장 실패", e);
            throw new RuntimeException("게임 기록 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * [GL-02] 내 게임 기록 전체 조회
     */
    public List<GameLogDto> getMyGameLog(Integer userNo) {
        List<GameLogEntity> list = gameLogRepository.findByUserNo(userNo);
        return list.stream()
                .map(GameLogEntity::toDto)
                .collect(Collectors.toList());
    }

    /**
     * [GL-03] 내 게임 기록 상세 조회
     */
    public GameLogDto getMyGameLogDetail(int userNo, int gameLogNo) {
        Optional<GameLogEntity> optional = gameLogRepository.findById(gameLogNo);
        if (optional.isPresent()) {
            GameLogEntity gameLogEntity = gameLogRepository.findByUserNoAndGameLogNo(userNo, gameLogNo);
            return gameLogEntity != null ? gameLogEntity.toDto() : null;
        }
        return null;
    }

    /**
     * [AGL-01] 게임 기록 삭제 (관리자)
     */
    public boolean deleteGameLog(Integer gameLogNo, Integer userNo) {
        try {
            if (gameLogNo != null && userNo == null) {
                if (!gameLogRepository.existsById(gameLogNo)) {
                    return false;
                }
                gameLogRepository.deleteById(gameLogNo);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("게임기록 삭제 실패: {}", e.getMessage());
            throw new RuntimeException("게임기록 삭제 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자 탈퇴 시 게임 기록 삭제
     */
    public void deleteGameLogByUser(int userNo) {
        try {
            gameLogRepository.deleteAllByUserNo(userNo);
            log.info("유저번호 {}의 게임 기록이 모두 삭제되었습니다.", userNo);
        } catch (Exception e) {
            throw new RuntimeException("게임 기록 삭제 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * [AGL-02] 게임 전체 기록 조회 (관리자)
     */
    public List<GameLogDto> getGameLog() {
        List<GameLogEntity> gameLogEntityList = gameLogRepository.findAllWithGameTitle();
        return gameLogEntityList.stream()
                .map(entity -> {
                    GameLogDto dto = entity.toDto();

                    if (entity.getGameEntity() != null) {
                        dto.setGameTitle(entity.getGameEntity().getGameTitle());
                    }

                    try {
                        String email = userMapper.findEmailByUserNo(entity.getUserNo());
                        dto.setEmail(email != null ? email : "알 수 없음");
                    } catch (Exception e) {
                        dto.setEmail("알 수 없음");
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * [AGL-03] 게임 상세 기록 조회 (관리자)
     */
    public GameLogDto getGameLogDetail(int gameLogNo) {
        GameLogEntity gameLogEntity = gameLogRepository.findByIdWithGameTitle(gameLogNo);

        if (gameLogEntity != null) {
            GameLogDto dto = gameLogEntity.toDto();

            if (gameLogEntity.getGameEntity() != null) {
                dto.setGameTitle(gameLogEntity.getGameEntity().getGameTitle());
            }

            try {
                String email = userMapper.findEmailByUserNo(gameLogEntity.getUserNo());
                dto.setEmail(email != null ? email : "알 수 없음");
            } catch (Exception e) {
                dto.setEmail("알 수 없음");
            }

            return dto;
        }

        return null;
    }

    /**
     * [AG-01] 게임 종류 추가 (관리자)
     */
    public GameDto createGame(GameDto gameDto) {
        GameEntity gameEntity = gameDto.toEntity();
        GameEntity saveEntity = gameRepository.save(gameEntity);
        if (saveEntity.getGameNo() >= 0) {
            return saveEntity.toDto();
        }
        return gameDto;
    }

    /**
     * [AG-02] 게임 전체 조회 (관리자)
     */
    public List<GameDto> getGame() {
        List<GameEntity> gameEntityList = gameRepository.findAll();
        return gameEntityList.stream()
                .map(GameEntity::toDto)
                .collect(Collectors.toList());
    }

    /**
     * [AG-03] 게임 상세 조회 (관리자)
     */
    public GameDto getGameDetail(int gameNo) {
        Optional<GameEntity> optional = gameRepository.findById(gameNo);
        if (optional.isPresent()) {
            GameEntity gameEntity = optional.get();
            return gameEntity.toDto();
        }
        return null;
    }

    /**
     * [AG-04] 게임 삭제 (관리자)
     */
    public boolean deleteGame(int gameNo) {
        try {
            if (!gameRepository.existsById(gameNo)) {
                return false;
            }

            GameEntity gameEntity = gameRepository.findById(gameNo)
                    .orElseThrow(() -> new RuntimeException("게임을 찾을 수 없습니다."));

            if (!gameEntity.getGameLogEntityList().isEmpty()) {
                throw new RuntimeException("해당 게임에 연관된 게임 기록이 존재하여 삭제할 수 없습니다.");
            }

            gameRepository.deleteById(gameNo);
            return true;
        } catch (Exception e) {
            log.error("게임 삭제 실패: {}", e.getMessage());
            throw new RuntimeException("게임 삭제 중 오류가 발생했습니다.", e);
        }
    }
}