package com.example.message_service.repository;

import com.example.message_service.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    Message findTopByConversationIdOrderByCreatedAtDesc(String conversationId);

    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    Optional<Message> findByIdAndConversationId(String id, String conversationId);

    List<Message> findBySenderIdAndConversationIdOrderByCreatedAtAsc(String senderId, String conversationId);

    Page<Message> findByConversationId(String conversationId, Pageable pageable);

    @EntityGraph(attributePaths = {"attachments"})
    List<Message> findWithAttachmentsByConversationIdOrderByCreatedAtAsc(String conversationId);

    List<Message> findByConversationIdAndSeenFalse(String conversationId);

    Page<Message> findByConversationIdAndContentContainingIgnoreCase(String conversationId, String keyword, Pageable pageable);
}

