package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.model.CallHistory;
import com.example.message_service.service.CallHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/call-history")
@RequiredArgsConstructor
public class CallHistoryController {
    
    private final CallHistoryService callHistoryService;
    
    // Lấy lịch sử cuộc gọi theo conversation
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ApiResponse<List<CallHistory>>> getCallHistoryByConversation(@PathVariable String conversationId) {
        log.info("Lấy lịch sử cuộc gọi cho conversation: {}", conversationId);
        try {
            List<CallHistory> callHistory = callHistoryService.getCallHistoryByConversation(conversationId);
            return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử cuộc gọi thành công", callHistory));
        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch sử cuộc gọi: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("99", "Có lỗi xảy ra khi lấy lịch sử cuộc gọi"));
        }
    }
    
    // Lấy lịch sử cuộc gọi theo user
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<CallHistory>>> getCallHistoryByUser(@PathVariable String userId) {
        log.info("Lấy lịch sử cuộc gọi cho user: {}", userId);
        try {
            List<CallHistory> callHistory = callHistoryService.getCallHistoryByUser(userId);
            return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử cuộc gọi thành công", callHistory));
        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch sử cuộc gọi: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("99", "Có lỗi xảy ra khi lấy lịch sử cuộc gọi"));
        }
    }
    
    // Lấy cuộc gọi nhỡ theo user
    @GetMapping("/user/{userId}/missed")
    public ResponseEntity<ApiResponse<List<CallHistory>>> getMissedCallsByUser(@PathVariable String userId) {
        log.info("Lấy cuộc gọi nhỡ cho user: {}", userId);
        try {
            List<CallHistory> missedCalls = callHistoryService.getMissedCallsByUser(userId);
            return ResponseEntity.ok(ApiResponse.success("Lấy cuộc gọi nhỡ thành công", missedCalls));
        } catch (Exception e) {
            log.error("Lỗi khi lấy cuộc gọi nhỡ: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("99", "Có lỗi xảy ra khi lấy cuộc gọi nhỡ"));
        }
    }
    
    // Đếm cuộc gọi nhỡ theo user
    @GetMapping("/user/{userId}/missed/count")
    public ResponseEntity<ApiResponse<Long>> countMissedCallsByUser(@PathVariable String userId) {
        log.info("Đếm cuộc gọi nhỡ cho user: {}", userId);
        try {
            long count = callHistoryService.countMissedCallsByUser(userId);
            return ResponseEntity.ok(ApiResponse.success("Đếm cuộc gọi nhỡ thành công", count));
        } catch (Exception e) {
            log.error("Lỗi khi đếm cuộc gọi nhỡ: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("99", "Có lỗi xảy ra khi đếm cuộc gọi nhỡ"));
        }
    }
    
    // Xóa cuộc gọi
    @DeleteMapping("/{callId}")
    public ResponseEntity<ApiResponse<Void>> deleteCall(@PathVariable String callId) {
        log.info("Xóa cuộc gọi: {}", callId);
        try {
            callHistoryService.deleteCall(callId);
            return ResponseEntity.ok(ApiResponse.success("Xóa cuộc gọi thành công", null));
        } catch (Exception e) {
            log.error("Lỗi khi xóa cuộc gọi: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("99", "Có lỗi xảy ra khi xóa cuộc gọi"));
        }
    }
}
