package web.service.community;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.model.dto.community.ChatRoomDto;
import web.model.dto.community.MessageDto;
import web.model.mapper.ChattingMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChattingService {

    private final ChattingMapper chattingMapper;

    // 친구 수락 시 1:1 채팅방 생성 또는 방 번호 반환
    public int ensureRoom(int u1, int u2) {
        if (chattingMapper.checkRoom(u1, u2) == 0) {
            chattingMapper.createRoom(u1, u2);
        }
        return chattingMapper.getRoomNo(u1, u2);
    }

    // 기존 친구 전체에 대한 방 생성 (초기 1회 실행)
    public int createRoomsForAllFriends() {
        return chattingMapper.createRoomsForExistingFriends();
    }

    // 채팅방 목록 조회
    public List<ChatRoomDto> myRooms(int userNo) {
        return chattingMapper.getMyRooms(userNo);
    }

    // 메시지 목록
    public List<MessageDto> messages(int roomNo) {
        return chattingMapper.getMessages(roomNo);
    }

    // 메시지 저장
    public void saveMessage(int roomNo, int userNo, String msg) {
        chattingMapper.insertMessage(roomNo, userNo, msg);
    }
}
