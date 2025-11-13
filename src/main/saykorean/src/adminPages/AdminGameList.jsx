import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import { deleteGame, deleteGameLog, getGameList, getGameLogList } from "../api/adminGameApi";
import "../styles/AdminCommon.css";

export default function AdminGameList() {

    // [*] 가상DOM , 리덕스
    const navigate = useNavigate();
    const dispatch = useDispatch();

    // [*] 상태관리
    const [games, setGames] = useState([]);
    const [gameLogs, setGameLogs] = useState([]);
    const [selectedGameNo, setSelectedGameNo] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [activeTab, setActiveTab] = useState("games"); // "games" | "logs" 탭 전환

    // [*] 렌더링
    useEffect(() => {
        fetchGames();
        fetchGameLog();
    }, []);

    // [1-1] 게임 목록 가져오기
    const fetchGames = async () => {
        try {
            const data = await getGameList();
            setGames(data);
        } catch (e) {
            setMessage("게임 목록 조회 실패");
        }
    };

    // [1-2] 게임 기록 목록 가져오기 
    const fetchGameLog = async () => {
        try {
            const data = await getGameLogList();
            setGameLogs(data);
        } catch (e) {
            setMessage("게임 기록 조회 실패");
        }
    }

    // [2-1] 게임 삭제
    const handleDeleteGame = async (gameNo, gameTitle) => {
        if (!window.confirm(`"${gameTitle}" 게임을 삭제하시겠습니까?`)) {
            return;
        }

        setIsLoading(true);
        try {
            await deleteGame(gameNo);
            setMessage("게임이 삭제되었습니다.");
            fetchGames();
        } catch (e) {
            setMessage(e.res?.data || "게임 삭제 실패");
        } finally {
            setIsLoading(false);
        }
    };

    // [2-2] 게임 기록 삭제
    const handleDeleteGameLog = async (gameLogNo) => {
        if (!window.confirm("이 게임 기록을 삭제하시겠습니까?")) {
            return;
        }

        setIsLoading(true);
        try {
            await deleteGameLog(gameLogNo);
            setMessage("게임 기록이 삭제되었습니다.");
            fetchGameLog();
        } catch (e) {
            setMessage(e.res?.data || "게임 기록 삭제 실패");
        } finally {
            setIsLoading(false);
        }
    };

    // [2-3] 사용자별 모든 게임 기록 삭제
    const handleDeleteUserLogs = async (userNo) => {
        if (!window.confirm(`사용자 ${userNo}의 모든 게임 기록을 삭제하시겠습니까?`))
            return;

        setIsLoading(true);
        try {
            await deleteGameLog(null, userNo);
            setMessage("사용자의 모든 게임 기록이 삭제되었습니다.");
            fetchGameLog();
        } catch (e) {
            setMessage(e.res?.data || "게임 기록 삭제 실패");
        } finally {
            setIsLoading(false);
        }
    };

    // [3] 게임 기록 없을 때 쓰는 필터
    const filteredLogs = selectedGameNo
        ? gameLogs.filter(log => log.gameNo == selectedGameNo)
        : gameLogs;

    // [4] 게임 기록 결과 패턴
    const getGameResultText = (result) => {
        switch (result) {
            case 2: return "매우 성공 🏆";
            case 1: return "성공 ✅";
            case 0: return "실패 ❌";
            default: return "알 수 없음";
        }
    };


    return (<>
        <div className="admin-container">
            <div className="admin-header">
                <h2>게임 관리</h2>
                <button
                    onClick={() => navigate('/admin/game/create')}
                    className="admin-btn admin-btn-success"
                >
                    새 게임 등록
                </button>
            </div>
            {message && (
                <div className={`admin-message-box ${message.includes("삭제") ? "success" : "error"}`}>
                    {message}
                </div>
            )}

            <div className="admin-tab-container">
                <button
                    className={`admin-tab-btn ${activeTab === "games" ? "active" : ""}`}
                    onClick={() => setActiveTab("games")}
                >
                    게임 목록
                </button>
                <button
                    className={`admin-tab-btn ${activeTab === "logs" ? "active" : ""}`}
                    onClick={() => setActiveTab("logs")}
                >
                    게임 기록
                </button>
            </div>

            {activeTab === "games" && (
                <div className="admin-section">
                    <h3>등록된 게임 ({games.length}개)</h3>
                    {games.length === 0 ? (
                        <p className="admin-empty-message">등록된 게임이 없습니다.</p>
                    ) : (
                        <table className="admin-table">
                            <thead>
                                <tr>
                                    <th>게임번호</th>
                                    <th>게임명</th>
                                    <th>관리</th>
                                </tr>
                            </thead>
                            <tbody>
                                {games.map((game) => (
                                    <tr key={game.gameNo}>
                                        <td>{game.gameNo}</td>
                                        <td>{game.gameTitle}</td>
                                        <td>
                                            <button
                                                className="admin-btn admin-btn-danger"
                                                onClick={() => handleDeleteGame(game.gameNo, game.gameTitle)}
                                                disabled={isLoading}
                                            >
                                                삭제
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            )}

            {activeTab === "logs" && (
                <div className="admin-section">
                    <div className="admin-filter-container">
                        <h3>게임별 필터</h3>
                        <button
                            className={`admin-filter-btn ${selectedGameNo === null ? "active" : ""}`}
                            onClick={() => setSelectedGameNo(null)}
                        >
                            전체
                        </button>
                        {games.map((game) => (
                            <button
                                key={game.gameNo}
                                className={`admin-filter-btn ${selectedGameNo === game.gameNo ? "active" : ""}`}
                                onClick={() => setSelectedGameNo(game.gameNo)}
                            >
                                {game.gameTitle}
                            </button>
                        ))}
                    </div>

                    <h3>게임 기록 ({filteredLogs.length}개)</h3>
                    {filteredLogs.length === 0 ? (
                        <p className="admin-empty-message">게임 기록이 없습니다.</p>
                    ) : (
                        <table className="admin-table">
                            <thead>
                                <tr>
                                    <th>기록번호</th>
                                    <th>이메일</th>
                                    <th>게임명</th>
                                    <th>결과</th>
                                    <th>점수</th>
                                    <th>완료시간</th>
                                    <th>관리</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredLogs.map((log) => (
                                    <tr key={log.gameLogNo}>
                                        <td>{log.gameLogNo}</td>
                                        <td>{log.email}</td>
                                        <td>{log.gameTitle}</td>
                                        <td>{getGameResultText(log.gameResult)}</td>
                                        <td>{log.gameScore}점</td>
                                        <td>{new Date(log.gameFinishedAt).toLocaleString('ko-KR')}</td>
                                        <td>
                                            <button
                                                className="admin-btn admin-btn-danger"
                                                onClick={() => handleDeleteGameLog(log.gameLogNo)}
                                                disabled={isLoading}
                                            >
                                                삭제
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            )}
        </div>

    </>);
}
