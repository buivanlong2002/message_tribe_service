package com.example.message_service.dto.response;

import com.example.message_service.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserResponse {
    private String id;
    private String email;
    private String displayName;
    private String phoneNumber;
    private String birthday;
    private String avatarUrl;
    private String status;
    private UserRole role;
    private Boolean isBlocked;
    private LocalDateTime lastLoginAt;
    private Integer loginCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
