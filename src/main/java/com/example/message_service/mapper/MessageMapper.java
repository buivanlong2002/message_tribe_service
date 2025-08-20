package com.example.message_service.mapper;

import com.example.message_service.dto.response.*;
import com.example.message_service.model.Attachment;
import com.example.message_service.model.Message;
import com.example.message_service.model.CallHistory;
import com.example.message_service.model.CallHistory.CallStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MessageMapper {

    public MessageResponse toMessageResponse(Message message) {
        return toMessageResponse(message, List.of());
    }

    public MessageResponse toMessageResponse(
            Message message,
            List<SeenByResponse> seenBy
    ) {
        SenderResponse senderResponse = new SenderResponse(
                message.getSender().getId(),
                message.getSender().getDisplayName(),
                message.getSender().getAvatarUrl()
        );

        // ───────── Reply info ─────────
        String replyToId = null;
        if (message.getReplyTo() != null) {
            replyToId = message.getReplyTo().getId();
        }

        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                senderResponse,
                message.getContent(),
                message.getMessageType().name(),
                message.getCreatedAt(),
                replyToId,
                message.isEdited(),
                message.isSeen(),
                message.isRecalled(),
                toAttachmentResponseList(message.getAttachments()),
                getTimeAgo(message.getCreatedAt()),
                seenBy
        );
    }

    private List<AttachmentResponse> toAttachmentResponseList(List<Attachment> attachments) {
        if (attachments == null || attachments.isEmpty()) return List.of();

        return attachments.stream()
                .map(att -> new AttachmentResponse(
                        att.getId(),
                        att.getOriginalFileName(),
                        att.getFileUrl(),
                        att.getFileType()
                ))
                .collect(Collectors.toList());
    }

    private String getTimeAgo(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());

        if (duration.toMinutes() < 1) return "Vừa xong";
        if (duration.toHours() < 1) return duration.toMinutes() + " phút trước";
        if (duration.toDays() < 1) return duration.toHours() + " giờ trước";
        return duration.toDays() + " ngày trước";
    }

    // Chuyển đổi CallHistory thành MessageResponse với type CALL
    public MessageResponse toCallHistoryMessageResponse(CallHistory callHistory) {
        // Tạo sender response từ caller
        SenderResponse senderResponse = new SenderResponse(
                callHistory.getCallerId(),
                callHistory.getCallerName() != null ? callHistory.getCallerName() : "Unknown",
                callHistory.getCallerAvatar()
        );

        // Tạo content cho call message
        String content = generateCallMessageContent(callHistory);

        return new MessageResponse(
                "call_" + callHistory.getId(), // Tạo ID duy nhất cho call message
                callHistory.getConversationId(),
                senderResponse,
                content,
                "CALL", // MessageType.CALL
                callHistory.getStartTime(), // Sử dụng startTime làm createdAt
                null, // replyToId
                false, // edited
                false, // seen
                false, // recalled
                List.of(), // attachments
                getTimeAgo(callHistory.getStartTime()), // timeAgo
                List.of() // seenBy
        );
    }

    // Tạo nội dung hiển thị cho call message từ CallHistory
    private String generateCallMessageContent(CallHistory callHistory) {
        String callTypeText = callHistory.getCallType() == CallHistory.CallType.VIDEO ? 
            "📹 Cuộc gọi video" : "📞 Cuộc gọi thoại";
        
        CallStatus status = callHistory.getCallStatus();
        if (status == CallStatus.COMPLETED) {
            if (callHistory.getDurationSeconds() != null && callHistory.getDurationSeconds() > 0) {
                return callTypeText + " - Đã kết thúc (" + formatDuration(callHistory.getDurationSeconds()) + ")";
            } else {
                return callTypeText + " - Đã kết thúc";
            }
        } else if (status == CallStatus.MISSED) {
            return callTypeText + " - Cuộc gọi nhỡ";
        } else if (status == CallStatus.REJECTED) {
            return callTypeText + " - Cuộc gọi bị từ chối";
        } else if (status == CallStatus.TIMEOUT) {
            return callTypeText + " - Cuộc gọi timeout";
        } else if (status == CallStatus.BUSY) {
            return callTypeText + " - Cuộc gọi bận";
        } else {
            return callTypeText + " - " + callHistory.getCallStatus();
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
}
