package com.example.message_service.repository;

import com.example.message_service.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    List<Conversation> findByCreatedBy(String createdBy);

    Page<Conversation> findConversationById(String userId, Pageable pageable);

}
