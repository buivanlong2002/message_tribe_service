package com.example.message_service.repository;

import com.example.message_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByUserId(String userId);

    List<Notification> findByUserIdAndRead(String userId, boolean isRead);
}
