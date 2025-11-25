package web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import web.config.RecaptchaConfig;
import web.model.dto.user.*;
import web.model.mapper.admin.AdminReportMapper;
import web.service.UserService;
import web.util.AuthUtil;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/saykorean")
@RequiredArgsConstructor
@Validated // dto가 아닌 param 검증 활성화 어노테이션
@PropertySource("classpath:application.properties")
public class UserController {

    private final UserService userService;
    private final AuthUtil authUtil;
    private AdminReportMapper adminReportMapper;

    @Value("${recaptcha.secretKey}")
    private String secretKey;

    // [US-01] 회원가입 signUp()
    @PostMapping("/signup")              // @Valid : dto에 @NotBlank, @Email 등등 어노테이션 활성화 어노테이션
    public ResponseEntity<Boolean> signUp(@Valid @RequestBody UserDto userDto ){
        try {
//            // reCaptcha 검증
//            RecaptchaConfig.setSecretKey(secretKey);
//            Boolean verify = RecaptchaConfig.verify(userDto.getRecaptcha());
//            System.out.println("회원가입 Recaptcha verify = " + verify);
//            // 검증 실패 시
//            if(!verify){ System.out.println("reCaptcha 검증 실패!");
//                return ResponseEntity.status(400).body(0);
//            }

            int result = userService.signUp(userDto);
            if (result >= 1) { // userNo 반환
                System.out.println("가입한 사용자 정보 : " + userDto);
                return ResponseEntity.status(200).body(true);

            } else {
                return ResponseEntity.status(400).body(false);
            }
        }catch (Exception e){ e.printStackTrace();
            return ResponseEntity.status(500).body(false);
        }
    } // func end

    // [US-02] 로그인 logIn()
    @PostMapping("/login")
    public ResponseEntity<?> logIn(@Valid @RequestBody LoginDto loginDto, HttpServletRequest request ){

        String clientType = request.getHeader("X-Client-Type");
        try{
            Object result = userService.logIn(loginDto , clientType , request );

            if( result == null ){
                return ResponseEntity.status(401).body(
                        Map.of("error", "LOGIN_FAILED", "message", "이메일 또는 비밀번호가 올바르지 않습니다.")
                );
            }

//            // 2단계: 사용자 정보 조회
//            UserDto user = userService.getUserByEmail(loginDto.getEmail());
//
//            if(user == null){
//                return ResponseEntity.status(401).body(
//                        Map.of("error", "USER_NOT_FOUND", "message", "사용자 정보를 찾을 수 없습니다.")
//                );
//            }
//
//            // ⭐ 3단계: 제재 여부 확인 (userState가 -2인 경우)
//            if (user.getUserState() == -2) {
//                log.info("제재된 계정 로그인 시도: userNo={}", user.getUserNo());
//
//                // 현재 제재 중인지 확인
//                boolean isRestricted = adminReportMapper.isRestricted(user.getUserNo());
//
//                if (isRestricted) {
//                    // 남은 제재 일수 조회
//                    Integer remainingDays = adminReportMapper.getRemainingRestrictDays(user.getUserNo());
//
//                    log.warn("제재 중인 사용자 로그인 차단: userNo={}, 남은 기간={}일",
//                            user.getUserNo(), remainingDays);
//
//                    // 제재 정보를 담은 에러 응답
//                    Map<String, Object> errorResponse = new HashMap<>();
//                    errorResponse.put("error", "ACCOUNT_RESTRICTED");
//                    errorResponse.put("message", "계정이 제재되었습니다.");
//                    errorResponse.put("remainingDays", remainingDays != null ? remainingDays : 0);
//                    errorResponse.put("userNo", user.getUserNo());
//
//                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
//
//                } else {
//                    // 제재 기간이 지났으면 userState를 1(정상)로 복구
//                    log.info("제재 기간 만료, 계정 복구: userNo={}", user.getUserNo());
//                    userService.restoreUserState(user.getUserNo());
//
//                    // 복구 후 사용자 정보 다시 조회
//                    user = userService.getUserByEmail(loginDto.getEmail());
//                }
//            }

            // 4단계 : 정상 로그인 처리
            // 플러터일 경우
            if ("flutter".equalsIgnoreCase(clientType)){
                // JWT 토큰 반환
                System.out.println("토큰 로그인 성공, 로그인한 회원 토큰 정보 : "+result);
                return ResponseEntity.ok(Map.of("token", result));
            } else { // 리액트일 경우
                // Web 세션 로그인 - 사용자 정보 반환
                System.out.println("세션 로그인 성공, 로그인한 회원 정보 : "+result);
                return ResponseEntity.ok(result);
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body("로그인 처리 중 오류 발생 : "+e);
        }
    }

    // [US-03] 로그아웃 logOut()
    @GetMapping("/logout")
    public ResponseEntity<?> logOut(HttpServletRequest request){
        String clientType = request.getHeader("X-Client-Type");

        try {
            boolean result = userService.logOut(clientType,request);

            if( result ) {
                System.out.println("로그아웃 성공");
                return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
            } else {
                System.out.println("로그아웃 실패");
                return ResponseEntity.status(400).body(Map.of("message", "로그아웃 실패"));
            }
        }catch (Exception e){
            return ResponseEntity.status(500).body(Map.of("message", "로그아웃 처리 중 오류 발생"));
        }
    }

    // [US-04] 내 정보 조회( 로그인 중인 사용자정보 조회 ) info()
    @GetMapping("/info")
    public ResponseEntity<UserDto> info( HttpServletRequest request ){
        Integer userNo = authUtil.getUserNo(request);
        // 로그인 된 세션 정보 가져오기
        // 세션이 없거나 세션내 userNo 값이 없으면 null 반환
        if( userNo == null ){
            System.out.println("인증 실패: userNo를 가져올 수 없음");
            return ResponseEntity.status(401).body(null);
        }

        // 서비스에게 전달하고 응답 받기
        UserDto result = userService.info(userNo);
            System.out.println("로그인 한 사용자의 정보조회 : "+result);
            return ResponseEntity.status(200).body(result);
    } // func end

    // [US-05] 이메일 중복검사 checkEmail()
    @GetMapping("/checkemail")                 // @Email 형식 유효성검사 어노테이션
    public ResponseEntity<Integer> checkEmail(@Email(message = "올바른 이메일 형식이 아닙니다.")
                                                // @NotBlack null 값 빈칸 입력 차단 어노테이션
                                                  @NotBlank @RequestParam String email){
        // 이메일이 null이거나, 공백 또는 빈 문자열일 때 -1 반환
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.status(400).body(-1);
        }
        int result = userService.checkEmail(email);
            System.out.println("이메일 중복검사 (0중복아님,1중복) : "+result);
            return ResponseEntity.status(200).body(result);
    } // func end

