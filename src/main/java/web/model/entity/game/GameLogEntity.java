package web.model.entity.game;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.model.dto.game.GameLogDto;

@Entity
@Table( name = "gamelog" )
@Data @Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameLogEntity {
    // 1. 테이블 설계
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private int gameLogNo;          // 게임기록번호
    private int gameResult;         // 게임결과 2 : 매우성공 (무한모드 진입) , 1 : 성공 , 0 : 실패
    private int gameScore;          // 게임점수
    @Builder.Default    // 빌더 초기값 활용
    private LocalDateTime gameFinishedAt = LocalDateTime.now();  // 게임완료시간
    private int userNo;             // 사용자번호(FK)
//    private int gameNo;             // 게임번호(FK) 외래키값은 과감하게 삭제

    // 2. 단방향연결
    // 하위 엔티티가 상위 엔티티참조 관계
    @ManyToOne( fetch = FetchType.EAGER )   // 캐스케이드 빼서 삭제 전파 방지
    @JoinColumn( name = "gameNo" ) // FK 필드명 (PK 필드명과 동일하게)
    private GameEntity gameEntity;

    // 3. Entity -> Dto
    public GameLogDto toDto () {
        return GameLogDto.builder()
                .gameLogNo( this.gameLogNo )
                .gameResult( this.gameResult )
                .gameScore( this.gameScore )
                .gameFinishedAt( this.gameFinishedAt )
                .userNo( this.userNo )
                .gameNo( this.gameEntity.getGameNo() )
                .build();
    }
}


// 게임 <--- 게임기록