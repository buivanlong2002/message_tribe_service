package com.example.message_service.service;

import java.util.List;

import com.example.message_service.model.Notification;
import com.example.message_service.model.User;
import com.example.message_service.repository.NotificationRepository;

public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Thêm thông báo
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    // Lấy tất cả thông báo của người dùng
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByReceiver(user);
    }

    // Đánh dấu thông báo là đã đọc
    public void markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // Xóa thông báo
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}
