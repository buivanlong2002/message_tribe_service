package com.example.message_service.repository;

import com.example.message_service.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {
    List<Attachment> findByMessageId(String messageId);

    List<Attachment> findByMessage_Conversation_Id(String conversationId);

}
