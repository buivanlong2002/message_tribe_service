package com.example.message_service.repository;

import com.example.message_service.model.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    // Lấy tất cả comments của một post
    List<PostComment> findByPostIdOrderByCreatedAtAsc(Long postId);

    // Lấy comments gốc (không có parent) của một post
    List<PostComment> findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(Long postId);

    // Lấy replies của một comment
    List<PostComment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    // Lấy comments của một user
    Page<PostComment> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Lấy comments của một post với pagination
    Page<PostComment> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

    // Tìm kiếm comments theo content
    @Query("SELECT pc FROM PostComment pc WHERE pc.content LIKE %:keyword% AND pc.deletedAt IS NULL")
    Page<PostComment> findByContentContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    // Lấy comments chưa bị xóa
    Page<PostComment> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    // Đếm số comments của một post
    long countByPostId(Long postId);

    // Đếm số comments của một user
    long countByUserId(String userId);
} 