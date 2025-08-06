package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.MessageFetchRequest;
import com.example.message_service.dto.response.MessageResponse;
import com.example.message_service.service.MessageService;
import com.example.message_service.service.util.PushNewMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final PushNewMessage pushNewMessage;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Client gửi: /app/conversations/get
     * Server trả về: /topic/conversations/{userId}
     */
    @MessageMapping("/conversations/get")
    public void fetchUserConversations(@Payload String userId) {
        if (userId != null && userId.startsWith("\"") && userId.endsWith("\"")) {
            userId = userId.substring(1, userId.length() - 1);
        }

        if (userId == null || userId.isEmpty()) {
            log.warn("userId không hợp lệ: {}", userId);
            return;
        }

        log.info("WebSocket yêu cầu danh sách cuộc trò chuyện cho userId={}", userId);

        pushNewMessage.pushUpdatedConversationsToUser(userId);
    }

    /**
     * Client gửi: /app/messages/get
     * Server trả về: /topic/messages/{conversationId}/{userId}
     */
    @MessageMapping("/messages/get")
    public void fetchMessagesInConversation(@Payload MessageFetchRequest request) {
        String conversationId = request.getConversationId();
        String userId = request.getUserId();
        int page = request.getPage();
        int size = request.getSize();
        if (conversationId == null || userId == null || conversationId.isEmpty() || userId.isEmpty()) {
            return;
        }

        ApiResponse<List<MessageResponse>> response = messageService.getMessagesByConversation(
                conversationId, page, size);

        if (response.getData() != null) {
            String destination = "/topic/messages/" + conversationId + "/" + userId;
            messagingTemplate.convertAndSend(destination, response.getData());
            log.info("Đã gửi danh sách tin nhắn của cuộc trò chuyện {} tới user {}", conversationId, userId);
        } else {
            log.warn("Không có tin nhắn nào trong cuộc trò chuyện {} cho user {}", conversationId, userId);
        }
    }


}
