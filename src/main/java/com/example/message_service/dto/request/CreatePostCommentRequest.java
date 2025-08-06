package com.example.message_service.dto.request;

import lombok.Data;

@Data
public class CreatePostCommentRequest {
    private String content;
    private Long parentCommentId; // null nếu là comment gốc
} 