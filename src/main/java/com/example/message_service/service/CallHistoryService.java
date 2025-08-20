package com.example.message_service.service;

import com.example.message_service.model.CallHistory;
import com.example.message_service.repository.CallHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallHistoryService {
    
    private final CallHistoryRepository callHistoryRepository;
    
    // Tạo cuộc gọi mới
    public CallHistory createCall(String conversationId, String callerId, String receiverId, 
                                 CallHistory.CallType callType, String sessionId,
                                 String callerName, String callerAvatar,
                                 String receiverName, String receiverAvatar) {
        log.info("Tạo cuộc gọi mới: conversationId={}, callerId={}, receiverId={}, callType={}, sessionId={}", 
                conversationId, callerId, receiverId, callType, sessionId);
        
        CallHistory callHistory = new CallHistory();
        callHistory.setConversationId(conversationId);
        callHistory.setCallerId(callerId);
        callHistory.setReceiverId(receiverId);
        callHistory.setCallType(callType);
        callHistory.setCallStatus(CallHistory.CallStatus.MISSED); // Mặc định là missed
        callHistory.setStartTime(LocalDateTime.now());
        callHistory.setSessionId(sessionId);
        
        // Lưu thông tin người gọi và người nhận
        callHistory.setCallerName(callerName);
        callHistory.setCallerAvatar(callerAvatar);
        callHistory.setReceiverName(receiverName);
        callHistory.setReceiverAvatar(receiverAvatar);
        
        CallHistory saved = callHistoryRepository.save(callHistory);
        log.info("Đã tạo cuộc gọi: {} với thông tin người gọi: {} ({})", 
                saved.getId(), callerName, callerAvatar);
        return saved;
    }
    
    // Cập nhật trạng thái cuộc gọi
    public CallHistory updateCallStatus(String sessionId, CallHistory.CallStatus status, String reason) {
        log.info("Cập nhật trạng thái cuộc gọi: sessionId={}, status={}, reason={}", sessionId, status, reason);
        
        CallHistory callHistory = callHistoryRepository.findBySessionId(sessionId);
        if (callHistory == null) {
            log.warn("Không tìm thấy cuộc gọi với sessionId: {}", sessionId);
            return null;
        }
        
        callHistory.setCallStatus(status);
        callHistory.setEndTime(LocalDateTime.now());
        callHistory.setMissedReason(reason);
        callHistory.calculateDuration();
        
        CallHistory updated = callHistoryRepository.save(callHistory);
        log.info("Đã cập nhật cuộc gọi: {} - status: {}, duration: {}s", 
                updated.getId(), status, updated.getDurationSeconds());
        return updated;
    }
    
    // Lấy lịch sử cuộc gọi theo conversation
    public List<CallHistory> getCallHistoryByConversation(String conversationId) {
        log.info("Lấy lịch sử cuộc gọi cho conversation: {}", conversationId);
        return callHistoryRepository.findByConversationIdOrderByStartTimeDesc(conversationId);
    }
    
    // Lấy lịch sử cuộc gọi theo user
    public List<CallHistory> getCallHistoryByUser(String userId) {
        log.info("Lấy lịch sử cuộc gọi cho user: {}", userId);
        return callHistoryRepository.findByUserIdOrderByStartTimeDesc(userId);
    }
    
    // Lấy cuộc gọi nhỡ theo user
    public List<CallHistory> getMissedCallsByUser(String userId) {
        log.info("Lấy cuộc gọi nhỡ cho user: {}", userId);
        return callHistoryRepository.findMissedCallsByUserId(userId);
    }
    
    // Đếm cuộc gọi nhỡ theo user
    public long countMissedCallsByUser(String userId) {
        return callHistoryRepository.countMissedCallsByUserId(userId);
    }
    
    // Xóa cuộc gọi
    public void deleteCall(String callId) {
        log.info("Xóa cuộc gọi: {}", callId);
        callHistoryRepository.deleteById(callId);
    }
    
    // Format thời gian gọi
    public String formatDuration(Long durationSeconds) {
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
    
    // Format thời gian bắt đầu
    public String formatStartTime(LocalDateTime startTime) {
        if (startTime == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today = now.toLocalDate().atStartOfDay();
        LocalDateTime yesterday = today.minusDays(1);
        
        if (startTime.isAfter(today)) {
            return "Hôm nay " + startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        } else if (startTime.isAfter(yesterday)) {
            return "Hôm qua " + startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            return startTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
    }
}
