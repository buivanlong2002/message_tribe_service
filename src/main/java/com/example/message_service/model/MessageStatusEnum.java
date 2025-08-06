package com.example.message_service.model;



public enum MessageStatusEnum {
    SENT,    // đã gửi
    SEEN,    // đã xem
    FAILED   // gửi hỏng (ví dụ do mạng, file lỗi, v.v.)
}
