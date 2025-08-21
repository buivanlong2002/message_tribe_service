package com.example.message_service.repository;

import com.example.message_service.model.UserConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserConversationRepository extends JpaRepository<UserConversation, String> {
    
    // Tìm UserConversation theo userId và conversationId
    Optional<UserConversation> findByUserIdAndConversationId(String userId, String conversationId);
    
    // Tìm tất cả conversation đã bị xóa bởi user
    List<UserConversation> findByUserIdAndIsDeletedTrue(String userId);
    
    // Kiểm tra xem user đã xóa conversation chưa
    boolean existsByUserIdAndConversationIdAndIsDeletedTrue(String userId, String conversationId);
    
    // Khôi phục conversation đã bị xóa
    @Modifying
    @Query("UPDATE UserConversation uc SET uc.isDeleted = false, uc.deletedAt = null WHERE uc.userId = :userId AND uc.conversationId = :conversationId")
    void restoreConversation(@Param("userId") String userId, @Param("conversationId") String conversationId);
}
