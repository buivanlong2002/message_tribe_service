package com.example.message_service.repository;

import com.example.message_service.model.PasswordResetOTP;
import com.example.message_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetOTPRepository extends JpaRepository<PasswordResetOTP, Long> {
    Optional<PasswordResetOTP> findByOtp(String otp);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetOTP o WHERE o.user = :user")
    void deleteByUser(User user);
} 