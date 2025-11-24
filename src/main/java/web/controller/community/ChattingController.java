package web.controller.community;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.model.dto.community.MessageDto;
import web.service.community.ChattingService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChattingController {
    private final ChattingService chattingService;

    // 채팅방 목록 조회
    @GetMapping("/rooms")
    public List<Map<String, Object>> getRooms(@RequestParam int userNo){
        return chattingService.getMyRooms(userNo);
    }

    // 메시지 히스토리 조회
    @GetMapping("/messages")
    public List<MessageDto> messages(@RequestParam int roomNo){
        return chattingService.getMessages(roomNo);
    }

    @PostMapping("/report")
    public ResponseEntity<?> reportMessage(@RequestBody ReportRequest req) {
        chattingService.reportMessage(req.getMessageNo(), req.getReporterNo(), req.getReason());
        return ResponseEntity.ok().build();
    }

    @Data
    public static class ReportRequest {
        private int messageNo;
        private int reporterNo; // 신고한 사람(현재 로그인 유저)
        private String reason;  // 신고 사유
    }


    // initRooms는 지금은 필요 없으니까 잠시 제거 or 주석 처리
    // @GetMapping("/initRooms")
    // public String initRooms() {
    //     int count = chattingService.createRoomsForAllFriends();
    //     return "created rooms: " + count;
    // }
}
