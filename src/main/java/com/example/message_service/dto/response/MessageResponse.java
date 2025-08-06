package com.example.message_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String id;
    private String conversationId;
    private SenderResponse sender;
    private String content;
    private String messageType;
    private LocalDateTime createdAt;

    private String replyToId; // chỉ giữ cái này
    private boolean edited;
    private boolean seen;
    private boolean recalled;
    private List<AttachmentResponse> attachments;
    private String timeAgo;
    private List<SeenByResponse> seenBy;
}
