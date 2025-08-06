package com.example.message_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PendingFriendRequestResponse {
    private String senderId;
    private String senderDisplayName;
    private String senderAvatarUrl;

    private String receiverId;
    private String receiverDisplayName;
    private String receiverAvatarUrl;

    private LocalDateTime requestedAt;
}
