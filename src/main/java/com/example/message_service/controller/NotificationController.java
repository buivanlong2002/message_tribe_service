package com.example.message_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.model.Notification;
import com.example.message_service.model.User;
import com.example.message_service.service.NotificationService;
import com.example.message_service.service.UserService;

@RestController
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    // lấy tất cả thông báo của người dùng
    @GetMapping("/notifications/{userId}")
    public ApiResponse<List<Notification>> getUserNotifications(@PathVariable("userId") String userId) {
        User user = userService.getUserById(userId);
        List<Notification> notifications = notificationService.getUserNotifications(user);
        return ApiResponse.success("00", "Lấy thông báo thành công", notifications);
    }
}