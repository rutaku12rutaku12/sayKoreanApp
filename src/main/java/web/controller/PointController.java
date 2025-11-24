package web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.model.dto.point.PointRecordDto;
import web.model.mapper.PointMapper;
import web.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("saykorean")
public class PointController {

    private final PointMapper pointMapper;
    private final JwtUtil jwtUtil;

    @GetMapping("/store/point")
    public ResponseEntity<Integer> getMyPoint(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        System.out.println("[PointController] /saykorean/store/point 호출됨");

        if (authorizationHeader == null) {
            System.out.println("Authorization 헤더 없음");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        System.out.println("Authorization: " + authorizationHeader);

        if (!authorizationHeader.startsWith("Bearer ")) {
            System.out.println("Bearer 토큰 형식 아님");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorizationHeader.substring(7);
        System.out.println("token: " + token);

        // 토큰 유효성 체크
        if (!jwtUtil.isTokenValid(token)) {
            System.out.println("토큰 유효성 실패");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer userNo = jwtUtil.getUserNo(token);
        System.out.println("토큰에서 꺼낸 userNo = " + userNo);

        if (userNo == null) {
            System.out.println("userNo == null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int totalPoint = pointMapper.getTotalPoint(userNo);
        System.out.println("getTotalPoint(userNo=" + userNo + ") = " + totalPoint);

        return ResponseEntity.ok(totalPoint);
    }


    @PostMapping("/store/theme/{themeId}/buy")
    public ResponseEntity<?> buyTheme(
            @PathVariable int themeId, // 1 = 다크, 2 = 민트
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        System.out.println("[PointController] /saykorean/store/theme/" + themeId + "/buy 호출됨");

        // 1. 인증 체크
        if (authorizationHeader == null) {
            System.out.println("Authorization 헤더 없음");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            System.out.println("Bearer 토큰 형식 아님");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorizationHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            System.out.println("토큰 유효성 실패");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer userNo = jwtUtil.getUserNo(token);
        System.out.println("토큰에서 꺼낸 userNo = " + userNo);

        if (userNo == null) {
            System.out.println("userNo == null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. 현재 포인트 조회
        int currentPoint = pointMapper.getTotalPoint(userNo);
        System.out.println("현재 포인트 = " + currentPoint);

        int price = 2000;        // 테마 가격
        int themePointNo = 6;

        if (currentPoint < price) {
            System.out.println("포인트 부족으로 구매 실패");
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("reason", "NOT_ENOUGH_POINT");
            body.put("currentPoint", currentPoint);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        // 3. 포인트 로그에 '테마 구매' 기록 남기기
        PointRecordDto record = new PointRecordDto();
        record.setPointNo(themePointNo); // ★★ 꼭 세팅
        record.setUserNo(userNo);        // ★★ 꼭 세팅

        pointMapper.insertPointRecord(record);

        // 4. 새 포인트 계산 (또는 getTotalPoint를 다시 호출해도 됨)
        int newPoint = currentPoint - price;

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("newPoint", newPoint);
        body.put("themeId", themeId);

        return ResponseEntity.ok(body);
    }

}
