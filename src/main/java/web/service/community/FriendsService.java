package web.service.community;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.model.dto.community.FriendsDto;
import web.model.mapper.FriendsMapper;
import web.service.community.ChattingService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendsService {

    private final FriendsMapper friendsMapper;
    private final ChattingService chattingService;

    // 친구 요청
    public Map<String, Object> addFriend(int offer, String email) {

        Map<String, Object> result = new HashMap<>();

        Integer receiver = friendsMapper.findUserNoByEmail(email);
        if (receiver == null) {
            result.put("success", false);
            result.put("message", "존재하지 않은 사용자입니다.");
            return result;
        }

        if (offer == receiver) {
            result.put("success", false);
            result.put("message", "본인에게는 친구요청이 불가합니다.");
            return result;
        }

        Integer status = friendsMapper.check(offer, receiver);

        // 1) 관계 없음 → 새 요청
        if (status == null) {
            friendsMapper.addFriend(offer, receiver);
            result.put("success", true);
            result.put("message", "친구 요청을 보냈습니다.");
            return result;
        }

        // 2) 요청 중
        if (status == 0) {
            result.put("success", false);
            result.put("message", "이미 요청중입니다.");
            return result;
        }

        // 3) 이미 친구
        if (status == 1) {
            result.put("success", false);
            result.put("message", "이미 친구입니다.");
            return result;
        }

        // 4) 차단(-2) 상태 → 절대 요청 못 보내게
        if (status == -2) {
            result.put("success", false);
            result.put("message", "차단 상태에서는 친구 요청이 불가합니다.");
            return result;
        }

        // 5) 삭제(-1) 상태일 때만 다시 요청 가능
        if (status == -1) {
            friendsMapper.updateStatus(offer, receiver, 0);
            result.put("success", true);
            result.put("message", "다시 친구 요청을 보냈습니다.");
            return result;
        }

        return result;
    }

    // 친구 수락 (자동 채팅방 생성)
    public boolean acceptFriend(int u1, int u2) {
        int a = Math.min(u1, u2);
        int b = Math.max(u1, u2);

        int updated = friendsMapper.updateStatus(a, b, 1);
        if (updated > 0) {
            // ★ 여기서 반드시 채팅방 생성/조회
            chattingService.ensureRoom(a, b);
            return true;
        }
        return false;
    }

    // 친구 거절 = 상태 -1 → 채팅방 삭제
    public boolean refusalFriend(int offer, int receiver) {
        int a = Math.min(offer, receiver);
        int b = Math.max(offer, receiver);

        boolean ok = friendsMapper.updateStatus(a, b, -1) > 0;
        if (ok) {
            chattingService.deleteRoomsByUsers(a, b);
        }
        return ok;
    }

    // 친구 삭제 = 상태 -1 → 채팅방 삭제
    public boolean deleteFriend(int u1, int u2) {
        int a = Math.min(u1, u2);
        int b = Math.max(u1, u2);

        boolean ok = friendsMapper.updateStatus(a, b, -1) > 0;
        if (ok) {
            chattingService.deleteRoomsByUsers(a, b);
        }
        return ok;
    }

    // 친구 차단 (방 삭제 X)
    public boolean blockFriend(int u1, int u2) {
        // friend 테이블이 LEAST/GREATEST로 저장한다면 이쪽도 맞춰주기
        int a = Math.min(u1, u2);
        int b = Math.max(u1, u2);
        return friendsMapper.updateStatus(a, b, -2) > 0;
    }

    // 요청 받은 목록
    public List<FriendsDto> requestsList(int userNo){
        return friendsMapper.findPendingList(userNo);
    }

    // 보낸 요청 목록
    public List<FriendsDto> sendList(int userNo){
        return friendsMapper.findSendList(userNo);
    }

    // 친구 목록 조회
    public List<FriendsDto> friendList(int userNo){
        return friendsMapper.FriendsList(userNo);
    }
}
