package com.example.message_service.dto.request;

import lombok.Data;

@Data
public class ResetPasswordAfterOTPRequest {
    private String email;
    private String newPassword;
}

