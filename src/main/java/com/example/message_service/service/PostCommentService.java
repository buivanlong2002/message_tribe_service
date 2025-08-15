package com.example.message_service.service;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.CreatePostCommentRequest;
import com.example.message_service.dto.request.UpdatePostCommentRequest;
import com.example.message_service.dto.response.PostCommentResponse;
import com.example.message_service.dto.response.SenderResponse;
import com.example.message_service.model.Post;
import com.example.message_service.model.PostComment;
import com.example.message_service.model.User;
import com.example.message_service.repository.PostCommentRepository;
import com.example.message_service.repository.PostRepository;
import com.example.message_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostCommentService {

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    // Tạo comment mới
    @Transactional
    public ApiResponse<PostCommentResponse> createComment(Long postId, String userId, CreatePostCommentRequest request) {
        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy post"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user"));

            PostComment comment = new PostComment();
            comment.setPost(post);
            comment.setUser(user);
            comment.setContent(request.getContent());

            // Nếu có parent comment
            if (request.getParentCommentId() != null) {
                PostComment parentComment = postCommentRepository.findById(request.getParentCommentId())
                        .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy parent comment"));
                comment.setParentComment(parentComment);
            }

            PostComment savedComment = postCommentRepository.save(comment);
            return ApiResponse.success("00", "Tạo comment thành công", convertToResponse(savedComment));
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi tạo comment: " + e.getMessage());
        }
    }

    // Lấy comment theo ID
    public ApiResponse<PostCommentResponse> getCommentById(Long commentId) {
        try {
            PostComment comment = postCommentRepository.findById(commentId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy comment"));

            if (comment.getDeletedAt() != null) {
                return ApiResponse.error("01", "Comment đã bị xóa");
            }

            return ApiResponse.success("00", "Lấy comment thành công", convertToResponse(comment));
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi lấy comment: " + e.getMessage());
        }
    }

    // Lấy tất cả comments của một post
    public List<PostCommentResponse> getCommentsByPost(Long postId) {
        List<PostComment> comments = postCommentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(postId);
        return comments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Lấy comments của post với pagination
    public ApiResponse<List<PostCommentResponse>> getCommentsByPostWithPagination(Long postId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<PostComment> comments = postCommentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);

            List<PostCommentResponse> responses = comments.getContent().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ApiResponse.success("00", "Lấy comments thành công", responses);
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi lấy comments: " + e.getMessage());
        }
    }

    // Lấy replies của một comment
    public List<PostCommentResponse> getRepliesByComment(Long commentId) {
        List<PostComment> replies = postCommentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);
        return replies.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Lấy comments của user
    public ApiResponse<List<PostCommentResponse>> getCommentsByUser(String userId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<PostComment> comments = postCommentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

            List<PostCommentResponse> responses = comments.getContent().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ApiResponse.success("00", "Lấy comments của user thành công", responses);
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi lấy comments của user: " + e.getMessage());
        }
    }

    // Cập nhật comment
    @Transactional
    public ApiResponse<PostCommentResponse> updateComment(Long commentId, String userId, UpdatePostCommentRequest request) {
        try {
            PostComment comment = postCommentRepository.findById(commentId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy comment"));

            if (!comment.getUser().getId().equals(userId)) {
                return ApiResponse.error("01", "Không có quyền cập nhật comment này");
            }

            if (comment.getDeletedAt() != null) {
                return ApiResponse.error("01", "Comment đã bị xóa");
            }

            comment.setContent(request.getContent());
            PostComment updatedComment = postCommentRepository.save(comment);

            return ApiResponse.success("00", "Cập nhật comment thành công", convertToResponse(updatedComment));
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi cập nhật comment: " + e.getMessage());
        }
    }

    // Xóa comment (soft delete)
    @Transactional
    public ApiResponse<String> deleteComment(Long commentId, String userId) {
        try {
            PostComment comment = postCommentRepository.findById(commentId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy comment"));

            if (!comment.getUser().getId().equals(userId)) {
                return ApiResponse.error("01", "Không có quyền xóa comment này");
            }

            if (comment.getDeletedAt() != null) {
                return ApiResponse.error("01", "Comment đã bị xóa");
            }

            comment.setDeletedAt(LocalDateTime.now());
            postCommentRepository.save(comment);

            return ApiResponse.success("00", "Xóa comment thành công", null);
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi xóa comment: " + e.getMessage());
        }
    }

    // Tìm kiếm comments
    public ApiResponse<List<PostCommentResponse>> searchComments(String keyword, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<PostComment> comments = postCommentRepository.findByContentContainingIgnoreCase(keyword, pageable);

            List<PostCommentResponse> responses = comments.getContent().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ApiResponse.success("00", "Tìm kiếm comments thành công", responses);
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi tìm kiếm comments: " + e.getMessage());
        }
    }

    // Đếm số comments của post
    public long getCommentCountByPost(Long postId) {
        return postCommentRepository.countByPostId(postId);
    }

    // Lấy chủ nhân của comment
    public User getCommentOwner(Long commentId) {
        try {
            PostComment comment = postCommentRepository.findById(commentId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy comment"));
            return comment.getUser();
        } catch (Exception e) {
            return null;
        }
    }

    // Đếm số comments của user
    public long getCommentCountByUser(String userId) {
        return postCommentRepository.countByUserId(userId);
    }

    // Convert PostComment entity to PostCommentResponse
    private PostCommentResponse convertToResponse(PostComment comment) {
        PostCommentResponse response = new PostCommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());

        // Set parent comment ID
        if (comment.getParentComment() != null) {
            response.setParentCommentId(comment.getParentComment().getId());
        }

        // Set user info
        if (comment.getUser() != null) {
            SenderResponse userResponse = new SenderResponse();
            userResponse.setSenderId(comment.getUser().getId());
            userResponse.setNameSender(comment.getUser().getDisplayName() != null ? comment.getUser().getDisplayName() : "Người dùng");
            userResponse.setAvatarSender(comment.getUser().getAvatarUrl());
            response.setUser(userResponse);
        }

        // Set replies
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            List<PostCommentResponse> replies = comment.getReplies().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            response.setReplies(replies);
        }

        return response;
    }
} 