package com.example.message_service.repository;

import com.example.message_service.model.NeoPostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NeoPostCommentRepository extends JpaRepository<NeoPostComment, Long> {
    boolean existsByIdAndUserId(String id, String userId);
}
