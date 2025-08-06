package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.EditMessageRequest;
import com.example.message_service.dto.request.SendMessageRequest;
import com.example.message_service.dto.response.MessageResponse;
import com.example.message_service.model.Message;
import com.example.message_service.model.MessageType;
import com.example.message_service.model.User;
import com.example.message_service.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // Gửi tin nhắn mới
    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MessageResponse> sendMessage(
            @RequestParam String senderId,
            @RequestParam(required = false) String conversationId,
            @RequestParam(required = false) String receiverId,
            @RequestParam(required = false) String content,
            @RequestParam MessageType messageType,
            @RequestParam(required = false) String replyToId,
            @RequestPart(required = false) MultipartFile[] files
    ) throws ChangeSetPersister.NotFoundException {
        return messageService.sendMessage(
                senderId,
                conversationId,
                receiverId,
                files,
                messageType,
                content,
                replyToId
        );
    }

    @GetMapping("/get-by-conversation")
    public ApiResponse<List<MessageResponse>> getMessages(
            @RequestParam String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return messageService.getMessagesByConversation(conversationId, page, size);
    }


//    // Lấy tất cả tin nhắn trong một cuộc trò chuyện
//    @PostMapping("/get-by-conversation")
//    public ApiResponse<List<MessageResponse>> getMessagesByConversation(@RequestParam String conversationId) {
//        return messageService.getMessagesByConversation(conversationId);
//    }

    // Lấy tin nhắn theo ID trong một cuộc trò chuyện
    @PostMapping("/get-by-id")
    public ApiResponse<Message> getMessageByIdAndConversation(
            @RequestParam String messageId,
            @RequestParam String conversationId) {

        Optional<Message> message = messageService.getMessageByIdAndConversation(messageId, conversationId);

        return message.map(m -> ApiResponse.success("00", "Lấy tin nhắn thành công", m))
                .orElseGet(() -> ApiResponse.error("01", "Không tìm thấy tin nhắn"));
    }


    // Lấy các tin nhắn theo người gửi trong một cuộc trò chuyện
    @PostMapping("/get-by-sender")
    public ApiResponse<List<MessageResponse>> getMessagesBySenderAndConversation(@RequestParam String senderId, @RequestParam String conversationId) {
        return messageService.getMessagesBySenderAndConversation(conversationId, senderId);
    }

    // Chỉnh sửa nội dung tin nhắn
    @PostMapping("/edit")
    public ApiResponse<MessageResponse> editMessage(@RequestBody EditMessageRequest request) {
        return messageService.editMessage(request.getMessageId(), request.getNewContent() , request.getConversationId());
    }

    @PutMapping("/{id}/seen")
    public ResponseEntity<?> markMessageAsSeen(@PathVariable String id) {
        messageService.markAsSeen(id);
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu đã xem thành công", null));
    }

    @PutMapping("/{id}/recall")
    public ResponseEntity<?> recallMessage(@PathVariable String id, @RequestParam String userId) {
        messageService.recallMessage(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Thu hồi tin nhắn thành công", null));
    }

    @GetMapping("/search")
    public ApiResponse<List<MessageResponse>> searchMessages(
            @RequestParam String conversationId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return messageService.searchMessagesByKeyword(conversationId, keyword, page, size);
    }


}
