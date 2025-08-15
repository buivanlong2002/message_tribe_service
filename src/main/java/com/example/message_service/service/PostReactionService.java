package com.example.message_service.service;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.CreatePostReactionRequest;
import com.example.message_service.dto.response.PostReactionResponse;
import com.example.message_service.dto.response.SenderResponse;
import com.example.message_service.model.Post;
import com.example.message_service.model.PostReaction;
import com.example.message_service.model.ReactionType;
import com.example.message_service.model.User;
import com.example.message_service.repository.PostReactionRepository;
import com.example.message_service.repository.PostRepository;
import com.example.message_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostReactionService {

    @Autowired
    private PostReactionRepository postReactionRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    // Tạo reaction mới
    @Transactional
    public ApiResponse<PostReactionResponse> createReaction(Long postId, String userId, CreatePostReactionRequest request) {
        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy post"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user"));

            // Kiểm tra xem user đã reaction chưa
            if (postReactionRepository.existsByPostIdAndUserId(postId, userId)) {
                // Nếu đã có reaction, cập nhật reaction type
                return updateReaction(postId, userId, request);
            }

            PostReaction reaction = new PostReaction();
            reaction.setPost(post);
            reaction.setUser(user);
            reaction.setReactionType(request.getReactionType());

            PostReaction savedReaction = postReactionRepository.save(reaction);
            return ApiResponse.success("00", "Tạo reaction thành công", convertToResponse(savedReaction));
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi tạo reaction: " + e.getMessage());
        }
    }

    // Cập nhật reaction
    @Transactional
    public ApiResponse<PostReactionResponse> updateReaction(Long postId, String userId, CreatePostReactionRequest request) {
        try {
            PostReaction reaction = postReactionRepository.findByPostIdAndUserId(postId, userId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy reaction"));

            reaction.setReactionType(request.getReactionType());
            PostReaction updatedReaction = postReactionRepository.save(reaction);

            return ApiResponse.success("00", "Cập nhật reaction thành công", convertToResponse(updatedReaction));
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi cập nhật reaction: " + e.getMessage());
        }
    }

    // Xóa reaction
    @Transactional
    public ApiResponse<String> deleteReaction(Long postId, String userId) {
        try {
            if (!postReactionRepository.existsByPostIdAndUserId(postId, userId)) {
                return ApiResponse.error("01", "Không tìm thấy reaction");
            }

            postReactionRepository.deleteByPostIdAndUserId(postId, userId);
            return ApiResponse.success("00", "Xóa reaction thành công", null);
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi xóa reaction: " + e.getMessage());
        }
    }

    // Lấy reactions của một post
    public List<PostReactionResponse> getReactionsByPost(Long postId) {
        List<PostReaction> reactions = postReactionRepository.findByPostId(postId);
        return reactions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Lấy reaction của user trên post
    public ApiResponse<PostReactionResponse> getReactionByUser(Long postId, String userId) {
        try {
            PostReaction reaction = postReactionRepository.findByPostIdAndUserId(postId, userId)
                    .orElse(null);

            if (reaction == null) {
                return ApiResponse.error("01", "User chưa reaction post này");
            }

            return ApiResponse.success("00", "Lấy reaction thành công", convertToResponse(reaction));
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi lấy reaction: " + e.getMessage());
        }
    }

    // Lấy reactions theo loại
    public List<PostReactionResponse> getReactionsByType(Long postId, ReactionType reactionType) {
        List<PostReaction> reactions = postReactionRepository.findByPostIdAndReactionType(postId, reactionType);
        return reactions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Đếm số reactions của post
    public long getReactionCountByPost(Long postId) {
        return postReactionRepository.countByPostId(postId);
    }

    // Đếm số reactions theo loại của post
    public long getReactionCountByType(Long postId, ReactionType reactionType) {
        return postReactionRepository.countByPostIdAndReactionType(postId, reactionType);
    }

    // Kiểm tra user đã reaction chưa
    public boolean hasUserReacted(Long postId, String userId) {
        return postReactionRepository.existsByPostIdAndUserId(postId, userId);
    }

    // Convert PostReaction entity to PostReactionResponse
    private PostReactionResponse convertToResponse(PostReaction reaction) {
        PostReactionResponse response = new PostReactionResponse();
        response.setId(reaction.getId());
        response.setReactionType(reaction.getReactionType());
        response.setCreatedAt(reaction.getCreatedAt());

        // Set user info
        if (reaction.getUser() != null) {
            SenderResponse userResponse = new SenderResponse();
            userResponse.setSenderId(reaction.getUser().getId());
            userResponse.setNameSender(reaction.getUser().getDisplayName() != null ? reaction.getUser().getDisplayName() : "Người dùng");
            userResponse.setAvatarSender(reaction.getUser().getAvatarUrl());
            response.setUser(userResponse);
        }

        return response;
    }
} 