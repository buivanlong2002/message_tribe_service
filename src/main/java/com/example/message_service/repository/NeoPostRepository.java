package com.example.message_service.repository;

import com.example.message_service.model.NeoPost;
import com.example.message_service.model.NeoPostVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NeoPostRepository extends JpaRepository<NeoPost, String> {
    boolean existsByIdAndUserId(String id, String userId);

    @Query("SELECT p FROM NeoPost p WHERE p.visibility = :visibility AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<NeoPost> findByVisibilityAndDeletedAtIsNull(@Param("visibility") NeoPostVisibility visibility,
            Pageable pageable);

    @Query("SELECT p FROM NeoPost p WHERE p.user.id = :userId AND p.visibility = :visibility AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<NeoPost> findByUserIdAndVisibilityAndDeletedAtIsNull(@Param("userId") String userId,
            @Param("visibility") NeoPostVisibility visibility, Pageable pageable);

    @Query("SELECT p FROM NeoPost p WHERE p.user.id = :userId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<NeoPost> findByUserIdAndDeletedAtIsNull(@Param("userId") String userId, Pageable pageable);
}
