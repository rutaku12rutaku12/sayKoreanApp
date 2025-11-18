package web.model.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import web.model.dto.community.ChattingDto;
import web.model.dto.community.MessageDto;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChattingMapper {

    // 1:1 채팅방 생성
    @Insert("""
        INSERT INTO chatList (chatListTitle, chatListState, userNo)
        VALUES (CONCAT(LEAST(#{u1}, #{u2}), '_', GREATEST(#{u1}, #{u2})), 1, #{u1})
    """)
    int createRoom(@Param("u1") int u1, @Param("u2") int u2);

    // 존재 여부 체크
    @Select("""
        SELECT COUNT(*)
        FROM chatList
        WHERE chatListTitle = CONCAT(LEAST(#{u1}, #{u2}), '_', GREATEST(#{u1}, #{u2}))
    """)
    int checkRoom(@Param("u1") int u1, @Param("u2") int u2);

    // 방 번호 가져오기
    @Select("""
        SELECT chatListNo
        FROM chatList
        WHERE chatListTitle = CONCAT(LEAST(#{u1}, #{u2}), '_', GREATEST(#{u1}, #{u2}))
        LIMIT 1
    """)
    Integer getRoomNo(@Param("u1") int u1, @Param("u2") int u2);

    // 기존 친구 전부 → 채팅방 생성
    @Insert("""
    INSERT INTO chatList (chatListTitle, chatListState, userNo)
    SELECT
        roomTitle,
        1,
        roomOwner
    FROM (
        SELECT
            CONCAT(LEAST(f.offer, f.receiver), '_', GREATEST(f.offer, f.receiver)) AS roomTitle,
            LEAST(f.offer, f.receiver) AS roomOwner
        FROM friend f
        WHERE f.frenStatus = 1
    ) AS sub
    WHERE sub.roomTitle NOT IN (SELECT chatListTitle FROM chatList)
""")
    int createRoomsForExistingFriends();


    // 채팅방 삭제
    @org.apache.ibatis.annotations.Delete("""
            DELETE FROM chatList WHERE chatListNo = #{roomNo}
            """)
    int deleteRoom(int roomNo);

    // 방 번호 조회 (친구 삭제 시)
    @Select("""
            SELECT chatListNo FROM chatList 
            WHERE chatListTitle CONCAT(EAST(#{u1}, #{u2}), '_', GREATEST(#{u1}, #{u2})
            LIMIT 1
            """)
    Integer getRoomNoForDelete(@Param("u1") int u1,@Param("u2") int u2);

    // 채팅방 목록
    @Select("""
        SELECT
            c.chatListNo AS roomNo,
            c.chatListTitle,
            CASE
                WHEN CAST(SUBSTRING_INDEX(c.chatListTitle, '_', 1) AS SIGNED) = #{userNo}
                    THEN CAST(SUBSTRING_INDEX(c.chatListTitle, '_', -1) AS SIGNED)
                ELSE CAST(SUBSTRING_INDEX(c.chatListTitle, '_', 1) AS SIGNED)
            END AS friendNo,
            (SELECT nickName FROM users WHERE userNo =
                CASE
                    WHEN CAST(SUBSTRING_INDEX(c.chatListTitle, '_', 1) AS SIGNED) = #{userNo}
                        THEN CAST(SUBSTRING_INDEX(c.chatListTitle, '_', -1) AS SIGNED)
                    ELSE CAST(SUBSTRING_INDEX(c.chatListTitle, '_', 1) AS SIGNED)
                END
            ) AS friendName,
            (SELECT chatMessage
             FROM chat WHERE chatListNo = c.chatListNo
             ORDER BY messageNo DESC LIMIT 1) AS lastMessage,
            (SELECT DATE_FORMAT(chatTime, '%Y-%m-%d %H:%i')
             FROM chat WHERE chatListNo = c.chatListNo
             ORDER BY messageNo DESC LIMIT 1) AS lastTime
        FROM chatList c
        WHERE c.chatListTitle LIKE CONCAT('%', #{userNo}, '%')
        ORDER BY lastTime DESC
    """)
    List<Map<String, Object>> getMyRooms(int userNo);


    // 메시지 목록
    @Select("""
        SELECT 
            messageNo,
            chatMessage,
            chatListNo,
            sendNo
        FROM chat
        WHERE chatListNo = #{roomNo}
        ORDER BY messageNo ASC
    """)
    List<Map<String, Object>> getMessages(int roomNo);

    // 메시지 저장
    @Insert("""
        INSERT INTO chat(chatMessage, chatTime, chatListNo, sendNo)
        VALUES (#{msg}, NOW(), #{roomNo}, #{sendNo})
    """)
    int insertMessage(@Param("roomNo") int roomNo, @Param("sendNo")
    int sendNo, @Param("msg") String msg);

    void createChatRoom(int u1, int u2);

    // 히스토리 조회
    @Select("""
            SELECT  messageNo, chatMessage, chatTime, chatListNo, sendNo
            FROM chat
            WHERE chatListNo = #{roomNo}
            ORDER BY chatTime ASC
            """)
    List<MessageDto> selectMessages(int roomNo);
}
