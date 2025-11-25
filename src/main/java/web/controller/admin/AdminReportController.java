package web.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.model.dto.community.ReportMessageDto;
import web.service.admin.AdminReportService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/saykorean/admin/report")
@RequiredArgsConstructor
@Log4j2
public class AdminReportController {

    private final AdminReportService adminReportService;

    // [1] 신고 메시지 목록 조회 (미처리/전체)
    @GetMapping("")
    public ResponseEntity<List<Map<String, Object>>> getReportList(
            @RequestParam(required = false, defaultValue = "0") int status) {
        try {
            log.info("[AR-01] 신고 목록 조회: status={}", status);
            List<Map<String, Object>> reports = adminReportService.getReportList(status);
            log.info("[AR-01] 조회 결과: {}건", reports.size());
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("[AR-01] 신고 목록 조회 실패", e);
            return ResponseEntity.status(500).body(List.of());
        }
    }

    // [2] 신고 메시지 상세 조회
    @GetMapping("/{reportNo}")
    public ResponseEntity<Map<String, Object>> getReportDetail(@PathVariable int reportNo) {
        try {
            log.info("[AR-02] 신고 상세 조회: reportNo={}", reportNo);
            Map<String, Object> report = adminReportService.getReportDetail(reportNo);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("[AR-02] 신고 상세 조회 실패", e);
            return ResponseEntity.status(500).body(new HashMap<>());
        }
    }

    // [3] 신고 처리 (승인 -> 사용자 제재)
    @PostMapping("/{reportNo}/approve")
    public ResponseEntity<?> approveReport(
            @PathVariable int reportNo,
            @RequestParam int restrictDay) {
        try {
            log.info("[AR-03] 신고 승인 및 제재: reportNo={}, restrictDay={}", reportNo, restrictDay);
            int result = adminReportService.approveReport(reportNo, restrictDay);
            return ResponseEntity.ok(Map.of("success", true, "result", result));
        } catch (Exception e) {
            log.error("[AR-03] 신고 승인 실패", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // [4] 신고 거부
    @PostMapping("/{reportNo}/reject")
    public ResponseEntity<?> rejectReport(@PathVariable int reportNo) {
        try {
            log.info("[AR-04] 신고 거부: reportNo={}", reportNo);
            int result = adminReportService.rejectReport(reportNo);
            return ResponseEntity.ok(Map.of("success", true, "result", result));
        } catch (Exception e) {
            log.error("[AR-04] 신고 거부 실패", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // [5] 신고 통계
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getReportStats() {
        try {
            log.info("[AR-05] 신고 통계 조회");
            Map<String, Object> stats = adminReportService.getReportStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("[AR-05] 신고 통계 조회 실패", e);
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("totalReports", 0);
            emptyStats.put("pendingReports", 0);
            emptyStats.put("approvedReports", 0);
            emptyStats.put("rejectedReports", 0);
            return ResponseEntity.ok(emptyStats);
        }
    }
}