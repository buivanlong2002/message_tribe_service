package com.example.message_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "friendships")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Friendship {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User sender;

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    @JsonIgnore
    private User receiver;

    @Column(nullable = false)
    private String status = "pending";

    @Column(nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    private LocalDateTime acceptedAt;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
}
