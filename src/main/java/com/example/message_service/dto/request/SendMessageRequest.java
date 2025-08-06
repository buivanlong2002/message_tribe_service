package com.example.message_service.dto.request;

import com.example.message_service.model.MessageType;
import lombok.*;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {
        private String senderId;
        private String conversationId;
        private String receiverId;
        private String content;
        private MessageType messageType;
        private String replyToId;

        // Getters and setters


}
