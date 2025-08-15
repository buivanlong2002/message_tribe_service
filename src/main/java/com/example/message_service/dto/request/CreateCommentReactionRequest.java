package com.example.message_service.dto.request;

import com.example.message_service.model.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentReactionRequest {
    private ReactionType reactionType = ReactionType.LIKE;
}
