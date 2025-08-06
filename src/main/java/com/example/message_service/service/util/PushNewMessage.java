package com.example.message_service.service.util;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.response.ConversationResponse;
import com.example.message_service.dto.response.MessageResponse;
import com.example.message_service.model.ConversationMember;
import com.example.message_service.repository.ConversationMemberRepository;
import com.example.message_service.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNewMessage {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationService conversationService;

    /**
     * Gửi danh sách cuộc trò chuyện của người dùng về client
     */
    public void pushUpdatedConversationsToUser(String userId) {
        ApiResponse<List<ConversationResponse>> response = conversationService.getConversationsByUser(userId);

        if (response.getData() != null) {
            String destination = "/topic/conversations/" + userId;
            messagingTemplate.convertAndSend(destination, response.getData());
            log.info("Đã gửi danh sách cuộc trò chuyện tới {}", destination);
        } else {
            log.warn("Không có dữ liệu cuộc trò chuyện cho userId: {}", userId);
        }
    }

    public void pushUpdatedConversationsToMemBer(String conversationsId , String memberId){
        ApiResponse<List<MessageResponse>> response = conversationService.getMessagesByConversationId(conversationsId ,memberId);

        if (response.getData() != null) {
            String destination = "/topic/messages/" + conversationsId + "/" + memberId;
            messagingTemplate.convertAndSend(destination, response.getData());
            log.info("Đã gửi danh sách tin nhắn của cuộc trò chuyện {} tới user {}", conversationsId, memberId);
        } else {
            log.warn("Không có tin nhắn nào trong cuộc trò chuyện {} cho user {}", conversationsId, memberId);
        }
    }



}