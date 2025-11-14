package web.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final JwtUtil jwtUtil;

    // 클라이언트 타입에 따라 userNo 추출

    public Integer getUserNo(HttpServletRequest request){
        String clientType = request.getHeader("X-Client-Type");

        if ("flutter".equalsIgnoreCase(clientType)) {
            // JWT 방식
            String authHeader = request.getHeader("Authorizzation");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.isTokenValid(token)) {
                    return jwtUtil.getUserNo(token);
                }
            }
            System.out.println("Flutter 인증 실패: 토크느이 없거나 유효하지 않음");
            return null;
        } else {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userNo") == null){
                System.out.println("세션 인증 실패: 세션이 없거나 userNo가 없음");
                return null;
            }
            return (Integer) session.getAttribute("userNo");
        }
    }

    // 클라이언트가 Flutter인지 확인
    public boolean isFlutter(HttpServletRequest request){
        String clientType = request.getHeader("X-Client-Type");
        return "flutter".equalsIgnoreCase(clientType);
    }
}
