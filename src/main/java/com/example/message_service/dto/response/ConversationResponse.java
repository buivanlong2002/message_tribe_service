package com.example.message_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponse {
    private String id;

    private String name;

    @JsonProperty("isGroup")
    @Column(name = "is_group", nullable = false)
    private boolean group;

    private LocalDateTime createdAt;

    private LastMessageInfo lastMessage;

    private String avatarUrl;

    @JsonProperty("isBlocked")
    private boolean isBlocked;

    @JsonProperty("blockedByMe")
    private boolean blockedByMe;

    @JsonProperty("blockedMe")
    private boolean blockedMe;

    private String createdBy;

    private List<MemberResponse> members;

    public ConversationResponse(String id, String name, boolean isGroup, String avatarUrl,
                                LocalDateTime createdAt, LastMessageInfo lastMessage, String createdBy) {
        this.id = id;
        this.name = name;
        this.group = isGroup;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.lastMessage = lastMessage;
        this.createdBy = createdBy;
    }
}

