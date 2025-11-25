import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { adminReportApi } from "../api/adminReportApi";

export default function AdminReportList() {
    const navigate = useNavigate();

    const [reports, setReports] = useState([]);
    const [stats, setStats] = useState({
        totalReports: 0,
        pendingReports: 0,
        approvedReports: 0,
        rejectedReports: 0
    });
    const [loading, setLoading] = useState(false);
    const [filterStatus, setFilterStatus] = useState(0);

    // 제재 모달
    const [showRestrictModal, setShowRestrictModal] = useState(false);
    const [selectedReport, setSelectedReport] = useState(null);
    const [restrictDay, setRestrictDay] = useState(7);

    useEffect(() => {
        fetchData();
    }, [filterStatus]);

    const fetchData = async () => {
        try {
            setLoading(true);

            // 신고 목록
            try {
                const reportsRes = await adminReportApi.getReportList(filterStatus);
                console.log("신고 목록:", reportsRes.data);
                setReports(reportsRes.data || []);
            } catch (err) {
                console.error("신고 목록 조회 실패:", err);
                setReports([]);
            }

            // 통계
            try {
                const statsRes = await adminReportApi.getReportStats();
                console.log("통계:", statsRes.data);
                setStats(statsRes.data || {
                    totalReports: 0,
                    pendingReports: 0,
                    approvedReports: 0,
                    rejectedReports: 0
                });
            } catch (err) {
                console.error("통계 조회 실패:", err);
            }
        } catch (e) {
            console.error("데이터 조회 실패:", e);
            alert("데이터를 불러오는 중 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    };

    const handleApprove = (report) => {
        setSelectedReport(report);
        setShowRestrictModal(true);
    };

    const confirmRestrict = async () => {
        if (!selectedReport) return;

        if (!window.confirm(`${selectedReport.reportedName}님을 ${restrictDay}일간 제재하시겠습니까?`)) {
            return;
        }

        try {
            await adminReportApi.approveReport(selectedReport.reportNo, restrictDay);
            alert("신고가 승인되고 사용자가 제재되었습니다.");
            setShowRestrictModal(false);
            setSelectedReport(null);
            fetchData();
        } catch (e) {
            console.error("제재 실패:", e);
            alert("제재 처리 중 오류가 발생했습니다.");
        }
    };

    const handleReject = async (reportNo) => {
        if (!window.confirm("이 신고를 거부하시겠습니까?")) return;

        try {
            await adminReportApi.rejectReport(reportNo);
            alert("신고가 거부되었습니다.");
            fetchData();
        } catch (e) {
            console.error("거부 실패:", e);
            alert("신고 거부 중 오류가 발생했습니다.");
        }
    };

    const getStatusText = (status) => {
        switch (status) {
            case 0: return { text: "미처리", color: "#FF9800" };
            case 1: return { text: "승인", color: "#4CAF50" };
            case 2: return { text: "거부", color: "#f44336" };
            default: return { text: "알 수 없음", color: "#999" };
        }
    };

    if (loading) {
        return <div className="admin-loading"><img src="/img/loading.png" alt="로딩" /></div>;
    }

    return (
        <div className="admin-container">
            <div className="admin-header">
                <h2>신고 관리</h2>
                <button onClick={() => navigate('/admin')} className="admin-btn admin-btn-secondary">
                    관리자 홈
                </button>
            </div>

            {/* 통계 대시보드 */}
            <div className="admin-section">
                <h3>📊 신고 통계</h3>
                <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: "15px" }}>
                    <div className="admin-card">
                        <div className="admin-card-body" style={{ textAlign: "center" }}>
                            <h4 style={{ color: "#2196F3", marginBottom: "10px" }}>전체 신고</h4>
                            <p style={{ fontSize: "32px", fontWeight: "bold", margin: 0 }}>
                                {stats.totalReports || 0}건
                            </p>
                        </div>
                    </div>
                    <div className="admin-card">
                        <div className="admin-card-body" style={{ textAlign: "center" }}>
                            <h4 style={{ color: "#FF9800", marginBottom: "10px" }}>미처리</h4>
                            <p style={{ fontSize: "32px", fontWeight: "bold", margin: 0 }}>
                                {stats.pendingReports || 0}건
                            </p>
                        </div>
                    </div>
                    <div className="admin-card">
                        <div className="admin-card-body" style={{ textAlign: "center" }}>
                            <h4 style={{ color: "#4CAF50", marginBottom: "10px" }}>승인</h4>
                            <p style={{ fontSize: "32px", fontWeight: "bold", margin: 0 }}>
                                {stats.approvedReports || 0}건
                            </p>
                        </div>
                    </div>
                    <div className="admin-card">
                        <div className="admin-card-body" style={{ textAlign: "center" }}>
                            <h4 style={{ color: "#f44336", marginBottom: "10px" }}>거부</h4>
                            <p style={{ fontSize: "32px", fontWeight: "bold", margin: 0 }}>
                                {stats.rejectedReports || 0}건
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            {/* 필터 */}
            <div className="admin-section">
                <h3>🔍 신고 필터</h3>
                <div className="admin-flex admin-flex-gap-md">
                    <button
                        onClick={() => setFilterStatus(0)}
                        className={`admin-btn ${filterStatus === 0 ? 'admin-btn-warning' : 'admin-btn-secondary'}`}
                    >
                        미처리
                    </button>
                    <button
                        onClick={() => setFilterStatus(1)}
                        className={`admin-btn ${filterStatus === 1 ? 'admin-btn-success' : 'admin-btn-secondary'}`}
                    >
                        승인됨
                    </button>
                    <button
                        onClick={() => setFilterStatus(2)}
                        className={`admin-btn ${filterStatus === 2 ? 'admin-btn-danger' : 'admin-btn-secondary'}`}
                    >
                        거부됨
                    </button>
                </div>
            </div>

            {/* 신고 목록 */}
            <div className="admin-section">
                <h3>📋 신고 목록 ({reports.length}건)</h3>
                {reports.length === 0 ? (
                    <p className="admin-empty-message">신고 내역이 없습니다.</p>
                ) : (
                    <div style={{ overflowX: "auto" }}>
                        <table style={{ width: "100%", borderCollapse: "collapse" }}>
                            <thead>
                                <tr style={{ backgroundColor: "#f5f5f5", borderBottom: "2px solid #ddd" }}>
                                    <th style={{ padding: "12px", textAlign: "center" }}>신고번호</th>
                                    <th style={{ padding: "12px", textAlign: "left" }}>신고자</th>
                                    <th style={{ padding: "12px", textAlign: "left" }}>피신고자</th>
                                    <th style={{ padding: "12px", textAlign: "left" }}>신고 사유</th>
                                    <th style={{ padding: "12px", textAlign: "left" }}>메시지 내용</th>
                                    <th style={{ padding: "12px", textAlign: "center" }}>상태</th>
                                    <th style={{ padding: "12px", textAlign: "center" }}>신고 시간</th>
                                    <th style={{ padding: "12px", textAlign: "center" }}>관리</th>
                                </tr>
                            </thead>
                            <tbody>
                                {reports.map((report) => {
                                    const statusInfo = getStatusText(report.reportStatus);
                                    return (
                                        <tr key={report.reportNo} style={{ borderBottom: "1px solid #eee" }}>
                                            <td style={{ padding: "12px", textAlign: "center" }}>
                                                {report.reportNo}
                                            </td>
                                            <td style={{ padding: "12px" }}>
                                                {report.reporterName}<br />
                                                <span style={{ fontSize: "12px", color: "#666" }}>
                                                    ({report.reporterEmail})
                                                </span>
                                            </td>
                                            <td style={{ padding: "12px" }}>
                                                {report.reportedName}<br />
                                                <span style={{ fontSize: "12px", color: "#666" }}>
                                                    ({report.reportedEmail})
                                                </span>
                                            </td>
                                            <td style={{ padding: "12px", fontSize: "13px" }}>
                                                {report.reportReason}
                                            </td>
                                            <td style={{ padding: "12px", fontSize: "13px", maxWidth: "200px" }}>
                                                {report.snapshotMessage?.substring(0, 50)}
                                                {report.snapshotMessage?.length > 50 ? "..." : ""}
                                            </td>
                                            <td style={{ padding: "12px", textAlign: "center" }}>
                                                <span style={{
                                                    padding: "3px 8px",
                                                    backgroundColor: `${statusInfo.color}20`,
                                                    color: statusInfo.color,
                                                    borderRadius: "4px",
                                                    fontWeight: "bold"
                                                }}>
                                                    {statusInfo.text}
                                                </span>
                                            </td>
                                            <td style={{ padding: "12px", textAlign: "center", fontSize: "13px" }}>
                                                {new Date(report.reportTime).toLocaleString()}
                                            </td>
                                            <td style={{ padding: "12px", textAlign: "center" }}>
                                                {report.reportStatus === 0 ? (
                                                    <div style={{ display: "flex", gap: "5px", justifyContent: "center" }}>
                                                        <button
                                                            onClick={() => handleApprove(report)}
                                                            className="admin-btn admin-btn-sm admin-btn-success"
                                                        >
                                                            승인
                                                        </button>
                                                        <button
                                                            onClick={() => handleReject(report.reportNo)}
                                                            className="admin-btn admin-btn-sm admin-btn-danger"
                                                        >
                                                            거부
                                                        </button>
                                                    </div>
                                                ) : (
                                                    <span style={{ color: "#999" }}>처리완료</span>
                                                )}
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* 제재 모달 */}
            {showRestrictModal && selectedReport && (
                <div style={{
                    position: "fixed", top: 0, left: 0, right: 0, bottom: 0,
                    backgroundColor: "rgba(0,0,0,0.5)", display: "flex",
                    justifyContent: "center", alignItems: "center", zIndex: 1000
                }}>
                    <div style={{
                        backgroundColor: "white", padding: "30px",
                        borderRadius: "8px", minWidth: "400px"
                    }}>
                        <h3 style={{ marginBottom: "20px" }}>신고 승인 및 사용자 제재</h3>
                        <div className="admin-detail-box">
                            <p><strong>피신고자:</strong> {selectedReport.reportedName}</p>
                            <p><strong>신고 사유:</strong> {selectedReport.reportReason}</p>
                            <p><strong>메시지:</strong> {selectedReport.snapshotMessage}</p>
                        </div>
                        <div className="admin-form-group" style={{ marginTop: "20px" }}>
                            <label className="admin-form-label">제재 일수</label>
                            <input
                                type="number"
                                value={restrictDay}
                                onChange={(e) => setRestrictDay(parseInt(e.target.value))}
                                className="admin-input"
                                min="1"
                            />
                        </div>
                        <p style={{ fontSize: "14px", color: "#666", marginTop: "10px" }}>
                            * {restrictDay}일간 로그인이 제한됩니다.
                        </p>
                        <div style={{ display: "flex", gap: "10px", justifyContent: "flex-end", marginTop: "20px" }}>
                            <button
                                onClick={() => {
                                    setShowRestrictModal(false);
                                    setSelectedReport(null);
                                }}
                                className="admin-btn admin-btn-secondary"
                            >
                                취소
                            </button>
                            <button
                                onClick={confirmRestrict}
                                className="admin-btn admin-btn-danger"
                            >
                                제재 확정
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}