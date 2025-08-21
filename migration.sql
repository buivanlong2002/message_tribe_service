-- Migration script để thêm thông tin người gọi vào bảng call_history
-- Chạy script này trong database để thêm các columns mới

ALTER TABLE call_history ADD COLUMN caller_name VARCHAR(255);
ALTER TABLE call_history ADD COLUMN caller_avatar VARCHAR(500);
ALTER TABLE call_history ADD COLUMN receiver_name VARCHAR(255);
ALTER TABLE call_history ADD COLUMN receiver_avatar VARCHAR(500);

-- Cập nhật dữ liệu cũ (nếu có)
-- UPDATE call_history SET caller_name = 'Unknown', receiver_name = 'Unknown' WHERE caller_name IS NULL;

-- Migration để tạo bảng user_conversations
CREATE TABLE IF NOT EXISTS user_conversations (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    conversation_id VARCHAR(255) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_conversation (user_id, conversation_id)
);

-- Migration để tạo bảng user_messages
CREATE TABLE IF NOT EXISTS user_messages (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    message_id VARCHAR(255) NOT NULL,
    conversation_id VARCHAR(255) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_message (user_id, message_id)
);
