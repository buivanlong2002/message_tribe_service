package com.example.message_service.repository;

import com.example.message_service.model.Post;
import com.example.message_service.model.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Lấy tất cả posts của một user
    Page<Post> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Lấy posts theo visibility
    Page<Post> findByVisibilityOrderByCreatedAtDesc(Visibility visibility, Pageable pageable);

    // Lấy posts của user theo visibility
    Page<Post> findByUserIdAndVisibilityOrderByCreatedAtDesc(String userId, Visibility visibility, Pageable pageable);

    // Tìm kiếm posts theo content
    @Query("SELECT p FROM Post p WHERE p.content LIKE %:keyword% AND p.deletedAt IS NULL")
    Page<Post> findByContentContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    // Lấy posts chưa bị xóa
    Page<Post> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    // Lấy posts của user chưa bị xóa
    Page<Post> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Đếm số posts của user
    long countByUserId(String userId);

    // Đếm số posts theo visibility
    long countByVisibility(Visibility visibility);
} 