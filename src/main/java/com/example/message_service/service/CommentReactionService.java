package com.example.message_service.service;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.CreateCommentReactionRequest;
import com.example.message_service.dto.response.CommentReactionResponse;
import com.example.message_service.model.CommentReaction;
import com.example.message_service.model.PostComment;
import com.example.message_service.model.ReactionType;
import com.example.message_service.model.User;
import com.example.message_service.repository.CommentReactionRepository;
import com.example.message_service.repository.PostCommentRepository;
import com.example.message_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CommentReactionService {

    @Autowired
    private CommentReactionRepository commentReactionRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private UserRepository userRepository;

    // Tạo reaction cho comment
    @Transactional
    public ApiResponse<CommentReactionResponse> createReaction(Long commentId, String userId, CreateCommentReactionRequest request) {
        try {
            // Kiểm tra comment tồn tại
            Optional<PostComment> commentOpt = postCommentRepository.findById(commentId);
            if (commentOpt.isEmpty()) {
                return ApiResponse.error("01", "Comment không tồn tại");
            }

            // Kiểm tra user tồn tại
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ApiResponse.error("01", "User không tồn tại");
            }

            PostComment comment = commentOpt.get();
            User user = userOpt.get();

            // Kiểm tra user đã reaction comment này chưa
            if (commentReactionRepository.existsByCommentIdAndUserId(commentId, userId)) {
                // Nếu đã có reaction, cập nhật reaction type
                return updateReaction(commentId, userId, request);
            }

            // Tạo reaction mới
            CommentReaction reaction = new CommentReaction();
            reaction.setComment(comment);
            reaction.setUser(user);
            reaction.setReactionType(request.getReactionType());

            CommentReaction savedReaction = commentReactionRepository.save(reaction);

            // Tạo response
            CommentReactionResponse response = new CommentReactionResponse();
            response.setId(savedReaction.getId());
            response.setCommentId(commentId);
            response.setUserId(userId);
            response.setUserName(user.getDisplayName());
            response.setReactionType(savedReaction.getReactionType());
            response.setCreatedAt(savedReaction.getCreatedAt());
            response.setReacted(true);

            return ApiResponse.success("00", "Tạo reaction thành công", response);
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi tạo reaction: " + e.getMessage());
        }
    }

    // Cập nhật reaction
    @Transactional
    public ApiResponse<CommentReactionResponse> updateReaction(Long commentId, String userId, CreateCommentReactionRequest request) {
        try {
            Optional<CommentReaction> reactionOpt = commentReactionRepository.findByCommentIdAndUserId(commentId, userId);
            if (reactionOpt.isEmpty()) {
                return ApiResponse.error("01", "Không tìm thấy reaction");
            }

            CommentReaction reaction = reactionOpt.get();
            reaction.setReactionType(request.getReactionType());
            CommentReaction updatedReaction = commentReactionRepository.save(reaction);

            CommentReactionResponse response = new CommentReactionResponse();
            response.setId(updatedReaction.getId());
            response.setCommentId(commentId);
            response.setUserId(userId);
            response.setUserName(updatedReaction.getUser().getDisplayName());
            response.setReactionType(updatedReaction.getReactionType());
            response.setCreatedAt(updatedReaction.getCreatedAt());
            response.setReacted(true);

            return ApiResponse.success("00", "Cập nhật reaction thành công", response);
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi cập nhật reaction: " + e.getMessage());
        }
    }

    // Xóa reaction
    @Transactional
    public ApiResponse<String> deleteReaction(Long commentId, String userId) {
        try {
            if (!commentReactionRepository.existsByCommentIdAndUserId(commentId, userId)) {
                return ApiResponse.error("01", "Không tìm thấy reaction");
            }

            commentReactionRepository.deleteByCommentIdAndUserId(commentId, userId);
            return ApiResponse.success("00", "Xóa reaction thành công", "Đã xóa reaction");
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi xóa reaction: " + e.getMessage());
        }
    }

    // Kiểm tra user đã reaction comment chưa
    public boolean hasUserReacted(Long commentId, String userId) {
        return commentReactionRepository.existsByCommentIdAndUserId(commentId, userId);
    }

    // Lấy reaction của user cho comment
    public Optional<CommentReaction> getUserReaction(Long commentId, String userId) {
        return commentReactionRepository.findByCommentIdAndUserId(commentId, userId);
    }

    // Đếm số reaction cho comment
    public long getReactionCount(Long commentId) {
        return commentReactionRepository.countByCommentId(commentId);
    }

    // Đếm số reaction theo type cho comment
    public long getReactionCountByType(Long commentId, ReactionType reactionType) {
        return commentReactionRepository.countByCommentIdAndReactionType(commentId, reactionType);
    }

    // Lấy thống kê reaction cho comment
    public Map<ReactionType, Long> getReactionStats(Long commentId) {
        List<CommentReaction> reactions = commentReactionRepository.findByCommentId(commentId);
        Map<ReactionType, Long> stats = new HashMap<>();
        
        for (CommentReaction reaction : reactions) {
            ReactionType type = reaction.getReactionType();
            stats.put(type, stats.getOrDefault(type, 0L) + 1);
        }
        
        return stats;
    }

    // Lấy danh sách reaction của comment
    public List<CommentReaction> getCommentReactions(Long commentId) {
        return commentReactionRepository.findByCommentId(commentId);
    }
}
