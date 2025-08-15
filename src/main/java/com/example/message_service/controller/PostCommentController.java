package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.CreatePostCommentRequest;
import com.example.message_service.dto.request.UpdatePostCommentRequest;
import com.example.message_service.dto.response.PostCommentResponse;
import com.example.message_service.model.NotificationType;
import com.example.message_service.model.User;
import com.example.message_service.service.NotificationService;
import com.example.message_service.service.PostCommentService;
import com.example.message_service.service.PostService;
import com.example.message_service.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post-comments")
public class PostCommentController {

    private final PostCommentService postCommentService;
    private final UserService userService;
    private final PostService postService;
    private final NotificationService notificationService;

    @Autowired
    public PostCommentController(PostCommentService postCommentService, UserService userService,
            PostService postService, NotificationService notificationService) {
        this.postCommentService = postCommentService;
        this.userService = userService;
        this.postService = postService;
        this.notificationService = notificationService;
    }

    // Tạo comment mới
    @PostMapping("/create")
    public ApiResponse<PostCommentResponse> createComment(
            @RequestParam Long postId,
            @RequestParam String userId,
            @RequestBody CreatePostCommentRequest request) {

        return postCommentService.createComment(postId, userId, request);
    }

    // Lấy comment theo ID
    @GetMapping("/{commentId}")
    public ApiResponse<PostCommentResponse> getCommentById(@PathVariable Long commentId) {
        return postCommentService.getCommentById(commentId);
    }

    // Lấy comments của post
    @GetMapping("/post/{postId}")
    public ApiResponse<List<PostCommentResponse>> getCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return postCommentService.getCommentsByPostWithPagination(postId, page, size);
    }

    // Lấy replies của comment
    @GetMapping("/{commentId}/replies")
    public ApiResponse<List<PostCommentResponse>> getRepliesByComment(@PathVariable Long commentId) {
        List<PostCommentResponse> replies = postCommentService.getRepliesByComment(commentId);
        return ApiResponse.success("00", "Lấy replies thành công", replies);
    }

    // Lấy comments của user
    @GetMapping("/user/{userId}")
    public ApiResponse<List<PostCommentResponse>> getCommentsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return postCommentService.getCommentsByUser(userId, page, size);
    }

    // Cập nhật comment
    @PutMapping("/{commentId}")
    public ApiResponse<PostCommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestParam String userId,
            @RequestBody UpdatePostCommentRequest request) {
        return postCommentService.updateComment(commentId, userId, request);
    }

    // Xóa comment
    @DeleteMapping("/{commentId}")
    public ApiResponse<String> deleteComment(
            @PathVariable Long commentId,
            @RequestParam String userId) {
        return postCommentService.deleteComment(commentId, userId);
    }

    // Tìm kiếm comments
    @GetMapping("/search")
    public ApiResponse<List<PostCommentResponse>> searchComments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return postCommentService.searchComments(keyword, page, size);
    }
}