package com.example.message_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.response.UserResponse;
import com.example.message_service.model.Notification;
import com.example.message_service.model.User;
import com.example.message_service.service.NotificationService;
import com.example.message_service.service.UserService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    // lấy tất cả thông báo của người dùng hiện tại
    @GetMapping("/my-notifications")
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications() throws Exception {

        UserResponse currentUser = userService.getCurrentUser();
        User user = userService.getUserById(currentUser.getId());
        List<Notification> notifications = notificationService.getUserNotifications(user);

        return ResponseEntity.ok(ApiResponse.success("00", "Lấy thông báo thành công", notifications));
    }

    // đánh dấu đã đọc thông báo
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<String>> markNotificationAsRead(@PathVariable Long notificationId)
            throws Exception {
        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success("00", "Đánh dấu thông báo đã đọc thành công"));
    }
}