package com.example.message_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyResponse {
    private String id;
    private UserResponse user;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
