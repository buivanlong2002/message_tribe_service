package com.example.message_service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostCommentResponse {
    private Long id;
    private SenderResponse user;
    private String content;
    private Long parentCommentId;
    private LocalDateTime createdAt;
    private List<PostCommentResponse> replies;
} 