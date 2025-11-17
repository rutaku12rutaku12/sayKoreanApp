package web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import web.model.dto.game.GameDto;
import web.model.dto.game.GameLogDto;
import web.service.GameService;
import web.util.AuthUtil;

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
    private final AuthUtil authUtil;
    
    // 테스트 모드 플래그 ( JWT 이식 전에 테스트용. 실제 배포 시에는 false로 변경!)
    private static final boolean TEST_MODE = false;
    private static final int TEST_USER_NO = 1;  // 테스트용 기본 사용자 번호

    // 플러터에서는 세션 안 먹힘! [GL-NN] 사용자 게임 관련 메소드는 JWT 토큰으로 처리할 것.
    // [GL-01]	게임기록생성	createGameLog()	사용자가 게임을 종료하면 해당 기록을 테이블에 저장한다.
    // * 게임 결과에 따라 해당 사용자의 포인트가 증가한다.
    // * 게임 점수에 따라 랭킹 테이블에 반영될 수 있다.
    // * 게임 테이블 FK로 받는다
    // URL : http://localhost:8080/saykorean/gamelog
    // HEADERS :
    //     *   - X-Client-Type: flutter (Flutter 앱인 경우)
    //     *   - Authorization: Bearer {JWT_TOKEN} (Flutter & JWT 모드)
    // BODY : { "gameNo" : "1"  ,  "gameResult" : "1" ,  "gameScore" : "300" }
    @PostMapping("/gamelog")
    public ResponseEntity<?> createGameLog(
            @RequestBody GameLogDto gameLogDto,
            HttpServletRequest request) {

        Integer userNo = null;

        // [테스트 모드] 인증 없이 기본 사용자로 처리
        if(TEST_MODE) {
            userNo = TEST_USER_NO;
            log.info("🧪 TEST MODE: 게임 기록 생성 - userNo: {}", userNo);
        }

        // [실제 운영 모드] AuthUtil 통한 통합 인증
        else {
            userNo = authUtil.getUserNo(request);
            
            if (userNo == null) {
                String clientType = request.getHeader("X-Client-Type");
                if("flutter".equalsIgnoreCase(clientType)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("로그인이 필요합니다. 유효한 JWT 토큰을 제공해주세요.");
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("로그인이 필요합니다.");
                }
            }
        }

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

    // [GL-02]	내 게임기록 전체조회	getMyGameLog()	사용자(본인)의 게임기록 전체를 조회한다
    // URL : http://localhost:8080/saykorean/gamelog
    // 로그인 상태에서만 가능!
    // HEADERS :
    //     *   - X-Client-Type: flutter (Flutter 앱인 경우)
    //     *   - Authorization: Bearer {JWT_TOKEN} (Flutter & JWT 모드)
    @GetMapping("/gamelog")
    public ResponseEntity<?> getMyGameLog(
            HttpServletRequest request) {

        Integer userNo = null;

        // [테스트 모드]
        if (TEST_MODE) {
            userNo = TEST_USER_NO;
            log.info("🧪 TEST MODE: 게임 기록 조회 - userNo: {}", userNo);
        }

        // [실제 운영 모드]
        else {
            userNo = authUtil.getUserNo(request);

            if (userNo == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("로그인이 필요합니다.");
            }
        }

        // 반환
        return ResponseEntity.ok(gameService.getMyGameLog(userNo));
    }

    // [GL-03]	내 게임기록 상세조회	getMyGameLogDetail()	사용자(본인)의 게임기록을 상세 조회한다
    // URL : http://localhost:8080/saykorean/gamelog/detail?gameLogNo=1
    // HEADERS :
    //     *   - X-Client-Type: flutter (Flutter 앱인 경우)
    //     *   - Authorization: Bearer {JWT_TOKEN} (Flutter & JWT 모드)
    // 로그인 상태에서만 가능!
    @GetMapping("/gamelog/detail")
    public ResponseEntity<?> getMyGameLogDetail(
            @RequestParam Integer gameLogNo,
            HttpServletRequest request) {

        Integer userNo = null;

        // [테스트 모드]
        if (TEST_MODE) {
            userNo = TEST_USER_NO;
            log.info("🧪 TEST MODE: 게임 기록 상세 조회 - userNo: {}, gameLogNo: {}", userNo, gameLogNo);
        }

        // [실제 운영 모드]
        else {
            userNo = authUtil.getUserNo(request);

            if (userNo == null) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                       .body("로그인이 필요합니다.");
            }
        }

        return ResponseEntity.ok(gameService.getMyGameLogDetail(userNo, gameLogNo));
    }

    // [GA-01] 게임 전체 목록 조회 (인증 불필요)
    // URL : http://localhost:8080/saykorean/game
    @GetMapping("/game")
    public ResponseEntity<?> getGameList() {
        log.info("📋 게임 목록 조회 요청");
        return ResponseEntity.ok(gameService.getGame());
    }

    // [AGL-01]	게임기록 삭제(관리자단)	deleteGameLog()	게임 기록 테이블을 삭제한다.
    // * 관리자가 부정한 게임 기록을 임의로 삭제한다.
    // * 사용자가 탈퇴했을 경우, 게임 기록을 삭제한다.
    // URL : http://localhost:8080/saykorean/admin/gamelog?gameLogNo=1&userNo=
    // URL : http://localhost:8080/saykorean/admin/gamelog?gameLogNo=&userNo=1
    @DeleteMapping("/admin/gamelog")
    public ResponseEntity<?> deleteGameLog(@RequestParam(required = false) Integer gameLogNo ,
                                           @RequestParam(required = false) Integer userNo) {
        log.info("🗑️ 관리자: 게임 기록 삭제 - gameLogNo: {}, userNo: {}", gameLogNo, userNo);
        return  ResponseEntity.ok(gameService.deleteGameLog(gameLogNo, userNo));
    }

    // [AGL-02]	게임전체기록 조회 (관리자단)	getGameLog()	게임기록 전체를 조회한다.
    // URL : http://localhost:8080/saykorean/admin/gamelog
    @GetMapping("/admin/gamelog")
    public ResponseEntity<?> getGameLog() {
        log.info("📋 관리자: 게임 전체 기록 조회");
        return ResponseEntity.ok(gameService.getGameLog());
    }

    // [AGL-03]	게임상세기록 조회 (관리자단)	getGameLogDetail()	게임 기록을 상세 조회한다.
    // URL : http://localhost:8080/saykorean/admin/gamelog/detail?gameLogNo=1
    @GetMapping("/admin/gamelog/detail")
    public ResponseEntity<?> getGameLogDetail(@RequestParam Integer gameLogNo) {
        log.info("🔍 관리자: 게임 기록 상세 조회 - gameLogNo: {}", gameLogNo);
        return ResponseEntity.ok(gameService.getGameLogDetail(gameLogNo));
    }

    // [AG-01]	게임 종류 추가(관리자단)	createGame()	게임 테이블을 추가한다.
    // * 실제 게임은 플러터 assets 폴더에 추가해야합니다.
    // URL : http://localhost:8080/saykorean/admin/game
    // BODY : { "gameTitle" : "날쌘돌이토돌이" }
    @PostMapping("/admin/game")
    public ResponseEntity<?> createGame(@RequestBody GameDto gameDto) {
        log.info("➕ 관리자: 게임 추가 - gameTitle: {}", gameDto.getGameTitle());
        return ResponseEntity.ok(gameService.createGame(gameDto));
    }

    // [AG-02]	게임 전체조회(관리자단)	getGame()	게임 테이블을 전체조회한다.
    // URL : http://localhost:8080/saykorean/admin/game
    @GetMapping("/admin/game")
    public ResponseEntity<?> getGame() {
        log.info("📋 관리자: 게임 목록 조회");
        return ResponseEntity.ok(gameService.getGame());
    }

    // [AG-03]	게임 상세조회(관리자단)	getDetailGame()	게임 테이블을 상세조회한다.
    // URL : http://localhost:8080/saykorean/admin/game/detail?gameNo=1
    @GetMapping("/admin/game/detail")
    public ResponseEntity<?> getGameDetail(@RequestParam int gameNo) {
        log.info("🔍 관리자: 게임 상세 조회 - gameNo: {}", gameNo);
        return ResponseEntity.ok(gameService.getGameDetail(gameNo));
    }

    // [AG-04]	게임 삭제(관리자단)	deleteGame()	게임 테이블을 삭제한다.
    // URL : http://localhost:8080/saykorean/admin/game?gameNo=1
    @DeleteMapping("/admin/game")
    public ResponseEntity<?> deleteGame(@RequestParam int gameNo) {
        log.info("🗑️ 관리자: 게임 삭제 - gameNo: {}", gameNo);
        return ResponseEntity.ok(gameService.deleteGame(gameNo));
    }

}