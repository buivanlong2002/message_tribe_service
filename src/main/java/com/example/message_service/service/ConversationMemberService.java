package com.example.message_service.service;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.model.Conversation;
import com.example.message_service.model.ConversationMember;
import com.example.message_service.model.User;
import com.example.message_service.repository.ConversationMemberRepository;
import com.example.message_service.repository.ConversationRepository;
import com.example.message_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConversationMemberService {

    @Autowired
    private ConversationMemberRepository conversationMemberRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    // Thêm 1 thành viên (mặc định role = "member")
    public ApiResponse<String> addMemberToConversation(String conversationId, String userId) {
        return addMemberToConversation(conversationId, userId, "member");
    }

    // Thêm thành viên với role cụ thể (dùng cho 1-1 hoặc nhóm)
    public ApiResponse<String> addMemberToConversation(String conversationId, String userId, String role) {
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalConversation.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy cuộc trò chuyện: " + conversationId);
        }

        if (optionalUser.isEmpty()) {
            return ApiResponse.error("03", "Không tìm thấy người dùng: " + userId);
        }

        Conversation conversation = optionalConversation.get();
        User user = optionalUser.get();

        boolean isMember = conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId);
        if (isMember) {
            return ApiResponse.error("01", "Người dùng đã là thành viên của cuộc trò chuyện.");
        }

        ConversationMember conversationMember = new ConversationMember();
        conversationMember.setId(UUID.randomUUID().toString());
        conversationMember.setConversation(conversation);
        conversationMember.setUser(user);
        conversationMember.setJoinedAt(LocalDateTime.now());
        conversationMember.setRole(role);

        conversationMemberRepository.save(conversationMember);

        return ApiResponse.success("00", "Thêm thành viên thành công.");
    }

    // Overload cho object Conversation (không cần query lại từ DB)
    public void addMemberToConversation(Conversation conversation, String userId, String role) {
        if (conversationMemberRepository.existsByConversationIdAndUserId(conversation.getId(), userId)) return;

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return;

        ConversationMember member = new ConversationMember();
        member.setId(UUID.randomUUID().toString());
        member.setConversation(conversation);
        member.setUser(userOpt.get());
        member.setRole(role);
        member.setJoinedAt(LocalDateTime.now());

        conversationMemberRepository.save(member);
    }

    // Lấy danh sách thành viên của cuộc trò chuyện
    public ApiResponse<Map<String, Object>> getMembersByConversationId(String conversationId) {
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);
        if (optionalConversation.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy cuộc trò chuyện: " + conversationId);
        }

        List<ConversationMember> members = conversationMemberRepository.findByConversationId(conversationId);
        List<User> users = members.stream()
                .map(ConversationMember::getUser)
                .collect(Collectors.toList());

        // Lấy id của trưởng nhóm (creator)
        String creatorId = optionalConversation.get().getCreatedBy();

        // Trả về cả danh sách users và creatorId
        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("creatorId", creatorId);

        return ApiResponse.success("00", "Lấy danh sách người dùng thành công", result);
    }

    // Xóa thành viên khỏi cuộc trò chuyện
    public ApiResponse<String> removeMemberFromConversation(String conversationId, String userId) {
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalConversation.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy cuộc trò chuyện: " + conversationId);
        }

        if (optionalUser.isEmpty()) {
            return ApiResponse.error("03", "Không tìm thấy người dùng: " + userId);
        }

        boolean isMember = conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId);
        if (!isMember) {
            return ApiResponse.error("01", "Người dùng không phải là thành viên của cuộc trò chuyện.");
        }

        conversationMemberRepository.deleteByConversationIdAndUserId(conversationId, userId);

        return ApiResponse.success("00", "Xóa thành viên thành công.");
    }

    // Thêm người tạo nhóm (nếu chưa có)
    public ApiResponse<String> addCreatorToConversation(Conversation conversation) {
        String creatorId = conversation.getCreatedBy();
        Optional<User> optionalUser = userRepository.findById(creatorId);
        if (optionalUser.isEmpty()) {
            return ApiResponse.error("03", "Không tìm thấy người tạo nhóm.");
        }

        boolean isCreatorMember = conversationMemberRepository.existsByConversationIdAndUserId(
                conversation.getId(), creatorId);

        if (!isCreatorMember) {
            ConversationMember member = new ConversationMember();
            member.setId(UUID.randomUUID().toString());
            member.setConversation(conversation);
            member.setUser(optionalUser.get());
            member.setJoinedAt(LocalDateTime.now());
            member.setRole("creator");

            conversationMemberRepository.save(member);
        }

        return ApiResponse.success("00", "Người tạo đã được thêm vào cuộc trò chuyện.");
    }
}
