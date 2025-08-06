package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.model.Attachment;
import com.example.message_service.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Autowired
    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    // Lấy tất cả file đính kèm của một tin nhắn
    @GetMapping("/message/{messageId}")
    public ApiResponse<List<Attachment>> getAttachmentsByMessage(@PathVariable String messageId) {
        return attachmentService.getAttachmentsByMessage(messageId);
    }

    // Thêm một file đính kèm mới
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Attachment> addAttachment(@RequestBody Attachment attachment) {
        return attachmentService.addAttachment(attachment);
    }

    // Lấy tất cả file đính kèm trong một cuộc trò chuyện
    @GetMapping("/conversation/{conversationId}")
    public ApiResponse<List<Attachment>> getAttachmentsByConversation(@PathVariable String conversationId) {
        return attachmentService.getAttachmentsByConversation(conversationId);
    }
}
