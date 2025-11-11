package web.service.community;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.model.mapper.ChattingMapper;
import web.model.mapper.FriendsMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendsService {
    private final FriendsMapper friendsMapper;
    private final ChattingService chattingService;

    //친구 추가(요청)
    public boolean addFriend(int offer, int receiver){
        if (friendsMapper.check(offer, receiver) > 0){ // 만약에 친구 상태가 0보다 크면(친구상태) 실패, 중복 방지
            return false;
        }
        return friendsMapper.addFriend(offer, receiver) == 1;
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

    //내 친구 목록 조회
}
