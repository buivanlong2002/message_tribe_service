package com.example.message_service.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponse {
    private String id;
    private String displayName;
    private String avatarUrl;
}
