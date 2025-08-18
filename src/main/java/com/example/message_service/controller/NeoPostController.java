package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.*;
import com.example.message_service.dto.response.*;
import com.example.message_service.service.NeoPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/neo-posts")
@RequiredArgsConstructor
@Slf4j
public class NeoPostController {

    private final NeoPostService neoPostService;

    // ==================== POST APIs ====================

    /**
     * Người dùng hiện tại đăng bài viết
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<NeoPostResponse>> createPost(
            @RequestBody CreatePostRequest request,
            @RequestPart(required = false) MultipartFile[] mediaFiles) {
        try {
            ApiResponse<NeoPostResponse> response = neoPostService.createPost(request, mediaFiles);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi tạo bài viết: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("01", "Lỗi khi tạo bài viết: " + e.getMessage()));
        }
    }

    /**
     * Người dùng hiện tại sửa bài viết của mình
     */
    @PutMapping("/{postId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<NeoPostResponse>> updatePost(
            @PathVariable String postId,
            @RequestBody UpdatePostRequest request) {
        try {
            ApiResponse<NeoPostResponse> response = neoPostService.updatePost(postId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật bài viết: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("01", "Lỗi khi cập nhật bài viết: " + e.getMessage()));
        }
    }

    /**
     * Người dùng hiện tại xóa bài viết của mình
     */
    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deletePost(@PathVariable String postId) {
        try {
            ApiResponse<String> response = neoPostService.deletePost(postId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi xóa bài viết: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("01", "Lỗi khi xóa bài viết: " + e.getMessage()));
        }
    }

