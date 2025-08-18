package com.example.message_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private String id;
    private UserResponse user;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReplyResponse> replies;
    private Integer replyCount;
}
