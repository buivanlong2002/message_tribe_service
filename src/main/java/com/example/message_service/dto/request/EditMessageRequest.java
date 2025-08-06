package com.example.message_service.dto.request;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EditMessageRequest {
    private String messageId;
    private String newContent;
    private String conversationId;
}
