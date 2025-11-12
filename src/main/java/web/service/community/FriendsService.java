package web.service.community;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.model.dto.community.ChattingDto;
import web.model.dto.community.FriendsDto;
import web.model.mapper.ChattingMapper;
import web.model.mapper.FriendsMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendsService {
    private final FriendsMapper friendsMapper;
    private final ChattingService chattingService;

    //친구 추가(요청)
    public boolean addFriend(int offer, int receiver){
        Integer status = friendsMapper.check(offer, receiver);

        if(status == null){// 관계가 전혀 없으면 새 요청 삽입
            friendsMapper.addFriend(offer, receiver);
            return true;
        }else if (status == 0){ // 이미 요청 대기 중
            return false;
        }else if (status == 1){ // 이미 친구 상태
            return false;
        }else {
            friendsMapper.updateStatus(offer, receiver, 0);
            return true;
        }
    }

    //친구 수락
    public boolean acceptFriend(int offer, int receiver){
        int updated = friendsMapper.updateStatus(offer, receiver, 1);// 친구상태일때
        if(updated > 0){
            chattingService.createChatRoom(offer,receiver);// 자동채팅방 생성
            return true;
        }
        return false;
    }

    //친구 삭제
    public boolean deleteFriend(int offer, int receiver){
        return friendsMapper.updateStatus(offer,receiver, -1) > 0;
    }

    //친구 차단
    public boolean blockFriend(int offer, int receiver){
        return friendsMapper.updateStatus(offer, receiver, -2) > 0;
    }

    //내 친구 목록 조회
    public List<Map<String, Object>> friendList(int userNo){
        List<FriendsDto> friends = friendsMapper.FriendsList(userNo);
        List<Map<String, Object>> result = new ArrayList<>();
        for(FriendsDto f : friends){
            // 각 친구의 채팅방 정보 조회
            ChattingDto chat = friendsMapper.findChatList(f.getOffer(), f.getReceiver());

            Map<String, Object> map = new HashMap<>();
            map.put("friend", f);
            map.put("chat", chat);

            result.add(map);
        }
        return result;
    }
}
