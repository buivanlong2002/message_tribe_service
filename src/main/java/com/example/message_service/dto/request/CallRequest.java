package com.example.message_service.dto.request;

import lombok.Data;

@Data
public class CallRequest {
    private String type; // "offer", "answer", "ice-candidate", "call-request", "call-accept", "call-reject", "call-end"
    private String fromUserId;
    private String toUserId;
    private String conversationId;
    private String callType; // "audio" or "video"
    private Object data; // SDP offer/answer or ICE candidate
    private String sessionId;
    
    // Thông tin người gọi
    private String callerName;
    private String callerAvatar;
    private String receiverName;
    private String receiverAvatar;
}

