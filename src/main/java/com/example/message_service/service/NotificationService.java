package com.example.message_service.service;

import com.example.message_service.dto.ApiResponse;
import com.example.message_service.model.Notification;
import com.example.message_service.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Lấy tất cả thông báo của một người dùng
    public ApiResponse<List<Notification>> getNotificationsByUser(String userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        if (notifications.isEmpty()) {
            return ApiResponse.error("01", "Không có thông báo nào cho người dùng này");
        }
        return ApiResponse.success("00", "Lấy danh sách thông báo thành công", notifications);
    }

    // Lấy tất cả thông báo chưa đọc của một người dùng
    public ApiResponse<List<Notification>> getUnreadNotificationsByUser(String userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndRead(userId, false);
        if (unread.isEmpty()) {
            return ApiResponse.error("02", "Không có thông báo chưa đọc");
        }
        return ApiResponse.success("00", "Lấy thông báo chưa đọc thành công", unread);
    }

    // Đánh dấu thông báo là đã đọc
    public ApiResponse<Notification> markAsRead(String notificationId) {
        Optional<Notification> optional = notificationRepository.findById(notificationId);
        if (optional.isEmpty()) {
            return ApiResponse.error("03", "Không tìm thấy thông báo");
        }

        Notification notification = optional.get();
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        Notification updated = notificationRepository.save(notification);

        return ApiResponse.success("00", "Đã đánh dấu là đã đọc", updated);
    }

    // Thêm một thông báo mới
    public ApiResponse<Notification> addNotification(Notification notification) {
        // Đảm bảo gán ID và thời gian tạo nếu chưa có
        if (notification.getId() == null || notification.getId().isEmpty()) {
            notification.setId(UUID.randomUUID().toString());
        }
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }

        notification.setRead(false); // Mặc định là chưa đọc
        Notification saved = notificationRepository.save(notification);

        return ApiResponse.success("00", "Tạo thông báo thành công", saved);
    }
}
