package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.CreateCommentReactionRequest;
import com.example.message_service.dto.response.CommentReactionResponse;
import com.example.message_service.model.CommentReaction;
import com.example.message_service.model.NotificationType;
import com.example.message_service.model.User;
import com.example.message_service.service.CommentReactionService;
import com.example.message_service.service.NotificationService;
import com.example.message_service.service.PostCommentService;
import com.example.message_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/comment-reactions")
public class CommentReactionController {

    @Autowired
    private CommentReactionService commentReactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private NotificationService notificationService;

    // Tạo reaction cho comment
    @PostMapping("/create")
    public ApiResponse<CommentReactionResponse> createReaction(
            @RequestParam Long commentId,
            @RequestParam String userId,
            @RequestBody CreateCommentReactionRequest request) {
        
        // Tạm thời bỏ thông báo để tránh lỗi database
        // TODO: Bật lại khi đã sửa xong database schema
        /*
        // Thêm thông báo reaction comment
        User reactor = userService.getUserById(userId);
        User commentOwner = postCommentService.getCommentOwner(commentId);
        
        if (reactor != null && commentOwner != null && !reactor.getId().equals(commentOwner.getId())) {
            notificationService.createNotification(NotificationType.REACTION_COMMENT, commentOwner,
                    reactor.getDisplayName() + " đã phản ứng với bình luận");
        }
        */
        
        return commentReactionService.createReaction(commentId, userId, request);
    }

    // Toggle reaction (tạo nếu chưa có, xóa nếu đã có)
    @PostMapping("/toggle")
    public ApiResponse<Object> toggleReaction(
            @RequestParam Long commentId,
            @RequestParam String userId,
            @RequestBody CreateCommentReactionRequest request) {

        try {
            boolean hasReacted = commentReactionService.hasUserReacted(commentId, userId);
            
            if (hasReacted) {
                ApiResponse<String> deleteResult = commentReactionService.deleteReaction(commentId, userId);
                if (deleteResult.getStatus().isSuccess()) {
                    return ApiResponse.success("00", "Đã bỏ reaction", null);
                } else {
                    return ApiResponse.error(deleteResult.getStatus().getCode(), 
                                           deleteResult.getStatus().getDisplayMessage());
                }
            } else {
                User reactor = userService.getUserById(userId);
                if (reactor == null) {
                    return ApiResponse.error("01", "Không tìm thấy người dùng");
                }

                // Tạm thời bỏ thông báo để tránh lỗi database
                /*
                User commentOwner = postCommentService.getCommentOwner(commentId);
                if (commentOwner != null && !reactor.getId().equals(commentOwner.getId())) {
                    notificationService.createNotification(NotificationType.REACTION_COMMENT, commentOwner,
                            reactor.getDisplayName() + " đã phản ứng với bình luận");
                }
                */
                
                ApiResponse<CommentReactionResponse> createResult = commentReactionService.createReaction(commentId, userId, request);
                if (createResult.getStatus().isSuccess()) {
                    return ApiResponse.success("00", "Đã tạo reaction", createResult.getData());
                } else {
                    return ApiResponse.error(createResult.getStatus().getCode(), 
                                           createResult.getStatus().getDisplayMessage());
                }
            }
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi toggle reaction: " + e.getMessage());
        }
    }

    // Cập nhật reaction
    @PutMapping("/update")
    public ApiResponse<CommentReactionResponse> updateReaction(
            @RequestParam Long commentId,
            @RequestParam String userId,
            @RequestBody CreateCommentReactionRequest request) {
        return commentReactionService.updateReaction(commentId, userId, request);
    }

    // Xóa reaction
    @DeleteMapping("/delete")
    public ApiResponse<String> deleteReaction(
            @RequestParam Long commentId,
            @RequestParam String userId) {
        return commentReactionService.deleteReaction(commentId, userId);
    }

    // Kiểm tra user đã reaction comment chưa
    @GetMapping("/check")
    public ApiResponse<Boolean> checkUserReaction(
            @RequestParam Long commentId,
            @RequestParam String userId) {
        boolean hasReacted = commentReactionService.hasUserReacted(commentId, userId);
        return ApiResponse.success("00", "Kiểm tra reaction thành công", hasReacted);
    }

    // Lấy reaction của user cho comment
    @GetMapping("/user-reaction")
    public ApiResponse<Object> getUserReaction(
            @RequestParam Long commentId,
            @RequestParam String userId) {
        Optional<CommentReaction> reaction = commentReactionService.getUserReaction(commentId, userId);
        if (reaction.isPresent()) {
            CommentReactionResponse response = new CommentReactionResponse();
            response.setId(reaction.get().getId());
            response.setCommentId(commentId);
            response.setUserId(userId);
            response.setUserName(reaction.get().getUser().getDisplayName());
            response.setReactionType(reaction.get().getReactionType());
            response.setCreatedAt(reaction.get().getCreatedAt());
            response.setReacted(true);
            return ApiResponse.success("00", "Lấy reaction thành công", response);
        } else {
            return ApiResponse.success("00", "User chưa reaction", null);
        }
    }

    // Đếm số reaction cho comment
    @GetMapping("/count/{commentId}")
    public ApiResponse<Long> getReactionCount(@PathVariable Long commentId) {
        long count = commentReactionService.getReactionCount(commentId);
        return ApiResponse.success("00", "Đếm reaction thành công", count);
    }

    // Lấy thống kê reaction cho comment
    @GetMapping("/stats/{commentId}")
    public ApiResponse<Map<String, Long>> getReactionStats(@PathVariable Long commentId) {
        Map<String, Long> stats = new java.util.HashMap<>();
        commentReactionService.getReactionStats(commentId).forEach((type, count) -> 
            stats.put(type.name(), count));
        return ApiResponse.success("00", "Lấy thống kê reaction thành công", stats);
    }

    // Lấy danh sách reaction của comment
    @GetMapping("/comment/{commentId}")
    public ApiResponse<List<CommentReactionResponse>> getCommentReactions(@PathVariable Long commentId) {
        List<CommentReaction> reactions = commentReactionService.getCommentReactions(commentId);
        List<CommentReactionResponse> responses = reactions.stream()
            .map(reaction -> {
                CommentReactionResponse response = new CommentReactionResponse();
                response.setId(reaction.getId());
                response.setCommentId(commentId);
                response.setUserId(reaction.getUser().getId());
                response.setUserName(reaction.getUser().getDisplayName());
                response.setReactionType(reaction.getReactionType());
                response.setCreatedAt(reaction.getCreatedAt());
                response.setReacted(true);
                return response;
            })
            .toList();
        return ApiResponse.success("00", "Lấy danh sách reaction thành công", responses);
    }
}
