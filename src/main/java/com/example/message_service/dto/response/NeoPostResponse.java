package com.example.message_service.dto.response;

import com.example.message_service.model.NeoPostVisibility;
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
public class NeoPostResponse {
    private Long id;
    private UserResponse user;
    private String content;
    private NeoPostVisibility visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private List<CommentResponse> comments;
    private List<ReactionResponse> reactions;
    private List<String> mediaUrls;
    private Integer commentCount;
    private Integer reactionCount;
}
