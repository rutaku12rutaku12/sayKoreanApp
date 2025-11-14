package web.controller;

import com.google.api.Http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import web.model.dto.game.GameDto;
import web.model.dto.game.GameLogDto;
import web.service.GameService;

// [*] 예외 핸들러 : 전역으로도 사용 가능
@Log4j2
@RestControllerAdvice(assignableTypes = {GameController.class}) // 해당 컨트롤러에서만 적용
class GameExceptionHandler { //
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        // 로그 에러 개발자에게 반환
        log.error("에러 발생 : {}", e.getMessage(), e);

        // 클라이언트에게 보낼 메시지는 명확하게!
        String userMessage = "요청 처리 중 오류 발생했습니다.";
        if (e.getMessage().contains("Duplicate entry")) {
            userMessage = "이미 존재하는 데이터입니다.";
        } else if (e.getMessage().contains("foreign key constraint")) {
            userMessage = "연관된 데이터가 있어 삭제할 수 없습니다.";
        }

        // 클라이언트 메시지 반환 뭐시꺵이
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(userMessage);
    }
}

@Slf4j
@RestController
@RequestMapping("/saykorean")
@RequiredArgsConstructor
public class GameController {
    // [*] DI
    private final GameService gameService;
    
    // 테스트 모드 플래그 ( JWT 이식 전에 테스트용. 실제 배포 시에는 false로 변경!)
    private static final boolean TEST_MODE = true;
    private static final int TEST_USER_NO = 1;  // 테스트용 기본 사용자 번호

