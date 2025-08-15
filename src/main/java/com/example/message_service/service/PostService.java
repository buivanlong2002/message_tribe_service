package com.example.message_service.service;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.CreatePostRequest;
import com.example.message_service.dto.request.UpdatePostRequest;
import com.example.message_service.dto.response.PostResponse;
import com.example.message_service.dto.response.SenderResponse;
import com.example.message_service.model.Post;
import com.example.message_service.model.PostMedia;
import com.example.message_service.model.User;
import com.example.message_service.model.Visibility;
import com.example.message_service.repository.PostMediaRepository;
import com.example.message_service.repository.PostRepository;
import com.example.message_service.repository.UserRepository;
import com.example.message_service.service.util.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostReactionService postReactionService;

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PostMediaRepository postMediaRepository;

    // Tạo post mới
    @Transactional
    public ApiResponse<PostResponse> createPost(String userId, CreatePostRequest request, MultipartFile[] files) {
        try {
            // Lưu Post
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user"));

            Post post = new Post();
            post.setUser(user);
            post.setContent(request.getContent());
            post.setVisibility(request.getVisibility());
            post.setCreatedAt(LocalDateTime.now());

            Post savedPost = postRepository.save(post);

            // Xử lý upload file (nếu có)
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    String url = fileStorageService.uploadFile(file); // Upload file & lấy URL
                    // Tạo PostMedia entity nếu có
                    PostMedia media = new PostMedia(savedPost, url, file.getContentType());
                    postMediaRepository.save(media);
                }
            }

            return ApiResponse.success("00", "Tạo post thành công", convertToResponse(savedPost));
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi tạo post: " + e.getMessage());
        }
    }

    // Lấy post theo ID
    public ApiResponse<PostResponse> getPostById(Long postId) {
        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy post"));

            if (post.getDeletedAt() != null) {
                return ApiResponse.error("01", "Post đã bị xóa");
            }

            return ApiResponse.success("00", "Lấy post thành công", convertToResponse(post));
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi lấy post: " + e.getMessage());
        }
    }

    // Lấy tất cả posts với pagination
    public ApiResponse<List<PostResponse>> getAllPosts(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> posts = postRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable);

            List<PostResponse> responses = posts.getContent().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ApiResponse.success("00", "Lấy danh sách posts thành công", responses);
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi lấy danh sách posts: " + e.getMessage());
        }
    }

    // Lấy posts của user
    public ApiResponse<List<PostResponse>> getPostsByUser(String userId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> posts = postRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, pageable);

            List<PostResponse> responses = posts.getContent().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ApiResponse.success("00", "Lấy posts của user thành công", responses);
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi lấy posts của user: " + e.getMessage());
        }
    }

    // Lấy posts theo visibility
    public ApiResponse<List<PostResponse>> getPostsByVisibility(Visibility visibility, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> posts = postRepository.findByVisibilityOrderByCreatedAtDesc(visibility, pageable);

            List<PostResponse> responses = posts.getContent().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ApiResponse.success("00", "Lấy posts theo visibility thành công", responses);
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi lấy posts theo visibility: " + e.getMessage());
        }
    }

    // Cập nhật post
    @Transactional
    public ApiResponse<PostResponse> updatePost(Long postId, String userId, UpdatePostRequest request,
            MultipartFile[] files) {
        try {
            System.out.println("PostService.updatePost - postId: " + postId + ", userId: " + userId);
            System.out.println("Request content: " + request.getContent());
            System.out.println("Request visibility: " + request.getVisibility());
            System.out.println("Files count: " + (files != null ? files.length : 0));
            
            // Kiểm tra post tồn tại
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy post"));

            System.out.println("Found post: " + post.getId() + ", user: " + post.getUser().getId());

            if (!post.getUser().getId().equals(userId)) {
                return ApiResponse.error("01", "Không có quyền cập nhật post này");
            }

            if (post.getDeletedAt() != null) {
                return ApiResponse.error("01", "Post đã bị xóa");
            }

            // Cập nhật nội dung post
            if (request.getContent() != null) {
                post.setContent(request.getContent());
            }
            if (request.getVisibility() != null) {
                post.setVisibility(request.getVisibility());
            }

            post.setUpdatedAt(LocalDateTime.now());
            System.out.println("Saving updated post...");
            Post updatedPost = postRepository.save(post);
            System.out.println("Post saved successfully with ID: " + updatedPost.getId());

            // Xử lý upload file mới (nếu có)
            if (files != null && files.length > 0) {
                System.out.println("Processing " + files.length + " files...");
                for (int i = 0; i < files.length; i++) {
                    MultipartFile file = files[i];
                    try {
                        System.out.println("Processing file " + i + ": " + file.getOriginalFilename() + 
                                         ", size: " + file.getSize() + ", type: " + file.getContentType());
                        
                        if (file == null) {
                            System.out.println("Warning: File " + i + " is null, skipping...");
                            continue;
                        }
                        
                        if (file.isEmpty()) {
                            System.out.println("Warning: File " + i + " is empty, skipping...");
                            continue;
                        }
                        
                        // Upload file
                        System.out.println("Uploading file " + i + "...");
                        String url = fileStorageService.uploadFile(file);
                        System.out.println("File " + i + " uploaded to: " + url);
                        
                        // Tạo PostMedia entity
                        PostMedia media = new PostMedia(updatedPost, url, file.getContentType());
                        System.out.println("Creating PostMedia entity for file " + i + ": " + media);
                        
                        // Lưu PostMedia
                        PostMedia savedMedia = postMediaRepository.save(media);
                        System.out.println("PostMedia " + i + " saved with ID: " + savedMedia.getId());
                        
                    } catch (Exception e) {
                        System.err.println("Error processing file " + i + " (" + file.getOriginalFilename() + "): " + e.getMessage());
                        e.printStackTrace();
                        // Không throw exception ngay, tiếp tục xử lý các file khác
                        System.err.println("Continuing with other files...");
                    }
                }
            }

            // Convert và trả về response
            System.out.println("Converting post to response...");
            PostResponse response = convertToResponse(updatedPost);
            System.out.println("Response converted successfully");
            
            return ApiResponse.success("00", "Cập nhật post thành công", response);
            
        } catch (EntityNotFoundException e) {
            System.err.println("EntityNotFoundException in updatePost: " + e.getMessage());
            return ApiResponse.error("01", e.getMessage());
        } catch (Exception e) {
            System.err.println("Error in PostService.updatePost: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("01", "Lỗi khi cập nhật post: " + e.getMessage());
        }
    }

    // Xóa post (soft delete)
    @Transactional
    public ApiResponse<String> deletePost(Long postId, String userId) {
        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy post"));

            if (!post.getUser().getId().equals(userId)) {
                return ApiResponse.error("01", "Không có quyền xóa post này");
            }

            if (post.getDeletedAt() != null) {
                return ApiResponse.error("01", "Post đã bị xóa");
            }

            post.setDeletedAt(LocalDateTime.now());
            postRepository.save(post);

            return ApiResponse.success("00", "Xóa post thành công", null);
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi xóa post: " + e.getMessage());
        }
    }

    // Tìm kiếm posts
    public ApiResponse<List<PostResponse>> searchPosts(String keyword, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> posts = postRepository.findByContentContainingIgnoreCase(keyword, pageable);

            List<PostResponse> responses = posts.getContent().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ApiResponse.success("00", "Tìm kiếm posts thành công", responses);
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi tìm kiếm posts: " + e.getMessage());
        }
    }

    // Xóa media của post
    @Transactional
    public ApiResponse<String> deletePostMedia(Long postId, Long mediaId, String userId) {
        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy post"));

            if (!post.getUser().getId().equals(userId)) {
                return ApiResponse.error("01", "Không có quyền xóa media của post này");
            }

            PostMedia media = postMediaRepository.findById(mediaId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy media"));

            if (!media.getPost().getId().equals(postId)) {
                return ApiResponse.error("01", "Media không thuộc về post này");
            }

            postMediaRepository.delete(media);
            return ApiResponse.success("00", "Xóa media thành công", null);
        } catch (Exception e) {
            return ApiResponse.error("01", "Lỗi khi xóa media: " + e.getMessage());
        }
    }

    // Convert Post entity to PostResponse
    private PostResponse convertToResponse(Post post) {
        try {
            System.out.println("Converting post " + post.getId() + " to response...");
            
            PostResponse response = new PostResponse();
            response.setId(post.getId());
            response.setContent(post.getContent());
            response.setVisibility(post.getVisibility());
            response.setCreatedAt(post.getCreatedAt());
            response.setUpdatedAt(post.getUpdatedAt());

            // Set user info
            if (post.getUser() != null) {
                System.out.println("Setting user info for user: " + post.getUser().getId());
                SenderResponse userResponse = new SenderResponse();
                userResponse.setSenderId(post.getUser().getId());
                userResponse.setNameSender(post.getUser().getDisplayName() != null ? post.getUser().getDisplayName() : "Người dùng");
                userResponse.setAvatarSender(post.getUser().getAvatarUrl());
                response.setUser(userResponse);
            } else {
                System.out.println("Warning: Post has no user");
            }

            // Set reaction count
            try {
                long reactionCount = postReactionService.getReactionCountByPost(post.getId());
                response.setReactionCount((int) reactionCount);
                System.out.println("Set reaction count: " + reactionCount);
            } catch (Exception e) {
                System.err.println("Error getting reaction count: " + e.getMessage());
                response.setReactionCount(0);
            }

            // Set comment count
            try {
                long commentCount = postCommentService.getCommentCountByPost(post.getId());
                response.setCommentCount((int) commentCount);
                System.out.println("Set comment count: " + commentCount);
            } catch (Exception e) {
                System.err.println("Error getting comment count: " + e.getMessage());
                response.setCommentCount(0);
            }

            // Set reactions
            try {
                response.setReactions(postReactionService.getReactionsByPost(post.getId()));
                System.out.println("Set reactions successfully");
            } catch (Exception e) {
                System.err.println("Error getting reactions: " + e.getMessage());
                response.setReactions(new ArrayList<>());
            }

            // Set comments
            try {
                response.setComments(postCommentService.getCommentsByPost(post.getId()));
                System.out.println("Set comments successfully");
            } catch (Exception e) {
                System.err.println("Error getting comments: " + e.getMessage());
                response.setComments(new ArrayList<>());
            }

            // Set media URLs
            try {
                List<String> mediaUrls = postMediaRepository.findByPostId(post.getId())
                        .stream()
                        .map(media -> media.getMediaUrl())
                        .collect(Collectors.toList());
                response.setMediaUrls(mediaUrls);
                System.out.println("Set media URLs: " + mediaUrls.size() + " items");
            } catch (Exception e) {
                System.err.println("Error getting media URLs: " + e.getMessage());
                response.setMediaUrls(new ArrayList<>());
            }

            System.out.println("Post response conversion completed successfully");
            return response;
            
        } catch (Exception e) {
            System.err.println("Error in convertToResponse: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // lấy chủ nhân bài viết
    public User getPostOwner(Long postId) {
        return postRepository.findPostOwner(postId);
    }
}