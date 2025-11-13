import { useState } from "react"
import { createGame } from "../api/adminGameApi";
import "../styles/AdminCommon.css";


export default function AdminGameCreate() {

    // [*] 상태 관리
    const [gameTitle, setGameTitle] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState("");

    // [*] 게임 등록 핸들러
    const handleSubmit = async (e) => {
        e.preventDefault(); // 입력칸 입력 방지

        if (!gameTitle.trim()) {
            setMessage("게임 이름을 입력해주세요.");
            return;
        }

        setIsLoading(true);
        setMessage("");

        try {
            const res = await createGame(gameTitle);
            setMessage(`게임이 성공적으로 등록되었습니다! (게임번호: ${res.gameNo})`);
            setGameTitle("");
        } catch (e) {
            setMessage(e.res?.data || "게임 등록에 실패했습니다.");
        } finally {
            setIsLoading(false);
        }
    };

    return (<>
        <div className="admin-game-register-container">
            <h2 className="admin-game-register-title">게임 등록</h2>
            
            <form onSubmit={handleSubmit}>
                <div className="admin-form-group">
                    <label className="admin-form-label">게임 이름</label>
                    <input
                        type="text"
                        value={gameTitle}
                        onChange={(e) => setGameTitle(e.target.value)}
                        placeholder="예: 토돌이 한글 받기"
                        className="admin-form-input"
                        disabled={isLoading}
                    />
                </div>

                <button
                    type="submit"
                    disabled={isLoading}
                    className={`admin-submit-button ${isLoading ? "disabled" : ""}`}
                >
                    {isLoading ? "등록 중..." : "게임 등록"}
                </button>
            </form>

            {message && (
                <div className={`admin-message-box ${message.includes("성공") ? "success" : "error"}`}>
                    {message}
                </div>
            )}

            <div className="admin-info-box">
                <h4>📌 안내사항</h4>
                <ul>
                    <li>게임 이름을 등록하면 데이터베이스에 저장됩니다.</li>
                    <li>실제 게임 파일은 Flutter assets 폴더에 별도로 추가해야 합니다.</li>
                    <li>등록된 게임은 사용자가 선택하여 플레이할 수 있습니다.</li>
                </ul>
            </div>
        </div>

    </>)


}