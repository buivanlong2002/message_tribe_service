package com.example.message_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "conversation_id", nullable = false)
    private String conversationId;
    
    @Column(name = "caller_id", nullable = false)
    private String callerId;
    
    @Column(name = "receiver_id", nullable = false)
    private String receiverId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", nullable = false)
    private CallType callType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "call_status", nullable = false)
    private CallStatus callStatus;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "duration_seconds")
    private Long durationSeconds;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "missed_reason")
    private String missedReason; // "timeout", "rejected", "busy", etc.
    
    // Thông tin người gọi và người nhận
    @Column(name = "caller_name")
    private String callerName;
    
    @Column(name = "caller_avatar")
    private String callerAvatar;
    
    @Column(name = "receiver_name")
    private String receiverName;
    
    @Column(name = "receiver_avatar")
    private String receiverAvatar;
    
    public enum CallType {
        AUDIO, VIDEO
    }
    
    public enum CallStatus {
        MISSED,      // Cuộc gọi nhỡ
        REJECTED,    // Cuộc gọi bị từ chối
        COMPLETED,   // Cuộc gọi hoàn thành
        TIMEOUT,     // Cuộc gọi timeout
        BUSY         // Cuộc gọi bận
    }
    
    // Tính thời gian gọi
    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
        }
    }
    
    // Kiểm tra có phải cuộc gọi nhỡ không
    public boolean isMissed() {
        return callStatus == CallStatus.MISSED || 
               callStatus == CallStatus.TIMEOUT || 
               callStatus == CallStatus.REJECTED;
    }
}
