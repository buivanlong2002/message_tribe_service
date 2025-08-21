package com.example.message_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_messages")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserMessage {

    @Id
    private String id;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "message_id", nullable = false)
    private String messageId;

    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
