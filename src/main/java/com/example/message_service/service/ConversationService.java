package com.example.message_service.service;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.UpdateConversationRequest;
import com.example.message_service.dto.response.ConversationResponse;
import com.example.message_service.dto.response.LastMessageInfo;
import com.example.message_service.dto.response.MemberResponse;
import com.example.message_service.dto.response.MessageResponse;
import com.example.message_service.mapper.MessageMapper;
import com.example.message_service.model.*;
import com.example.message_service.repository.*;

import com.example.message_service.service.util.PushNewMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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


    public Conversation getOrCreateOneToOneConversation(String senderId, String receiverId) {
        Optional<Conversation> existing = findOneToOneConversation(senderId, receiverId);
        if (existing.isPresent()) return existing.get();

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
        Optional<Conversation> optional = conversationRepository.findById(conversationId);
        if (optional.isEmpty()) {
            return ApiResponse.error("04", "Không tìm thấy cuộc trò chuyện với ID: " + conversationId);
        }

        Conversation conversation = optional.get();
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

        // Kiểm tra quyền: chỉ creator mới được phép cập nhật
        Optional<ConversationMember> creatorOpt =
                conversationMemberRepository.findByConversationIdAndUserId(conversationId, conversation.getCreatedBy());

        if (creatorOpt.isEmpty() || !creatorOpt.get().getRole().equalsIgnoreCase("creator")) {
            return ApiResponse.error("06", "Chỉ người tạo nhóm mới được phép đổi ảnh đại diện");
        }

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

        // Lấy last message cho từng conversation
        Map<String, Message> lastMessages = new HashMap<>();
        for (Conversation conv : conversations) {
            Message lastMsg = messageRepository.findTopByConversationIdOrderByCreatedAtDesc(conv.getId());
            if (lastMsg != null) {
                lastMessages.put(conv.getId(), lastMsg);
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
                                            member.getAvatarUrl()
                                    );
                                })
                                .collect(Collectors.toList());

                        // Bảo vệ: nếu null thì set danh sách rỗng
                        if (memberResponses == null) {
                            memberResponses = new ArrayList<>();
                        }

                        response.setMembers(memberResponses);
                    } else {
                        // Group không cần trả members (nếu muốn có thể thêm logic riêng ở đây)
                        response.setMembers(new ArrayList<>()); // Đảm bảo JSON hợp lệ
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
