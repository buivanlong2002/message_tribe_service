package com.example.message_service.controller;

import com.example.message_service.components.JwtTokenUtil;
import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.LoginRequest;
import com.example.message_service.dto.request.RegisterRequest;
import com.example.message_service.dto.request.ResetPasswordAfterOTPRequest;
import com.example.message_service.dto.request.ResetPasswordWithOTPRequest;
import com.example.message_service.dto.request.VerifyOTPRequest;
import com.example.message_service.dto.response.UserResponse;
import com.example.message_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // ==== 1. Đăng nhập ====
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            ApiResponse<String> response = userService.loginUser(
                    loginRequest.getEmail(),
                    loginRequest.getPassword());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("99", "Lỗi hệ thống"));
        }
    }

    // ==== 2. Đăng ký ====
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    // ==== 3. Lấy user theo ID ====
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getByUserId(id));
    }

    // ==== 4. Logout ====
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("02", "Token không hợp lệ"));
            }
            String token = authHeader.substring(7);
            String username = jwtTokenUtil.extractUsername(token);
            return ResponseEntity.ok(userService.logoutUser(username, token));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("99", "Lỗi hệ thống"));
        }
    }

    // ==== 5. Quên mật khẩu - Gửi OTP ====
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        return ResponseEntity.ok(userService.requestPasswordReset(email));
    }

    // ==== 6. Xác thực OTP ====
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOTP(@Valid @RequestBody VerifyOTPRequest request) {
        return ResponseEntity.ok(userService.verifyOTP(request.getEmail(), request.getOtp()));
    }

    // ==== 7. Đặt lại mật khẩu bằng OTP ====
    @PostMapping("/reset-password-otp")
    public ResponseEntity<ApiResponse<String>> resetPasswordWithOTP(
            @Valid @RequestBody ResetPasswordWithOTPRequest request) {
        return ResponseEntity
                .ok(userService.resetPasswordWithOTP(request.getEmail(), request.getOtp(), request.getNewPassword()));
    }

    // ==== 8. Đặt lại mật khẩu sau khi verify OTP ====
    @PostMapping("/reset-password-after-otp")
    public ResponseEntity<ApiResponse<String>> resetPasswordAfterOTP(
            @Valid @RequestBody ResetPasswordAfterOTPRequest request) {
        return ResponseEntity
                .ok(userService.resetPasswordAfterOTPVerification(request.getEmail(), request.getNewPassword()));
    }

    // ==== 9. Lấy thông tin người dùng hiện tại ====
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        try {
            UserResponse userResponse = userService.getCurrentUser();
            ApiResponse<UserResponse> response = ApiResponse.success("00", "Lấy thông tin thành công", userResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Xử lý các loại lỗi khác nhau
            String errorCode;
            String errorMessage = e.getMessage();

            if (errorMessage.contains("chưa đăng nhập")) {
                errorCode = "01";
            } else if (errorMessage.contains("Email không hợp lệ")) {
                errorCode = "02";
            } else if (errorMessage.contains("User không tồn tại")) {
                errorCode = "03";
            } else {
                errorCode = "99";
                errorMessage = "Lỗi hệ thống: " + errorMessage;
            }

            ApiResponse<UserResponse> errorResponse = ApiResponse.error(errorCode, errorMessage);
            return ResponseEntity.ok(errorResponse);
        }
    }

}
