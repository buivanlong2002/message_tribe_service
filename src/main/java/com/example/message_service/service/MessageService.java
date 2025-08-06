package com.example.message_service.service;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.response.MessageResponse;
import com.example.message_service.dto.response.SeenByResponse;
import com.example.message_service.mapper.MessageMapper;
import com.example.message_service.model.*;
import com.example.message_service.repository.*;
import com.example.message_service.service.util.PushNewMessage;
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
    @Autowired   private  SimpMessagingTemplate messagingTemplate;

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

        // 12. Trả về kết quả
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
        if (!conversationRepository.existsById(conversationId)) {
            return ApiResponse.error("01", "Không tìm thấy cuộc trò chuyện với ID: " + conversationId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Message> messagePage = messageRepository.findByConversationId(conversationId, pageable);
        List<Message> messages = messagePage.getContent();

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

        Collections.reverse(responseList);

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


}