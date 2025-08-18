package com.example.message_service.repository;

import com.example.message_service.model.NeoPostCommentReply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NeoPostCommentReplyRepository extends JpaRepository<NeoPostCommentReply, Long> {
}
