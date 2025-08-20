package com.example.message_service.controller;

import com.example.message_service.dto.request.CallRequest;
import com.example.message_service.service.CallService;
import com.example.message_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CallController {

    private final CallService callService;
    private final UserService userService;

    @MessageMapping("/call/request")
    public void handleCallRequest(@Payload CallRequest request) {
        log.info("=== BACKEND: NHẬN CALL REQUEST ===");
        log.info("Request: {}", request);
        log.info("From user: {}", request.getFromUserId());
        log.info("To user: {}", request.getToUserId());
        log.info("Call type: {}", request.getCallType());
        log.info("Conversation ID: {}", request.getConversationId());
        log.info("Request type: {}", request.getType());
        log.info("Request data: {}", request.getData());
        log.info("Caller name from client: {}", request.getCallerName());
        log.info("Caller avatar from client: {}", request.getCallerAvatar());
        log.info("Receiver name from client: {}", request.getReceiverName());
        log.info("Receiver avatar from client: {}", request.getReceiverAvatar());
        
        // Lấy thông tin người gọi và người nhận
        try {
            log.info("🔍 Đang lấy thông tin user từ database...");
            log.info("FromUserId: {}", request.getFromUserId());
            log.info("ToUserId: {}", request.getToUserId());
            
            var caller = userService.getUserById(request.getFromUserId());
            var receiver = userService.getUserById(request.getToUserId());
            
            log.info("Caller from DB: {}", caller);
            log.info("Receiver from DB: {}", receiver);
            
            if (caller != null) {
                // Ưu tiên thông tin từ client, fallback về database
                String callerName = request.getCallerName();
                if (callerName == null || callerName.trim().isEmpty()) {
                    callerName = caller.getDisplayName();
                    if (callerName == null || callerName.trim().isEmpty()) {
                        callerName = caller.getEmail(); // Fallback to email if displayName is empty
                    }
                }
                
                String callerAvatar = request.getCallerAvatar();
                if (callerAvatar == null || callerAvatar.trim().isEmpty()) {
                    callerAvatar = caller.getAvatarUrl();
                }
                
                request.setCallerName(callerName);
                request.setCallerAvatar(callerAvatar);
                log.info("✅ Set caller info: name={}, avatar={}", callerName, callerAvatar);
            } else {
                log.warn("❌ Không tìm thấy caller với ID: {}", request.getFromUserId());
            }
            
            if (receiver != null) {
                String receiverName = receiver.getDisplayName();
                if (receiverName == null || receiverName.trim().isEmpty()) {
                    receiverName = receiver.getEmail(); // Fallback to email if displayName is empty
                }
                request.setReceiverName(receiverName);
                request.setReceiverAvatar(receiver.getAvatarUrl());
                log.info("✅ Set receiver info: name={}, avatar={}", receiverName, receiver.getAvatarUrl());
            } else {
                log.warn("❌ Không tìm thấy receiver với ID: {}", request.getToUserId());
            }
            
            log.info("✅ Final caller info: {} ({})", request.getCallerName(), request.getCallerAvatar());
            log.info("✅ Final receiver info: {} ({})", request.getReceiverName(), request.getReceiverAvatar());
        } catch (Exception e) {
            log.error("❌ Lỗi khi lấy thông tin user: {}", e.getMessage(), e);
        }
        
        callService.handleCallRequest(request);
        log.info("=== BACKEND: ĐÃ XỬ LÝ CALL REQUEST ===");
    }

    @MessageMapping("/call/accept")
    public void handleCallAccept(@Payload CallRequest request) {
        log.info("Received call accept: {}", request);
        callService.handleCallAccept(request);
    }

    @MessageMapping("/call/reject")
    public void handleCallReject(@Payload CallRequest request) {
        log.info("Received call reject: {}", request);
        callService.handleCallReject(request);
    }

    @MessageMapping("/call/end")
    public void handleCallEnd(@Payload CallRequest request) {
        log.info("Received call end: {}", request);
        callService.handleCallEnd(request);
    }

    @MessageMapping("/webrtc/offer")
    public void handleWebRTCOffer(@Payload CallRequest request) {
        log.info("Received WebRTC offer for session: {}", request.getSessionId());
        callService.handleOffer(request);
    }

    @MessageMapping("/webrtc/answer")
    public void handleWebRTCAnswer(@Payload CallRequest request) {
        log.info("Received WebRTC answer for session: {}", request.getSessionId());
        callService.handleAnswer(request);
    }

    @MessageMapping("/webrtc/ice-candidate")
    public void handleIceCandidate(@Payload CallRequest request) {
        log.debug("Received ICE candidate for session: {}", request.getSessionId());
        callService.handleIceCandidate(request);
    }
}

