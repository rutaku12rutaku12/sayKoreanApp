package web.controller.community;

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

    //채팅방 목록 조회
    @GetMapping("/rooms")
    public List<Map<String, Object>> getRooms(@RequestParam int userNo){
        return chattingService.getMyRooms(userNo);
    }

    //메시지 저장 목록 조회
    @GetMapping("/messages")
    public List<Map<String,Object>> messages(@RequestParam int roomNo){
        return chattingService.messages(roomNo);
    }

    @GetMapping("/initRooms")
    public String initRooms() {
        int count = chattingService.createRoomsForAllFriends();
        return "created rooms: " + count;
    }


}
