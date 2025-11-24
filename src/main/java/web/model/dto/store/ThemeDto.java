package web.model.dto.store;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThemeDto {

    private int theme_id; // 테마번호
    private String theme_name; // 테마이름
    private int price; // 가격
}
