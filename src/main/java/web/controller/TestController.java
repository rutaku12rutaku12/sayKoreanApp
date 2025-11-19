package web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.model.dto.common.RankingDto;
import web.model.dto.test.TestDto;
import web.service.TestService;
import web.util.AuthUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/saykorean/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    // [1] 시험 목록 조회
    @GetMapping("")
    public ResponseEntity<List<TestDto>> getListTest(@RequestParam int langNo) {
        return ResponseEntity.ok(testService.getListTest(langNo));
    }

    // ex) GET /saykorean/test/by-study?studyNo=12
    @GetMapping("/by-study")
    public ResponseEntity<List<TestDto>> getTestsByStudy(
            @RequestParam int studyNo,
            @RequestParam int langNo
    ) {
        return ResponseEntity.ok(testService.findByStudyNo(studyNo, langNo));
    }

    // [2] 특정 시험 문항 + 보기 조회
    @GetMapping("/findtestitem")
    public ResponseEntity<List<Map<String , Object>>> findTestItem(
            @RequestParam int testNo,
            @RequestParam int langNo
    ){
        return ResponseEntity.ok(testService.findTestItemWithOptions(testNo, langNo));
    }

    // ✅ [2-1] [TI-INFI] 무한모드 문항 조회 (완료한 studyNo들의 모든 문항)
    // GET /saykorean/test/infinite-items?langNo=1&studyNos=1,2,3
    @GetMapping("/infinite-items")
    public ResponseEntity<List<Map<String, Object>>> getInfiniteItems(
            @RequestParam int langNo,
            @RequestParam List<Integer> studyNos
    ) {
        List<Map<String, Object>> items = testService.getItemsByStudyNos(studyNos, langNo);
        return ResponseEntity.ok(items);
    }


    // ✅ [2-2] [TI-HARD] 하드모드 문항 조회 (전체 DB의 모든 문항)
    // GET /saykorean/test/hard-items?langNo=1
    @GetMapping("/hard-items")
    public ResponseEntity<List<Map<String, Object>>> getHardItems(
            @RequestParam int langNo
    ) {
        List<Map<String, Object>> items = testService.getAllItems(langNo);
        return ResponseEntity.ok(items);
    }


    // [3-1] 최신 회차 점수 조회 (수정)
    @GetMapping("/getscore")
    public ResponseEntity<RankingDto> getScore(
            @RequestParam int userNo,
            @RequestParam int testNo,
            @RequestParam(required = false) Integer testRound   // null 값 먹이려고 Integer로 변경
    ) {
        // 추가 : testRound가 없으면 최신 회차를 조회
        if (testRound == null){
            return ResponseEntity.ok(testService.getLatestScore(userNo, testNo));
        }
        // testRound가 있으면 특정 회차를 조회
        return ResponseEntity.ok(testService.getScore(userNo, testNo, testRound));
    }

    // [3-2] 다음 회차 번호 조회 (추가)
    @GetMapping("/getnextround")
    public ResponseEntity<Integer> getNextRound(
            @RequestParam int testNo,
            HttpSession session
    ) {
        Integer userNo = (Integer) session.getAttribute("userNo");
        if (userNo == null){
            userNo = 1; // 임시 (실제로는 로그인 필수)
        }
        int nextRound = testService.getNextRound(userNo, testNo);
        return ResponseEntity.ok(nextRound);
    }

    // [4] 제출 API
    // 시험 컨트롤러 내부

        private final AuthUtil authUtil;   // AuthUtil 주입

        // [4] 제출 API
        @PostMapping("/{testNo}/items/{testItemNo}/answer")
        public ResponseEntity<?> submitAnswer(
                @PathVariable int testNo,
                @PathVariable int testItemNo,
                @RequestBody SubmitReq body,
                HttpServletRequest request   // 여기로 요청 받기
        ) {
            try {
                // 공통 userNo 추출 (Flutter: JWT / 웹: 세션)
                Integer userNo = authUtil.getUserNo(request);
                if (userNo == null) {
                    return ResponseEntity.status(401).body("로그인 필요");
                }

                // 필수값 체크
                if (body.getTestRound() == null) {
                    return ResponseEntity.badRequest().body("testRound는 필수입니다.");
                }
                if (body.getLangNo() == null) {
                    body.setLangNo(1); // 기본 한국어(1) 같은 디폴트 주고 싶으면
                }

                // 서비스 호출 (이미 만들어 둔 submitFreeAnswer 사용)
                int score = testService.submitFreeAnswer(
                        userNo,
                        testNo,
                        testItemNo,
                        body.getTestRound(),
                        body.getSelectedExamNo(),
                        body.getUserAnswer(),
                        body.getLangNo()
                );

                // 결과 반환 (React/Flutter 공통으로 사용 가능)
                return ResponseEntity.ok(
                        java.util.Map.of(
                                "score", score,
                                "isCorrect", (score >= 60 ? 1 : 0)
                        )
                );
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError()
                        .body("제출 중 오류: " + e.getMessage());
            }
        }

        @Data
        static class SubmitReq {
            private Integer testRound;
            private Integer selectedExamNo;
            private String  userAnswer;
            private Integer langNo;
            // userNo는 이제 바디에서 안 읽어도 됨 (AuthUtil이 헤더/세션에서 해결)
        }
    }
