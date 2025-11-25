import axios from "axios";

const BASE_URL = "http://localhost:8080/saykorean/admin/report";

const api = axios.create({
    baseURL: BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
});

export const adminReportApi = {
    // AR-01: 신고 목록 조회
    getReportList: (status = 0) => api.get(`?status=${status}`),

    // AR-02: 신고 상세 조회
    getReportDetail: (reportNo) => api.get(`/${reportNo}`),

    // AR-03: 신고 승인 및 제재
    approveReport: (reportNo, restrictDay) => 
        api.post(`/${reportNo}/approve`, null, {
            params: { restrictDay }
        }),

    // AR-04: 신고 거부
    rejectReport: (reportNo) => api.post(`/${reportNo}/reject`),

    // AR-05: 신고 통계
    getReportStats: () => api.get("/stats"),
};

export default api;