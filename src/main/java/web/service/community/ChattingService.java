package web.service.community;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.model.mapper.ChattingMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class ChattingService {
    private final ChattingMapper chattingMapper;

    // 친구 수락시 개인 채팅방 자동 생성
    public void createChatRoom(int offer, int receiver){
        if(chattingMapper.checkChatRoom(offer, receiver) ==0 ){
            chattingMapper.createChatRoom(offer, receiver);
            System.out.println("채팅방 생성완료");
        }else {
            System.out.println("이미 채팅방 존재");
        }
    }


}
