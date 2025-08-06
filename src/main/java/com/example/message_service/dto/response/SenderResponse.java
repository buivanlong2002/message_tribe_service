package com.example.message_service.dto.response;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SenderResponse {
    private String senderId;
    private String nameSender ;
    private String avatarSender;
}
