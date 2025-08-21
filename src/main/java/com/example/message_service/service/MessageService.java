package com.example.message_service.service;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.response.MessageResponse;
import com.example.message_service.dto.response.SeenByResponse;
import com.example.message_service.mapper.MessageMapper;
import com.example.message_service.model.*;
import com.example.message_service.repository.*;
import com.example.message_service.service.util.PushNewMessage;
import com.example.message_service.service.CallHistoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired private MessageRepository messageRepository;
    @Autowired private ConversationRepository conversationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MessageMapper messageMapper;
    @Autowired private ConversationService conversationService;
    @Autowired private PushNewMessage pushNewMessage;
    @Autowired private ConversationMemberRepository conversationMemberRepository;
    @Autowired private MessageStatusRepository messageStatusRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private CallHistoryService callHistoryService;
    @Autowired private UserMessageRepository userMessageRepository;

    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024;

    public ApiResponse<MessageResponse> sendMessage(
            String senderId,
            String conversationId,
            String receiverId,
            MultipartFile[] files,
            MessageType messageType,
            String content,
            String replyToId
    ) {
        // 1. Kiểm tra người gửi
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người gửi"));

        // 2. Lấy hoặc tạo cuộc trò chuyện
        Conversation conversation;
        if (conversationId != null && !conversationId.isBlank()) {
            conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy cuộc trò chuyện"));
        } else {
            if (receiverId == null || receiverId.isBlank()) {
                return ApiResponse.error("05", "Thiếu receiverId để tạo cuộc trò chuyện 1-1");
            }
            conversation = conversationService.getOrCreateOneToOneConversation(senderId, receiverId);
        }

        // 3. Tạo tin nhắn
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setSender(sender);
        message.setConversation(conversation);
        message.setMessageType(messageType);
        message.setContent(content != null ? content : "");
        message.setCreatedAt(LocalDateTime.now());
        message.setEdited(false);
        message.setSeen(false);
        message.setRecalled(false);

        // 4. Nếu là tin nhắn trả lời
        if (replyToId != null && !replyToId.isBlank()) {
            Message replyTo = messageRepository.findById(replyToId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn để trả lời"));
            message.setReplyTo(replyTo);
        }

        // 5. Xử lý file đính kèm
        List<Attachment> attachments = new ArrayList<>();
        if (files != null && files.length > 0) {
            try {
                for (MultipartFile file : files) {
                    if (file.isEmpty()) continue;

                    String contentType = file.getContentType();
                    String folder = getFolderByContentType(contentType);

                    if ("video".equals(folder) && file.getSize() > MAX_VIDEO_SIZE) {
                        return ApiResponse.error("06", "Video quá lớn. Tối đa 100MB.");
                    }

                    Path uploadPath = Paths.get("uploads", folder);
                    Files.createDirectories(uploadPath);

                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
                    String fileUrl = "/uploads/" + folder + "/" + encodedName;

                    Attachment attachment = new Attachment();
                    attachment.setId(UUID.randomUUID().toString());
                    attachment.setFileUrl(fileUrl);
                    attachment.setFileType(contentType);
                    attachment.setFileSize(file.getSize());
                    attachment.setMessage(message);
                    attachment.setOriginalFileName(
                            Optional.ofNullable(file.getOriginalFilename()).orElse("unknown")
                    );

                    attachments.add(attachment);
                }

                if (!attachments.isEmpty()) {
                    message.setAttachments(attachments);
                }
            } catch (IOException e) {
                return ApiResponse.error("99", "Lỗi khi upload file: " + e.getMessage());
            }
        }

        // 6. Lưu tin nhắn
        Message savedMessage = messageRepository.save(message);

        // 7. Lưu trạng thái tin nhắn cho từng thành viên trong cuộc trò chuyện
        List<ConversationMember> members = conversationMemberRepository.findByConversationId(conversation.getId());
        List<MessageStatus> statusList = new ArrayList<>();

        for (ConversationMember member : members) {
            MessageStatus status = new MessageStatus();
            status.setMessage(savedMessage);
            status.setUser(member.getUser());

            if (member.getUser().getId().equals(senderId)) {
                status.setStatus(MessageStatusEnum.SEEN);
            } else {
                status.setStatus(MessageStatusEnum.SENT);
            }

            status.setUpdatedAt(LocalDateTime.now());
            statusList.add(status);
        }

        messageStatusRepository.saveAll(statusList);

        // 8. Reload message từ DB để tránh lỗi lazy/null sender
        savedMessage = messageRepository.findById(savedMessage.getId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn sau khi lưu"));

        // 9. Lấy trạng thái đã lưu
        List<MessageStatus> statusListSaved = messageStatusRepository.findByMessageId(savedMessage.getId());
        List<SeenByResponse> seenByList = statusListSaved.stream()
                .filter(s -> s.getStatus() == MessageStatusEnum.SEEN)
                .map(s -> new SeenByResponse(
                        s.getUser().getId(),
                        s.getUser().getDisplayName(),
                        s.getUser().getAvatarUrl(),
                        s.getUpdatedAt()
                ))
                .collect(Collectors.toList());

        // 10. Mapping sang DTO
        MessageResponse response = messageMapper.toMessageResponse(savedMessage, seenByList);

        // 11. Đẩy socket đến các thành viên
        for (ConversationMember member : members) {
            String memberId = member.getUser().getId();
            pushNewMessage.pushUpdatedConversationsToUser(memberId);

            if (!memberId.equals(senderId)) {
                pushNewMessage.pushUpdatedConversationsToMemBer(conversation.getId(), memberId);
            }
        }

        // 12. Gửi tin nhắn mới đến tất cả thành viên trong conversation
        pushNewMessage.pushNewMessageToConversation(conversation.getId(), response);

        // 13. Trả về kết quả
        return ApiResponse.success("00", "Gửi tin nhắn thành công", response);
    }


    private String getFolderByContentType(String contentType) {
        if (contentType == null) return "file";
        if (contentType.startsWith("image/")) return "image";
        if (contentType.startsWith("video/")) return "video";
        return "file";
    }

    @Transactional
    public ApiResponse<List<MessageResponse>> getMessagesByConversation(String conversationId, int page, int size) {
        return getMessagesByConversation(conversationId, page, size, null);
    }

    @Transactional
    public ApiResponse<List<MessageResponse>> getMessagesByConversation(String conversationId, int page, int size, String userId) {
        if (!conversationRepository.existsById(conversationId)) {
            return ApiResponse.error("01", "Không tìm thấy cuộc trò chuyện với ID: " + conversationId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Message> messagePage = messageRepository.findByConversationId(conversationId, pageable);
        List<Message> messages = messagePage.getContent();

        // Lọc ra những tin nhắn đã bị user xóa
        if (userId != null) {
            List<String> deletedMessageIds = userMessageRepository.findDeletedMessageIdsByUserAndConversation(userId, conversationId);
            messages = messages.stream()
                    .filter(message -> !deletedMessageIds.contains(message.getId()))
                    .collect(Collectors.toList());
        }

        // Lấy toàn bộ messageId
        List<String> messageIds = messages.stream()
                .map(Message::getId)
                .toList();

        // Lấy danh sách những người đã xem (status = SEEN)
        List<MessageStatus> allSeenStatuses = messageStatusRepository
                .findByMessageIdInAndStatus(messageIds, MessageStatusEnum.SEEN);

        // Gom theo messageId
        Map<String, List<SeenByResponse>> seenMap = allSeenStatuses.stream()
                .collect(Collectors.groupingBy(
                        ms -> ms.getMessage().getId(),
                        Collectors.mapping(ms -> new SeenByResponse(
                                ms.getUser().getId(),
                                ms.getUser().getDisplayName(),
                                ms.getUser().getAvatarUrl(),
                                ms.getUpdatedAt()
                        ), Collectors.toList())
                ));

        // Map sang MessageResponse
        List<MessageResponse> responseList = messages.stream()
                .map(message -> {
                    List<SeenByResponse> seenStatuses = seenMap.getOrDefault(message.getId(), List.of());
                    return messageMapper.toMessageResponse(message, seenStatuses);
                })
                .collect(Collectors.toList());

        // Lấy call history cho conversation này
        List<CallHistory> callHistoryList = callHistoryService.getCallHistoryByConversation(conversationId);
        
        // Chuyển đổi call history thành MessageResponse với type CALL
        List<MessageResponse> callHistoryResponses = callHistoryList.stream()
                .map(callHistory -> messageMapper.toCallHistoryMessageResponse(callHistory))
                .collect(Collectors.toList());

        // Kết hợp messages và call history
        responseList.addAll(callHistoryResponses);

        // Sắp xếp lại theo thời gian (từ cũ đến mới)
        responseList.sort((a, b) -> {
            LocalDateTime timeA = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.now();
            LocalDateTime timeB = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.now();
            return timeA.compareTo(timeB);
        });

        return ApiResponse.success("00", "Lấy danh sách tin nhắn thành công", responseList);
    }





    public Optional<Message> getMessageByIdAndConversation(String id, String conversationId) {
        return messageRepository.findByIdAndConversationId(id, conversationId);
    }

    public ApiResponse<List<MessageResponse>> getMessagesBySenderAndConversation(String conversationId, String senderId) {
        if (!conversationRepository.existsById(conversationId)) {
            return ApiResponse.error("01", "Không tìm thấy cuộc trò chuyện");
        }

        List<Message> messages = messageRepository.findBySenderIdAndConversationIdOrderByCreatedAtAsc(senderId, conversationId);
        List<MessageResponse> responseList = messages.stream()
                .map(messageMapper::toMessageResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("00", "Lấy tin nhắn theo người gửi thành công", responseList);
    }
    public ApiResponse<MessageResponse> editMessage(String messageId, String newContent, String conversationId) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            return ApiResponse.error("04", "Không tìm thấy tin nhắn để chỉnh sửa");
        }

        Message message = messageOpt.get();
        message.setContent(newContent);
        message.setEdited(true);

        Message updated = messageRepository.save(message);
        MessageResponse response = messageMapper.toMessageResponse(updated);

        // Gửi thông báo đến tất cả thành viên của cuộc trò chuyện
        List<ConversationMember> members = conversationMemberRepository.findByConversationId(conversationId);
        for (ConversationMember member : members) {
            // Gửi thông báo cập nhật message đến từng thành viên
            messagingTemplate.convertAndSend(
                    "/topic/message-updated/" + member.getId(),
                    response
            );

            // Đồng thời cập nhật danh sách cuộc trò chuyện nếu cần
            pushNewMessage.pushUpdatedConversationsToMemBer(conversationId, member.getUser().getId());
        }

        return ApiResponse.success("00", "Chỉnh sửa thành công", response);
    }


    public void markAsSeen(String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn"));
        message.setSeen(true);
        messageRepository.save(message);
    }

    public void recallMessage(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn"));

        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền thu hồi tin nhắn này.");
        }

        message.setRecalled(true);
        message.setContent("Tin nhắn đã được thu hồi");
        messageRepository.save(message);
    }

    public ApiResponse<List<MessageResponse>> searchMessagesByKeyword(String conversationId, String keyword, int page, int size) {
        if (conversationId == null || conversationId.isBlank()) {
            return ApiResponse.error("02", "Thiếu conversationId");
        }

        if (!conversationRepository.existsById(conversationId)) {
            return ApiResponse.error("01", "Không tìm thấy cuộc trò chuyện");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Message> messagePage = messageRepository
                .findByConversationIdAndContentContainingIgnoreCase(conversationId, keyword, pageable);

        List<MessageResponse> responseList = messagePage.getContent().stream()
                .map(messageMapper::toMessageResponse)
                .toList();

        return ApiResponse.success("00", "Tìm kiếm thành công", responseList);
    }

    /**
     * Chuyển tiếp tin nhắn đến một hoặc nhiều cuộc trò chuyện
     */
    public ApiResponse<List<MessageResponse>> forwardMessage(String messageId, String senderId, List<String> targetConversationIds) {
        // Kiểm tra tin nhắn gốc
        Message originalMessage = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn để chuyển tiếp"));

        // Kiểm tra người gửi
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người gửi"));

        List<MessageResponse> forwardedMessages = new ArrayList<>();

        for (String targetConversationId : targetConversationIds) {
            // Kiểm tra cuộc trò chuyện đích
            Conversation targetConversation = conversationRepository.findById(targetConversationId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy cuộc trò chuyện đích: " + targetConversationId));

            // Kiểm tra người gửi có trong cuộc trò chuyện đích không
            boolean isMember = conversationMemberRepository.existsByConversationIdAndUserId(targetConversationId, senderId);
            if (!isMember) {
                continue; // Bỏ qua nếu không phải thành viên
            }

            // Tạo tin nhắn chuyển tiếp
            Message forwardedMessage = new Message();
            forwardedMessage.setId(UUID.randomUUID().toString());
            forwardedMessage.setSender(sender);
            forwardedMessage.setConversation(targetConversation);
            forwardedMessage.setMessageType(originalMessage.getMessageType());
            forwardedMessage.setContent(originalMessage.getContent());
            forwardedMessage.setCreatedAt(LocalDateTime.now());
            forwardedMessage.setEdited(false);
            forwardedMessage.setSeen(false);
            forwardedMessage.setRecalled(false);

            // Chuyển tiếp file đính kèm nếu có
            if (originalMessage.getAttachments() != null && !originalMessage.getAttachments().isEmpty()) {
                List<Attachment> forwardedAttachments = new ArrayList<>();
                for (Attachment originalAttachment : originalMessage.getAttachments()) {
                    Attachment newAttachment = new Attachment();
                    newAttachment.setId(UUID.randomUUID().toString());
                    newAttachment.setMessage(forwardedMessage);
                    newAttachment.setOriginalFileName(originalAttachment.getOriginalFileName());
                    newAttachment.setFileUrl(originalAttachment.getFileUrl());
                    newAttachment.setFileType(originalAttachment.getFileType());
                    newAttachment.setFileSize(originalAttachment.getFileSize());
                    forwardedAttachments.add(newAttachment);
                }
                forwardedMessage.setAttachments(forwardedAttachments);
            }

            // Lưu tin nhắn chuyển tiếp
            Message savedMessage = messageRepository.save(forwardedMessage);

            // Tạo MessageStatus cho tin nhắn chuyển tiếp
            List<ConversationMember> members = conversationMemberRepository.findByConversationId(targetConversationId);
            for (ConversationMember member : members) {
                MessageStatus status = new MessageStatus();
                status.setId(UUID.randomUUID().toString());
                status.setMessage(savedMessage);
                status.setUser(member.getUser());
                status.setStatus(MessageStatusEnum.SENT);
                messageStatusRepository.save(status);
            }

            // Mapping sang DTO
            MessageResponse response = messageMapper.toMessageResponse(savedMessage);
            forwardedMessages.add(response);

            // Gửi thông báo đến các thành viên của cuộc trò chuyện đích
            pushNewMessage.pushNewMessageToConversation(targetConversationId, response);
            
            // Cập nhật danh sách cuộc trò chuyện cho các thành viên
            for (ConversationMember member : members) {
                if (!member.getUser().getId().equals(senderId)) {
                    pushNewMessage.pushUpdatedConversationsToMemBer(targetConversationId, member.getUser().getId());
                }
            }
        }

        // Cập nhật trạng thái tin nhắn gốc thành đã xem cho người chuyển tiếp
        try {
            System.out.println("🔄 Đang cập nhật trạng thái tin nhắn gốc - MessageId: " + messageId + ", SenderId: " + senderId);
            System.out.println("🔄 Tin nhắn gốc thuộc conversation: " + originalMessage.getConversation().getId());
            System.out.println("🔄 Người gửi tin nhắn gốc: " + originalMessage.getSender().getId());
            
            // 1. Đánh dấu tin nhắn gốc là đã xem cho người chuyển tiếp
            MessageStatus originalMessageStatus = messageStatusRepository.findByMessageIdAndUserId(messageId, senderId);
            if (originalMessageStatus != null) {
                System.out.println("✅ Tìm thấy MessageStatus cũ, đang cập nhật...");
                originalMessageStatus.setStatus(MessageStatusEnum.SEEN);
                originalMessageStatus.setUpdatedAt(LocalDateTime.now());
                messageStatusRepository.save(originalMessageStatus);
                System.out.println("✅ Đã cập nhật MessageStatus thành SEEN");
            } else {
                System.out.println("⚠️ Không tìm thấy MessageStatus, đang tạo mới...");
                // Tạo mới MessageStatus nếu chưa có
                MessageStatus newStatus = new MessageStatus();
                newStatus.setId(UUID.randomUUID().toString());
                newStatus.setMessage(originalMessage);
                newStatus.setUser(sender);
                newStatus.setStatus(MessageStatusEnum.SEEN);
                newStatus.setUpdatedAt(LocalDateTime.now());
                messageStatusRepository.save(newStatus);
                System.out.println("✅ Đã tạo mới MessageStatus với status SEEN");
            }
            
            // 2. Đánh dấu tất cả tin nhắn chưa xem trong các conversation đích thành đã xem
            for (String targetConversationId : targetConversationIds) {
                System.out.println("🔄 Đang đánh dấu tất cả tin nhắn chưa xem trong conversation: " + targetConversationId);
                
                // Sử dụng method markAllAsSeen có sẵn
                Integer updatedCount = messageStatusRepository.markAllAsSeen(targetConversationId, senderId);
                System.out.println("✅ Đã đánh dấu " + updatedCount + " tin nhắn thành đã xem trong conversation: " + targetConversationId);
            }
            
            // Gửi thông báo cập nhật trạng thái
            System.out.println("🔄 Đang gửi WebSocket notification...");
            pushNewMessage.pushUpdatedConversationsToUser(senderId);
            System.out.println("✅ Đã gửi WebSocket notification");
            
        } catch (Exception e) {
            // Log lỗi nhưng không ảnh hưởng đến việc chuyển tiếp
            System.err.println("❌ Lỗi khi cập nhật trạng thái tin nhắn gốc: " + e.getMessage());
            e.printStackTrace();
        }

        return ApiResponse.success("00", "Chuyển tiếp tin nhắn thành công", forwardedMessages);
    }

    /**
     * Xóa tin nhắn (chỉ xóa cho người dùng cụ thể)
     */
    public ApiResponse<String> deleteMessage(String messageId, String userId, String conversationId) {
        // Kiểm tra tin nhắn
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn"));

        // Kiểm tra cuộc trò chuyện
        if (!conversationRepository.existsById(conversationId)) {
            return ApiResponse.error("01", "Không tìm thấy cuộc trò chuyện");
        }

        // Kiểm tra người dùng có trong cuộc trò chuyện không
        boolean isMember = conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId);
        if (!isMember) {
            return ApiResponse.error("03", "Bạn không có quyền xóa tin nhắn trong cuộc trò chuyện này");
        }

        // Kiểm tra xem đã có UserMessage record chưa
        Optional<UserMessage> existingUserMessage = userMessageRepository.findByUserIdAndMessageId(userId, messageId);
        
        UserMessage userMessage;
        if (existingUserMessage.isPresent()) {
            userMessage = existingUserMessage.get();
        } else {
            // Tạo mới UserMessage record nếu chưa có
            userMessage = new UserMessage();
            userMessage.setId(UUID.randomUUID().toString());
            userMessage.setUserId(userId);
            userMessage.setMessageId(messageId);
            userMessage.setConversationId(conversationId);
        }

        // Đánh dấu tin nhắn đã bị xóa
        userMessage.setDeleted(true);
        userMessage.setDeletedAt(LocalDateTime.now());
        userMessageRepository.save(userMessage);

        // Gửi thông báo đến các thành viên khác trong cuộc trò chuyện
        List<ConversationMember> members = conversationMemberRepository.findByConversationId(conversationId);
        for (ConversationMember member : members) {
            if (!member.getUser().getId().equals(userId)) {
                // Gửi thông báo xóa tin nhắn
                Map<String, Object> deleteNotification = new HashMap<>();
                deleteNotification.put("type", "MESSAGE_DELETED");
                deleteNotification.put("messageId", messageId);
                deleteNotification.put("conversationId", conversationId);
                deleteNotification.put("deletedBy", userId);
                
                messagingTemplate.convertAndSend(
                    "/topic/message-deleted/" + member.getUser().getId(),
                    deleteNotification
                );
            }
        }

        return ApiResponse.success("00", "Xóa tin nhắn thành công", "Tin nhắn đã được xóa");
    }

    /**
     * Xóa tin nhắn vĩnh viễn (chỉ người gửi mới có quyền)
     */
    public ApiResponse<String> permanentlyDeleteMessage(String messageId, String userId) {
        // Kiểm tra tin nhắn
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn"));

        // Kiểm tra quyền (chỉ người gửi mới có quyền xóa vĩnh viễn)
        if (!message.getSender().getId().equals(userId)) {
            return ApiResponse.error("03", "Bạn không có quyền xóa vĩnh viễn tin nhắn này");
        }

        // Xóa tất cả UserMessage records liên quan
        List<UserMessage> userMessages = userMessageRepository.findByMessageId(messageId);
        userMessageRepository.deleteAll(userMessages);

        // Xóa tất cả MessageStatus records liên quan
        List<MessageStatus> messageStatuses = messageStatusRepository.findByMessageId(messageId);
        messageStatusRepository.deleteAll(messageStatuses);

        // Xóa tin nhắn
        messageRepository.delete(message);

        // Gửi thông báo đến tất cả thành viên trong cuộc trò chuyện
        List<ConversationMember> members = conversationMemberRepository.findByConversationId(message.getConversation().getId());
        for (ConversationMember member : members) {
            if (!member.getUser().getId().equals(userId)) {
                // Gửi thông báo xóa vĩnh viễn tin nhắn
                Map<String, Object> deleteNotification = new HashMap<>();
                deleteNotification.put("type", "MESSAGE_PERMANENTLY_DELETED");
                deleteNotification.put("messageId", messageId);
                deleteNotification.put("conversationId", message.getConversation().getId());
                deleteNotification.put("deletedBy", userId);
                
                messagingTemplate.convertAndSend(
                    "/topic/message-permanently-deleted/" + member.getUser().getId(),
                    deleteNotification
                );
            }
        }

        return ApiResponse.success("00", "Xóa vĩnh viễn tin nhắn thành công", "Tin nhắn đã được xóa vĩnh viễn");
    }


}