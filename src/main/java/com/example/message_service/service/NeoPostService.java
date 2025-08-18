package com.example.message_service.service;

import org.springframework.stereotype.Service;

import com.example.message_service.repository.NeoPostRepository;
import com.example.message_service.repository.NeoPostCommentRepository;
import com.example.message_service.dto.response.UserResponse;
import com.example.message_service.model.NeoPost;
import com.example.message_service.repository.NeoPostCommentReplyRepository;
import com.example.message_service.repository.NeoPostReactionRepository;

@Service
public class NeoPostService {

    private final UserService userService;
    private final NeoPostRepository neoPostRepository;
    private final NeoPostCommentRepository neoPostCommentRepository;
    private final NeoPostCommentReplyRepository neoPostCommentReplyRepository;
    private final NeoPostReactionRepository neoPostReactionRepository;

    public NeoPostService(
            UserService userService,
            NeoPostRepository neoPostRepository,
            NeoPostCommentRepository neoPostCommentRepository,
            NeoPostCommentReplyRepository neoPostCommentReplyRepository,
            NeoPostReactionRepository neoPostReactionRepository) {
        this.userService = userService;
        this.neoPostRepository = neoPostRepository;
        this.neoPostCommentRepository = neoPostCommentRepository;
        this.neoPostCommentReplyRepository = neoPostCommentReplyRepository;
        this.neoPostReactionRepository = neoPostReactionRepository;
    }

    // Helper method to get current user when needed
    private UserResponse getCurrentUser() throws Exception {
        return userService.getCurrentUser();
    }

    private boolean isPostOwnedByCurrentUser(String postId) {
        try {
            UserResponse currentUser = getCurrentUser();
            return neoPostRepository.existsByIdAndUserId(postId, currentUser.getId());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPostCommentOwnedByCurrentUser(String postCommentId) {
        try {
            UserResponse currentUser = getCurrentUser();
            return neoPostCommentRepository.existsByIdAndUserId(postCommentId, currentUser.getId());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPostCommentReplyOwnedByCurrentUser(String postCommentReplyId) {
        try {
            UserResponse currentUser = getCurrentUser();
            return neoPostCommentReplyRepository.existsByIdAndUserId(postCommentReplyId, currentUser.getId());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPostReactionOwnedByCurrentUser(String postReactionId) {
        try {
            UserResponse currentUser = getCurrentUser();
            return neoPostReactionRepository.existsByIdAndUserId(postReactionId, currentUser.getId());
        } catch (Exception e) {
            return false;
        }
    }


}
