package com.example.message_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LastMessageInfo {
    private String lastMessageContent;
    private String lastMessageSenderName;
    private String lastMessageTimeAgo;
    private String lastMessageStatus ;
    private LocalDateTime createdAt;
    private boolean seen;
}
