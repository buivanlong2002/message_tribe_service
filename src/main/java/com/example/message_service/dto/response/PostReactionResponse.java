package com.example.message_service.dto.response;

import com.example.message_service.model.ReactionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostReactionResponse {
    private Long id;
    private SenderResponse user;
    private ReactionType reactionType;
    private LocalDateTime createdAt;
} 