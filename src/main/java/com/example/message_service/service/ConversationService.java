package com.example.message_service.service;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.UpdateConversationRequest;
import com.example.message_service.dto.response.ConversationResponse;
import com.example.message_service.dto.response.LastMessageInfo;
import com.example.message_service.dto.response.MemberResponse;
import com.example.message_service.dto.response.MessageResponse;
import com.example.message_service.mapper.MessageMapper;
import com.example.message_service.model.*;
import com.example.message_service.model.UserMessage;
import com.example.message_service.repository.*;

import com.example.message_service.service.util.PushNewMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMemberRepository conversationMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationMemberService conversationMemberService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private FriendshipRepository friendshipRepository;
    @Autowired
    private MessageStatusRepository messageStatusRepository;

    @Autowired
    private UserConversationRepository userConversationRepository;

    @Autowired
    private UserMessageRepository userMessageRepository;




    // ----------------- CREATE --------------------

    public ApiResponse<Conversation> createGroupConversation(String name, String createdBy) {
        try {
            if (name == null || name.trim().isEmpty()) {
                return ApiResponse.error("01", "Tên nhóm không được để trống");
            }

            if (createdBy == null || createdBy.trim().isEmpty()) {
                return ApiResponse.error("02", "Người tạo không được để trống");
            }

            // Kiểm tra user có tồn tại không
            Optional<User> userOpt = userRepository.findById(createdBy);
            if (userOpt.isEmpty()) {
                return ApiResponse.error("03", "Không tìm thấy người dùng với ID: " + createdBy);
            }

            Conversation conversation = new Conversation();
            conversation.setName(name.trim());
            conversation.setGroup(true);
            conversation.setCreatedBy(createdBy);
            conversation.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            
            Conversation saved = conversationRepository.save(conversation);
            
            // Thêm creator vào conversation
            ApiResponse<String> addCreatorResult = conversationMemberService.addCreatorToConversation(saved);
            if (!addCreatorResult.getStatus().isSuccess()) {
                return ApiResponse.error("04", "Lỗi khi thêm người tạo vào nhóm: " + addCreatorResult.getStatus().getDisplayMessage());
            }

            return ApiResponse.success("00", "Tạo nhóm thành công", saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("99", "Lỗi khi tạo nhóm: " + e.getMessage());
        }
    }


    @Transactional
    public Conversation getOrCreateOneToOneConversation(String senderId, String receiverId) {
        // Tìm conversation 1-1 giữa sender và receiver
        Optional<Conversation> existing = findOneToOneConversation(senderId, receiverId);
                
        if (existing.isPresent()) {
            // Kiểm tra và khôi phục conversation nếu cần
            restoreConversationIfDeleted(senderId, existing.get().getId());
            restoreConversationIfDeleted(receiverId, existing.get().getId());
            
            return existing.get();
        }

        Conversation conversation = new Conversation();
        conversation.setGroup(false);
        conversation.setName(null);
        conversation.setCreatedBy(senderId);
        conversation.setCreatedAt(LocalDateTime.now());

        Conversation saved = conversationRepository.save(conversation);
        conversationMemberService.addMemberToConversation(saved, senderId, "member");
        conversationMemberService.addMemberToConversation(saved, receiverId, "member");

        return saved;
    }

    private void restoreConversationIfDeleted(String userId, String conversationId) {
        try {
            Optional<UserConversation> userConversationOpt = userConversationRepository
                .findByUserIdAndConversationId(userId, conversationId);
            
            System.out.println("🔍 Checking conversation " + conversationId + " for user " + userId + ": " + 
                (userConversationOpt.isPresent() ? "found, deleted=" + userConversationOpt.get().isDeleted() : "not found"));
            
            if (userConversationOpt.isPresent() && userConversationOpt.get().isDeleted()) {
                // Khôi phục conversation (chỉ conversation, không khôi phục tin nhắn cũ)
                UserConversation userConversation = userConversationOpt.get();
                userConversation.setDeleted(false);
                userConversation.setDeletedAt(null);
                userConversationRepository.save(userConversation);
                
                // KHÔNG khôi phục tin nhắn cũ - để user chỉ thấy tin nhắn mới
                System.out.println("🔄 Restored conversation " + conversationId + " for user " + userId + " (old messages remain deleted)");
            } else if (userConversationOpt.isPresent()) {
                System.out.println("ℹ️ Conversation " + conversationId + " for user " + userId + " is already active");
            } else {
                System.out.println("ℹ️ No UserConversation record found for user " + userId + " in conversation " + conversationId);
            }
        } catch (Exception e) {
            System.err.println("Error restoring conversation for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Conversation createDynamicGroupFromMessage(String senderId, String receiverId) {
        Optional<User> senderOpt = userRepository.findById(senderId);
        if (senderOpt.isEmpty()) throw new RuntimeException("Sender not found");

        Conversation group = new Conversation();
        group.setGroup(true);
        group.setName(senderOpt.get().getDisplayName());
        group.setCreatedBy(senderId);
        group.setCreatedAt(LocalDateTime.now());

        Conversation saved = conversationRepository.save(group);
        conversationMemberService.addMemberToConversation(saved, senderId, "member");
        conversationMemberService.addMemberToConversation(saved, receiverId, "member");

        return saved;
    }

    public ApiResponse<ConversationResponse> updateConversation(String conversationId, UpdateConversationRequest request) {
        return updateConversation(conversationId, request, null);
    }

    public ApiResponse<ConversationResponse> updateConversation(String conversationId, UpdateConversationRequest request, String requesterId) {
        Optional<Conversation> optional = conversationRepository.findById(conversationId);
        if (optional.isEmpty()) {
            return ApiResponse.error("04", "Không tìm thấy cuộc trò chuyện với ID: " + conversationId);
        }

        Conversation conversation = optional.get();

        // Không kiểm tra quyền - ai cũng được đổi tên nhóm

        conversation.setName(request.getName());
        conversation.setGroup(request.isGroup());
        conversationRepository.save(conversation);

        ConversationResponse dto = toConversationResponse(conversation, null, null);
        return ApiResponse.success("00", "Cập nhật cuộc trò chuyện thành công", dto);
    }

    public ApiResponse<String> updateGroupAvatar(String conversationId, MultipartFile file) {
        Optional<Conversation> optional = conversationRepository.findById(conversationId);
        if (optional.isEmpty()) {
            return ApiResponse.error("04", "Không tìm thấy cuộc trò chuyện");
        }

        Conversation conversation = optional.get();

        // Kiểm tra phải là nhóm
        if (!conversation.isGroup()) {
            return ApiResponse.error("05", "Chỉ nhóm mới được cập nhật ảnh đại diện");
        }

        // Không kiểm tra quyền - ai cũng được đổi ảnh nhóm

        try {
            // Tạo đường dẫn lưu ảnh
            String originalFilename = Path.of(file.getOriginalFilename()).getFileName().toString();
            String fileName = UUID.randomUUID() + "_" + originalFilename;
            String uploadDir = "uploads/conversations/";
            Files.createDirectories(Paths.get(uploadDir));

            // Lưu file
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Lưu đường dẫn tương đối vào DB
            String avatarUrl = "/uploads/conversations/" + fileName;
            conversation.setAvatarUrl(avatarUrl);
            conversationRepository.save(conversation);

            return ApiResponse.success("00", "Cập nhật ảnh đại diện nhóm thành công", avatarUrl);

        } catch (IOException e) {
            return ApiResponse.error("07", "Lỗi khi upload ảnh đại diện nhóm");
        }
    }


    public void archiveConversation(String conversationId) {
        conversationRepository.findById(conversationId).ifPresent(c -> {
            c.setArchived(true);
            conversationRepository.save(c);
        });
    }

    // Xóa conversation 1 phía (chỉ ẩn khỏi danh sách của user hiện tại)
    public ApiResponse<String> deleteConversationForUser(String conversationId, String userId) {
        try {
            // Kiểm tra conversation có tồn tại không
            Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
            if (conversationOpt.isEmpty()) {
                return ApiResponse.error("01", "Không tìm thấy cuộc trò chuyện");
            }

            // Kiểm tra user có tồn tại không
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ApiResponse.error("02", "Không tìm thấy người dùng");
            }

            // Kiểm tra user có phải là thành viên của conversation không
            Optional<ConversationMember> memberOpt = conversationMemberRepository.findByConversationIdAndUserId(conversationId, userId);
            if (memberOpt.isEmpty()) {
                return ApiResponse.error("03", "Bạn không phải là thành viên của cuộc trò chuyện này");
            }

            // Tìm hoặc tạo UserConversation record
            Optional<UserConversation> userConversationOpt = userConversationRepository.findByUserIdAndConversationId(userId, conversationId);
            UserConversation userConversation;
            
            if (userConversationOpt.isPresent()) {
                userConversation = userConversationOpt.get();
            } else {
                userConversation = new UserConversation();
                userConversation.setUserId(userId);
                userConversation.setConversationId(conversationId);
                userConversation.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            }

            // Đánh dấu là đã xóa
            userConversation.setDeleted(true);
            userConversation.setDeletedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            
            userConversationRepository.save(userConversation);

            // Xóa tất cả tin nhắn trong conversation này (chỉ ở phía user hiện tại)
            // Sử dụng Pageable để lấy tất cả tin nhắn
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Message> messagePage = messageRepository.findByConversationId(conversationId, pageable);
            List<Message> messagesToDelete = messagePage.getContent();
            
            if (!messagesToDelete.isEmpty()) {
                // Thay vì xóa tin nhắn thực sự, chúng ta sẽ đánh dấu là đã xóa cho user này
                // Tạo UserMessage records để track việc xóa tin nhắn
                for (Message message : messagesToDelete) {
                    // Tạo record để đánh dấu tin nhắn đã bị xóa bởi user này
                    UserMessage userMessage = new UserMessage();
                    userMessage.setUserId(userId);
                    userMessage.setMessageId(message.getId());
                    userMessage.setConversationId(conversationId);
                    userMessage.setDeleted(true);
                    userMessage.setDeletedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                    userMessageRepository.save(userMessage);
                }
                System.out.println("🗑️ Marked " + messagesToDelete.size() + " messages as deleted for user " + userId + " in conversation " + conversationId);
            }

                    return ApiResponse.success("00", "Đã xóa cuộc trò chuyện khỏi danh sách");
    } catch (Exception e) {
        e.printStackTrace();
        return ApiResponse.error("99", "Lỗi khi xóa cuộc trò chuyện: " + e.getMessage());
    }
}

// Khôi phục conversation đã bị xóa
public ApiResponse<String> restoreConversationForUser(String conversationId, String userId) {
    try {
        // Kiểm tra conversation có tồn tại không
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isEmpty()) {
            return ApiResponse.error("01", "Không tìm thấy cuộc trò chuyện");
        }

        // Kiểm tra user có tồn tại không
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy người dùng");
        }

        // Tìm UserConversation record
        Optional<UserConversation> userConversationOpt = userConversationRepository.findByUserIdAndConversationId(userId, conversationId);
        
        if (userConversationOpt.isEmpty()) {
            return ApiResponse.error("03", "Không tìm thấy bản ghi xóa conversation");
        }

        UserConversation userConversation = userConversationOpt.get();
        
        if (!userConversation.isDeleted()) {
            return ApiResponse.error("04", "Conversation chưa bị xóa");
        }

        // Khôi phục conversation
        userConversation.setDeleted(false);
        userConversation.setDeletedAt(null);
        userConversationRepository.save(userConversation);

        return ApiResponse.success("00", "Đã khôi phục cuộc trò chuyện");
    } catch (Exception e) {
        e.printStackTrace();
        return ApiResponse.error("99", "Lỗi khi khôi phục cuộc trò chuyện: " + e.getMessage());
    }
}

