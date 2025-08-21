package com.example.message_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForwardMessageRequest {
    private String messageId;
    private String senderId;
    private List<String> targetConversationIds;
}
