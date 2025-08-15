package com.example.message_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;


    public void sendOTPEmail(String to, String otp) {
        String subject = "Tribe - Mã OTP đặt lại mật khẩu";
        String text = "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản Tribe.\n\n"
                + "Mã OTP của bạn là: " + otp + "\n\n"
                + "Mã này sẽ hết hạn sau 5 phút.\n\n"
                + "Nếu bạn không yêu cầu, vui lòng bỏ qua email này.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
