package com.example.message_service.service;

import java.time.LocalDateTime;
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
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
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
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notificationRepository.deleteById(notificationId);
    }

    // Tạo thông báo cho friendship
    public Notification createFriendshipNotification(NotificationType type, User receiver, User sender, String action) {
        String content = sender.getDisplayName() + " " + action;
        return createNotification(type, receiver, content);
    }

    // Tạo thông báo cho lời mời kết bạn
    public Notification createFriendRequestNotification(User receiver, User sender) {
        return createFriendshipNotification(NotificationType.FRIEND_REQUEST, receiver, sender,
                "đã gửi lời mời kết bạn");
    }

    // Tạo thông báo cho việc chấp nhận lời mời kết bạn
    public Notification createFriendAcceptedNotification(User sender, User receiver) {
        return createFriendshipNotification(NotificationType.FRIEND_ACCEPTED, sender, receiver,
                "đã chấp nhận lời mời kết bạn của bạn");
    }
}