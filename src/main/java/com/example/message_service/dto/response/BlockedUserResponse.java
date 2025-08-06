package com.example.message_service.dto.response;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BlockedUserResponse {
    private String id;
    private String displayName;
    private String avatarUrl;
}
