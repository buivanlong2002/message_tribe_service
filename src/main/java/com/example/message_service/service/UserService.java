package com.example.message_service.service;

import com.example.message_service.components.JwtTokenUtil;
import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.ChangePasswordRequest;
import com.example.message_service.dto.request.RegisterRequest;
import com.example.message_service.dto.request.UpdateProfileRequest;
import com.example.message_service.model.PasswordResetOTP;
import com.example.message_service.model.PasswordResetToken;
import com.example.message_service.model.User;
import com.example.message_service.repository.PasswordResetOTPRepository;
import com.example.message_service.repository.PasswordResetTokenRepository;
import com.example.message_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordResetOTPRepository passwordResetOTPRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private EmailService emailService;

    @Value("${user.avatar.upload-dir}")
    private String uploadDir;

    /**
     * Đăng nhập người dùng và sinh token
     */
    public ApiResponse<String> loginUser(String email, String password) throws Exception {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ApiResponse.error("01", "User không tồn tại");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ApiResponse.error("02", "Mật khẩu không đúng");
        }

        String token = jwtTokenUtil.generateToken(user);
        long expirationTime = jwtTokenUtil.getExpirationTime(token);

        return ApiResponse.success("00", "Đăng nhập thành công", token);
    }

    /**
     * Đăng ký người dùng mới
     */
    public ApiResponse<String> registerUser(RegisterRequest request) {
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            return ApiResponse.error("02", "Số điện thoại đã được sử dụng");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ApiResponse.error("03", "Email đã được sử dụng");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setPassword(encodedPassword);
        user.setDisplayName(request.getDisplayName());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setBirthday(request.getBirthday());

        userRepository.save(user);

        return ApiResponse.success("00", "Đăng ký thành công", null);
    }

    /**
     * Lấy thông tin người dùng theo ID
     */
    public ApiResponse<User> getByUserId(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return ApiResponse.error("01", "User không tồn tại");
        }

        return ApiResponse.success("00", userOptional.get());
    }

    public ApiResponse<String> logoutUser(String username, String token) {

        return ApiResponse.success("00", "Logout thành công", null);
    }

    public ResponseEntity<?> uploadAvatar(MultipartFile file, User user) {
        try {
            Path directory = Paths.get(uploadDir);
            Files.createDirectories(directory);

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = directory.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String avatarUrl = "/uploads/avatar/" + fileName;
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            return ResponseEntity.ok().body(avatarUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi khi upload avatar");
        }
    }

    // ========== QUÊN MẬT KHẨU =================

    @Transactional
    public ApiResponse<String> requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            // Trả về thành công luôn để tránh dò email
            return ApiResponse.success("00", "Nếu email tồn tại, mã OTP đã được gửi.");
        }

        User user = userOpt.get();

        // Xóa OTP cũ nếu có
        passwordResetOTPRepository.deleteByUser(user);

        // Tạo OTP mới (6 số)
        String otp = String.format("%06d", (int)(Math.random() * 1000000));
        PasswordResetOTP resetOTP = new PasswordResetOTP();
        resetOTP.setOtp(otp);
        resetOTP.setUser(user);
        resetOTP.setExpiryDate(LocalDateTime.now().plusMinutes(5));

        passwordResetOTPRepository.save(resetOTP);

        // Gửi email với OTP
        emailService.sendOTPEmail(email, otp);

        return ApiResponse.success("00", "Nếu email tồn tại, mã OTP đã được gửi.");
    }

    @Transactional
    public ApiResponse<String> verifyOTP(String email, String otp) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ApiResponse.error("01", "Email không tồn tại");
        }

        User user = userOpt.get();
        Optional<PasswordResetOTP> otpOpt = passwordResetOTPRepository.findByOtp(otp);

        if (otpOpt.isEmpty()) {
            return ApiResponse.error("02", "Mã OTP không hợp lệ");
        }

        PasswordResetOTP resetOTP = otpOpt.get();

        // Kiểm tra OTP có thuộc về user này không
        if (!resetOTP.getUser().getId().equals(user.getId())) {
            return ApiResponse.error("03", "Mã OTP không hợp lệ");
        }

        if (resetOTP.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ApiResponse.error("04", "Mã OTP đã hết hạn");
        }

        return ApiResponse.success("00", "Mã OTP hợp lệ");
    }

    @Transactional
    public ApiResponse<String> resetPasswordWithOTP(String email, String otp, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ApiResponse.error("01", "Email không tồn tại");
        }

        User user = userOpt.get();
        Optional<PasswordResetOTP> otpOpt = passwordResetOTPRepository.findByOtp(otp);

        if (otpOpt.isEmpty()) {
            return ApiResponse.error("02", "Mã OTP không hợp lệ");
        }

        PasswordResetOTP resetOTP = otpOpt.get();

        // Kiểm tra OTP có thuộc về user này không
        if (!resetOTP.getUser().getId().equals(user.getId())) {
            return ApiResponse.error("03", "Mã OTP không hợp lệ");
        }

        if (resetOTP.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ApiResponse.error("04", "Mã OTP đã hết hạn");
        }

        // Đặt lại mật khẩu
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Xoá OTP sau khi sử dụng
        passwordResetOTPRepository.delete(resetOTP);

        return ApiResponse.success("00", "Mật khẩu đã được đặt lại thành công");
    }

    // Giữ lại method cũ để tương thích ngược (nếu cần)
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);

        if (tokenOpt.isEmpty())
            return false;

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false; // Token hết hạn
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Xoá token sau khi sử dụng
        passwordResetTokenRepository.delete(resetToken);

        return true;
    }

    public List<User> searchByEmail(String email) {
        return userRepository.searchByEmail(email);
    }

    public ApiResponse<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return ApiResponse.error("02", "Email không được để trống");
        }

        String cleanEmail = email.trim().toLowerCase();

        Optional<User> userOpt = userRepository.findByEmail(cleanEmail);
        if (userOpt.isPresent()) {
            return ApiResponse.success("00", "Tìm thấy user", userOpt.get());
        } else {
            return ApiResponse.error("01", "User không tồn tại");
        }
    }

    @Transactional
    public ApiResponse<?> changePassword(User user, ChangePasswordRequest request) {
        try {
            // Validate input
            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                return ApiResponse.error("02", "Mật khẩu hiện tại không được để trống");
            }

            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return ApiResponse.error("03", "Mật khẩu mới không được để trống");
            }

            if (request.getNewPassword().length() < 6) {
                return ApiResponse.error("04", "Mật khẩu mới phải có ít nhất 6 ký tự");
            }

            // 1. Kiểm tra mật khẩu hiện tại có khớp không
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return ApiResponse.error("01", "Mật khẩu hiện tại không đúng");
            }

            // 2. Mã hóa và lưu mật khẩu mới
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            return ApiResponse.success("00", "Đổi mật khẩu thành công", null);
        } catch (Exception e) {
            return ApiResponse.error("99", "Lỗi hệ thống: " + e.getMessage());
        }
    }

    public User updateProfile(User user, UpdateProfileRequest request) {
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        return userRepository.save(user);
    }

    public List<User> searchByDisplayName(String displayName) {
        return userRepository.findByDisplayNameContainingIgnoreCase(displayName);
    }

    // lấy người dùng theo id
    public User getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

}
