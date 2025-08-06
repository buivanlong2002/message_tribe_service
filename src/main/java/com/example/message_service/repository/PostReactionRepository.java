package com.example.message_service.repository;

import com.example.message_service.model.PostReaction;
import com.example.message_service.model.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

    // Lấy tất cả reactions của một post
    List<PostReaction> findByPostId(Long postId);

    // Lấy reaction của một user trên một post
    Optional<PostReaction> findByPostIdAndUserId(Long postId, String userId);

    // Lấy reactions theo loại
    List<PostReaction> findByPostIdAndReactionType(Long postId, ReactionType reactionType);

    // Lấy reactions của một user
    List<PostReaction> findByUserId(String userId);

    // Kiểm tra user đã reaction chưa
    boolean existsByPostIdAndUserId(Long postId, String userId);

    // Đếm số reactions của một post
    long countByPostId(Long postId);

    // Đếm số reactions theo loại của một post
    long countByPostIdAndReactionType(Long postId, ReactionType reactionType);

    // Đếm số reactions của một user
    long countByUserId(String userId);

    // Xóa reaction của user trên post
    void deleteByPostIdAndUserId(Long postId, String userId);
} 