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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NeoPostService {

    private static final Logger log = LoggerFactory.getLogger(NeoPostService.class);

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
            Optional<NeoPost> postOpt = neoPostRepository.findById(postId);
            if (postOpt.isEmpty())
                return false;

            NeoPost post = postOpt.get();
            return post.getUser() != null && post.getUser().getId().equals(currentUser.getId());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPostCommentOwnedByCurrentUser(String postCommentId) {
        try {
            UserResponse currentUser = getCurrentUser();
            Optional<NeoPostComment> commentOpt = neoPostCommentRepository.findById(postCommentId);
            if (commentOpt.isEmpty())
                return false;

            NeoPostComment comment = commentOpt.get();
            return comment.getUser() != null && comment.getUser().getId().equals(currentUser.getId());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPostCommentReplyOwnedByCurrentUser(String postCommentReplyId) {
        try {
            UserResponse currentUser = getCurrentUser();
            Optional<NeoPostCommentReply> replyOpt = neoPostCommentReplyRepository
                    .findById(postCommentReplyId);
            if (replyOpt.isEmpty())
                return false;

            NeoPostCommentReply reply = replyOpt.get();
            return reply.getUser() != null && reply.getUser().getId().equals(currentUser.getId());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPostReactionOwnedByCurrentUser(String postReactionId) {
        try {
            UserResponse currentUser = getCurrentUser();
            Optional<NeoPostReaction> reactionOpt = neoPostReactionRepository.findById(postReactionId);
            if (reactionOpt.isEmpty())
                return false;

            NeoPostReaction reaction = reactionOpt.get();
            return reaction.getUser() != null && reaction.getUser().getId().equals(currentUser.getId());
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== POST METHODS ====================

    public ApiResponse<NeoPostResponse> createPost(CreatePostRequest request, MultipartFile[] mediaFiles)
            throws Exception {
        try {
            // Validate request
            if (request == null) {
                throw new IllegalArgumentException("Request không được null");
            }
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Nội dung bài viết không được để trống");
            }

            UserResponse currentUser = getCurrentUser();
            if (currentUser == null) {
                throw new IllegalStateException("Không thể xác định người dùng hiện tại");
            }

            NeoPost post = new NeoPost();
            post.setContent(request.getContent().trim());
            post.setVisibility(request.getVisibility() != null ? request.getVisibility() : NeoPostVisibility.PUBLIC);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());

            // Xử lý media files nếu có
            if (mediaFiles != null && mediaFiles.length > 0) {
                List<String> mediaUrls = processMediaFiles(mediaFiles);
                post.setUrlMedia(mediaUrls);
            } else {
                post.setUrlMedia(new ArrayList<>());
            }

            // Set user from currentUser
            User user = userService.getUserById(currentUser.getId());
            if (user == null) {
                throw new IllegalStateException("Không tìm thấy thông tin người dùng");
            }
            post.setUser(user);

            NeoPost savedPost = neoPostRepository.save(post);
            NeoPostResponse response = convertToNeoPostResponse(savedPost);

            return ApiResponse.success("00", "Tạo bài viết thành công", response);
        } catch (Exception e) {
            log.error("Lỗi khi tạo bài viết: {}", e.getMessage(), e);
            throw e; // Re-throw để controller xử lý
        }
    }

    public ApiResponse<NeoPostResponse> updatePost(String postId, UpdatePostRequest request, MultipartFile[] mediaFiles)
            throws Exception {
        if (!isPostOwnedByCurrentUser(postId)) {
            return ApiResponse.error("01", "Bạn không có quyền chỉnh sửa bài viết này");
        }

        Optional<NeoPost> postOpt = neoPostRepository.findById(postId);
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

        // Xử lý media files nếu có
        if (mediaFiles != null && mediaFiles.length > 0) {
            List<String> newMediaUrls = processMediaFiles(mediaFiles);
            // Nếu có file mới được upload, thay thế danh sách media URLs hiện tại
            post.setUrlMedia(newMediaUrls);
            log.info("Đã cập nhật {} media files cho bài viết {}", newMediaUrls.size(), postId);
        } else if (request.getMediaUrls() != null) {
            // Nếu không có file mới nhưng có mediaUrls trong request, sử dụng mediaUrls đó
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

        Optional<NeoPost> postOpt = neoPostRepository.findById(postId);
        if (postOpt.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy bài viết");
        }

        NeoPost post = postOpt.get();
        post.setDeletedAt(LocalDateTime.now());
        neoPostRepository.save(post);

        return ApiResponse.success("00", "Xóa bài viết thành công", "Đã xóa bài viết");
    }

    @Transactional
    public ApiResponse<Page<NeoPostResponse>> getNewsfeed(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NeoPost> posts = neoPostRepository.findByVisibilityAndDeletedAtIsNull(NeoPostVisibility.PUBLIC, pageable);

        Page<NeoPostResponse> response = posts.map(this::convertToNeoPostResponse);
        return ApiResponse.success("00", "Lấy newsfeed thành công", response);
    }

    @Transactional
    public ApiResponse<Page<NeoPostResponse>> getPublicPostsByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NeoPost> posts = neoPostRepository.findByUserIdAndVisibilityAndDeletedAtIsNull(userId,
                NeoPostVisibility.PUBLIC, pageable);

        Page<NeoPostResponse> response = posts.map(this::convertToNeoPostResponse);
        return ApiResponse.success("00", "Lấy bài viết public của user thành công", response);
    }

    @Transactional
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

        Optional<NeoPost> postOpt = neoPostRepository.findById(postId);
        if (postOpt.isEmpty()) {
            return ApiResponse.error("01", "Không tìm thấy bài viết");
        }

        NeoPostComment comment = new NeoPostComment();
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        // Set post and user from currentUser
        comment.setNeoPost(postOpt.get());
        User user = userService.getUserById(currentUser.getId());
        comment.setUser(user);

        NeoPostComment savedComment = neoPostCommentRepository.save(comment);
        CommentResponse response = convertToCommentResponse(savedComment);

        return ApiResponse.success("00", "Tạo comment thành công", response);
    }

    public ApiResponse<CommentResponse> updateComment(String commentId, UpdateCommentRequest request) throws Exception {
        if (!isPostCommentOwnedByCurrentUser(commentId)) {
            return ApiResponse.error("01", "Bạn không có quyền chỉnh sửa comment này");
        }

        Optional<NeoPostComment> commentOpt = neoPostCommentRepository.findById(commentId);
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

        Optional<NeoPostComment> commentOpt = neoPostCommentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy comment");
        }

        neoPostCommentRepository.delete(commentOpt.get());
        return ApiResponse.success("00", "Xóa comment thành công", "Đã xóa comment");
    }

    // ==================== REPLY METHODS ====================

    public ApiResponse<ReplyResponse> createReply(String commentId, CreateReplyRequest request) throws Exception {
        UserResponse currentUser = getCurrentUser();

        Optional<NeoPostComment> commentOpt = neoPostCommentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return ApiResponse.error("01", "Không tìm thấy comment");
        }

        NeoPostCommentReply reply = new NeoPostCommentReply();
        reply.setContent(request.getContent());
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUpdatedAt(LocalDateTime.now());

        // Set comment and user from currentUser
        reply.setNeoPostComment(commentOpt.get());
        User user = userService.getUserById(currentUser.getId());
        reply.setUser(user);

        NeoPostCommentReply savedReply = neoPostCommentReplyRepository.save(reply);
        ReplyResponse response = convertToReplyResponse(savedReply);

        return ApiResponse.success("00", "Tạo reply thành công", response);
    }

    public ApiResponse<ReplyResponse> updateReply(String replyId, UpdateReplyRequest request) throws Exception {
        if (!isPostCommentReplyOwnedByCurrentUser(replyId)) {
            return ApiResponse.error("01", "Bạn không có quyền chỉnh sửa reply này");
        }

        Optional<NeoPostCommentReply> replyOpt = neoPostCommentReplyRepository.findById(replyId);
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

        Optional<NeoPostCommentReply> replyOpt = neoPostCommentReplyRepository.findById(replyId);
        if (replyOpt.isEmpty()) {
            return ApiResponse.error("02", "Không tìm thấy reply");
        }

        neoPostCommentReplyRepository.delete(replyOpt.get());
        return ApiResponse.success("00", "Xóa reply thành công", "Đã xóa reply");
    }

    // ==================== REACTION METHODS ====================

    public ApiResponse<ReactionResponse> createReaction(String postId, CreateReactionRequest request) throws Exception {
        UserResponse currentUser = getCurrentUser();

        Optional<NeoPost> postOpt = neoPostRepository.findById(postId);
        if (postOpt.isEmpty()) {
            return ApiResponse.error("01", "Không tìm thấy bài viết");
        }

        NeoPostReaction reaction = new NeoPostReaction();
        reaction.setType(request.getType());
        reaction.setCreatedAt(LocalDateTime.now());

        // Set post and user from currentUser
        reaction.setNeoPost(postOpt.get());
        User user = userService.getUserById(currentUser.getId());
        reaction.setUser(user);

        NeoPostReaction savedReaction = neoPostReactionRepository.save(reaction);
        ReactionResponse response = convertToReactionResponse(savedReaction);

        return ApiResponse.success("00", "Tạo reaction thành công", response);
    }

    public ApiResponse<ReactionResponse> updateReaction(String reactionId, UpdateReactionRequest request)
            throws Exception {
        if (!isPostReactionOwnedByCurrentUser(reactionId)) {
            return ApiResponse.error("01", "Bạn không có quyền chỉnh sửa reaction này");
        }

        Optional<NeoPostReaction> reactionOpt = neoPostReactionRepository.findById(reactionId);
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

        Optional<NeoPostReaction> reactionOpt = neoPostReactionRepository.findById(reactionId);
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
        List<String> mediaUrls = new ArrayList<>();

        if (mediaFiles == null || mediaFiles.length == 0) {
            return mediaUrls;
        }

        try {
            Path uploadPath = Paths.get("uploads", "neo-posts");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : mediaFiles) {
                if (file == null || file.isEmpty()) {
                    log.warn("Bỏ qua file null hoặc rỗng");
                    continue;
                }

                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || originalFilename.trim().isEmpty()) {
                    log.warn("Bỏ qua file không có tên");
                    continue;
                }

                // Validate file size (giới hạn 10MB)
                if (file.getSize() > 10 * 1024 * 1024) {
                    log.warn("File {} quá lớn, bỏ qua", originalFilename);
                    continue;
                }

                String extension = getFileExtension(originalFilename);
                String uniqueName = UUID.randomUUID().toString() + extension;

                Path filePath = uploadPath.resolve(uniqueName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                String publicUrl = "/uploads/neo-posts/" + uniqueName;
                mediaUrls.add(publicUrl);
                log.info("Đã lưu file: {} -> {}", originalFilename, publicUrl);
            }
        } catch (IOException e) {
            log.error("Lỗi khi lưu media files: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lưu media của bài viết: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Lỗi không xác định khi xử lý media files: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi không xác định khi xử lý media files: " + e.getMessage(), e);
        }

        return mediaUrls;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null)
            return "";
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(idx) : "";
    }

    private NeoPostResponse convertToNeoPostResponse(NeoPost post) {
        // Convert user to UserResponse
        UserResponse userResponse = null;
        if (post.getUser() != null) {
            userResponse = UserResponse.builder()
                    .id(post.getUser().getId())
                    .displayName(post.getUser().getDisplayName())
                    .avatarUrl(post.getUser().getAvatarUrl())
                    .build();
        }

        // Map reactions
        List<ReactionResponse> reactionResponses = post.getReactions() == null ? List.of()
                : post.getReactions().stream().map(this::convertToReactionResponse).collect(Collectors.toList());

        // Map comments with replies
        List<CommentResponse> commentResponses = post.getComments() == null ? List.of()
                : post.getComments().stream().map(c -> {
                    CommentResponse cr = convertToCommentResponse(c);
                    List<ReplyResponse> rr = c.getReplies() == null ? List.of()
                            : c.getReplies().stream().map(this::convertToReplyResponse).collect(Collectors.toList());
                    cr.setReplies(rr);
                    cr.setReplyCount(rr.size());
                    return cr;
                }).collect(Collectors.toList());

        return NeoPostResponse.builder()
                .id(post.getId())
                .user(userResponse)
                .content(post.getContent())
                .visibility(post.getVisibility())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .deletedAt(post.getDeletedAt())
                .mediaUrls(post.getUrlMedia())
                .reactions(reactionResponses)
                .comments(commentResponses)
                .reactionCount(reactionResponses.size())
                .commentCount(commentResponses.size())
                .build();
    }

    private CommentResponse convertToCommentResponse(NeoPostComment comment) {
        // Convert user to UserResponse
        UserResponse userResponse = null;
        if (comment.getUser() != null) {
            userResponse = UserResponse.builder()
                    .id(comment.getUser().getId())
                    .displayName(comment.getUser().getDisplayName())
                    .avatarUrl(comment.getUser().getAvatarUrl())
                    .build();
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .user(userResponse)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private ReplyResponse convertToReplyResponse(NeoPostCommentReply reply) {
        // Convert user to UserResponse
        UserResponse userResponse = null;
        if (reply.getUser() != null) {
            userResponse = UserResponse.builder()
                    .id(reply.getUser().getId())
                    .displayName(reply.getUser().getDisplayName())
                    .avatarUrl(reply.getUser().getAvatarUrl())
                    .build();
        }

        return ReplyResponse.builder()
                .id(reply.getId())
                .user(userResponse)
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }

    private ReactionResponse convertToReactionResponse(NeoPostReaction reaction) {
        // Convert user to UserResponse
        UserResponse userResponse = null;
        if (reaction.getUser() != null) {
            userResponse = UserResponse.builder()
                    .id(reaction.getUser().getId())
                    .displayName(reaction.getUser().getDisplayName())
                    .avatarUrl(reaction.getUser().getAvatarUrl())
                    .build();
        }

        return ReactionResponse.builder()
                .id(reaction.getId())
                .user(userResponse)
                .type(reaction.getType())
                .createdAt(reaction.getCreatedAt())
                .build();
    }
}
