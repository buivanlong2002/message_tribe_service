package com.example.message_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ai nhận thông báo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User receiver;

    // Loại thông báo (dùng String cho linh hoạt)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    // Nội dung thông báo
    @Column(nullable = false, length = 500)
    private String content;

    // Trạng thái đã đọc
    @Column(nullable = false)
    private boolean isRead = false;

    // Thời điểm tạo
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
