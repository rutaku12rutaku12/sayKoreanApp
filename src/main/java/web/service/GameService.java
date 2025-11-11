package web.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import web.model.dto.game.GameLogDto;
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


    // [GL-03]	내 게임기록 상세조회	getMyGameLogDetail()	사용자(본인)의 게임기록을 상세 조회한다

    // [AGL-01]	게임기록 삭제(관리자단)	deleteGameLog()	게임 기록 테이블을 삭제한다.
    // * 관리자가 부정한 게임 기록을 임의로 삭제한다.
    // * 사용자가 탈퇴했을 경우, 게임 기록을 삭제한다.

    // [AGL-02]	게임전체기록 조회 (관리자단)	getGameLog()	게임기록 전체를 조회한다.

    // [AGL-03]	게임상세기록 조회 (관리자단)	getGameLogDetail()	게임 기록을 상세 조회한다.

    // [AGL-04]	게임 종류 추가(관리자단)	createGame()	게임 테이블을 추가한다.
    // * 실제 게임은 플러터 assets 폴더에 추가해야합니다.

    // [AGL-05]	게임 전체조회(관리자단)	getGame()	게임 테이블을 전체조회한다.

    // [AGL-06]	게임 상세조회(관리자단)	getDetailGame()	게임 테이블을 상세조회한다.

    // [AGL-07]	게임 삭제(관리자단)	deleteGame()	게임 테이블을 삭제한다.

}
