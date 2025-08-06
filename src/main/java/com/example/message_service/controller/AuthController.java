package com.example.message_service.controller;

import com.example.message_service.components.JwtTokenUtil;
import com.example.message_service.dto.ApiResponse;
import com.example.message_service.dto.request.LoginRequest;
import com.example.message_service.dto.request.RegisterRequest;
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
                    loginRequest.getPassword()
            );
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

    // ==== 5. Quên mật khẩu ====
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        return ResponseEntity.ok(userService.requestPasswordReset(email));
    }

    // ==== 6. Đặt lại mật khẩu bằng token ====
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        boolean success = userService.resetPassword(token, newPassword);
        if (!success) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("01", "Token không hợp lệ hoặc đã hết hạn"));
        }
        return ResponseEntity.ok(ApiResponse.success("00", "Mật khẩu đã được đặt lại thành công"));
    }
}
