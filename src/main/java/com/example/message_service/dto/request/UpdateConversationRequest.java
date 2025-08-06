package com.example.message_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateConversationRequest {
    private String name;
    @JsonProperty("isGroup")
    @Column(name = "is_group", nullable = false)
    private boolean group;
}

