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
    public boolean addFriend(int offer, String email) {

        Integer receiver = friendsMapper.findUserNoByEmail(email);
        if (receiver == null) return false; // 존재하지 않음

        Integer status = friendsMapper.check(offer, receiver);

        if (status == null) {
            friendsMapper.addFriend(offer, receiver);
            return true;
        } else if (status == 0) {
            return false; // 이미 요청 중
        } else if (status == 1) {
            return false; // 이미 친구 상태
        } else {
            friendsMapper.updateStatus(offer, receiver, 0);
            return true;
        }
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
    public boolean deleteFriend(int offer, int receiver) {
        return friendsMapper.updateStatus(offer, receiver, -1) > 0;
    }

    // 친구 차단
    public boolean blockFriend(int offer, int receiver) {
        return friendsMapper.updateStatus(offer, receiver, -2) > 0;
    }

    public List<FriendsDto> requestsList(int userNo){
        return friendsMapper.findPendingList(userNo);
    }

    public List<FriendsDto> friendList(int userNo){
        return friendsMapper.FriendsList(userNo);
    }

}

// 받은 요청
