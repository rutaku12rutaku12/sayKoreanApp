import axios from "axios";

// 기본 주소값 설정
const BASE_URL = "http://localhost:8080/saykorean/admin";

// [*] 게임 관리 API

// [AGL-04] 게임 생성
export const createGame = async (gameTitle) => {
    try {
        const res = await axios.post(`${BASE_URL}/game`, {
            gameTitle
        });
        return res.data;
    } catch (e) {
        console.error("게임 생성 실패:", e);
        throw e;
    }
};

// [AGL-05] 게임 전체 조회
export const getGameList = async () => {
    try {
        const res = await axios.get(`${BASE_URL}/game`);
        return res.data;
    } catch (e) {
        console.error("게임 목록 조회 실패:", e);
        throw e;
    }
};

// [AGL-06] 게임 상세 조회
export const getGameDetail = async (gameNo) => {
    try {
        const res = await axios.get(`${BASE_URL}/game/detail`, {
            params: { gameNo }
        });
        return res.data;
    } catch (e) {
        console.error("게임 상세 조회 실패:", e);
        throw e;
    }
};

// [AGL-07] 게임 삭제
export const deleteGame = async (gameNo) => {
    try {
        const res = await axios.delete(`${BASE_URL}/game`, {
            params: { gameNo }
        });
        return res.data;
    } catch (e) {
        console.error("게임 삭제 실패:", e)
        throw e;
    }
};

// [*] 게임 기록 관리 API


// [AGL-01] 게임 기록 삭제 (특정 기록 또는 사용자의 모든 기록 삭제)
export const deleteGameLog = async (gameLogNo = null, userNo = null) => { // 스프링의 require = null
    try {
        const params = {};
        if (gameLogNo != null) params.gameLogNo = gameLogNo;
        if (userNo != null) params.userNo = userNo;

        const res = await axios.delete(`${BASE_URL}/gamelog`, { params });
        return res.data;
    } catch (e) {
        console.error("게임 기록 삭제 실패:", e);
        throw e;
    }
};

// [AGL-02] 게임 기록 전체 조회
export const getGameLogList = async () => {
    try {
        const res = await axios.get(`${BASE_URL}/gamelog/detail`, {
            params: { gameLogNo }
        });
        return res.data;
    } catch (e) {
        console.error("게임 기록 조회 실패:", e);
        throw e;
    }
}

// [AGL-03] 게임 기록 상세 조회
export const getGameLogDetail = async (gameLogNo) => {
    try {
        const res = await axios.get(`${BASE_URL}/gamelog/detail`, {
            params: { gameLogNo }
        })
        return res.data;
    } catch (e) {
        console.error("게임 기록 상세 조회 실패:", e);
        throw e;
    }
}



