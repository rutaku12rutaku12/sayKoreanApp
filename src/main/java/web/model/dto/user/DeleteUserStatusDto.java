package web.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteUserStatusDto {
    private int userNo; // PK: 사용자번호 자동증가
    @NotBlank
    @Pattern(regexp = "^[\\S]{8,}$" , message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password; // 비밀번호(예시 길이)
}
