package web.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor
@NoArgsConstructor @Builder
public class LoginDto {
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank
    private String email;
    @NotBlank
    @Pattern(regexp = "^[\\S]{8,}$" , message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;
    private int userNo;
    private int userState =1;
}