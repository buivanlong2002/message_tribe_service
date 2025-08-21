package com.example.message_service.dto.request;

import lombok.*;

import java.util.UUID;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddMemberRequest {
    private UUID conversationId;
    private UUID userId;
    private UUID requesterId; // ID của người thực hiện thao tác
}
