package web.controller.community;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import web.model.dto.community.ChatRoomDto;
import web.model.dto.community.MessageDto;
import web.service.community.ChattingService;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChattingController {
    private final ChattingService chattingService;

    //채팅방 목록 조회
    @GetMapping("/rooms")
    public List<ChatRoomDto> rooms(@RequestParam int userNo){
        return chattingService.myRooms(userNo);
    }

    //메시지 저장 목록 조회
    @GetMapping("/messages")
    public List<MessageDto> messages(@RequestParam int roomNo){
        return chattingService.messages(roomNo);
    }



}
