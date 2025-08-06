package com.example.message_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Message {


    @Id
    private String id;

    @PrePersist
    public void generateId() {
        this.id = UUID.randomUUID().toString();
    }

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "reply_to")
    private Message replyTo;

    private boolean edited = false;
    private boolean seen = false;
    private boolean recalled = false;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Attachment> attachments = new ArrayList<>();


}
