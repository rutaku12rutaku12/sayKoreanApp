package web.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePwrdDto {
    private int userNo; // PK: 사용자번호 자동증가
    @NotBlank
    private String currentPassword; // 현재 비밀번호 ( 입력값 해시값x 평문o)
    @NotBlank
    private String newPassword; // 새로운 비밀번호 ( 입력값 해시값x 평문o)
    private String password; // 실제 비밀번호 ( 입력값들을 해시값으로 변환해서 db에 저장되는 해시값o 평문x)
}
