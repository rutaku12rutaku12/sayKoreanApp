package web.model.mapper;

import lombok.Value;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.security.core.parameters.P;

@Mapper
public interface ChattingMapper {

    // 친구 간 채팅방 존재 여부 확인
    @Select("SELECT COUNT(*) FROM chatList WHERE chatListTitle = CONCAT(LEAST(#{offer}, #{receiver}), '_', GREATEST(#{offer}, #{receiver}))")
    int checkChatRoom(@Param("offer") int offer, @Param("receiver") int receiver);

    // 개인 채팅방 생성 (친구 수락 시 자동)
    @Insert("INSERT INTO chatList (chatListTitle, chatListState, userNo) VALUES (CONCAT(LEAST(#{offer}, #{receiver}), '_', GREATEST(#{offer}, #{receiver}))")
    int createChatRoom(@Param("offer") int offer, @Param("receiver") int receiver);


}
