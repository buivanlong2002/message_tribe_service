-- Migration script để thêm thông tin người gọi vào bảng call_history
-- Chạy script này trong database để thêm các columns mới

ALTER TABLE call_history ADD COLUMN caller_name VARCHAR(255);
ALTER TABLE call_history ADD COLUMN caller_avatar VARCHAR(500);
ALTER TABLE call_history ADD COLUMN receiver_name VARCHAR(255);
ALTER TABLE call_history ADD COLUMN receiver_avatar VARCHAR(500);

-- Cập nhật dữ liệu cũ (nếu có)
-- UPDATE call_history SET caller_name = 'Unknown', receiver_name = 'Unknown' WHERE caller_name IS NULL;
