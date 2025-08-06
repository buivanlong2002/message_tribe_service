package com.example.message_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeenByResponse {
    private String userId;
    private String displayName;
    private String avatarUrl;
    private LocalDateTime seenAt;
}
