package com.example.message_service.service;

import com.example.message_service.dto.request.BlockUserRequest;
import com.example.message_service.dto.request.ChangeUserRoleRequest;
import com.example.message_service.dto.response.AdminUserResponse;
import com.example.message_service.dto.response.UserStatisticsResponse;
import com.example.message_service.model.User;
import com.example.message_service.model.UserRole;
import com.example.message_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;

    /**
     * Lấy danh sách tất cả người dùng với phân trang và các bộ lọc
     * @param page số trang (bắt đầu từ 0)
     * @param size số lượng bản ghi trên mỗi trang
     * @param search từ khóa tìm kiếm theo tên hiển thị
     * @param status trạng thái người dùng cần lọc
     * @return Page<AdminUserResponse> danh sách người dùng với thông tin admin
     */
    public Page<AdminUserResponse> getAllUsers(int page, int size, String search, String status) {
        Pageable pageable = PageRequest.of(page, size);
        
        if (search != null && !search.trim().isEmpty()) {
            if (status != null && !status.trim().isEmpty()) {
                return userRepository.findByDisplayNameContainingIgnoreCaseAndStatusAndIsBlocked(
                    search, status, false, pageable
                ).map(this::convertToAdminUserResponse);
            } else {
                return userRepository.findByDisplayNameContainingIgnoreCase(search, pageable)
                    .map(this::convertToAdminUserResponse);
            }
        } else {
            if (status != null && !status.trim().isEmpty()) {
                return userRepository.findByStatusAndIsBlocked(status, false, pageable)
                    .map(this::convertToAdminUserResponse);
            } else {
                return userRepository.findAll(pageable).map(this::convertToAdminUserResponse);
            }
        }
    }

    /**
     * Chặn hoặc bỏ chặn người dùng
     * @param userId ID của người dùng cần thay đổi trạng thái chặn
     * @param request yêu cầu chứa thông tin trạng thái chặn
     * @return AdminUserResponse thông tin người dùng sau khi cập nhật
     * @throws RuntimeException nếu không tìm thấy người dùng
     */
    public AdminUserResponse blockUser(String userId, BlockUserRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsBlocked(request.getIsBlocked());
        User savedUser = userRepository.save(user);
        
        log.info("User {} {} by admin", userId, request.getIsBlocked() ? "blocked" : "unblocked");
        return convertToAdminUserResponse(savedUser);
    }

    /**
     * Thay đổi vai trò của người dùng
     * @param userId ID của người dùng cần thay đổi vai trò
     * @param request yêu cầu chứa vai trò mới
     * @return AdminUserResponse thông tin người dùng sau khi cập nhật
     * @throws RuntimeException nếu không tìm thấy người dùng
     */
    public AdminUserResponse changeUserRole(String userId, ChangeUserRoleRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRole(request.getRole());
        User savedUser = userRepository.save(user);
        
        log.info("User {} role changed to {} by admin", userId, request.getRole());
        return convertToAdminUserResponse(savedUser);
    }

    /**
     * Lấy thống kê tổng quan về người dùng
     * @return UserStatisticsResponse thông tin thống kê bao gồm:
     *         - Tổng số người dùng
     *         - Số người dùng đang hoạt động
     *         - Số người dùng bị chặn
     *         - Số người dùng mới hôm nay
     *         - Số quản trị viên
     */
    public UserStatisticsResponse getUserStatistics() {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countByStatusAndIsBlocked("active", false);
        Long blockedUsers = userRepository.countByIsBlocked(true);
        Long newUsersToday = userRepository.countByCreatedAtAfter(today);
        Long adminUsers = userRepository.countByRole(UserRole.ROLE_ADMIN);
        
        return UserStatisticsResponse.builder()
            .totalUsers(totalUsers)
            .activeUsers(activeUsers)
            .blockedUsers(blockedUsers)
            .newUsersToday(newUsersToday)
            .adminUsers(adminUsers)
            .build();
    }

    /**
     * Lấy thông tin chi tiết của một người dùng
     * @param userId ID của người dùng cần lấy thông tin
     * @return AdminUserResponse thông tin chi tiết của người dùng
     * @throws RuntimeException nếu không tìm thấy người dùng
     */
    public AdminUserResponse getUserDetails(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return convertToAdminUserResponse(user);
    }

    /**
     * Chuyển đổi entity User thành AdminUserResponse DTO
     * @param user entity User cần chuyển đổi
     * @return AdminUserResponse DTO chứa thông tin người dùng cho admin
     */
    public AdminUserResponse convertToAdminUserResponse(User user) {
        return AdminUserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .displayName(user.getDisplayName())
            .phoneNumber(user.getPhoneNumber())
            .birthday(user.getBirthday())
            .avatarUrl(user.getAvatarUrl())
            .status(user.getStatus())
            .role(user.getRole())
            .isBlocked(user.getIsBlocked())
            .lastLoginAt(user.getLastLoginAt())
            .loginCount(user.getLoginCount())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
