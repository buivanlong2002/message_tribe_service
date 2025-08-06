package com.example.message_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 8 ký tự")
    @Schema(description = "Password", example = "123456@Aa")
    private String password;
}
