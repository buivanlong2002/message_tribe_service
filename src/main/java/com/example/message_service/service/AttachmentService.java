package com.example.message_service.service;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.model.Attachment;
import com.example.message_service.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    @Autowired
    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    // Lấy tất cả file đính kèm của một tin nhắn
    public ApiResponse<List<Attachment>> getAttachmentsByMessage(String messageId) {
        List<Attachment> attachments = attachmentRepository.findByMessageId(messageId);
        return ApiResponse.success("00", "Lấy file đính kèm thành công", attachments);
    }

    // Thêm một file đính kèm mới
    public ApiResponse<Attachment> addAttachment(Attachment attachment) {
        Attachment savedAttachment = attachmentRepository.save(attachment);
        return ApiResponse.success("00", "Thêm file đính kèm thành công", savedAttachment);
    }

    // Lấy tất cả file đính kèm trong một cuộc trò chuyện
    public ApiResponse<List<Attachment>> getAttachmentsByConversation(String conversationId) {
        List<Attachment> attachments = attachmentRepository.findByMessage_Conversation_Id(conversationId);
        return ApiResponse.success("00", "Lấy tất cả file trong cuộc trò chuyện thành công", attachments);
    }

}
