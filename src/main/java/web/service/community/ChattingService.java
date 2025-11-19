package web.service.community;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.model.dto.community.MessageDto;
import web.model.mapper.ChattingMapper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ChattingService {

    private final ChattingMapper chattingMapper;

    // 친구 수락 시 1:1 채팅방 생성 또는 방 번호 반환
    public int ensureRoom(int u1, int u2) {
        System.out.println("🔍 checkRoom(" + u1 + ", " + u2 + ") = " + chattingMapper.checkRoom(u1, u2));
        if (chattingMapper.checkRoom(u1, u2) == 0) {
            System.out.println("➡ createRoom 실행됨");
            chattingMapper.createRoom(u1, u2);
        }else {
            System.out.println("❗ 이미 방이 존재함");
        }
        int roomNo = chattingMapper.getRoomNo(u1, u2);
        System.out.println("📌 최종 roomNo = " + roomNo);
        return roomNo;
    }

    // 기존 친구 전체에 대한 방 생성 (초기 1회 실행)
    public int createRoomsForAllFriends() {
        return chattingMapper.createRoomsForExistingFriends();
    }

    // 채팅방 목록 조회
    public List<Map<String,Object>> getMyRooms(int userNo) {
        return chattingMapper.getMyRooms(userNo);
    }

    // 메시지 목록
    public List<Map<String, Object>> messages(int roomNo) {
        return chattingMapper.getMessages(roomNo);
    }

    // 메시지 저장
    public void saveMessage(int roomNo, int userNo, String msg) {
        chattingMapper.insertMessage(roomNo, userNo, msg);
    }

    // 메시지 히스토리 불러오기
    public List<MessageDto> getMessages(int roomNo){
        return chattingMapper.selectMessages(roomNo);
    }

    // 방 삭제
    public void deleteRoom(int u1, int u2) {
        Integer roomNo = chattingMapper.getRoomNoForDelete(u1, u2);

        if (roomNo != null) {
            chattingMapper.deleteRoom(roomNo);
            System.out.println("🗑 채팅방 삭제됨 : roomNo = " + roomNo);
        } else {
            System.out.println("⚠ 삭제할 채팅방 없음");
        }
    }

}
