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
    // [*] DI
    private final GameRepository gameRepository;
    private final GameLogRepository gameLogRepository;
    private final UserMapper userMapper;
    private final PointMapper pointMapper;

    private final int game_pointNo = 5;

    // [GL-01] 게임기록생성 createGameLog()
//  - 사용자가 게임을 종료하면 해당 기록을 테이블에 저장한다.
//  - 게임 결과에 따라 해당 사용자의 포인트가 증가한다.
//  - 게임 점수에 따라 랭킹 테이블에 반영될 수 있다.
//  - Game 테이블을 FK로 참조한다.
    public GameLogDto createGameLog(GameLogDto gameLogDto) {
        // 1. dto -> entity 변환
        GameLogEntity gameLogEntity = gameLogDto.toEntity();

        // 2. JPA save() 로 영속화
        GameLogEntity saveEntity = gameLogRepository.save(gameLogEntity);

        // 3. 저장 성공 여부 확인 (PK가 정상 발급되었는지)
        if ( saveEntity.getGameLogNo() <= 0 ) {
            // 실패 시, 원본 dto 그대로 반환
            return gameLogDto;
        }
    // [GL-01]	게임기록생성	createGameLog()	사용자가 게임을 종료하면 해당 기록을 테이블에 저장한다.
    // * 게임 결과에 따라 해당 사용자의 포인트가 증가한다.
    // * 게임 점수에 따라 랭킹 테이블에 반영될 수 있다.
    // * 게임 테이블 FK로 받는다
    // 251120 - detached entity 문제 해결
    public GameLogDto createGameLog(GameLogDto gameLogDto) { // 1. 저장할 dto 매개변수 넣기
        try {

            log.info("\uD83C\uDFAE 게임 기록 저장 시작 - gameNo: {} , userNo: {} , score: {}" ,
                    gameLogDto.getGameNo(), gameLogDto.getUserNo(), gameLogDto.getGameScore());

            // 1. 게임 엔티티 존재 확인 (DB 조회)
            GameEntity gameEntity = gameRepository.findById(gameLogDto.getGameNo())
                    .orElseThrow(() -> {
                       log.error("❌ 존재하지 않는 게임 - gameNo: {}\", gameLogDto.getGameNo());");
                       return new RuntimeException("존재하지 않는 게임입니다. gameNo: " + gameLogDto.getGameNo());
                    });

            log.info("✅ 게임 엔티티 조회 성공 - gameTitle: {}", gameEntity.getGameTitle());

            // 2. dto -> entity로 변환
            GameLogEntity gameLogEntity = gameLogDto.toEntity();

            // 3. .save() 이용한 엔티티 영속화
            GameLogEntity savedEntity = gameLogRepository.save(gameLogEntity);
            log.info("✅ 게임 기록 저장 완료 - gameLogNo: {}", savedEntity.getGameLogNo());

            // 4 dto 변환 및 반환
            return savedEntity.toDto();

        } catch (Exception e) {
            log.error("❌ 게임 기록 저장 실패" , e);
            throw new RuntimeException("게임 기록 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }


    }

        // ─────────────────────────────────────────────
        // 4. 게임 결과에 따라 포인트 적립
        //  - 예) gameResult = 1 이면 성공, 0 이면 실패라고 가정
        //  - 또는 "SUCCESS"/"FAIL" 같은 문자열이면 그에 맞게 비교
        // ─────────────────────────────────────────────
        boolean isSuccess = saveEntity.getGameResult() == 1;
        // 문자열이면 예: "SUCCESS".equalsIgnoreCase(saveEntity.getGameResult())

        if (isSuccess) {
            PointRecordDto record = new PointRecordDto();
            record.setPointNo(game_pointNo);   // "게임 성공" 포인트 정책 번호
            record.setUserNo(saveEntity.getUserNo());   // 게임 로그에 이미 userNo 있지?

            pointMapper.insertPointRecord(record);
        }

        // 5. 최종적으로 저장된 엔티티를 DTO로 변환해서 반환
        return saveEntity.toDto();
    }

    // [GL-02] 내 게임기록 전체 조회
    public List<GameLogDto> getMyGameLog(Integer userNo) {
        List<GameLogEntity> list = gameLogRepository.findByUserNo(userNo);
        return list.stream()
                .map(GameLogEntity::toDto)
                .collect(Collectors.toList());
    }

    // [GL-03]	내 게임기록 상세조회	getMyGameLogDetail()	사용자(본인)의 게임기록을 상세 조회한다
    public GameLogDto getMyGameLogDetail(int userNo, int gameLogNo) {
        // 1. gameLogNo(pk)로 엔티티 조회
        Optional<GameLogEntity> optional = gameLogRepository.findById(gameLogNo);
        // 2. 존재 여부 확인 후 있으면 반환할 쿼리메소드 가져오기
        if (optional.isPresent()) {
            GameLogEntity gameLogEntity = gameLogRepository.findByUserNoAndGameLogNo(userNo, gameLogNo);
            return gameLogEntity.toDto();
        }
        // 3. 없으면 null
        return null;
    }

    // [AGL-01]	게임기록 삭제(관리자단)	deleteGameLog()	게임 기록 테이블을 삭제한다.
    // * 관리자가 부정한 게임 기록을 임의로 삭제한다.

    public boolean deleteGameLog(Integer gameLogNo, Integer userNo) {
        try {
            // [1] 관리자 : 특정 게임 기록 번호로 삭제
            if (gameLogNo != null && userNo == null) {
                // 1. gameLogNo(pk)로 엔티티 조회
                if (!gameLogRepository.existsById(gameLogNo)) {
                    return false;
                }
                gameLogRepository.deleteById(gameLogNo);
                return true;

                // [2] 잘못된 요청일 경우 (둘 다 값이 있거나 둘 다 null)
            }  else {
                return false;
            }
        } catch (Exception e) {
            System.err.println("게임기록 삭제 실패:" + e.getMessage());
            throw new RuntimeException("게임기록 삭제 중 오류가 발생했습니다." + e);
        }
    }

    // * 사용자가 탈퇴했을 경우, 게임 기록을 삭제한다.
    public void deleteGameLogByUser(int userNo) {
        try {
            gameLogRepository.deleteAllByUserNo(userNo);
            System.out.println("유저번호 " + userNo + "의 게임 기록이 모두 삭제되었습니다.");
        } catch (Exception e) {
            throw new RuntimeException("게임 기록 삭제 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // [AGL-02]	게임전체기록 조회 (관리자단)	getGameLog()	게임기록 전체를 조회한다.
    public List<GameLogDto> getGameLog() {
        // 1. 모든 엔티티 조회 및 스트림으로 엔티티 -> dto 변환 (+게임명도 함께 조회)
        List<GameLogEntity> gameLogEntityList = gameLogRepository.findAllWithGameTitle();
        // 2. 모든 엔티티 dto 변환
        List<GameLogDto> gameLogDtoList = gameLogEntityList
                .stream().map(entity -> {
                    GameLogDto dto = entity.toDto();

                    // 게임명 추가 (JPA Entity에서 가져옴)
                    if (entity.getGameEntity() != null) {
                        dto.setGameTitle(entity.getGameEntity().getGameTitle());
                    }

                    // 이메일 추가 (MyBatis Mapper로 조회)
                    try {
                        String email = userMapper.findEmailByUserNo(entity.getUserNo());
                        dto.setEmail(email != null ? email : "알 수 없음");
                    } catch (Exception e) {
                        dto.setEmail("알 수 없음");
                    }
                    // dto 반환
                    return dto;
                }).collect(Collectors.toList());

        // 3. dto list 배열 반환
        return gameLogDtoList;
    }

    // [AGL-03]	게임상세기록 조회 (관리자단)	getGameLogDetail()	게임 기록을 상세 조회한다.
    public GameLogDto getGameLogDetail(int gameLogNo) {
        // 1. 엔티티 조회 및 dto 변환
        GameLogEntity gameLogEntity = gameLogRepository.findByIdWithGameTitle(gameLogNo);

        if (gameLogEntity != null) {
            GameLogDto dto = gameLogEntity.toDto();

            // 게임명 추가 (JPA Entity에서 가져옴)
            if (gameLogEntity.getGameEntity() != null) {
                dto.setGameTitle(gameLogEntity.getGameEntity().getGameTitle());
            }

            // 이메일 추가 (MyBatis Mapper로 조회)
            try {
                String email = userMapper.findEmailByUserNo(gameLogEntity.getUserNo());
                dto.setEmail(email != null ? email : "알 수 없음");
            } catch (Exception e) {
                dto.setEmail("알 수 없음");
            }

            // dto 반환
            return dto;
        }

        // 3. 없음 null이지
        return null;
    }

    // [AGL-04]	게임 종류 추가(관리자단)	createGame()	게임 테이블을 추가한다.
    // * 실제 게임은 플러터 assets 폴더에 추가해야합니다.
    public GameDto createGame(GameDto gameDto) { // 1. 저장할 dto 매개변수 받기
        // 2. 저장할 dto를 entity로 변환
        GameEntity gameEntity = gameDto.toEntity();
        // 3. .save() 이용한 엔티티 영속화
        GameEntity saveEntity = gameRepository.save(gameEntity);
        // 4-1. 만약 PK가 생성되었으면 생성된 엔티티를 dto로 변환하여 반환
        if (saveEntity.getGameNo() >= 0) {
            return saveEntity.toDto();
        }
        // 4-2. 실패
        return gameDto;
    }

    // [AGL-05]	게임 전체조회(관리자단)	getGame()	게임 테이블을 전체조회한다.
    public List<GameDto> getGame() {
        // 1. 모든 엔티티 조회
        List<GameEntity> gameEntityList = gameRepository.findAll();
        // 2. 모든 엔티티 dto 변환
        List<GameDto> gameDtoList = gameEntityList
                .stream().map(GameEntity::toDto)
                .collect(Collectors.toList());
        // 3. dto list로 반환
        return gameDtoList;
    }

    // [AGL-06]	게임 상세조회(관리자단)	getDetailGame()	게임 테이블을 상세조회한다.
    public GameDto getGameDetail(int gameNo) {
        // 1. gameNo로 엔티티 조회
        Optional<GameEntity> optional = gameRepository.findById(gameNo);
        // 2. 존재 여부 확인
        if (optional.isPresent()) {
            GameEntity gameEntity = optional.get();
            return gameEntity.toDto();
        }
        // 3. 없으면 null
        return null;
    }

    // [AGL-07]	게임 삭제(관리자단)	deleteGame()	게임 테이블을 삭제한다.
    public boolean deleteGame(int gameNo) {
        try {
            if (!gameRepository.existsById(gameNo)) {
                return false;
            }

            // ⭐ 연관된 게임 기록이 있는지 확인
            GameEntity gameEntity = gameRepository.findById(gameNo)
                    .orElseThrow(() -> new RuntimeException("게임을 찾을 수 없습니다."));

            if (!gameEntity.getGameLogEntityList().isEmpty()) {
                throw new RuntimeException("해당 게임에 연관된 게임 기록이 존재하여 삭제할 수 없습니다.");
            }

            gameRepository.deleteById(gameNo);
            return true;
        } catch (Exception e) {
            System.err.println("게임 삭제 실패: " + e.getMessage());
            throw new RuntimeException("게임 삭제 중 오류가 발생했습니다.", e);
        }
    }

}
