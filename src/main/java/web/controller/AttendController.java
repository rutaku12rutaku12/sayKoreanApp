package web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.model.dto.user.AttendDto;
import web.service.AttendService;
import web.util.AuthUtil;

import java.util.List;

@RestController
@RequestMapping("/saykorean")
@RequiredArgsConstructor
public class AttendController {

    private final AttendService attendService;
    private final AuthUtil authUtil;

    // [AT-1] 출석하기 attend()
    @PostMapping("/attend")
    public ResponseEntity<?> attend(@RequestBody AttendDto attendDto, HttpServletRequest request){
        try {
            // 세션 또는 토큰 가져오기
            Integer userNo = authUtil.getUserNo(request);
            // 로그인 성공한 사용자 번호 확인
            if (userNo == null ) {
                System.out.println("인증 실패: userNo를 가져올 수 없음");
                return ResponseEntity.status(401).body(null);
            }
            // 로그인된 사용자번호 꺼내기 = 수정하는 사용자의 번호
            System.out.println("현재 수정할 사용자의 번호 : "+userNo);

            // dto 담아주기
            attendDto.setUserNo(userNo);
            int result = attendService.attend(attendDto);
            System.out.println("출석체크 성공시 1 반환 : "+result);
            return ResponseEntity.status(200).body(result);
        }catch (DuplicateKeyException e){
            System.out.println("이미 오늘 출석이 완료되었습니다.");
            return ResponseEntity.status(222).body("이미 출석체크가 되었습니다.");
        }
    } // m end


    // [AT-2] 출석 조회 getAttend()
    @GetMapping("/attend")
    public ResponseEntity<List<AttendDto>> getAttend(HttpServletRequest request){
        // 세션 또는 토큰 가져오기
        Integer userNo = authUtil.getUserNo(request);
        // 로그인 성공한 사용자 번호 확인
        if ( userNo ==null){
            return ResponseEntity.status(401).body(null);
        }
        // List 담아주기
        List<AttendDto> result = attendService.getAttend(userNo);
        System.out.println("List<AttendDto>: "+result);
        return ResponseEntity.status(200).body(result);
    }
}
