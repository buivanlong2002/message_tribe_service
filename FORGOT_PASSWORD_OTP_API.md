# API Quên Mật Khẩu với OTP

## Luồng hoạt động mới:

1. **Nhấn "Quên mật khẩu"** → Chuyển sang trang nhập email
2. **Nhập email** → Gửi mã OTP về gmail
3. **Nhập mã OTP** → Xác thực OTP
4. **Nhập mật khẩu mới** → Đặt lại mật khẩu

## Các API Endpoints:

### 1. Gửi OTP về email
```
POST /api/auth/forgot-password
Content-Type: application/json

{
    "email": "user@example.com"
}
```

**Response thành công:**
```json
{
    "code": "00",
    "message": "Nếu email tồn tại, mã OTP đã được gửi.",
    "data": null
}
```

### 2. Xác thực OTP
```
POST /api/auth/verify-otp
Content-Type: application/json

{
    "email": "user@example.com",
    "otp": "123456"
}
```

**Response thành công:**
```json
{
    "code": "00",
    "message": "Mã OTP hợp lệ",
    "data": null
}
```

**Response lỗi:**
```json
{
    "code": "01",
    "message": "Email không tồn tại",
    "data": null
}
```
```json
{
    "code": "02",
    "message": "Mã OTP không hợp lệ",
    "data": null
}
```
```json
{
    "code": "04",
    "message": "Mã OTP đã hết hạn",
    "data": null
}
```

### 3. Đặt lại mật khẩu với OTP
```
POST /api/auth/reset-password-otp
Content-Type: application/json

{
    "email": "user@example.com",
    "otp": "123456",
    "newPassword": "newpassword123"
}
```

**Response thành công:**
```json
{
    "code": "00",
    "message": "Mật khẩu đã được đặt lại thành công",
    "data": null
}
```

## Thông tin kỹ thuật:

- **OTP**: 6 số ngẫu nhiên (000000-999999)
- **Thời gian hết hạn**: 5 phút
- **Email template**: Gửi qua SMTP Gmail
- **Bảo mật**: OTP bị xóa sau khi sử dụng

## Các file đã thay đổi:

1. **Model**: `PasswordResetOTP.java` (mới)
2. **Repository**: `PasswordResetOTPRepository.java` (mới)
3. **DTO**: 
   - `VerifyOTPRequest.java` (mới)
   - `ResetPasswordWithOTPRequest.java` (mới)
4. **Service**: 
   - `EmailService.java` - thêm method `sendOTPEmail()`
   - `UserService.java` - cập nhật logic OTP
5. **Controller**: `AuthController.java` - thêm endpoints mới

## Lưu ý:

- API cũ `/api/auth/reset-password` vẫn được giữ lại để tương thích ngược
- OTP được tạo ngẫu nhiên 6 số thay vì UUID
- Thời gian hết hạn giảm từ 30 phút xuống 5 phút
- Email template đã được cập nhật để hiển thị OTP thay vì link 