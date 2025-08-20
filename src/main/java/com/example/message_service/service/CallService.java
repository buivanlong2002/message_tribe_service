package com.example.message_service.service;

import com.example.message_service.dto.request.CallRequest;
import com.example.message_service.dto.request.CallMessageRequest;
import com.example.message_service.model.CallHistory;
import com.example.message_service.model.Message;
import com.example.message_service.model.MessageType;
import com.example.message_service.model.User;
import com.example.message_service.model.Conversation;
import com.example.message_service.repository.MessageRepository;
import com.example.message_service.repository.UserRepository;
import com.example.message_service.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallService {

    private final SimpMessagingTemplate messagingTemplate;
    private final CallHistoryService callHistoryService;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    
    // Store active call sessions: sessionId -> CallSession
    private final Map<String, CallSession> activeSessions = new ConcurrentHashMap<>();
    
    // Store user's current session: userId -> sessionId
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    public void handleCallRequest(CallRequest request) {
        log.info("=== DEBUG YÊU CẦU GỌI ===");
        log.info("Nhận yêu cầu gọi: {}", request);
        log.info("Từ user: {}", request.getFromUserId());
        log.info("Đến user: {}", request.getToUserId());
        log.info("Loại gọi: {}", request.getCallType());
        log.info("Conversation ID: {}", request.getConversationId());
        
        String sessionId = generateSessionId();
        request.setSessionId(sessionId);
        
        // Tạo phiên gọi mới
        CallSession session = new CallSession(
            sessionId,
            request.getFromUserId(),
            request.getToUserId(),
            request.getConversationId(),
            request.getCallType()
        );
        
        activeSessions.put(sessionId, session);
        userSessions.put(request.getFromUserId(), sessionId);
        
        log.info("Phiên gọi đã tạo: {}", sessionId);
        log.info("Active sessions count: {}", activeSessions.size());
        log.info("User sessions: {}", userSessions);
        
        // Lưu lịch sử cuộc gọi
        try {
            CallHistory.CallType callType = "video".equals(request.getCallType()) ? 
                CallHistory.CallType.VIDEO : CallHistory.CallType.AUDIO;
            
            callHistoryService.createCall(
                request.getConversationId(),
                request.getFromUserId(),
                request.getToUserId(),
                callType,
                sessionId,
                request.getCallerName(),
                request.getCallerAvatar(),
                request.getReceiverName(),
                request.getReceiverAvatar()
            );
            log.info("✅ Đã lưu lịch sử cuộc gọi cho session: {} với thông tin người gọi", sessionId);
        } catch (Exception e) {
            log.error("❌ Lỗi khi lưu lịch sử cuộc gọi: {}", e.getMessage(), e);
        }
        
        log.info("Gửi yêu cầu gọi đến user: {} qua queue: /user/{}/queue/call-request", 
            request.getToUserId(), request.getToUserId());
        
        try {
            // Gửi yêu cầu gọi đến người nhận
            messagingTemplate.convertAndSendToUser(
                request.getToUserId(),
                "/queue/call-request",
                request
            );
            
            log.info("✅ Yêu cầu gọi đã gửi thành công từ {} đến {} cho phiên {}", 
                request.getFromUserId(), request.getToUserId(), sessionId);
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi yêu cầu gọi: {}", e.getMessage(), e);
        }
        
        log.info("=== KẾT THÚC DEBUG YÊU CẦU GỌI ===");
    }

    public void handleCallAccept(CallRequest request) {
        String sessionId = request.getSessionId();
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("Yêu cầu chấp nhận gọi với sessionId null hoặc rỗng từ user: {}", request.getFromUserId());
            return;
        }
        
        CallSession session = activeSessions.get(sessionId);
        
        if (session != null) {
            session.setAccepted(true);
            userSessions.put(request.getFromUserId(), sessionId);
            
            // Cập nhật trạng thái cuộc gọi thành COMPLETED
            callHistoryService.updateCallStatus(sessionId, CallHistory.CallStatus.COMPLETED, "accepted");
            
            // Tạo call message cho cuộc gọi được accept
            createCallMessage(
                session.getConversationId(),
                session.getFromUserId(),
                session.getToUserId(),
                session.getCallType(),
                "COMPLETED",
                null, // Duration sẽ được cập nhật khi call end
                sessionId,
                "accepted"
            );
            
            // Thông báo cho người gọi rằng cuộc gọi đã được chấp nhận
            messagingTemplate.convertAndSendToUser(
                session.getFromUserId(),
                "/queue/call-accepted",
                request
            );
            
            log.info("Cuộc gọi đã được chấp nhận cho phiên {}", sessionId);
        } else {
            log.warn("Yêu cầu chấp nhận gọi cho phiên không tồn tại: {}", sessionId);
        }
    }

    public void handleCallReject(CallRequest request) {
        String sessionId = request.getSessionId();
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("Yêu cầu từ chối gọi với sessionId null hoặc rỗng từ user: {}", request.getFromUserId());
            return;
        }
        
        CallSession session = activeSessions.get(sessionId);
        
        if (session != null) {
            // Cập nhật trạng thái cuộc gọi thành REJECTED
            callHistoryService.updateCallStatus(sessionId, CallHistory.CallStatus.REJECTED, "rejected");
            
            // Tạo call message cho cuộc gọi bị reject
            createCallMessage(
                session.getConversationId(),
                session.getFromUserId(),
                session.getToUserId(),
                session.getCallType(),
                "REJECTED",
                0L,
                sessionId,
                "rejected"
            );
            
            // Thông báo cho người gọi rằng cuộc gọi đã bị từ chối
            messagingTemplate.convertAndSendToUser(
                session.getFromUserId(),
                "/queue/call-rejected",
                request
            );
            
            // Dọn dẹp phiên
            cleanupSession(sessionId);
            
            log.info("Cuộc gọi đã bị từ chối cho phiên {}", sessionId);
        } else {
            log.warn("Yêu cầu từ chối gọi cho phiên không tồn tại: {}", sessionId);
        }
    }

    public void handleCallEnd(CallRequest request) {
        String sessionId = request.getSessionId();
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("Yêu cầu kết thúc gọi với sessionId null hoặc rỗng từ user: {}", request.getFromUserId());
            return;
        }
        
        CallSession session = activeSessions.get(sessionId);
        
        if (session != null) {
            // Cập nhật trạng thái cuộc gọi thành COMPLETED (nếu đã accept) hoặc TIMEOUT (nếu chưa accept)
            CallHistory.CallStatus status = session.isAccepted() ? 
                CallHistory.CallStatus.COMPLETED : CallHistory.CallStatus.TIMEOUT;
            String reason = session.isAccepted() ? "ended" : "timeout";
            
            // Tính thời gian gọi nếu đã accept
            Long durationSeconds = null;
            if (session.isAccepted()) {
                long endTime = System.currentTimeMillis();
                durationSeconds = (endTime - session.getStartTime()) / 1000;
            }
            
            callHistoryService.updateCallStatus(sessionId, status, reason);
            
            // Tạo call message cho cuộc gọi kết thúc
            createCallMessage(
                session.getConversationId(),
                session.getFromUserId(),
                session.getToUserId(),
                session.getCallType(),
                status.name(),
                durationSeconds,
                sessionId,
                reason
            );
            
            // Thông báo cho người tham gia khác
            String otherUserId = request.getFromUserId().equals(session.getFromUserId()) 
                ? session.getToUserId() 
                : session.getFromUserId();
            
            messagingTemplate.convertAndSendToUser(
                otherUserId,
                "/queue/call-ended",
                request
            );
            
            // Dọn dẹp phiên
            cleanupSession(sessionId);
            
            log.info("Cuộc gọi đã kết thúc cho phiên {}", sessionId);
        } else {
            log.warn("Yêu cầu kết thúc gọi cho phiên không tồn tại: {}", sessionId);
        }
    }

    public void handleOffer(CallRequest request) {
        String sessionId = request.getSessionId();
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("WebRTC offer với sessionId null hoặc rỗng từ user: {}", request.getFromUserId());
            return;
        }
        
        CallSession session = activeSessions.get(sessionId);
        
        if (session != null) {
            // Chuyển tiếp SDP offer đến người tham gia khác
            String targetUserId = request.getFromUserId().equals(session.getFromUserId()) 
                ? session.getToUserId() 
                : session.getFromUserId();
            
            messagingTemplate.convertAndSendToUser(
                targetUserId,
                "/queue/webrtc-offer",
                request
            );
            
            log.info("SDP offer đã được chuyển tiếp cho phiên {}", sessionId);
        } else {
            log.warn("WebRTC offer cho phiên không tồn tại: {}", sessionId);
        }
    }

    public void handleAnswer(CallRequest request) {
        String sessionId = request.getSessionId();
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("WebRTC answer với sessionId null hoặc rỗng từ user: {}", request.getFromUserId());
            return;
        }
        
        CallSession session = activeSessions.get(sessionId);
        
        if (session != null) {
            // Chuyển tiếp SDP answer đến người tham gia khác
            String targetUserId = request.getFromUserId().equals(session.getFromUserId()) 
                ? session.getToUserId() 
                : session.getFromUserId();
            
            messagingTemplate.convertAndSendToUser(
                targetUserId,
                "/queue/webrtc-answer",
                request
            );
            
            log.info("SDP answer đã được chuyển tiếp cho phiên {}", sessionId);
        } else {
            log.warn("WebRTC answer cho phiên không tồn tại: {}", sessionId);
        }
    }

    public void handleIceCandidate(CallRequest request) {
        String sessionId = request.getSessionId();
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("ICE candidate với sessionId null hoặc rỗng từ user: {}", request.getFromUserId());
            return;
        }
        
        CallSession session = activeSessions.get(sessionId);
        
        if (session != null) {
            // Chuyển tiếp ICE candidate đến người tham gia khác
            String targetUserId = request.getFromUserId().equals(session.getFromUserId()) 
                ? session.getToUserId() 
                : session.getFromUserId();
            
            messagingTemplate.convertAndSendToUser(
                targetUserId,
                "/queue/ice-candidate",
                request
            );
            
            log.debug("ICE candidate đã được chuyển tiếp cho phiên {}", sessionId);
        } else {
            log.warn("ICE candidate cho phiên không tồn tại: {}", sessionId);
        }
    }

    private void cleanupSession(String sessionId) {
        CallSession session = activeSessions.remove(sessionId);
        if (session != null) {
            userSessions.remove(session.getFromUserId());
            userSessions.remove(session.getToUserId());
            log.info("Phiên gọi đã được dọn dẹp: {}", sessionId);
        }
    }
    
    // Tạo message cho cuộc gọi
    private void createCallMessage(String conversationId, String senderId, String receiverId, 
                                 String callType, String callStatus, Long durationSeconds, 
                                 String sessionId, String missedReason) {
        try {
            log.info("Tạo call message: conversationId={}, senderId={}, callType={}, callStatus={}", 
                    conversationId, senderId, callType, callStatus);
            
            User sender = userRepository.findById(senderId).orElse(null);
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            
            if (sender == null || conversation == null) {
                log.error("Không tìm thấy sender hoặc conversation để tạo call message");
                return;
            }
            
            // Tạo content cho call message
            String content = generateCallMessageContent(callType, callStatus, durationSeconds, missedReason);
            
            Message callMessage = new Message();
            callMessage.setConversation(conversation);
            callMessage.setSender(sender);
            callMessage.setContent(content);
            callMessage.setMessageType(MessageType.CALL);
            callMessage.setCreatedAt(LocalDateTime.now());
            
            // Lưu thông tin cuộc gọi vào content dưới dạng JSON
            String callData = String.format(
                "{\"callType\":\"%s\",\"callStatus\":\"%s\",\"durationSeconds\":%d,\"sessionId\":\"%s\",\"missedReason\":\"%s\"}",
                callType, callStatus, durationSeconds != null ? durationSeconds : 0, 
                sessionId != null ? sessionId : "", missedReason != null ? missedReason : ""
            );
            
            // Thêm call data vào content
            callMessage.setContent(content + "|" + callData);
            
            Message savedMessage = messageRepository.save(callMessage);
            log.info("Đã tạo call message: {}", savedMessage.getId());
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo call message: {}", e.getMessage(), e);
        }
    }
    
    // Tạo nội dung hiển thị cho call message
    private String generateCallMessageContent(String callType, String callStatus, Long durationSeconds, String missedReason) {
        String callTypeText = "video".equals(callType) ? "📹 Cuộc gọi video" : "📞 Cuộc gọi thoại";
        
        switch (callStatus) {
            case "COMPLETED":
                if (durationSeconds != null && durationSeconds > 0) {
                    return callTypeText + " - Đã kết thúc (" + formatDuration(durationSeconds) + ")";
                } else {
                    return callTypeText + " - Đã kết thúc";
                }
            case "MISSED":
                return callTypeText + " - Cuộc gọi nhỡ";
            case "REJECTED":
                return callTypeText + " - Cuộc gọi bị từ chối";
            case "TIMEOUT":
                return callTypeText + " - Cuộc gọi timeout";
            default:
                return callTypeText + " - " + callStatus;
        }
    }
    
    // Format thời gian gọi
    private String formatDuration(Long durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0) {
            return "0s";
        }
        
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        long seconds = durationSeconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    private String generateSessionId() {
        return "call_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // Inner class to represent a call session
    private static class CallSession {
        private final String sessionId;
        private final String fromUserId;
        private final String toUserId;
        private final String conversationId;
        private final String callType;
        private boolean accepted = false;
        private long startTime;

        public CallSession(String sessionId, String fromUserId, String toUserId, 
                         String conversationId, String callType) {
            this.sessionId = sessionId;
            this.fromUserId = fromUserId;
            this.toUserId = toUserId;
            this.conversationId = conversationId;
            this.callType = callType;
            this.startTime = System.currentTimeMillis();
        }

        // Getters and setters
        public String getSessionId() { return sessionId; }
        public String getFromUserId() { return fromUserId; }
        public String getToUserId() { return toUserId; }
        public String getConversationId() { return conversationId; }
        public String getCallType() { return callType; }
        public boolean isAccepted() { return accepted; }
        public void setAccepted(boolean accepted) { this.accepted = accepted; }
        public long getStartTime() { return startTime; }
    }
}

