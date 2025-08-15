package com.example.message_service.dto.response;

import com.example.message_service.model.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentReactionResponse {
    private Long id;
    private Long commentId;
    private String userId;
    private String userName;
    private ReactionType reactionType;
    private LocalDateTime createdAt;
    private boolean reacted;
}
