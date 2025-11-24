package web.model.dto.store;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserThemeDto {

    private int userThemeNo; // 로그번호
    private int userNo; // 유저번호
    private int theme_id; // 테마번호
    private String owned_at; // 구매시간
}
