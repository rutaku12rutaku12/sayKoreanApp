package web.model.mapper;

import org.apache.ibatis.annotations.*;
import web.model.dto.community.MessageDto;
import web.model.dto.community.ReportMessageDto;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChattingMapper {

    // 1:1 채팅방 생성 (u1, u2 각각 1개씩 = 2 row 생성)
    @Insert("""
        INSERT INTO chatList (chatListTitle, chatListState, userNo)
        VALUES 
        (CONCAT(LEAST(#{u1}, #{u2}), '_', GREATEST(#{u1}, #{u2})), 1, #{u1}),
        (CONCAT(LEAST(#{u1}, #{u2}), '_', GREATEST(#{u1}, #{u2})), 1, #{u2})
        """)
    int createRoom(@Param("u1") int u1, @Param("u2") int u2);

    // 방 존재 여부
    @Select("""
        SELECT COUNT(*)
        FROM chatList
        WHERE chatListTitle = CONCAT(LEAST(#{u1}, #{u2}), '_', GREATEST(#{u1}, #{u2}))
        """)
    int checkRoom(@Param("u1") int u1, @Param("u2") int u2);

    // 대표 roomNo = 같은 제목(chatListTitle) 중 가장 작은 chatListNo
    @Select("""
        SELECT MIN(chatListNo)
        FROM chatList
        WHERE chatListTitle = CONCAT(LEAST(#{u1}, #{u2}), '_', GREATEST(#{u1}, #{u2}))
        """)
    Integer getRoomNo(@Param("u1") int u1, @Param("u2") int u2);

    // 내 채팅방 목록
    @Select("""
        WITH room_base AS (
            SELECT
                -- ★ 이 유저가 속한 방 제목들에 대해, 전체 chatList 중에서의 MIN(chatListNo)를 대표 roomNo로 사용
                (SELECT MIN(c2.chatListNo)
                 FROM chatList c2
                 WHERE c2.chatListTitle = c.chatListTitle
                ) AS roomNo,
                c.chatListTitle
            FROM chatList c
            WHERE c.userNo = #{userNo}
            GROUP BY c.chatListTitle
        )
        SELECT
            rb.roomNo,
            rb.chatListTitle,
            CASE
                WHEN CAST(SUBSTRING_INDEX(rb.chatListTitle, '_', 1) AS SIGNED) = #{userNo}
                    THEN CAST(SUBSTRING_INDEX(rb.chatListTitle, '_', -1) AS SIGNED)
                ELSE CAST(SUBSTRING_INDEX(rb.chatListTitle, '_', 1) AS SIGNED)
            END AS friendNo,
            (SELECT nickName FROM users u WHERE u.userNo =
                CASE
                    WHEN CAST(SUBSTRING_INDEX(rb.chatListTitle, '_', 1) AS SIGNED) = #{userNo}
                        THEN CAST(SUBSTRING_INDEX(rb.chatListTitle, '_', -1) AS SIGNED)
                    ELSE CAST(SUBSTRING_INDEX(rb.chatListTitle, '_', 1) AS SIGNED)
                END
            ) AS friendName,
            (SELECT chatMessage
             FROM chat
             WHERE chatListNo = rb.roomNo
             ORDER BY messageNo DESC 
             LIMIT 1
            ) AS lastMessage,
            (SELECT DATE_FORMAT(chatTime, '%Y-%m-%d %H:%i')
             FROM chat 
             WHERE chatListNo = rb.roomNo
             ORDER BY messageNo DESC 
             LIMIT 1
            ) AS lastTime
        FROM room_base rb
        ORDER BY lastTime IS NULL, lastTime DESC
        """)
    List<Map<String, Object>> getMyRooms(int userNo);

    // 메시지 목록 (히스토리)
    @Select("""
        SELECT 
            messageNo,
            chatMessage,
            chatTime,
            chatListNo,
            sendNo
        FROM chat
        WHERE chatListNo = #{roomNo}
        ORDER BY messageNo ASC
        """)
    List<MessageDto> selectMessages(int roomNo);

    // 메시지 저장 (chatTime 은 DB DEFAULT NOW() 사용)
    @Insert("""
        INSERT INTO chat(chatMessage, chatListNo, sendNo)
        VALUES (#{chatMessage}, #{chatListNo}, #{sendNo})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "messageNo")
    int insertMessage(MessageDto dto);

    // 디버그용 (필요하면 사용)
    @Select("SELECT chatListTitle FROM chatList WHERE chatListNo = #{roomNo}")
    String getChatListTitle(int roomNo);

    // 친구 삭제 시 해당 방(두 row 모두) 제거
    @Delete("""
        DELETE FROM chatList
        WHERE chatListTitle = CONCAT(LEAST(#{u1}, #{u2}), '_', GREATEST(#{u1}, #{u2}))
        """)
    int deleteRoomsByUsers(@Param("u1") int u1, @Param("u2") int u2);

    // 1) 특정 메시지 조회 (신고 시 메시지 내용/보낸 사람 얻기용)
    @Select("""
        SELECT messageNo, chatMessage, chatTime, chatListNo, sendNo
        FROM chat
        WHERE messageNo = #{messageNo}
        """)
    MessageDto findMessageByNo(int messageNo);

    // 2) 특정 유저가 이미 같은 메시지를 신고했는지 체크
    @Select("""
        SELECT COUNT(*) 
        FROM reportMessage
        WHERE messageNo = #{messageNo}
          AND reporterNo = #{reporterNo}
        """)
    int countReportByUser(@Param("messageNo") int messageNo,
                          @Param("reporterNo") int reporterNo);

    // 3) 신고 저장
    @Insert("""
        INSERT INTO reportMessage
            (messageNo, reporterNo, reportedNo, reportReason, snapshotMessage)
        VALUES
            (#{messageNo}, #{reporterNo}, #{reportedNo}, #{reportReason}, #{snapshotMessage})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "reportNo")
    int insertReportMessage(ReportMessageDto dto);


}