// Khôi phục tất cả tin nhắn đã bị xóa trong conversation (cho trường hợp user muốn khôi phục tin nhắn cũ)
public ApiResponse<String> restoreMessagesInConversation(String conversationId, String userId) {
    try {
        // Kiểm tra conversation có tồn tại không
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isEmpty()) {
            return ApiResponse.error("01", "Không tìm thấy cuộc trò chuyện");
        }

        // Kiểm tra user có tồn tại không
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy người dùng");
        }

        // Khôi phục tất cả tin nhắn đã bị xóa bởi user này
        int restoredCount = userMessageRepository.restoreMessagesForUser(userId, conversationId);
        
        return ApiResponse.success("00", "Đã khôi phục " + restoredCount + " tin nhắn");
    } catch (Exception e) {
        e.printStackTrace();
        return ApiResponse.error("99", "Lỗi khi khôi phục tin nhắn: " + e.getMessage());
    }
}

    // Xóa nhóm (chỉ creator mới được xóa)
    public ApiResponse<String> deleteGroup(String conversationId, String requesterId) {
        Optional<Conversation> optional = conversationRepository.findById(conversationId);
        if (optional.isEmpty()) {
            return ApiResponse.error("04", "Không tìm thấy cuộc trò chuyện");
        }

        Conversation conversation = optional.get();

        // Kiểm tra phải là nhóm
        if (!conversation.isGroup()) {
            return ApiResponse.error("05", "Chỉ nhóm mới được xóa");
        }

        // Kiểm tra quyền: chỉ creator mới được xóa nhóm
        Optional<ConversationMember> requesterMember = conversationMemberRepository.findByConversationIdAndUserId(conversationId, requesterId);
        if (requesterMember.isEmpty() || !requesterMember.get().getRole().equalsIgnoreCase("creator")) {
            return ApiResponse.error("06", "Chỉ người tạo nhóm mới được phép xóa nhóm.");
        }

        // Xóa tất cả thành viên trước
        List<ConversationMember> members = conversationMemberRepository.findByConversationId(conversationId);
        conversationMemberRepository.deleteAll(members);
        
        // Xóa nhóm
        conversationRepository.delete(conversation);

        return ApiResponse.success("00", "Xóa nhóm thành công.");
    }

    public ApiResponse<List<ConversationResponse>> getConversationsByUser(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("03", "Không tìm thấy người dùng: " + userId);
        }

        // Lấy tất cả ConversationMember liên quan đến user này
        List<ConversationMember> myMemberships = conversationMemberRepository.findByUserId(userId);
        List<Conversation> conversations = myMemberships.stream()
                .map(ConversationMember::getConversation)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Lọc ra những conversation đã bị user xóa (chỉ những conversation thực sự đã bị xóa)
        List<String> deletedConversationIds = userConversationRepository.findByUserIdAndIsDeletedTrue(userId)
                .stream()
                .map(UserConversation::getConversationId)
                .collect(Collectors.toList());

        System.out.println("📋 Found " + conversations.size() + " conversations for user " + userId);
        System.out.println("🗑️ Deleted conversation IDs: " + deletedConversationIds);

        conversations = conversations.stream()
                .filter(conv -> !deletedConversationIds.contains(conv.getId()))
                .collect(Collectors.toList());

        System.out.println("✅ After filtering: " + conversations.size() + " conversations remain");

        // Lấy last message cho từng conversation (lọc ra tin nhắn đã bị xóa)
        Map<String, Message> lastMessages = new HashMap<>();
        for (Conversation conv : conversations) {
            // Lấy tất cả tin nhắn đã bị user xóa trong conversation này
            List<String> deletedMessageIds = userMessageRepository.findDeletedMessageIdsByUserAndConversation(userId, conv.getId());
            
            // Lấy tất cả tin nhắn trong conversation, sắp xếp theo thời gian giảm dần
            Pageable pageable = PageRequest.of(0, 100); // Lấy nhiều tin nhắn để tìm tin nhắn cuối cùng không bị xóa
            Page<Message> messagePage = messageRepository.findByConversationId(conv.getId(), pageable);
            
            // Tìm tin nhắn cuối cùng không bị xóa
            Optional<Message> lastNonDeletedMessage = messagePage.getContent().stream()
                    .filter(msg -> !deletedMessageIds.contains(msg.getId()))
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // Sắp xếp theo thời gian giảm dần
                    .findFirst();
            
            if (lastNonDeletedMessage.isPresent()) {
                lastMessages.put(conv.getId(), lastNonDeletedMessage.get());
            }
        }

        List<ConversationResponse> responses = conversations.stream()
                .map(conv -> {
                    Message lastMsg = lastMessages.get(conv.getId());
                    ConversationResponse response = toConversationResponse(conv, userId, lastMsg);

                    if (!conv.isGroup()) {
                        // Tìm thành viên từ repository thay vì conv.getMembers()
                        List<ConversationMember> members = conversationMemberRepository.findByConversationId(conv.getId());

                        List<MemberResponse> memberResponses = members.stream()
                                .filter(cm -> cm.getUser() != null && !cm.getUser().getId().equals(userId))
                                .map(cm -> {
                                    User member = cm.getUser();
                                    return new MemberResponse(
                                            member.getId(),
                                            member.getDisplayName(),
                                            member.getAvatarUrl(),
                                            cm.getRole()
                                    );
                                })
                                .collect(Collectors.toList());

                        // Bảo vệ: nếu null thì set danh sách rỗng
                        if (memberResponses == null) {
                            memberResponses = new ArrayList<>();
                        }

                        response.setMembers(memberResponses);
                    } else {
                        // Group cũng cần trả members để frontend có thể kiểm tra role
                        List<ConversationMember> groupMembers = conversationMemberRepository.findByConversationId(conv.getId());
                        List<MemberResponse> groupMemberResponses = groupMembers.stream()
                                .map(cm -> {
                                    User member = cm.getUser();
                                    return new MemberResponse(
                                            member.getId(),
                                            member.getDisplayName(),
                                            member.getAvatarUrl(),
                                            cm.getRole()
                                    );
                                })
                                .collect(Collectors.toList());
                        response.setMembers(groupMemberResponses);
                    }

                    return response;
                })
                .sorted((a, b) -> {
                    LocalDateTime timeA = a.getLastMessage() != null ? a.getLastMessage().getCreatedAt() : a.getCreatedAt();
                    LocalDateTime timeB = b.getLastMessage() != null ? b.getLastMessage().getCreatedAt() : b.getCreatedAt();
                    return timeB.compareTo(timeA); // Mới nhất lên đầu
                })
                .collect(Collectors.toList());

        return ApiResponse.success("00", "Lấy danh sách cuộc trò chuyện thành công", responses);
    }


    private Optional<Conversation> findOneToOneConversation(String userId1, String userId2) {
        return conversationMemberRepository.findByUserId(userId1).stream()
                .map(ConversationMember::getConversation)
                .filter(conv -> !conv.isGroup())
                .filter(conv -> {
                    List<ConversationMember> members = conversationMemberRepository.findByConversationId(conv.getId());
                    return members.size() == 2 && members.stream().anyMatch(m -> m.getUser().getId().equals(userId2));
                })
                .findFirst();
    }
    private ConversationResponse toConversationResponse(Conversation conversation, String requesterId, Message lastMessage) {
        String name;
        String avatarUrl = null;
        // Lấy danh sách thành viên
        List<ConversationMember> members = conversationMemberRepository.findByConversationId(conversation.getId());

        User partner;

        // Nếu là nhóm
        if (conversation.isGroup()) {
            partner = null;
            name = conversation.getName();
            avatarUrl = conversation.getAvatarUrl();
        } else {
            // Nếu là cuộc trò chuyện 1-1
            partner = members.stream()
                    .map(ConversationMember::getUser)
                    .filter(user -> !user.getId().equals(requesterId))
                    .findFirst()
                    .orElse(null);

            name = (partner != null) ? partner.getDisplayName() : "Cuộc trò chuyện";
            avatarUrl = (partner != null) ? partner.getAvatarUrl() : null;
        }

        // Xử lý thông tin tin nhắn cuối cùng
        LastMessageInfo lastMessageInfo = null;
        if (lastMessage != null) {
            boolean seen = false;
            String statusStr = null;

            // Kiểm tra trạng thái tin nhắn cuối cùng của requester
            MessageStatus status = messageStatusRepository.findByMessageIdAndUserId(lastMessage.getId(), requesterId);
            if (status != null) {
                statusStr = status.getStatus().name();
                seen = status.getStatus() == MessageStatusEnum.SEEN;
            }

            lastMessageInfo = new LastMessageInfo(
                    lastMessage.getContent(),
                    lastMessage.getSender().getDisplayName(),
                    getTimeAgo(lastMessage.getCreatedAt()),
                    statusStr,
                    lastMessage.getCreatedAt(),
                    seen

            );
        }

        // Tạo response
        ConversationResponse response = new ConversationResponse(
                conversation.getId(),
                name,
                conversation.isGroup(),
                avatarUrl,
                conversation.getCreatedAt(),
                lastMessageInfo,
                conversation.getCreatedBy()
        );

        // Kiểm tra trạng thái chặn nếu không phải nhóm
        if (!conversation.isGroup() && partner != null && requesterId != null) {
            userRepository.findById(requesterId).ifPresent(requester -> {
                boolean blockedByMe = friendshipRepository.existsBySenderAndReceiverAndStatus(requester, partner, "blocked");
                boolean blockedMe = friendshipRepository.existsBySenderAndReceiverAndStatus(partner, requester, "blocked");
                response.setBlockedByMe(blockedByMe);
                response.setBlockedMe(blockedMe);
                response.setBlocked(blockedByMe || blockedMe);
            });
        }

        return response;
    }

    private String getTimeAgo(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        if (duration.toMinutes() < 1) return "Vừa xong";
        if (duration.toHours() < 1) return duration.toMinutes() + " phút trước";
        if (duration.toDays() < 1) return duration.toHours() + " giờ trước";
        return duration.toDays() + " ngày trước";
    }

    @Transactional
    public ApiResponse<List<MessageResponse>> getMessagesByConversationId(String conversationId, String userId) {
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);
        if (optionalConversation.isEmpty()) {
            return ApiResponse.error("01", "Không tìm thấy cuộc trò chuyện");
        }

        // Kiểm tra người dùng có trong cuộc trò chuyện không
        boolean isMember = conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId);
        if (!isMember) {
            return ApiResponse.error("02", "Người dùng không thuộc cuộc trò chuyện này");
        }

        // Lấy tất cả tin nhắn và sắp xếp theo thời gian tạo tăng dần
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        List<MessageResponse> messageResponses = messages.stream()
                .map(messageMapper::toMessageResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("00", "Lấy danh sách tin nhắn thành công", messageResponses);
    }



}
