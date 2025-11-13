import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import { deleteGameLog, getGameList } from "../api/adminGameApi";


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
        if(!window.confirm("이 게임 기록을 삭제하시겠습니까?")) {
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
    const 

    // [4] 게임 기록 결과 패턴


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

        </div>

    </>);
}