    /**
     * Lấy tất cả bài viết public (newsfeed) có phân trang
     */
    @GetMapping("/newsfeed")
    public ResponseEntity<ApiResponse<Page<NeoPostResponse>>> getNewsfeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            ApiResponse<Page<NeoPostResponse>> response = neoPostService.getNewsfeed(page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi lấy newsfeed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("01", "Lỗi khi lấy newsfeed: " + e.getMessage()));
        }
    }

    /**
     * Lấy tất cả bài viết public của một user (tường nhà người khác) có phân trang
     */
    @GetMapping("/user/{userId}/public")
    public ResponseEntity<ApiResponse<Page<NeoPostResponse>>> getPublicPostsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            ApiResponse<Page<NeoPostResponse>> response = neoPostService.getPublicPostsByUser(userId, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi lấy bài viết public của user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("01", "Lỗi khi lấy bài viết: " + e.getMessage()));
        }
    }

    /**
     * Lấy tất cả bài viết (public + private) của user hiện tại (tường mình) có phân
     * trang
     */
    @GetMapping("/my-posts")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<NeoPostResponse>>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            ApiResponse<Page<NeoPostResponse>> response = neoPostService.getMyPosts(page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi lấy bài viết của mình: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("01", "Lỗi khi lấy bài viết: " + e.getMessage()));
        }
    }

    // ==================== COMMENT APIs ====================

    /**
     * Người dùng hiện tại comment vào bài viết
     */
    @PostMapping("/{postId}/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable String postId,
            @RequestBody CreateCommentRequest request) {
        try {
            ApiResponse<CommentResponse> response = neoPostService.createComment(postId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi tạo comment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("01", "Lỗi khi tạo comment: " + e.getMessage()));
        }
    }

    /**
     * Người dùng hiện tại sửa comment của mình
     */
    @PutMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable String commentId,
            @RequestBody UpdateCommentRequest request) {
        try {
            ApiResponse<CommentResponse> response = neoPostService.updateComment(commentId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật comment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("01", "Lỗi khi cập nhật comment: " + e.getMessage()));
        }
    }

    /**
     * Người dùng hiện tại xóa comment của mình
     */
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deleteComment(@PathVariable String commentId) {
        try {
            ApiResponse<String> response = neoPostService.deleteComment(commentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi xóa comment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("01", "Lỗi khi xóa comment: " + e.getMessage()));
        }
    }

    // ==================== REPLY APIs ====================

    /**
     * Người dùng hiện tại reply vào comment
     */
    @PostMapping("/comments/{commentId}/replies")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ReplyResponse>> createReply(
            @PathVariable String commentId,
            @RequestBody CreateReplyRequest request) {
        try {
            ApiResponse<ReplyResponse> response = neoPostService.createReply(commentId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi tạo reply: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("01", "Lỗi khi tạo reply: " + e.getMessage()));
        }
    }

    /**
     * Người dùng hiện tại sửa reply của mình
     */
    @PutMapping("/replies/{replyId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ReplyResponse>> updateReply(
            @PathVariable String replyId,
            @RequestBody UpdateReplyRequest request) {
        try {
            ApiResponse<ReplyResponse> response = neoPostService.updateReply(replyId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật reply: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("01", "Lỗi khi cập nhật reply: " + e.getMessage()));
        }
    }

    /**
     * Người dùng hiện tại xóa reply của mình
     */
    @DeleteMapping("/replies/{replyId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deleteReply(@PathVariable String replyId) {
        try {
            ApiResponse<String> response = neoPostService.deleteReply(replyId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi xóa reply: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("01", "Lỗi khi xóa reply: " + e.getMessage()));
        }
    }

    // ==================== REACTION APIs ====================

    /**
     * Người dùng hiện tại reaction vào bài viết
     */
    @PostMapping("/{postId}/reactions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ReactionResponse>> createReaction(
            @PathVariable String postId,
            @RequestBody CreateReactionRequest request) {
        try {
            ApiResponse<ReactionResponse> response = neoPostService.createReaction(postId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi tạo reaction: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("01", "Lỗi khi tạo reaction: " + e.getMessage()));
        }
    }

    /**
     * Người dùng hiện tại sửa reaction của mình
     */
    @PutMapping("/reactions/{reactionId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ReactionResponse>> updateReaction(
            @PathVariable String reactionId,
            @RequestBody UpdateReactionRequest request) {
        try {
            ApiResponse<ReactionResponse> response = neoPostService.updateReaction(reactionId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật reaction: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("01", "Lỗi khi cập nhật reaction: " + e.getMessage()));
        }
    }

    /**
     * Người dùng hiện tại xóa reaction của mình
     */
    @DeleteMapping("/reactions/{reactionId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deleteReaction(@PathVariable String reactionId) {
        try {
            ApiResponse<String> response = neoPostService.deleteReaction(reactionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi xóa reaction: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("01", "Lỗi khi xóa reaction: " + e.getMessage()));
        }
    }

    // ==================== CHECK OWNERSHIP APIs ====================

    /**
     * Check post có phải của người dùng hiện tại
     */
    @GetMapping("/{postId}/check-ownership")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> checkPostOwnership(@PathVariable String postId) {
        try {
            ApiResponse<Boolean> response = neoPostService.checkPostOwnership(postId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra quyền sở hữu bài viết: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("01", "Lỗi khi kiểm tra quyền sở hữu: " + e.getMessage()));
        }
    }

    /**
     * Check comment có phải của người dùng hiện tại
     */
    @GetMapping("/comments/{commentId}/check-ownership")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> checkCommentOwnership(@PathVariable String commentId) {
        try {
            ApiResponse<Boolean> response = neoPostService.checkCommentOwnership(commentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra quyền sở hữu comment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("01", "Lỗi khi kiểm tra quyền sở hữu: " + e.getMessage()));
        }
    }

    /**
     * Check reply có phải của người dùng hiện tại
     */
    @GetMapping("/replies/{replyId}/check-ownership")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> checkReplyOwnership(@PathVariable String replyId) {
        try {
            ApiResponse<Boolean> response = neoPostService.checkReplyOwnership(replyId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra quyền sở hữu reply: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("01", "Lỗi khi kiểm tra quyền sở hữu: " + e.getMessage()));
        }
    }

    /**
     * Check reaction có phải của người dùng hiện tại
     */
    @GetMapping("/reactions/{reactionId}/check-ownership")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> checkReactionOwnership(@PathVariable String reactionId) {
        try {
            ApiResponse<Boolean> response = neoPostService.checkReactionOwnership(reactionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra quyền sở hữu reaction: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("01", "Lỗi khi kiểm tra quyền sở hữu: " + e.getMessage()));
        }
    }
}
