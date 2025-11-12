package web.model.entity.game;

import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import web.model.dto.game.GameDto;

@Entity
@Table( name = "game" )
@Data @Builder
@NoArgsConstructor
@AllArgsConstructor
@Access(AccessType.FIELD) // 생략해도됨.
public class GameEntity {

    // 1. 테이블 설계
    @Id // PK
    @GeneratedValue( strategy = GenerationType.IDENTITY ) // auto_increment
    private int gameNo;                     // 게임번호 (PK)
    @Column( nullable = false, length = 50 )
    private String gameTitle;               // 게임명

    // 2. 양방향 연결
    // 상위 엔티티가 하위 엔티티 참조관계
    @OneToMany( mappedBy = "gameEntity" , fetch = FetchType.EAGER ) // 1:M , 하나의 PK가 다수 FK에게
    // JPA 양방향 사용. DB에서는 양방향 사용하지 않는다.
    @ToString.Exclude   // 순환 참조 방지
    @Builder.Default    // 빌더 패턴 사용시 초기값 사용
    private List<GameLogEntity>
            gameLogEntityList = new ArrayList<>();

    // 3. Entity -> Dto 변환 : R
    public GameDto toDto () {
        return GameDto.builder()
                .gameNo( this.gameNo )
                .gameTitle( this.gameTitle )
                .build();
    }

}

// 게임 --> 게임기록
