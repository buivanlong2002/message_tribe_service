package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.AddMemberRequest;
import com.example.message_service.dto.request.GetMembersByConversationRequest;
import com.example.message_service.dto.request.RemoveMemberRequest;
import com.example.message_service.model.User;
import com.example.message_service.service.ConversationMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversation-members")
public class ConversationMemberController {

    private final ConversationMemberService conversationMemberService;

    @Autowired
    public ConversationMemberController(ConversationMemberService conversationMemberService) {
        this.conversationMemberService = conversationMemberService;
    }

    // Thêm thành viên vào cuộc trò chuyện
    @PostMapping("/add")
    public ApiResponse<String> addMember(@RequestBody AddMemberRequest addMemberRequest) {
        String uuidStringConversationId = addMemberRequest.getConversationId().toString();
        String uuidStringUserId = addMemberRequest.getUserId().toString();
        return conversationMemberService.addMemberToConversation(uuidStringConversationId, uuidStringUserId);
    }

    // Lấy danh sách thành viên trong một cuộc trò chuyện
    @PostMapping("/members-by-conversation")
    public ApiResponse<Map<String, Object>> getMembersByConversation(
            @RequestBody GetMembersByConversationRequest request) {
        String conversationId = request.getConversationId().toString();  // hoặc request.getConversationId() nếu đã là String
        return conversationMemberService.getMembersByConversationId(conversationId);
    }

    // Xóa thành viên khỏi cuộc trò chuyện
    @PostMapping("/remove")
    public ApiResponse<String> removeMember(@RequestBody RemoveMemberRequest removeMemberRequest) {
        String uuidStringConversationId = removeMemberRequest.getConversationId().toString();
        String uuidStringUserId = removeMemberRequest.getUserId().toString();
        return conversationMemberService.removeMemberFromConversation(uuidStringConversationId, uuidStringUserId);
    }
}
