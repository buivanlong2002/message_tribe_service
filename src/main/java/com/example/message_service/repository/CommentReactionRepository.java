package com.example.message_service.repository;

import com.example.message_service.model.CommentReaction;
import com.example.message_service.model.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {

    // Kiểm tra user đã reaction comment chưa
    boolean existsByCommentIdAndUserId(Long commentId, String userId);

    // Lấy reaction của user cho comment
    Optional<CommentReaction> findByCommentIdAndUserId(Long commentId, String userId);

    // Đếm số reaction theo type cho comment
    long countByCommentIdAndReactionType(Long commentId, ReactionType reactionType);

    // Đếm tổng số reaction cho comment
    long countByCommentId(Long commentId);

    // Lấy tất cả reaction của comment
    List<CommentReaction> findByCommentId(Long commentId);

    // Xóa reaction của user cho comment
    void deleteByCommentIdAndUserId(Long commentId, String userId);

    // Lấy danh sách user đã reaction comment
    @Query("SELECT cr.user FROM CommentReaction cr WHERE cr.comment.id = :commentId")
    List<Object[]> findUsersByCommentId(@Param("commentId") Long commentId);

    // Lấy thống kê reaction theo type cho comment
    @Query("SELECT cr.reactionType, COUNT(cr) FROM CommentReaction cr WHERE cr.comment.id = :commentId GROUP BY cr.reactionType")
    List<Object[]> getReactionStatsByCommentId(@Param("commentId") Long commentId);
}
