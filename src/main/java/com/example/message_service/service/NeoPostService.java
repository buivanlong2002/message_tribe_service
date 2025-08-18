package com.example.message_service.service;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.example.message_service.repository.NeoPostRepository;
import com.example.message_service.repository.NeoPostCommentRepository;
import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.*;
import com.example.message_service.dto.response.*;
import com.example.message_service.model.*;
import com.example.message_service.repository.NeoPostCommentReplyRepository;
import com.example.message_service.repository.NeoPostReactionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            // TODO: Implement proper ownership check when user relationship is set up
            // This should check if the post belongs to the current user
            return true; // Temporary return true for now
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPostCommentOwnedByCurrentUser(String postCommentId) {
        try {
            UserResponse currentUser = getCurrentUser();
            // TODO: Implement proper ownership check when user relationship is set up
            return true; // Temporary return true for now
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPostCommentReplyOwnedByCurrentUser(String postCommentReplyId) {
        try {
            UserResponse currentUser = getCurrentUser();
            // TODO: Implement proper ownership check when user relationship is set up
            return true; // Temporary return true for now
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPostReactionOwnedByCurrentUser(String postReactionId) {
        try {
            UserResponse currentUser = getCurrentUser();
            // TODO: Implement proper ownership check when user relationship is set up
            return true; // Temporary return true for now
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== POST METHODS ====================

    public ApiResponse<NeoPostResponse> createPost(CreatePostRequest request, MultipartFile[] mediaFiles)
            throws Exception {
        UserResponse currentUser = getCurrentUser();

        NeoPost post = new NeoPost();
        post.setContent(request.getContent());
        post.setVisibility(request.getVisibility());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // Xử lý media files nếu có
        if (mediaFiles != null && mediaFiles.length > 0) {
            List<String> mediaUrls = processMediaFiles(mediaFiles);
            post.setUrlMedia(mediaUrls);
        }

        // TODO: Set user from currentUser - need to convert UserResponse to User entity
        // post.setUser(userService.getUserById(currentUser.getId()));

        NeoPost savedPost = neoPostRepository.save(post);
        NeoPostResponse response = convertToNeoPostResponse(savedPost);

        return ApiResponse.success("00", "Tạo bài viết thành công", response);
    }

    public ApiResponse<NeoPostResponse> updatePost(String postId, UpdatePostRequest request) throws Exception {
        if (!isPostOwnedByCurrentUser(postId)) {
            return ApiResponse.error("01", "Bạn không có quyền chỉnh sửa bài viết này");
        }

        Optional<NeoPost> postOpt = neoPostRepository.findById(Long.valueOf(postId));
        if (postOpt.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy bài viết");
        }

        NeoPost post = postOpt.get();
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getVisibility() != null) {
            post.setVisibility(request.getVisibility());
        }
        if (request.getMediaUrls() != null) {
            post.setUrlMedia(request.getMediaUrls());
        }
        post.setUpdatedAt(LocalDateTime.now());

        NeoPost savedPost = neoPostRepository.save(post);
        NeoPostResponse response = convertToNeoPostResponse(savedPost);

        return ApiResponse.success("00", "Cập nhật bài viết thành công", response);
    }

    public ApiResponse<String> deletePost(String postId) throws Exception {
        if (!isPostOwnedByCurrentUser(postId)) {
            return ApiResponse.error("01", "Bạn không có quyền xóa bài viết này");
        }

        Optional<NeoPost> postOpt = neoPostRepository.findById(Long.valueOf(postId));
        if (postOpt.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy bài viết");
        }

        NeoPost post = postOpt.get();
        post.setDeletedAt(LocalDateTime.now());
        neoPostRepository.save(post);

        return ApiResponse.success("00", "Xóa bài viết thành công", "Đã xóa bài viết");
    }

    public ApiResponse<Page<NeoPostResponse>> getNewsfeed(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NeoPost> posts = neoPostRepository.findByVisibilityAndDeletedAtIsNull(NeoPostVisibility.PUBLIC, pageable);

        Page<NeoPostResponse> response = posts.map(this::convertToNeoPostResponse);
        return ApiResponse.success("00", "Lấy newsfeed thành công", response);
    }

    public ApiResponse<Page<NeoPostResponse>> getPublicPostsByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NeoPost> posts = neoPostRepository.findByUserIdAndVisibilityAndDeletedAtIsNull(userId,
                NeoPostVisibility.PUBLIC, pageable);

        Page<NeoPostResponse> response = posts.map(this::convertToNeoPostResponse);
        return ApiResponse.success("00", "Lấy bài viết public của user thành công", response);
    }

    public ApiResponse<Page<NeoPostResponse>> getMyPosts(int page, int size) throws Exception {
        UserResponse currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<NeoPost> posts = neoPostRepository.findByUserIdAndDeletedAtIsNull(currentUser.getId(), pageable);

        Page<NeoPostResponse> response = posts.map(this::convertToNeoPostResponse);
        return ApiResponse.success("00", "Lấy bài viết của mình thành công", response);
    }

    // ==================== COMMENT METHODS ====================

    public ApiResponse<CommentResponse> createComment(String postId, CreateCommentRequest request) throws Exception {
        UserResponse currentUser = getCurrentUser();

        Optional<NeoPost> postOpt = neoPostRepository.findById(Long.valueOf(postId));
        if (postOpt.isEmpty()) {
            return ApiResponse.error("01", "Không tìm thấy bài viết");
        }

        NeoPostComment comment = new NeoPostComment();
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        // TODO: Set post and user from currentUser - need to convert UserResponse to
        // User entity
        // comment.setNeoPost(postOpt.get());
        // comment.setUser(userService.getUserById(currentUser.getId()));

        NeoPostComment savedComment = neoPostCommentRepository.save(comment);
        CommentResponse response = convertToCommentResponse(savedComment);

        return ApiResponse.success("00", "Tạo comment thành công", response);
    }

    public ApiResponse<CommentResponse> updateComment(String commentId, UpdateCommentRequest request) throws Exception {
        if (!isPostCommentOwnedByCurrentUser(commentId)) {
            return ApiResponse.error("01", "Bạn không có quyền chỉnh sửa comment này");
        }

        Optional<NeoPostComment> commentOpt = neoPostCommentRepository.findById(Long.valueOf(commentId));
        if (commentOpt.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy comment");
        }

        NeoPostComment comment = commentOpt.get();
        comment.setContent(request.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        NeoPostComment savedComment = neoPostCommentRepository.save(comment);
        CommentResponse response = convertToCommentResponse(savedComment);

        return ApiResponse.success("00", "Cập nhật comment thành công", response);
    }

    public ApiResponse<String> deleteComment(String commentId) throws Exception {
        if (!isPostCommentOwnedByCurrentUser(commentId)) {
            return ApiResponse.error("01", "Bạn không có quyền xóa comment này");
        }

        Optional<NeoPostComment> commentOpt = neoPostCommentRepository.findById(Long.valueOf(commentId));
        if (commentOpt.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy comment");
        }

        neoPostCommentRepository.delete(commentOpt.get());
        return ApiResponse.success("00", "Xóa comment thành công", "Đã xóa comment");
    }

    // ==================== REPLY METHODS ====================

    public ApiResponse<ReplyResponse> createReply(String commentId, CreateReplyRequest request) throws Exception {
        UserResponse currentUser = getCurrentUser();

        Optional<NeoPostComment> commentOpt = neoPostCommentRepository.findById(Long.valueOf(commentId));
        if (commentOpt.isEmpty()) {
            return ApiResponse.error("01", "Không tìm thấy comment");
        }

        NeoPostCommentReply reply = new NeoPostCommentReply();
        reply.setContent(request.getContent());
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUpdatedAt(LocalDateTime.now());

        // TODO: Set comment and user from currentUser - need to convert UserResponse to
        // User entity
        // reply.setNeoPostComment(commentOpt.get());
        // reply.setUser(userService.getUserById(currentUser.getId()));

        NeoPostCommentReply savedReply = neoPostCommentReplyRepository.save(reply);
        ReplyResponse response = convertToReplyResponse(savedReply);

        return ApiResponse.success("00", "Tạo reply thành công", response);
    }

    public ApiResponse<ReplyResponse> updateReply(String replyId, UpdateReplyRequest request) throws Exception {
        if (!isPostCommentReplyOwnedByCurrentUser(replyId)) {
            return ApiResponse.error("01", "Bạn không có quyền chỉnh sửa reply này");
        }

        Optional<NeoPostCommentReply> replyOpt = neoPostCommentReplyRepository.findById(Long.valueOf(replyId));
        if (replyOpt.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy reply");
        }

        NeoPostCommentReply reply = replyOpt.get();
        reply.setContent(request.getContent());
        reply.setUpdatedAt(LocalDateTime.now());

        NeoPostCommentReply savedReply = neoPostCommentReplyRepository.save(reply);
        ReplyResponse response = convertToReplyResponse(savedReply);

        return ApiResponse.success("00", "Cập nhật reply thành công", response);
    }

    public ApiResponse<String> deleteReply(String replyId) throws Exception {
        if (!isPostCommentReplyOwnedByCurrentUser(replyId)) {
            return ApiResponse.error("01", "Bạn không có quyền xóa reply này");
        }

        Optional<NeoPostCommentReply> replyOpt = neoPostCommentReplyRepository.findById(Long.valueOf(replyId));
        if (replyOpt.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy reply");
        }

        neoPostCommentReplyRepository.delete(replyOpt.get());
        return ApiResponse.success("00", "Xóa reply thành công", "Đã xóa reply");
    }

    // ==================== REACTION METHODS ====================

    public ApiResponse<ReactionResponse> createReaction(String postId, CreateReactionRequest request) throws Exception {
        UserResponse currentUser = getCurrentUser();

        Optional<NeoPost> postOpt = neoPostRepository.findById(Long.valueOf(postId));
        if (postOpt.isEmpty()) {
            return ApiResponse.error("01", "Không tìm thấy bài viết");
        }

        NeoPostReaction reaction = new NeoPostReaction();
        reaction.setType(request.getType());
        reaction.setCreatedAt(LocalDateTime.now());

        // TODO: Set post and user from currentUser - need to convert UserResponse to
        // User entity
        // reaction.setNeoPost(postOpt.get());
        // reaction.setUser(userService.getUserById(currentUser.getId()));

        NeoPostReaction savedReaction = neoPostReactionRepository.save(reaction);
        ReactionResponse response = convertToReactionResponse(savedReaction);

        return ApiResponse.success("00", "Tạo reaction thành công", response);
    }

    public ApiResponse<ReactionResponse> updateReaction(String reactionId, UpdateReactionRequest request)
            throws Exception {
        if (!isPostReactionOwnedByCurrentUser(reactionId)) {
            return ApiResponse.error("01", "Bạn không có quyền chỉnh sửa reaction này");
        }

        Optional<NeoPostReaction> reactionOpt = neoPostReactionRepository.findById(Long.valueOf(reactionId));
        if (reactionOpt.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy reaction");
        }

        NeoPostReaction reaction = reactionOpt.get();
        reaction.setType(request.getType());

        NeoPostReaction savedReaction = neoPostReactionRepository.save(reaction);
        ReactionResponse response = convertToReactionResponse(savedReaction);

        return ApiResponse.success("00", "Cập nhật reaction thành công", response);
    }

    public ApiResponse<String> deleteReaction(String reactionId) throws Exception {
        if (!isPostReactionOwnedByCurrentUser(reactionId)) {
            return ApiResponse.error("01", "Bạn không có quyền xóa reaction này");
        }

        Optional<NeoPostReaction> reactionOpt = neoPostReactionRepository.findById(Long.valueOf(reactionId));
        if (reactionOpt.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy reaction");
        }

        neoPostReactionRepository.delete(reactionOpt.get());
        return ApiResponse.success("00", "Xóa reaction thành công", "Đã xóa reaction");
    }

    // ==================== OWNERSHIP CHECK METHODS ====================

    public ApiResponse<Boolean> checkPostOwnership(String postId) throws Exception {
        boolean isOwner = isPostOwnedByCurrentUser(postId);
        return ApiResponse.success("00", "Kiểm tra quyền sở hữu thành công", isOwner);
    }

    public ApiResponse<Boolean> checkCommentOwnership(String commentId) throws Exception {
        boolean isOwner = isPostCommentOwnedByCurrentUser(commentId);
        return ApiResponse.success("00", "Kiểm tra quyền sở hữu thành công", isOwner);
    }

    public ApiResponse<Boolean> checkReplyOwnership(String replyId) throws Exception {
        boolean isOwner = isPostCommentReplyOwnedByCurrentUser(replyId);
        return ApiResponse.success("00", "Kiểm tra quyền sở hữu thành công", isOwner);
    }

    public ApiResponse<Boolean> checkReactionOwnership(String reactionId) throws Exception {
        boolean isOwner = isPostReactionOwnedByCurrentUser(reactionId);
        return ApiResponse.success("00", "Kiểm tra quyền sở hữu thành công", isOwner);
    }

    // ==================== HELPER METHODS ====================

    private List<String> processMediaFiles(MultipartFile[] mediaFiles) {
        // TODO: Implement media file processing logic
        return List.of();
    }

    private NeoPostResponse convertToNeoPostResponse(NeoPost post) {
        return NeoPostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .visibility(post.getVisibility())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .deletedAt(post.getDeletedAt())
                .mediaUrls(post.getUrlMedia())
                .build();
    }

    private CommentResponse convertToCommentResponse(NeoPostComment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private ReplyResponse convertToReplyResponse(NeoPostCommentReply reply) {
        return ReplyResponse.builder()
                .id(reply.getId())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }

    private ReactionResponse convertToReactionResponse(NeoPostReaction reaction) {
        return ReactionResponse.builder()
                .id(reaction.getId())
                .type(reaction.getType())
                .createdAt(reaction.getCreatedAt())
                .build();
    }
}
