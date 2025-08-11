package com.example.message_service.dto.request;

import lombok.Data;

@Data
public class VerifyOTPRequest {
    private String email;
    private String otp;
} 