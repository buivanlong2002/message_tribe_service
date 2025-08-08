package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.CreatePostReactionRequest;
import com.example.message_service.dto.response.PostReactionResponse;
import com.example.message_service.model.NotificationType;
import com.example.message_service.model.User;
import com.example.message_service.service.NotificationService;
import com.example.message_service.service.PostReactionService;
import com.example.message_service.service.PostService;
import com.example.message_service.service.UserService;

import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post-reactions")
public class PostReactionController {

    private final PostReactionService postReactionService;
    private final NotificationService notificationService;
    private final PostService postService;
    private final UserService userService;

    @Autowired
    public PostReactionController(PostReactionService postReactionService, NotificationService notificationService,
            PostService postService, UserService userService) {
        this.postReactionService = postReactionService;
        this.notificationService = notificationService;
        this.postService = postService;
        this.userService = userService;
    }

    // Tạo reaction mới
    @PostMapping("/create")
    public ApiResponse<PostReactionResponse> createReaction(
            @RequestParam Long postId,
            @RequestParam String userId,
            @RequestBody CreatePostReactionRequest request) {

        // lấy tên người reaction
        User reactor = userService.getUserById(userId);

        // lấy chủ nhân bài viết
        User postOwner = postService.getPostOwner(postId);

        // thêm thông báo
        notificationService.createNotification(NotificationType.REACTION_POST, postOwner,
                reactor.getDisplayName() + " đã phản ứng với bài viết");
        return postReactionService.createReaction(postId, userId, request);
    }

    // Cập nhật reaction
    @PutMapping("/update")
    public ApiResponse<PostReactionResponse> updateReaction(
            @RequestParam Long postId,
            @RequestParam String userId,
            @RequestBody CreatePostReactionRequest request) {
        return postReactionService.updateReaction(postId, userId, request);
    }

    // Xóa reaction
    @DeleteMapping("/delete")
    public ApiResponse<String> deleteReaction(
            @RequestParam Long postId,
            @RequestParam String userId) {
        return postReactionService.deleteReaction(postId, userId);
    }

    // Lấy reactions của post
    @GetMapping("/post/{postId}")
    public ApiResponse<List<PostReactionResponse>> getReactionsByPost(@PathVariable Long postId) {
        List<PostReactionResponse> reactions = postReactionService.getReactionsByPost(postId);
        return ApiResponse.success("00", "Lấy reactions thành công", reactions);
    }

    // Lấy reaction của user trên post
    @GetMapping("/user")
    public ApiResponse<PostReactionResponse> getReactionByUser(
            @RequestParam Long postId,
            @RequestParam String userId) {
        return postReactionService.getReactionByUser(postId, userId);
    }

    // Kiểm tra user đã reaction chưa
    @GetMapping("/check")
    public ApiResponse<Boolean> hasUserReacted(
            @RequestParam Long postId,
            @RequestParam String userId) {
        boolean hasReacted = postReactionService.hasUserReacted(postId, userId);
        return ApiResponse.success("00", "Kiểm tra reaction thành công", hasReacted);
    }

    // Đếm số reactions của post
    @GetMapping("/count/{postId}")
    public ApiResponse<Long> getReactionCount(@PathVariable Long postId) {
        long count = postReactionService.getReactionCountByPost(postId);
        return ApiResponse.success("00", "Đếm reactions thành công", count);
    }
}