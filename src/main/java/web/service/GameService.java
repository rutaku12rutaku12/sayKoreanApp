package web.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import web.model.dto.game.GameDto;
import web.model.dto.game.GameLogDto;
import web.model.entity.game.GameEntity;
import web.model.entity.game.GameLogEntity;
import web.repository.GameLogRepository;
import web.repository.GameRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class GameService {
    // [*] DI
    private final GameRepository gameRepository;
    private final GameLogRepository gameLogRepository;

    // [GL-01]	게임기록생성	createGameLog()	사용자가 게임을 종료하면 해당 기록을 테이블에 저장한다.
    // * 게임 결과에 따라 해당 사용자의 포인트가 증가한다.
    // * 게임 점수에 따라 랭킹 테이블에 반영될 수 있다.
    // * 게임 테이블 FK로 받는다
    public GameLogDto createGameLog(GameLogDto gameLogDto) { // 1. 저장할 dto 매개변수 넣기
        // 2. dto -> entity로 변환
        GameLogEntity gameLogEntity = gameLogDto.toEntity();
        // 3. .save() 이용한 엔티티 영속화
        GameLogEntity saveEntity = gameLogRepository.save(gameLogEntity);
        // 4-1. 성공) PK 생성 시, 생성된 엔티티 -> dto 변환 및 반환
        if (saveEntity.getGameLogNo() >= 0) {
            return saveEntity.toDto();
        }
        // 4-2. 실패) PK 없으면 dto 반환
        return gameLogDto;
    }

    // [GL-02]	내 게임기록 전체조회	getMyGameLog()	사용자(본인)의 게임기록 전체를 조회한다
    public List<GameLogDto> getMyGameLog(int userNo) {
        // 1. 모든 엔티티 조회 및 스트림으로 엔티티 -> dto 변환
        List<GameLogDto> gameLogDtoList = gameLogRepository.findByUserNo(userNo)
                .stream().map( GameLogEntity::toDto)
                .collect(Collectors.toList());
        // 2. dto 배열 반환
        return gameLogDtoList;
    }

    // [GL-03]	내 게임기록 상세조회	getMyGameLogDetail()	사용자(본인)의 게임기록을 상세 조회한다
    public GameLogDto getMyGameLogDetail(int userNo, int gameLogNo) {
        // 1. gameLogNo(pk)로 엔티티 조회
        Optional<GameLogEntity> optional =gameLogRepository.findById(gameLogNo);
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
    // * 사용자가 탈퇴했을 경우, 게임 기록을 삭제한다.
    public boolean deleteGameLog(Integer gameLogNo ,Integer userNo) {
        try{
            // [1] 관리자 : 특정 게임 기록 번호로 삭제
            if (gameLogNo != null && userNo == null) {
                // 1. gameLogNo(pk)로 엔티티 조회
                if (!gameLogRepository.existsById(gameLogNo)) {
                    return false;
                }
                   gameLogRepository.deleteById(gameLogNo);
                   return true;

                // [2] 회원탈퇴 : 특정 회원의 모든 게임 기록삭제
                } else if (userNo != null && gameLogNo == null) {
                List<GameLogEntity> logs = gameLogRepository.findByUserNo(userNo);
                if(logs.isEmpty()){
                    return false;
                }

                gameLogRepository.deleteAllByUserNo(userNo);
                return true;
            }

            // [3] 잘못된 요청일 경우 (둘 다 값이 있거나 둘 다 null)
            else {
                return false;
            }
        } catch (Exception e){
            System.err.println("게임기록 삭제 실패:" + e.getMessage());
            throw new RuntimeException("게임기록 삭제 중 오류가 발생했습니다." + e);
        }
    }

    // [AGL-02]	게임전체기록 조회 (관리자단)	getGameLog()	게임기록 전체를 조회한다.
    public List<GameLogDto> getGameLog() {
        // 1. 모든 엔티티 조회 및 스트림으로 엔티티 -> dto 변환
        List<GameLogEntity> gameLogEntityList = gameLogRepository.findAll();
        // 2. 모든 엔티티 dto 변환
        List<GameLogDto> gameLogDtoList = gameLogEntityList
                .stream().map(GameLogEntity::toDto)
                .collect(Collectors.toList());
        // 3. dto list 배열 반환
        return gameLogDtoList;
    }

    // [AGL-03]	게임상세기록 조회 (관리자단)	getGameLogDetail()	게임 기록을 상세 조회한다.
    public GameLogDto getGameLogDetail(int gameLogNo) {
        // 1. gameLogNo(pk) 엔티티 조회
        Optional<GameLogEntity> optional = gameLogRepository.findById(gameLogNo);
        // 2. 옵셔널로 존재 확인 후 쿼리 메소드 가져오기
        if (optional.isPresent()) {
            GameLogEntity gameLogEntity = optional.get();
            return gameLogEntity.toDto();
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
                .stream().map(GameEntity :: toDto)
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
