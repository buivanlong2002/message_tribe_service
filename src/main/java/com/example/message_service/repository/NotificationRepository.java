package com.example.message_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.message_service.model.Notification;
import com.example.message_service.model.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Tìm tất cả thông báo của người dùng theo trạng thái đã đọc
    List<Notification> findByReceiverAndIsRead(User receiver, boolean isRead);

    // Xóa thông báo theo người nhận
    void deleteByReceiver(User receiver);

    // Tìm tất cả thông báo của người dùng
    List<Notification> findByReceiver(User receiver);
}
