package web.model.mapper;

import org.apache.ibatis.annotations.*;
import org.springframework.security.core.parameters.P;
import web.model.dto.community.ChattingDto;
import web.model.dto.community.FriendsDto;

import java.util.List;

@Mapper
public interface FriendsMapper {

    //친구 추가(요청)
    @Insert("INSERT INTO friend (offer, receiver, frenStatus) VALUES (#{offer}, #{receiver}, 0)")
    int addFriend(@Param("offer") int offer, @Param("receiver")int receiver);

    //친구 상태 변경(수락, 거절, 삭제, 차단 등)
    @Update("UPDATE friend SET frenStatus = #{status}, frenUpdate = NOW() WHERE ( offer = #{offer} AND receiver = #{receiver}) OR (offer = #{receiver} AND receiver = #{offer})")
    int updateStatus(@Param("offer") int offer, @Param("receiver") int receiver, @Param("status") int status);

    //친구 관계 중복 확인 + *상태 확인*
    @Select("SELECT frenStatus FROM friend WHERE ((offer = #{offer} AND receiver = #{receiver}) OR (offer = #{receiver} AND receiver = #{offer})) AND frenStatus IN (0,1) LIMIT 1")
    Integer check(@Param("offer") int offer, @Param("receiver") int receiver);


    // 친구 삭제(양방향 삭제)
    @Delete("DELETE FROM friend WHERE (offer = #{offer} AND receiver = #{receiver}) OR (offer = #{receiver} AND receiver = #{offer})")
    int deleteFriend(@Param("offer") int offer, @Param("receiver") int receiver);

    //친구 차단

    //받은 친구 요청 목록
    @Select("SELECT * FROM friend WHERE receiver = #{userNo} AND frenStatus = 0")
    List<FriendsDto> findPendingList(int userNo);

    //내 친구 목록 조회
    @Select("SELECT * FROM friend WHERE (offer = #{userNo} OR receiver = #{userNo}) AND frenStatus =1")
    List<FriendsDto> FriendsList(int userNo);

    //각 친구의 채팅방 정보 조회 (chatListTitle 기준)
    @Select("SELECT chatListTitle, chatListState, userNo FROM chatList WHERE chatListTitle = CONCAT(LEAST(#{offer}, #{receiver}), '_', GREATEST(#{offer}, #{receiver}))")//LEAST 는 두값 중 작은 값 반환 , GREATEST 는 두값 중 큰 값 반환 ex) 3_7 으로 반환되어 채팅방 확인
    ChattingDto findChatList(@Param("offer") int offer, @Param("receiver") int receiver);
}
