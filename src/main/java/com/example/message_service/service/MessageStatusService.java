package com.example.message_service.service;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.model.MessageStatus;
import com.example.message_service.model.MessageStatusEnum;
import com.example.message_service.repository.MessageStatusRepository;
import com.example.message_service.service.util.PushNewMessage;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageStatusService {
    @Autowired
    private  MessageStatusRepository messageStatusRepository;
    @Autowired
    private PushNewMessage pushNewMessage;

    // Lấy tất cả trạng thái của một tin nhắn
    public ApiResponse<List<MessageStatus>> getStatusByMessage(String messageId) {
        List<MessageStatus> statuses = messageStatusRepository.findByMessageId(messageId);
        if (statuses.isEmpty()) {
            return ApiResponse.error("01", "Không có trạng thái nào cho tin nhắn này");
        }
        return ApiResponse.success("00", "Lấy trạng thái theo tin nhắn thành công", statuses);
    }


    // Lấy trạng thái theo userId và status
    public ApiResponse<List<MessageStatus>> getStatusByUserAndStatus(String userId, String status) {
        List<MessageStatus> statuses = messageStatusRepository.findByUserIdAndStatus(userId, status);
        if (statuses.isEmpty()) {
            return ApiResponse.error("03", "Không có trạng thái nào phù hợp với người dùng và trạng thái này");
        }
        return ApiResponse.success("00", "Lấy trạng thái theo người dùng và trạng thái thành công", statuses);
    }

    // Thêm trạng thái mới
    public ApiResponse<MessageStatus> addMessageStatus(MessageStatus messageStatus) {
        messageStatus.setUpdatedAt(LocalDateTime.now()); // Optional
        MessageStatus saved = messageStatusRepository.save(messageStatus);
        return ApiResponse.success("00", "Thêm trạng thái tin nhắn thành công", saved);
    }


    // Cập nhật trạng thái
    public ApiResponse<MessageStatus> updateMessageStatus(String messageStatusId, String newStatus) {
        Optional<MessageStatus> optionalStatus = messageStatusRepository.findById(messageStatusId);
        if (optionalStatus.isEmpty()) {
            return ApiResponse.error("04", "Không tìm thấy trạng thái tin nhắn với ID: " + messageStatusId);
        }

        MessageStatus status = optionalStatus.get();
        status.setStatus(MessageStatusEnum.valueOf(newStatus));
        status.setUpdatedAt(LocalDateTime.now());
        MessageStatus updated = messageStatusRepository.save(status);

        return ApiResponse.success("00", "Cập nhật trạng thái thành công", updated);
    }


    @Transactional
    public ApiResponse<Integer> markAllMessagesAsSeen(String conversationId, String userId) {
        Integer number = messageStatusRepository.markAllAsSeen(conversationId, userId);
        pushNewMessage.pushUpdatedConversationsToUser(userId);
        return ApiResponse.success("00","Cập nhập thành công" , number);
    }
}
