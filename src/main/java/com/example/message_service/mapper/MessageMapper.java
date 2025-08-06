package com.example.message_service.mapper;

import com.example.message_service.dto.response.*;
import com.example.message_service.model.Attachment;
import com.example.message_service.model.Message;
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
}
