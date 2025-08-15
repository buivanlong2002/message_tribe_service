package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.CreatePostRequest;
import com.example.message_service.dto.request.UpdatePostRequest;
import com.example.message_service.dto.response.PostResponse;
import com.example.message_service.model.Visibility;
import com.example.message_service.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private ObjectMapper objectMapper;


    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> createPost(
            @RequestParam("metadata") String metadata,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) throws JsonProcessingException {
        // Convert String JSON -> CreatePostRequest object
        CreatePostRequest request = objectMapper.readValue(metadata, CreatePostRequest.class);
        return postService.createPost(request.getUserId(), request, files);
    }


    // Tạo post đơn giản (không có file)
    @PostMapping(value = "/create-simple", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PostResponse> createPostSimple(@RequestBody @Valid @NotNull CreatePostRequest request) {
        return postService.createPost(request.getUserId(), request, null);
    }

    // Lấy post theo ID
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPostById(@PathVariable Long postId) {
        return postService.getPostById(postId);
    }

    // Lấy tất cả posts với pagination
    @GetMapping("/all")
    public ApiResponse<List<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return postService.getAllPosts(page, size);
    }

    // Lấy posts của user
    @GetMapping("/user/{userId}")
    public ApiResponse<List<PostResponse>> getPostsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return postService.getPostsByUser(userId, page, size);
    }

    // Lấy posts theo visibility
    @GetMapping("/visibility/{visibility}")
    public ApiResponse<List<PostResponse>> getPostsByVisibility(
            @PathVariable Visibility visibility,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return postService.getPostsByVisibility(visibility, page, size);
    }

    // Cập nhật post
    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestParam("metadata") String metadata,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        try {
            if (metadata == null || metadata.trim().isEmpty()) {
                return ApiResponse.error("01", "Metadata không được để trống");
            }

            // Parse JSON metadata sang DTO
            UpdatePostRequest request = objectMapper.readValue(metadata, UpdatePostRequest.class);

            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                return ApiResponse.error("01", "UserId không được để trống");
            }

            // Debug log
            System.out.println("[UpdatePost] postId=" + postId +
                    ", userId=" + request.getUserId() +
                    ", content=" + request.getContent() +
                    ", visibility=" + request.getVisibility());

            if (files != null) {
                for (MultipartFile file : files) {
                    if (file != null) {
                        System.out.println("File: " + file.getOriginalFilename() +
                                ", size=" + file.getSize() +
                                ", type=" + file.getContentType());
                    }
                }
            }

            // Gọi service xử lý
            return postService.updatePost(postId, request.getUserId(), request, files);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ApiResponse.error("01", "Lỗi parse JSON metadata: " + e.getOriginalMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("99", "Lỗi hệ thống: " + e.getMessage());
        }
    }



    // Cập nhật post đơn giản (không có file)
    @PutMapping(value = "/{postId}/simple", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PostResponse> updatePostSimple(
            @PathVariable Long postId,
            @RequestParam String userId,
            @RequestBody @Valid @NotNull UpdatePostRequest request
    ) {
        return postService.updatePost(postId, userId, request, null);
    }

    // Xóa post
    @DeleteMapping("/{postId}")
    public ApiResponse<String> deletePost(
            @PathVariable Long postId,
            @RequestParam String userId
    ) {
        return postService.deletePost(postId, userId);
    }

    // Tìm kiếm posts
    @GetMapping("/search")
    public ApiResponse<List<PostResponse>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return postService.searchPosts(keyword, page, size);
    }

    // Xóa media của post
    @DeleteMapping("/{postId}/media/{mediaId}")
    public ApiResponse<String> deletePostMedia(
            @PathVariable Long postId,
            @PathVariable Long mediaId,
            @RequestParam String userId
    ) {
        return postService.deletePostMedia(postId, mediaId, userId);
    }
} 