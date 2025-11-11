package web.model.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.model.entity.game.GameEntity;

@Data @Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameDto {
    // 1. 테이블 설계
    private int gameNo;         // 게임번호 (PK)
    private String gameTitle;   // 게임명

    // 2. Dto -> Entity 변환 : C
    public GameEntity toEntity () {
        return  GameEntity.builder()
                .gameNo(gameNo)
                .gameTitle(gameTitle)
                .build();
    }

}
