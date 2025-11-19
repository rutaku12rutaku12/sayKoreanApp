package web.service.community;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.model.dto.community.FriendsDto;
import web.model.mapper.FriendsMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendsService {
    private final FriendsMapper friendsMapper;
    private final ChattingService chattingService;

    // 친구 요청 (추가)
    public Map<String, Object> addFriend(int offer, String email) {

        Map<String, Object> result = new HashMap<>();

        Integer receiver = friendsMapper.findUserNoByEmail(email);
        if (receiver == null){ // 존재하지 않음
            result.put("success", false);
            result.put("message", "존재하지 않은 사용자입니다.");
            return result;
        }

        if (offer == receiver) {// 본인에게 친구 요청 불가
            result.put("success", false);
            result.put("message", "본인에게는 친구요청이 불가합니다.");
            return result;
        }

        Integer status = friendsMapper.check(offer, receiver); // 기존 관계 상태 조회

        if (status == null) {
            friendsMapper.addFriend(offer, receiver);
            result.put("success", true);
            result.put("message", "친구 요청을 보냈습니다.");

        } else if (status == 0) {//요청중인 상태
            result.put("success", false);
            result.put("message", "이미 요청중입니다.");
        } else if (status == 1) {//이미 친구
            result.put("success", false);
            result.put("message", "이미 친구입니다.");
        } else {
            friendsMapper.updateStatus(offer, receiver, 0);
            result.put("success", true);
        }
        return result;
    }

    // 친구 수락
    public boolean acceptFriend(int offer, int receiver) {
        int updated = friendsMapper.updateStatus(offer, receiver, 1);
        if (updated > 0) {
            // 🔵 1:1 채팅방 자동 생성
            chattingService.ensureRoom(offer, receiver);
            return true;
        }
        return false;
    }

    // 친구 거절
    public boolean refusalFriend(int offer, int receiver) {
        return friendsMapper.deleteFriend(offer, receiver) > 0;
    }

    // 친구 삭제
    public boolean deleteFriend(int u1, int u2) {
        int a = Math.min(u1, u2);
        int b = Math.max(u1, u2);
        boolean ok = friendsMapper.updateStatus(u1, u2, -1) > 0;
        if(ok){
            chattingService.deleteRoom(a,b);
        }
        return ok;
    }

    // 친구 차단
    public boolean blockFriend(int offer, int receiver) {
        return friendsMapper.updateStatus(offer, receiver, -2) > 0;
    }

    //요청 받은 목록 조회
    public List<FriendsDto> requestsList(int userNo){
        return friendsMapper.findPendingList(userNo);
    }

    //보낸 요청 목록 조회
    public List<FriendsDto> sendList(int userNo){
        return friendsMapper.findSendList(userNo);
    }

    //내 친구 목록 조회
    public List<FriendsDto> friendList(int userNo){
        return friendsMapper.FriendsList(userNo);
    }

}