    // [US-06] 연락처 중복검사 checkPhone()
    @GetMapping("/checkphone")                  // @Pattern 정규식 유효성 어노테이션
    public ResponseEntity<Integer> checkPhone(@Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "올바른 휴대폰 번호를 입력해주세요.")
                                                  @NotBlank @RequestParam String phone){
        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.status(400).body(-1);
        }
        int result = userService.checkPhone(phone);
            System.out.println("연락처 중복검사 (0중복아님,1중복) : "+result);
            return ResponseEntity.status(200).body(result);
    } // func end

    // [US-07] 이메일 찾기 findEmail()
    @GetMapping("/findemail")
    public ResponseEntity<?> findEmail(@NotBlank @RequestParam String name, @Pattern(regexp = "(^\\+?[1-9]\\d{7,14}$)", message = "올바른 휴대폰 번호를 입력해주세요.")
    @NotBlank @RequestParam String phone){
        try {
            System.out.println(name);
            System.out.println(phone);
            String result = userService.findEmail(name,phone);
            System.out.println(result);
            if( result == null){return ResponseEntity.status(400).body("올바른 값을 입력해주세요.");}
            System.out.println("찾는 이메일 : "+result);
            return ResponseEntity.status(200).body(result);
        }catch (Exception e){System.out.println("오류발생: "+e);}
        return ResponseEntity.status(500).body("이메일 찾기 오류 발생");
    } // func end

    // [US-08] 비밀번호 찾기 findPwrd()
    @GetMapping("/findpwrd")
    public ResponseEntity<String> findPwrd(@NotBlank @RequestParam String name, @Pattern(regexp = "(^\\+?[1-9]\\d{7,14}$)", message = "올바른 휴대폰 번호를 입력해주세요.")
    @NotBlank @RequestParam String phone, @NotBlank @RequestParam String email){
        try {
            userService.findPwrd(name,phone,email);
            return ResponseEntity.ok("임시 비밀번호가 이메일로 발송되었습니다.");
        }catch (RuntimeException e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    } // func end

    // [US-09] 회원정보 수정 updateUserInfo()
    @PutMapping("/updateuserinfo")
    public ResponseEntity<Integer> updateUserInfo(@Valid @RequestBody UpdateUserInfoDto updateUserInfoDto , HttpServletRequest request ){
        // 세션 또는 토큰 꺼내기
        Integer userNo = authUtil.getUserNo(request);
        // 만약 세션이 없거나 로그인이 안되어 있으면 null
        if( userNo == null ){
            System.out.println("인증 실패: userNo를 가져올 수 없음");
            return ResponseEntity.status(401).body(null);
        }
        // 로그인된 사용자번호 꺼내기 = 수정하는 사용자의 번호
        System.out.println("현재 수정할 사용자의 번호 : "+userNo);
        // dto 담아주기
        updateUserInfoDto.setUserNo(userNo);
        int result = userService.updateUserInfo(updateUserInfoDto);
        System.out.println("회원정보 수정 성공 시 1이 출력 : "+result);
        return ResponseEntity.status(200).body(result);
    } // func end

    // [US-10] 비밀번호 수정 updatePwrd()
    @PutMapping("/updatepwrd")
    public ResponseEntity<?> updatePwrd(@Valid @RequestBody UpdatePwrdDto updatePwrdDto , HttpServletRequest request){
        // 세션 또는 토큰 꺼내기
        Integer userNo = authUtil.getUserNo(request);
        // 만약 세션이 없거나 토큰이 없으면 null
        if( userNo == null ){
            System.out.println("인증 실패: userNo를 가져올 수 없음");
            return ResponseEntity.status(401).body(null);
        }
        // 로그인된 사용자번호 꺼내기 = 수정하는 사용자의 번호
        System.out.println("현재 수정할 사용자의 번호 : "+userNo);
        // dto 담아주기
        updatePwrdDto.setUserNo(userNo);
        UpdatePwrdDto result = userService.updatePwrd(updatePwrdDto);
        if( result == null){
            return ResponseEntity.status(400).body("기존 비밀번호 불일치");
        }
        System.out.println("비밀번호 수정 성공한 회원 UpdatePwrdDto 반환 : "+result);
        return ResponseEntity.status(200).body(result+"비밀번호 변경 성공");
    } // func end

    // [US-11] 회원상태 수정(삭제) deleteUserStatus()
    @PutMapping("/deleteuser")
    public ResponseEntity<Integer> deleteUserStatus(@Valid @RequestBody DeleteUserStatusDto deleteUserStatusDto, HttpServletRequest request){
        // 세션 또는 토큰 꺼내기
        Integer userNo= authUtil.getUserNo(request);
        if(userNo==null){
            System.out.println("인증 실패: userNo를 가져올 수 없음");
            return ResponseEntity.status(401).body(null);
        }
        // 로그인된 사용자번호 꺼내기 = 탈퇴하는 사용자의 번호
        System.out.println("현재 탈퇴할 사용자의 번호 : "+userNo);
        // dto 담아주기
        deleteUserStatusDto.setUserNo(userNo);
        int result = userService.deleteUserStatus(deleteUserStatusDto);
        if( result > 0){
        // 회원상태 수정(삭제) 후 세션 제거 : 로그아웃 상태로
        logOut(request);
            System.out.println(result);
        return ResponseEntity.status(200).body(result);
        }
        else System.out.println(result);
        return ResponseEntity.status(400).body(0);
    }

//    // getGenreNo
//    @GetMapping("/me/genre")
//    public ResponseEntity<?> getGenreNo(HttpSession session) {
//        Integer userNo = (Integer) session.getAttribute("userNo");
//        if (userNo == null) return ResponseEntity.status(401).build();
//
//        Integer genreNo = userService.getGenreNo(userNo); // DB에서 읽음
//        if (genreNo == null) return ResponseEntity.notFound().build();
//
//        return ResponseEntity.ok(genreNo);
//    }
//
//    // 선호 장르 변경
//    @PutMapping("/me/genre")
//    public ResponseEntity<Void> updateGenre(@RequestParam int genreNo, HttpSession session) {
//        Integer userNo = (Integer) session.getAttribute("userNo");
//        if (userNo == null) return ResponseEntity.status(401).build();
//
//        userService.updateGenre(userNo, genreNo); // DB 업데이트
//
//        return ResponseEntity.noContent().build(); // 204
//    }

    @ControllerAdvice
    public class GlobalExceptionHandler {

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<String> handleValidationException(ConstraintViolationException ex) {
            ex.getConstraintViolations().forEach(v -> {
                System.out.println("Invalid value: " + v.getInvalidValue());
                System.out.println("Message: " + v.getMessage());
            });
            return ResponseEntity.badRequest().body("입력 값 검증 실패");
        }
    }


} // class end
