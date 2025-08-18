package com.example.message_service.dto.response;

import com.example.message_service.model.NeoPostReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReactionResponse {
    private String id;
    private UserResponse user;
    private NeoPostReactionType type;
    private LocalDateTime createdAt;
}
