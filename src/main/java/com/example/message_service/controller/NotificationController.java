package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.model.Notification;
import com.example.message_service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Lấy tất cả thông báo của một người dùng
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Notification>> getNotificationsByUser(@PathVariable String userId) {
        return notificationService.getNotificationsByUser(userId);
    }

    // Lấy tất cả thông báo chưa đọc của một người dùng
    @GetMapping("/user/{userId}/unread")
    public ApiResponse<List<Notification>> getUnreadNotificationsByUser(@PathVariable String userId) {
        return notificationService.getUnreadNotificationsByUser(userId);
    }

    // Đánh dấu một thông báo là đã đọc
    @PutMapping("/{notificationId}/read")
    public ApiResponse<Notification> markAsRead(@PathVariable String notificationId) {
        return notificationService.markAsRead(notificationId);
    }

    // Tạo mới một thông báo
    @PostMapping
    public ApiResponse<Notification> addNotification(@RequestBody Notification notification) {
        return notificationService.addNotification(notification);
    }
}
