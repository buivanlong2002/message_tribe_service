package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.ChangePasswordRequest;
import com.example.message_service.dto.request.UpdateProfileRequest;
import com.example.message_service.model.User;
import com.example.message_service.repository.UserRepository;
import com.example.message_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    @Value("${user.avatar.upload-dir}")
    private String uploadDir;

    @Autowired
    private UserService userService;

    // Upload avatar
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file,
                                          @AuthenticationPrincipal User user) {
        return userService.uploadAvatar(file, user);
    }

    // Lấy profile người dùng hiện tại
    @GetMapping("/profile")
    public ApiResponse<?> getMyProfile() {
        String loginIdentifier = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOptional = userRepository.findByEmail(loginIdentifier); // hoặc findByPhone

        if (userOptional.isEmpty()) {
            return ApiResponse.error("404", "Không tìm thấy người dùng với định danh từ token: " + loginIdentifier);
        }

        return ApiResponse.success("00", "Lấy thông tin profile thành công", userOptional.get());
    }

    // Cập nhật thông tin người dùng
    @PutMapping("/profile")
    public ApiResponse<?> updateProfile(@RequestBody UpdateProfileRequest request,
                                        @AuthenticationPrincipal User currentUser) {
        User updated = userService.updateProfile(currentUser, request);
        return ApiResponse.success("00", "Cập nhật thông tin thành công", updated);
    }

    // Lấy người dùng theo ID
    @GetMapping("/{userId}")
    public ApiResponse<User> getUserById(@PathVariable String userId) {
        return userService.getByUserId(userId);
    }

    // Tìm người dùng theo email (search tương đối)
    @GetMapping("/search")
    public List<User> searchUsersByEmail(@RequestParam String email) {
        return userService.searchByEmail(email);
    }

    // Tìm chính xác người dùng theo email
    @GetMapping("/find")
    public ApiResponse<User> findUsersByEmail(@RequestParam String email) {
        return userService.findByEmail(email);
    }

    @PutMapping("/change-password")
    public ApiResponse<?> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        try {
            String loginIdentifier = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOptional = userRepository.findByEmail(loginIdentifier);
            
            if (userOptional.isEmpty()) {
                return ApiResponse.error("01", "Không tìm thấy thông tin người dùng");
            }
            
            User user = userOptional.get();
            return userService.changePassword(user, request);
        } catch (Exception e) {
            return ApiResponse.error("99", "Lỗi hệ thống: " + e.getMessage());
        }
    }

}
