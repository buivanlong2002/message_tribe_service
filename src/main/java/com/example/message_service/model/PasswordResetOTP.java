package com.example.message_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetOTP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String otp;

    private LocalDateTime expiryDate;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
} 