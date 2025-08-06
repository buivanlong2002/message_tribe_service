package com.example.message_service.dto.request;

import com.example.message_service.model.ReactionType;
import lombok.Data;

@Data
public class CreatePostReactionRequest {
    private ReactionType reactionType;
} 