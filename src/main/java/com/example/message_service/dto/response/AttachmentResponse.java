package com.example.message_service.dto.response;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentResponse {
    private String id;
    private String originalFileName;
    private String url;
    private String fileType;
}
