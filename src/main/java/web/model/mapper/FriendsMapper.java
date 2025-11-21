package web.model.mapper;

import org.apache.ibatis.annotations.*;
import org.springframework.security.core.parameters.P;
import web.model.dto.community.ChattingDto;
import web.model.dto.community.FriendsDto;

import java.util.List;

@Mapper
public interface FriendsMapper {

    //친구 추가(요청)
    @Insert("""
    INSERT INTO friend (offer, receiver, frenStatus) 
    VALUES (LEAST(#{u1}, #{u2}), GREATEST(#{u1}, #{u2}), 0)
""")
    int addFriend(@Param("u1") int u1, @Param("u2") int u2);

    // 2) 이메일로 userNo 조회
    @Select("SELECT userNo FROM users WHERE email = #{email}")
    Integer findUserNoByEmail(String email);

    //친구 상태 변경(수락, 거절, 삭제, 차단 등)
    @Update("UPDATE friend SET frenStatus = #{status}, frenUpdate = NOW() WHERE offer = LEAST(#{u1}, #{u2}) AND receiver = GREATEST (#{u1}, #{u2})")
    int updateStatus(@Param("u1") int offer, @Param("u2") int receiver, @Param("status") int status);

    //친구 관계 중복 확인 + *상태 확인*
    @Select("SELECT frenStatus FROM friend WHERE ((offer = #{offer} AND receiver = #{receiver}) OR (offer = #{receiver} AND receiver = #{offer})) AND frenStatus IN (0,1) LIMIT 1")
    Integer check(@Param("offer") int offer, @Param("receiver") int receiver);


    // 친구 삭제(양방향 삭제)
    @Delete("DELETE FROM friend WHERE offer = LEAST(#{u1}, #{u2}) AND receiver = GREATEST(#{u1}, #{u2})")
    int deleteFriend(@Param("u1") int u1, @Param("u2") int u2);

    //친구 차단

    //받은 친구 요청 목록
    @Select("""
    SELECT
        f.frenNo,
        f.frenStatus,
        f.offer,
        f.receiver,
        u.name AS friendName
    FROM friend f
    JOIN users u
        ON u.userNo = f.offer
    WHERE
        f.receiver = #{userNo}
        AND f.frenStatus = 0
    """)
    @Results({
            @Result(column = "frenNo", property = "frenNo"),
            @Result(column = "frenStatus", property = "frenStatus"),
            @Result(column = "offer", property = "offer"),
            @Result(column = "receiver", property = "receiver"),
            @Result(column = "friendName", property = "friendName")
    })
    List<FriendsDto> findPendingList(int userNo);

    //보낸 친구 요청 목록
    @Select("""
    SELECT
        f.frenNo,
        f.frenStatus,
        f.offer,
        f.receiver,
        u.name AS friendName
    FROM friend f
    JOIN users u
        ON u.userNo = f.receiver
    WHERE
        f.offer = #{userNo}
        AND f.frenStatus = 0
    """)
    @Results({
            @Result(column = "frenNo", property = "frenNo"),
            @Result(column = "frenStatus", property = "frenStatus"),
            @Result(column = "offer", property = "offer"),
            @Result(column = "receiver", property = "receiver"),
            @Result(column = "friendName", property = "friendName")
    })
    List<FriendsDto> findSendList(int userNo);

    //내 친구 목록 조회
    @Select("""
    SELECT
        f.frenNo, f.frenStatus, f.offer, f.receiver,
        u.name AS friendName
    FROM friend f
    JOIN users u
        ON u.userNo =
            CASE
                WHEN f.offer = #{userNo} THEN f.receiver
                ELSE f.offer
            END
    WHERE
        (f.offer = #{userNo} OR f.receiver = #{userNo})
        AND f.frenStatus = 1
    """)
    @Results({
            @Result(column = "frenNo" , property = "frenNo"),
            @Result(column = "frenStatus", property = "frenStatus"),
            @Result(column = "offer", property = "offer"),
            @Result(column = "receiver", property = "receiver"),
            @Result(column = "friendName", property = "friendName")
    })
    List<FriendsDto> FriendsList(int userNo);

    //각 친구의 채팅방 정보 조회 (chatListTitle 기준)
    @Select("SELECT chatListTitle, chatListState, userNo FROM chatList WHERE chatListTitle = CONCAT(LEAST(#{offer}, #{receiver}), '_', GREATEST(#{offer}, #{receiver}))")//LEAST 는 두값 중 작은 값 반환 , GREATEST 는 두값 중 큰 값 반환 ex) 3_7 으로 반환되어 채팅방 확인
    ChattingDto findChatList(@Param("offer") int offer, @Param("receiver") int receiver);





}
