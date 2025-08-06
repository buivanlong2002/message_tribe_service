package com.example.message_service.repository;

import com.example.message_service.model.MessageStatus;
import com.example.message_service.model.MessageStatusEnum;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, String> {

    List<MessageStatus> findByMessageId(String messageId);

    MessageStatus findByMessageIdAndUserId(String messageId, String userId);

    List<MessageStatus> findByMessageIdInAndStatus(Collection<String> message_id, MessageStatusEnum status);


    @Modifying
    @Query(value = """
                UPDATE message_status ms
                JOIN message m ON ms.message_id = m.id
                SET ms.status = 'SEEN',
                    ms.updated_at = CURRENT_TIMESTAMP
                WHERE ms.user_id = :userId
                  AND m.conversation_id = :conversationId
                  AND ms.status <> 'SEEN'
            """, nativeQuery = true)
    Integer markAllAsSeen(@Param("conversationId") String conversationId,
                      @Param("userId") String userId);

    List<MessageStatus> findByUserIdAndStatus(String userId, String status);
}
