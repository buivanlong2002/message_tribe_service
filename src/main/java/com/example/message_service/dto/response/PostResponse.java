package com.example.message_service.dto.response;

import com.example.message_service.model.Visibility;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponse {
    private Long id;
    private SenderResponse user;
    private String content;
    private String mediaUrl;
    private Visibility visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int reactionCount;
    private int commentCount;
    private List<PostReactionResponse> reactions;
    private List<PostCommentResponse> comments;
} 