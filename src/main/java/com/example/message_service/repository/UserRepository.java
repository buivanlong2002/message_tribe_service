package com.example.message_service.repository;

import com.example.message_service.model.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.message_service.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Tìm người dùng theo email
    Optional<User> findByEmail(String email);

    // Tìm người dùng theo số điện thoại
    Optional<User> findByPhoneNumber(String phoneNumber);

    // Tìm kiếm người dùng theo email (tìm kiếm tương đối)
    @Query("SELECT u FROM User u WHERE u.email LIKE %:email%")
    List<User> searchByEmail(@Param("email") String email);

    // Tìm người dùng theo tên hiển thị (không phân biệt hoa thường)
    List<User> findByDisplayNameContainingIgnoreCase(String displayName);

    // Tìm người dùng theo tên hiển thị với phân trang
    Page<User> findByDisplayNameContainingIgnoreCase(String displayName, Pageable pageable);

    // Tìm người dùng theo tên hiển thị, trạng thái và trạng thái bị chặn với phân trang
    Page<User> findByDisplayNameContainingIgnoreCaseAndStatusAndIsBlocked(String displayName, String status, Boolean isBlocked, Pageable pageable);

    // Tìm người dùng theo trạng thái và trạng thái bị chặn với phân trang
    Page<User> findByStatusAndIsBlocked(String status, Boolean isBlocked, Pageable pageable);

    // Đếm số người dùng theo trạng thái và trạng thái bị chặn
    Long countByStatusAndIsBlocked(String status, Boolean isBlocked);

    // Đếm số người dùng bị chặn
    Long countByIsBlocked(Boolean isBlocked);

    // Đếm số người dùng được tạo sau thời điểm nhất định
    Long countByCreatedAtAfter(LocalDateTime dateTime);

    // Đếm số người dùng theo vai trò
    Long countByRole(UserRole role);

}
