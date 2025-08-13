package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.BlockUserRequest;
import com.example.message_service.dto.request.ChangeUserRoleRequest;
import com.example.message_service.dto.response.AdminUserResponse;
import com.example.message_service.dto.response.UserStatisticsResponse;
import com.example.message_service.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * Lấy danh sách tất cả người dùng với phân trang và bộ lọc
     * @param page số trang (mặc định: 0)
     * @param size số lượng bản ghi trên mỗi trang (mặc định: 10)
     * @param search từ khóa tìm kiếm theo tên hiển thị
     * @param status trạng thái người dùng cần lọc
     * @return danh sách người dùng với thông tin admin
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        
        try {
            Page<AdminUserResponse> users = adminService.getAllUsers(page, size, search, status);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công", users));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách người dùng: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("01", "Lỗi khi lấy danh sách người dùng: " + e.getMessage()));
        }
    }

    /**
     * Lấy thông tin chi tiết của một người dùng
     * @param userId ID của người dùng
     * @return thông tin chi tiết của người dùng
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUserDetails(@PathVariable String userId) {
        try {
            AdminUserResponse user = adminService.getUserDetails(userId);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", user));
        } catch (RuntimeException e) {
            log.error("Không tìm thấy người dùng với ID: {}", userId);
            return ResponseEntity.badRequest().body(ApiResponse.error("02", "Không tìm thấy người dùng: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin người dùng: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("03", "Lỗi khi lấy thông tin người dùng: " + e.getMessage()));
        }
    }

    /**
     * Chặn hoặc bỏ chặn người dùng
     * @param userId ID của người dùng
     * @param request yêu cầu chứa thông tin trạng thái chặn
     * @return thông tin người dùng sau khi cập nhật
     */
    @PutMapping("/users/{userId}/block")
    public ResponseEntity<ApiResponse<AdminUserResponse>> blockUser(
            @PathVariable String userId,
            @RequestBody BlockUserRequest request) {
        
        try {
            AdminUserResponse user = adminService.blockUser(userId, request);
            String action = request.getIsBlocked() ? "chặn" : "bỏ chặn";
            return ResponseEntity.ok(ApiResponse.success("Đã " + action + " người dùng thành công", user));
        } catch (RuntimeException e) {
            log.error("Không tìm thấy người dùng với ID: {}", userId);
            return ResponseEntity.badRequest().body(ApiResponse.error("04", "Không tìm thấy người dùng: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi thay đổi trạng thái chặn người dùng: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("05", "Lỗi khi thay đổi trạng thái chặn người dùng: " + e.getMessage()));
        }
    }

    /**
     * Thay đổi vai trò của người dùng
     * @param userId ID của người dùng
     * @param request yêu cầu chứa vai trò mới
     * @return thông tin người dùng sau khi cập nhật
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponse<AdminUserResponse>> changeUserRole(
            @PathVariable String userId,
            @RequestBody ChangeUserRoleRequest request) {
        
        try {
            AdminUserResponse user = adminService.changeUserRole(userId, request);
            return ResponseEntity.ok(ApiResponse.success("Đã thay đổi vai trò người dùng thành công", user));
        } catch (RuntimeException e) {
            log.error("Không tìm thấy người dùng với ID: {}", userId);
            return ResponseEntity.badRequest().body(ApiResponse.error("06", "Không tìm thấy người dùng: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi thay đổi vai trò người dùng: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("07", "Lỗi khi thay đổi vai trò người dùng: " + e.getMessage()));
        }
    }

    /**
     * Lấy thống kê tổng quan về người dùng
     * @return thông tin thống kê người dùng
     */
    @GetMapping("/statistics/users")
    public ResponseEntity<ApiResponse<UserStatisticsResponse>> getUserStatistics() {
        try {
            UserStatisticsResponse statistics = adminService.getUserStatistics();
            return ResponseEntity.ok(ApiResponse.success("Lấy thống kê người dùng thành công", statistics));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê người dùng: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("08", "Lỗi khi lấy thống kê người dùng: " + e.getMessage()));
        }
    }
}
