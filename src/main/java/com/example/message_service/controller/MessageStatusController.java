package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.model.MessageStatus;
import com.example.message_service.service.MessageStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message-statuses")
public class MessageStatusController {

    private final MessageStatusService messageStatusService;

    @Autowired
    public MessageStatusController(MessageStatusService messageStatusService) {
        this.messageStatusService = messageStatusService;
    }

    // Lấy tất cả trạng thái của một tin nhắn
    @GetMapping("/message/{messageId}")
    public ApiResponse<List<MessageStatus>> getStatusesByMessage(@PathVariable String messageId) {
        return messageStatusService.getStatusByMessage(messageId);
    }

    // Lấy trạng thái của tin nhắn theo người dùng
//    @GetMapping("/message/{messageId}/user/{userId}")
//    public ApiResponse<List<MessageStatus>> getStatusesByMessageAndUser(
//            @PathVariable String messageId,
//            @PathVariable String userId) {
//        return messageStatusService.getStatusByMessageAndUser(messageId, userId);
//    }

    // Lấy trạng thái theo người dùng và trạng thái cụ thể (ví dụ: DELIVERED, READ)
    @GetMapping("/user/{userId}/status/{status}")
    public ApiResponse<List<MessageStatus>> getStatusesByUserAndStatus(
            @PathVariable String userId,
            @PathVariable String status) {
        return messageStatusService.getStatusByUserAndStatus(userId, status);
    }

    // Thêm trạng thái mới cho tin nhắn
    @PostMapping
    public ApiResponse<MessageStatus> addMessageStatus(@RequestBody MessageStatus messageStatus) {
        return messageStatusService.addMessageStatus(messageStatus);
    }

    // Cập nhật trạng thái của tin nhắn
    @PutMapping("/{messageStatusId}")
    public ApiResponse<MessageStatus> updateMessageStatus(
            @PathVariable String messageStatusId,
            @RequestParam("status") String newStatus) {
        return messageStatusService.updateMessageStatus(messageStatusId, newStatus);
    }

    @PostMapping("/mark-all-seen")
    public ApiResponse<Integer> markAllAsSeen(@RequestParam String conversationId,
                                              @RequestParam String userId) {
        return messageStatusService.markAllMessagesAsSeen(conversationId, userId);
    }
}
