package com.example.message_service.dto.request;

import lombok.Data;

@Data
public class ResetPasswordWithOTPRequest {
    private String email;
    private String otp;
    private String newPassword;
} 