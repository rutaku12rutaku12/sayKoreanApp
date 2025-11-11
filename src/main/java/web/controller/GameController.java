package web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import web.service.GameService;

@RestController
@RequestMapping("/saykorean")
@RequiredArgsConstructor
public class GameController {
    // [*] DI
    private final GameService gameService;

    // [GL-01]	게임기록생성	createGameLog()	사용자가 게임을 종료하면 해당 기록을 테이블에 저장한다.
    // * 게임 결과에 따라 해당 사용자의 포인트가 증가한다.
    // * 게임 점수에 따라 랭킹 테이블에 반영될 수 있다.
    // * 게임 테이블 FK로 받는다

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
