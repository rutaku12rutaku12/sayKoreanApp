package web.model.dto.game;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.model.entity.game.GameEntity;
import web.model.entity.game.GameLogEntity;

@Data @Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameLogDto {
    private int gameLogNo;          // 게임기록번호
    private int gameResult;         // 게임결과 2 : 매우성공 (무한모드 진입) , 1 : 성공 , 0 : 실패
    private int gameScore;          // 게임점수
    private LocalDateTime gameFinishedAt;  // 게임완료시간
    private int userNo;             // 사용자번호(PK)
    private int gameNo;             // 게임번호(PK)

    // Dto -> Entity 변환 : C
    public GameLogEntity toEntity() {
        // ⭐ gameNo를 GameEntity로 변환
        GameEntity gameEntity = GameEntity.builder()
                .gameNo(this.gameNo)
                .build();

        return GameLogEntity.builder()
                .gameLogNo( this.gameLogNo )
                .gameResult( this.gameResult )
                .gameScore( this.gameScore )    // 시간은 엔티티에서 빌더 디폴트로 처리
                .userNo( this.userNo )
                .gameEntity( gameEntity )
                .build();
    }

}
