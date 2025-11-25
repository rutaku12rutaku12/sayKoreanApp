package web.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.model.mapper.admin.AdminReportMapper;
import web.model.mapper.admin.AdminUserMapper;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminReportService {

    private final AdminReportMapper adminReportMapper;
    private final AdminUserMapper adminUserMapper;

    // [1] 신고 목록 조회
    public List<Map<String, Object>> getReportList(int status) {
        return adminReportMapper.getReportList(status);
    }

    // [2] 신고 상세 조회
    public Map<String, Object> getReportDetail(int reportNo) {
        Map<String, Object> report = adminReportMapper.getReportDetail(reportNo);
        if (report == null) {
            throw new RuntimeException("신고 정보를 찾을 수 없습니다.");
        }
        return report;
    }

    // [3] 신고 승인 및 사용자 제재
    public int approveReport(int reportNo, int restrictDay) {
        // 신고 정보 조회
        Map<String, Object> report = adminReportMapper.getReportDetail(reportNo);
        if (report == null) {
            throw new RuntimeException("신고 정보를 찾을 수 없습니다.");
        }

        int reportedNo = (int) report.get("reportedNo");

        // 1) 사용자 제재
        int restrictResult = adminUserMapper.updateRestrictUser(reportedNo);
        if (restrictResult == 0) {
            throw new RuntimeException("사용자 제재에 실패했습니다.");
        }

        // 2) 제재 기록 저장
        int recordResult = adminReportMapper.insertRestrictRecord(reportedNo, restrictDay);
        if (recordResult == 0) {
            throw new RuntimeException("제재 기록 저장에 실패했습니다.");
        }

        // 3) 신고 상태 업데이트 (승인: 1)
        int updateResult = adminReportMapper.updateReportStatus(reportNo, 1);
        if (updateResult == 0) {
            throw new RuntimeException("신고 상태 업데이트에 실패했습니다.");
        }

        return updateResult;
    }

    // [4] 신고 거부
    public int rejectReport(int reportNo) {
        // 신고 상태 업데이트 (거부: 2)
        int result = adminReportMapper.updateReportStatus(reportNo, 2);
        if (result == 0) {
            throw new RuntimeException("신고 거부에 실패했습니다.");
        }
        return result;
    }

    // [5] 신고 통계
    public Map<String, Object> getReportStats() {
        return adminReportMapper.getReportStats();
    }
}