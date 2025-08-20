package com.example.message_service.dto.request;

import lombok.Data;
import com.example.message_service.model.CallHistory;

@Data
public class CallMessageRequest {
    private String conversationId;
    private String senderId;
    private String receiverId;
    private String callType; // "audio" hoặc "video"
    private String callStatus; // "missed", "completed", "rejected", "timeout"
    private Long durationSeconds; // Thời gian gọi (nếu hoàn thành)
    private String sessionId; // Session ID của cuộc gọi
    private String missedReason; // Lý do nhỡ cuộc gọi
}
