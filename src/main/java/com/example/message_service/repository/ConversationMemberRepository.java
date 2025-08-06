package com.example.message_service.repository;

import com.example.message_service.model.ConversationMember;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, String> {

    List<ConversationMember> findByConversationId(String conversationId);

    List<ConversationMember> findByUserId(String userId);

    @Transactional
    void deleteByConversationIdAndUserId(String conversationId, String userId);

    boolean existsByConversationIdAndUserId(String conversationId, String userId);

    Optional<ConversationMember> findByConversationIdAndUserId(String conversationId, String userId);

}
