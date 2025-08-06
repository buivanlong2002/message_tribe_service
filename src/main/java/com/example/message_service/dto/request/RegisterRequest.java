package com.example.message_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {


    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, max = 100, message = "Password phải từ 6 đến 100 ký tự")
    private String password;

    @NotBlank(message = "Display name không được để trống")
    private String displayName;

    private String avatarUrl;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @Email(message = "Email không hợp lệ")
    private String email;
}