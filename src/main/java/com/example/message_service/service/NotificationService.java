package com.example.message_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.message_service.model.Notification;
import com.example.message_service.model.NotificationType;
import com.example.message_service.model.User;
import com.example.message_service.repository.NotificationRepository;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Thêm thông báo
    public Notification createNotification(NotificationType type, User receiver, String content) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setReceiver(receiver);
        notification.setContent(content);
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
