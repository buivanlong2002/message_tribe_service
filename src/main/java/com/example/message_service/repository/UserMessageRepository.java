package com.example.message_service.repository;

import com.example.message_service.model.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMessageRepository extends JpaRepository<UserMessage, String> {
    
    // Tìm UserMessage theo userId và messageId
    Optional<UserMessage> findByUserIdAndMessageId(String userId, String messageId);
    
    // Tìm tất cả tin nhắn đã bị xóa bởi user trong conversation
    List<UserMessage> findByUserIdAndConversationIdAndIsDeletedTrue(String userId, String conversationId);
    
    // Kiểm tra xem user đã xóa tin nhắn chưa
    boolean existsByUserIdAndMessageIdAndIsDeletedTrue(String userId, String messageId);
    
    // Lấy tất cả messageId đã bị xóa bởi user trong conversation
    @Query("SELECT um.messageId FROM UserMessage um WHERE um.userId = :userId AND um.conversationId = :conversationId AND um.isDeleted = true")
    List<String> findDeletedMessageIdsByUserAndConversation(@Param("userId") String userId, @Param("conversationId") String conversationId);
    
    // Khôi phục tin nhắn đã bị xóa
    @Modifying
    @Transactional
    @Query("UPDATE UserMessage um SET um.isDeleted = false, um.deletedAt = null WHERE um.userId = :userId AND um.conversationId = :conversationId AND um.isDeleted = true")
    int restoreMessagesForUser(@Param("userId") String userId, @Param("conversationId") String conversationId);
}
