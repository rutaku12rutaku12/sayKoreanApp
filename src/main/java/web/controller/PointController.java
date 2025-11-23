package web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.model.mapper.PointMapper;
import web.util.JwtUtil;

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
}
