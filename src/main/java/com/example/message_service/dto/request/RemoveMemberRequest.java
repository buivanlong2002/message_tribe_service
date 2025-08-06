package com.example.message_service.dto.request;

import lombok.*;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RemoveMemberRequest {
    private UUID conversationId;
    private UUID userId;

}

