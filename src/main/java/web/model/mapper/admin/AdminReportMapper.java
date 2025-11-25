package web.model.mapper.admin;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminReportMapper {

    // [1] 신고 목록 조회 (status: 0=전체, 1=승인, 2=거부, 기타=미처리)
    @Select("""
        <script>
        SELECT 
            r.reportNo,
            r.messageNo,
            r.reporterNo,
            r.reportedNo,
            r.reportReason,
            r.reportStatus,
            r.reportTime,
            r.snapshotMessage,
            u1.nickName AS reporterName,
            u1.email AS reporterEmail,
            u2.nickName AS reportedName,
            u2.email AS reportedEmail
        FROM reportMessage r
        LEFT JOIN users u1 ON r.reporterNo = u1.userNo
        LEFT JOIN users u2 ON r.reportedNo = u2.userNo
        <where>
            <if test="status == 1">
                r.reportStatus = 1
            </if>
            <if test="status == 2">
                r.reportStatus = 2
            </if>
            <if test="status != 0 and status != 1 and status != 2">
                r.reportStatus = 0
            </if>
        </where>
        ORDER BY r.reportTime DESC
        </script>
        """)
    List<Map<String, Object>> getReportList(@Param("status") int status);

    // [2] 신고 상세 조회
    @Select("""
        SELECT 
            r.reportNo,
            r.messageNo,
            r.reporterNo,
            r.reportedNo,
            r.reportReason,
            r.reportStatus,
            r.reportTime,
            r.snapshotMessage,
            u1.nickName AS reporterName,
            u1.email AS reporterEmail,
            u2.nickName AS reportedName,
            u2.email AS reportedEmail,
            u2.userState AS reportedState
        FROM reportMessage r
        LEFT JOIN users u1 ON r.reporterNo = u1.userNo
        LEFT JOIN users u2 ON r.reportedNo = u2.userNo
        WHERE r.reportNo = #{reportNo}
        """)
    Map<String, Object> getReportDetail(int reportNo);

    // [3] 신고 상태 업데이트 (0: 미처리, 1: 승인, 2: 거부)
    @Update("""
        UPDATE reportMessage
        SET reportStatus = #{status}
        WHERE reportNo = #{reportNo}
        """)
    int updateReportStatus(@Param("reportNo") int reportNo, @Param("status") int status);

    // [4] 제재 기록 저장 (새 테이블 필요: restrictRecord)
    @Insert("""
        INSERT INTO restrictRecord (userNo, restrictDay, restrictDate, restrictEndDate)
        VALUES (#{userNo}, #{restrictDay}, NOW(), DATE_ADD(NOW(), INTERVAL #{restrictDay} DAY))
        """)
    int insertRestrictRecord(@Param("userNo") int userNo, @Param("restrictDay") int restrictDay);

    // [5] 신고 통계
    @Select("""
        SELECT 
            COUNT(*) AS totalReports,
            SUM(CASE WHEN reportStatus = 0 THEN 1 ELSE 0 END) AS pendingReports,
            SUM(CASE WHEN reportStatus = 1 THEN 1 ELSE 0 END) AS approvedReports,
            SUM(CASE WHEN reportStatus = 2 THEN 1 ELSE 0 END) AS rejectedReports
        FROM reportMessage
        """)
    Map<String, Object> getReportStats();

    // [6] 사용자의 제재 기록 조회
    @Select("""
        SELECT 
            restrictNo,
            userNo,
            restrictDay,
            restrictDate,
            restrictEndDate
        FROM restrictRecord
        WHERE userNo = #{userNo}
        ORDER BY restrictDate DESC
        """)
    List<Map<String, Object>> getRestrictRecords(int userNo);

    // [7] 현재 제재 중인지 확인
    @Select("""
        SELECT COUNT(*) > 0
        FROM restrictRecord
        WHERE userNo = #{userNo}
        AND NOW() < restrictEndDate
        """)
    boolean isRestricted(int userNo);

    // [8] 남은 제재 일수 조회
    @Select("""
        SELECT DATEDIFF(restrictEndDate, NOW()) AS remainingDays
        FROM restrictRecord
        WHERE userNo = #{userNo}
        AND NOW() < restrictEndDate
        ORDER BY restrictEndDate DESC
        LIMIT 1
        """)
    Integer getRemainingRestrictDays(int userNo);
}