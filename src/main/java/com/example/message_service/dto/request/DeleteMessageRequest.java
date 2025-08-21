package com.example.message_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteMessageRequest {
    private String messageId;
    private String userId;
    private String conversationId;
}
