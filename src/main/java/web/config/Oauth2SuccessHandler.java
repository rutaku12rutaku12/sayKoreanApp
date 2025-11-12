package web.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import web.model.dto.user.UserDto;
import web.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // OAuth2 로그인 성공 후 호출
        System.out.println("request = " + request + ", response = "
                + response + ", authentication = " + authentication);
        // 로그인 성공한 회원의 타사 발급한 토큰 확인
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        System.out.println("authToken = " + authToken);
        // 로그인 성공한 회원 동의항목 정보 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        System.out.println("oAuth2User = " + oAuth2User);
        // 타사 로그인 인지 식별 , 서로 다른 회사별 동의항목
        String provider = authToken.getAuthorizedClientRegistrationId();
        System.out.println("provider = " + provider);
        // 공급자별 사용자 정보(email, name) 추출
        String uid = null ; String name = null ;
        if( provider.equals("google")){
            // Google: 이메일과 이름 직접 추출
            uid = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        }
        else if( provider.equals("kakao")){
            // Kakao: kakao_account 안에서 이메일과 프로필 닉네임 추출
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            // 카카오 계정이 없거나 카카오계정에 이메일이 없을 경우 로그인 실패 처리
            if(kakaoAccount == null || kakaoAccount.get("email") == null){
                response.sendRedirect("http://localhost:5173/login");
                return;
            }
            uid = (String)kakaoAccount.get("email");
            System.out.println("uid = " + uid);
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            name = (String)profile.get("nickname");
        }
        // oauth2 정보를 데이터베이스 저장 (가입 처리)
        UserDto userDto = userService.oauth2UserSignup( uid, name);
        try {
            userDto = userService.oauth2UserSignup(uid, name);
        } catch (IllegalStateException e) {
            // 이메일 중복 예외 발생 시 로그인 실패 후 로그인 페이지로 리다이렉트
            System.out.println("OAuth2 signup error: " + e.getMessage());
            response.sendRedirect(
                    "http://localhost:5173/login?error=email_exists");
            return;
        }

        // UserDto 기반으로 Authentication 생성
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDto,
                        null,
                        List.of(new SimpleGrantedAuthority("USER"))
                );
        // SecurityContextHolder에 인증정보 세팅
        SecurityContextHolder.getContext().setAuthentication(auth);
        // 세션에 사용자 번호 저장
        HttpSession session = request.getSession(true);
        session.setAttribute("userNo", userDto.getUserNo());
        // 로그인 성공시 (프론트엔드 루트) 경로로 리다이렉트
        response.sendRedirect("http://localhost:5173/home");


    }
}