    // 플러터에서는 세션 안 먹힘! [GL-NN] 사용자 게임 관련 메소드는 JWT 토큰으로 처리할 것.
    // [GL-01]	게임기록생성	createGameLog()	사용자가 게임을 종료하면 해당 기록을 테이블에 저장한다.
    // * 게임 결과에 따라 해당 사용자의 포인트가 증가한다.
    // * 게임 점수에 따라 랭킹 테이블에 반영될 수 있다.
    // * 게임 테이블 FK로 받는다
    // URL : http://localhost:8080/saykorean/gamelog
    // 로그인 상태에서만 가능!
    // BODY : { "gameNo" : "1"  ,  "gameResult" : "1" ,  "gameScore" : "300" }
    @PostMapping("/gamelog")
    public ResponseEntity<?> createGameLog(
            @RequestBody GameLogDto gameLogDto,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization" , required = false) String authHeader) {

        Integer userNo = null;

        // [테스트 모드] 인증 없이 기본 사용자로 처리
        if(TEST_MODE) {
            userNo = TEST_USER_NO;
            log.info("🧪 TEST MODE: 게임 기록 생성 - userNo: {}", userNo);
        }

        // [실제 운영 모드] JWT 토큰 또는 세션으로 인증
//        else {
//            // JWT 토큰 우선 확인
//            if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                String token = authHeader.substring(7);
//                userNo = jwtUtil.getUserNoFromToken(token);
//
//                if (userNo == null || !jwtUtil.validateToken(token) ) {
//                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body("유효하지 않은 토큰입니다.");
//                }
//            }
//            // JWT 토큰 없으면 세션 확인 (웹 브라우저용)
//            else {
//                HttpSession session = request.getSession(false); // false → 기존 세션 없으면 null 리턴
//                // 세션 정보 가져오기
//                if (session == null || session.getAttribute("userNo") != null ){
//                    userNo = (int) session.getAttribute("userNo");
//                }
//            }
//
//            // 인증 실패
//            if(userNo == null) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body("로그인이 필요합니다.");
//            }
//
//        }

        // 사용자 번호 설정
        gameLogDto.setUserNo(userNo);

        // 서비스 호출
        try{
            GameLogDto result = gameService.createGameLog(gameLogDto);
            log.info("게임 기록 저장 성공 - userNo: {}, gameNo: {} , score: {}",
                    userNo, gameLogDto.getGameNo(), gameLogDto.getGameScore());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("게임 기록 저장 실패" , e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("게임 기록 저장에 실패했습니다.");
        }
    }
    // 오류내용 Caused by: org.hibernate.exception.SQLGrammarException: could not execute statement [Unknown column 'game_no' in 'field list'] [insert into gamelog (game_no,game_finished_at,game_result,game_score,user_no) values (?,?,?,?,?)]

    // [GL-02]	내 게임기록 전체조회	getMyGameLog()	사용자(본인)의 게임기록 전체를 조회한다
    // URL : http://localhost:8080/saykorean/gamelog
    // 로그인 상태에서만 가능!
    @GetMapping("/gamelog")
    public ResponseEntity<?> getMyGameLog(
            HttpServletRequest request ,
            @RequestHeader(value = "Authorization" , required = false) String authHeader) {

        Integer userNo = null;

        // [테스트 모드]
        if (TEST_MODE) {
            userNo = TEST_USER_NO;
        }

        // [실제 운영 모드]
//        else {
//            // JWT 토큰 확인
//            if(authHeader != null && authHeader.startsWith("Bearer ")) {
//                String token = authHeader.substring(7);
//                userNo = jwtUtil.getUserNoFromToken(token);
//
//                if (userNo == null || !jwtUtil.validateToken(token)) {
//                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                            .body("유효하지 않은 토큰입니다.");
//                }
//            }
//            // 세션 확인
//            else {
//                HttpSession session = request.getSession(false); // false → 기존 세션 없으면 null 리턴
//                // 세션 정보 가져오기
//                if (session == null || session.getAttribute("userNo") == null ){
//                    userNo = (int) session.getAttribute("userNo");
//                }
//            }
//
//            if (userNo == null) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body("로그인이 필요합니다.");
//            }
//        }
        // 반환
        return ResponseEntity.ok(gameService.getMyGameLog(userNo));
    }

    // [GL-03]	내 게임기록 상세조회	getMyGameLogDetail()	사용자(본인)의 게임기록을 상세 조회한다
    // URL : http://localhost:8080/saykorean/gamelog/detail?gameLogNo=1
    // 로그인 상태에서만 가능!
    @GetMapping("/gamelog/detail")
    public ResponseEntity<?> getMyGameLogDetail(
            HttpServletRequest request,
            @RequestParam int gameLogNo) {
        GameLogDto gameLogDto = new GameLogDto();
        HttpSession session = request.getSession(false); // false → 기존 세션 없으면 null 리턴
        // 세션 정보 가져오기
        if (session == null || session.getAttribute("userNo") == null ){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 세션에서 userNo 꺼내 DTO 주입
        int userNo = (int) session.getAttribute("userNo");
        gameLogDto.setUserNo(userNo);

        // 리턴
        return ResponseEntity.ok(gameService.getMyGameLogDetail(userNo, gameLogNo));
    }


    // [AGL-01]	게임기록 삭제(관리자단)	deleteGameLog()	게임 기록 테이블을 삭제한다.
    // * 관리자가 부정한 게임 기록을 임의로 삭제한다.
    // * 사용자가 탈퇴했을 경우, 게임 기록을 삭제한다.
    // URL : http://localhost:8080/saykorean/admin/gamelog?gameLogNo=1&userNo=
    // URL : http://localhost:8080/saykorean/admin/gamelog?gameLogNo=&userNo=1
    @DeleteMapping("/admin/gamelog")
    public ResponseEntity<?> deleteGameLog(@RequestParam(required = false) Integer gameLogNo ,
                                           @RequestParam(required = false) Integer userNo) {
        return  ResponseEntity.ok(gameService.deleteGameLog(gameLogNo, userNo));
    }

    // [AGL-02]	게임전체기록 조회 (관리자단)	getGameLog()	게임기록 전체를 조회한다.
    // URL : http://localhost:8080/saykorean/admin/gamelog
    @GetMapping("/admin/gamelog")
    public ResponseEntity<?> getGameLog() {
        return ResponseEntity.ok(gameService.getGameLog());
    }

    // [AGL-03]	게임상세기록 조회 (관리자단)	getGameLogDetail()	게임 기록을 상세 조회한다.
    // URL : http://localhost:8080/saykorean/admin/gamelog/detail?gameLogNo=1
    @GetMapping("/admin/gamelog/detail")
    public ResponseEntity<?> getGameLogDetail(@RequestParam Integer gameLogNo) {
        return ResponseEntity.ok(gameService.getGameLogDetail(gameLogNo));
    }

    // [AG-01]	게임 종류 추가(관리자단)	createGame()	게임 테이블을 추가한다.
    // * 실제 게임은 플러터 assets 폴더에 추가해야합니다.
    // URL : http://localhost:8080/saykorean/admin/game
    // BODY : { "gameTitle" : "날쌘돌이토돌이" }
    @PostMapping("/admin/game")
    public ResponseEntity<?> createGame(@RequestBody GameDto gameDto) {
        return ResponseEntity.ok(gameService.createGame(gameDto));
    }

    // [AG-02]	게임 전체조회(관리자단)	getGame()	게임 테이블을 전체조회한다.
    // URL : http://localhost:8080/saykorean/admin/game
    @GetMapping("/admin/game")
    public ResponseEntity<?> getGame() {
        return ResponseEntity.ok(gameService.getGame());
    }

    // [AG-03]	게임 상세조회(관리자단)	getDetailGame()	게임 테이블을 상세조회한다.
    // URL : http://localhost:8080/saykorean/admin/game/detail?gameNo=1
    @GetMapping("/admin/game/detail")
    public ResponseEntity<?> getGameDetail(@RequestParam int gameNo) {
        return ResponseEntity.ok(gameService.getGameDetail(gameNo));
    }

    // [AG-04]	게임 삭제(관리자단)	deleteGame()	게임 테이블을 삭제한다.
    // URL : http://localhost:8080/saykorean/admin/game?gameNo=1
    @DeleteMapping("/admin/game")
    public ResponseEntity<?> deleteGame(@RequestParam int gameNo) {
        return ResponseEntity.ok(gameService.deleteGame(gameNo));
    }

}