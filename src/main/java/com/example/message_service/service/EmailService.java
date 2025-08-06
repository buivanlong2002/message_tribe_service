package com.example.message_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetPasswordEmail(String to, String token) {
        // Link đến trang reset-password.html trên frontend
        String resetLink = "https://tomotalk.netlify.app/reset-password.html?token=" + token;

        String subject = "TomoTalk - Đặt lại mật khẩu";
        String text = "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản TomoTalk.\n\n"
                + "Nhấn vào liên kết sau để thiết lập mật khẩu mới:\n"
                + resetLink + "\n\n"
                + "Liên kết này sẽ hết hạn sau 30 phút.\n\n"
                + "Nếu bạn không yêu cầu, vui lòng bỏ qua email này.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
