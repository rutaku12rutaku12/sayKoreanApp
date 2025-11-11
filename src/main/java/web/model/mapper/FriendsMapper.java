package web.model.mapper;

import org.apache.ibatis.annotations.*;
import org.springframework.security.core.parameters.P;
import web.model.dto.community.FriendsDto;

import java.util.List;

@Mapper
public interface FriendsMapper {

    //친구 추가(요청)
    @Insert("INSERT INTO friend (offer, receiver, frenStatus) VALUES (#{offer}, #{receiver}, 0)")
    int addFriend(@Param("offer") int offer, @Param("receiver")int receiver);

    //친구 상태 변경(수락, 거절, 삭제, 차단 등)

    @Update("UPDATE friend SET frenStatus = #{status}, offer = #{offer}, receiver = #{receiver}")
    int updateStatus(@Param("offer") int offer, @Param("receiver") int receiver, @Param("status") int status);


    //친구 관계 중복 확인
    @Select("SELECT COUNT(*) FROM friend WHERE (offer = #{offer} AND receiver = #{receiver}) OR (offer = #{receiver} AND receiver = #{offer})")
    int check(@Param("offer") int offer, @Param("receiver") int receiver);


    // 친구 삭제(양방향 삭제)
    @Delete("DELETE FROM friend WHERE (offer = #{offer} AND receiver = #{receiver}) OR (offer = #{recevier} AND receiver = #{offer}")
    int deleteFriend(@Param("offer") int offer, @P("receiver") int receiver);

    //친구 차단

    //받은 친구 요청 목록
    @Select("SELECT * FROM friend WHERE receiver = #{userNo} AND frenStatus == 0")
    List<FriendsDto> findPendingList(int userNo);

    //내 친구 목록 조회
    @Select("SELECT * FROM friend WHERE (offer = #{userNo} OR receiver = #{userNo}) AND frenStatus =1")
    List<FriendsDto> FriendsList(int userNo);
}
