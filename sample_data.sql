-- =====================================================
-- SAMPLE DATA FOR MESSAGE TRIBE SERVICE
-- Mỗi bảng gồm 5 hàng dữ liệu mẫu
-- =====================================================

-- 1. BẢNG USERS
INSERT INTO users (id, password, display_name, avatar_url, phone_number, birthday, email, status, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Nguyễn Văn An', '/uploads/avatar/avatar1.jpg', '0123456789', '1990-05-15', 'an.nguyen@email.com', 'active', '2024-01-15 08:30:00', '2024-01-15 08:30:00'),
('550e8400-e29b-41d4-a716-446655440002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Trần Thị Bình', '/uploads/avatar/avatar2.jpg', '0987654321', '1992-08-20', 'binh.tran@email.com', 'active', '2024-01-16 09:15:00', '2024-01-16 09:15:00'),
('550e8400-e29b-41d4-a716-446655440003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Lê Văn Cường', '/uploads/avatar/avatar3.jpg', '0369852147', '1988-12-10', 'cuong.le@email.com', 'active', '2024-01-17 10:45:00', '2024-01-17 10:45:00'),
('550e8400-e29b-41d4-a716-446655440004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Phạm Thị Dung', '/uploads/avatar/avatar4.jpg', '0521478963', '1995-03-25', 'dung.pham@email.com', 'active', '2024-01-18 11:20:00', '2024-01-18 11:20:00'),
('550e8400-e29b-41d4-a716-446655440005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Hoàng Văn Em', '/uploads/avatar/avatar5.jpg', '0741258963', '1993-07-08', 'em.hoang@email.com', 'active', '2024-01-19 14:30:00', '2024-01-19 14:30:00');

-- 2. BẢNG CONVERSATIONS
INSERT INTO conversations (id, name, created_by, is_group, created_at, is_archived, avatar_url) VALUES
('conv-001', 'Nhóm bạn thân', '550e8400-e29b-41d4-a716-446655440001', 1, '2024-01-20 08:00:00', 0, '/uploads/conversations/group1.jpg'),
('conv-002', NULL, '550e8400-e29b-41d4-a716-446655440001', 0, '2024-01-20 09:00:00', 0, NULL),
('conv-003', 'Nhóm học tập', '550e8400-e29b-41d4-a716-446655440002', 1, '2024-01-21 10:00:00', 0, '/uploads/conversations/group2.jpg'),
('conv-004', NULL, '550e8400-e29b-41d4-a716-446655440003', 0, '2024-01-21 11:00:00', 0, NULL),
('conv-005', 'Nhóm gia đình', '550e8400-e29b-41d4-a716-446655440004', 1, '2024-01-22 12:00:00', 0, '/uploads/conversations/group3.jpg');

-- 3. BẢNG CONVERSATION_MEMBERS
INSERT INTO conversation_members (id, conversation_id, user_id, joined_at, role) VALUES
('cm-001', 'conv-001', '550e8400-e29b-41d4-a716-446655440001', '2024-01-20 08:00:00', 'admin'),
('cm-002', 'conv-001', '550e8400-e29b-41d4-a716-446655440002', '2024-01-20 08:05:00', 'member'),
('cm-003', 'conv-001', '550e8400-e29b-41d4-a716-446655440003', '2024-01-20 08:10:00', 'member'),
('cm-004', 'conv-002', '550e8400-e29b-41d4-a716-446655440001', '2024-01-20 09:00:00', 'member'),
('cm-005', 'conv-002', '550e8400-e29b-41d4-a716-446655440002', '2024-01-20 09:00:00', 'member');

-- 4. BẢNG MESSAGES
INSERT INTO messages (id, conversation_id, sender_id, content, message_type, created_at, reply_to, edited, seen, recalled) VALUES
('msg-001', 'conv-001', '550e8400-e29b-41d4-a716-446655440001', 'Chào mọi người!', 'TEXT', '2024-01-20 08:30:00', NULL, 0, 1, 0),
('msg-002', 'conv-001', '550e8400-e29b-41d4-a716-446655440002', 'Chào An!', 'TEXT', '2024-01-20 08:32:00', NULL, 0, 1, 0),
('msg-003', 'conv-002', '550e8400-e29b-41d4-a716-446655440001', 'Bạn có rảnh không?', 'TEXT', '2024-01-20 09:15:00', NULL, 0, 1, 0),
('msg-004', 'conv-002', '550e8400-e29b-41d4-a716-446655440002', 'Có, có gì không?', 'TEXT', '2024-01-20 09:17:00', NULL, 0, 1, 0),
('msg-005', 'conv-001', '550e8400-e29b-41d4-a716-446655440003', 'Hôm nay đi ăn không?', 'TEXT', '2024-01-20 10:00:00', NULL, 0, 0, 0);

-- 5. BẢNG MESSAGE_STATUS
INSERT INTO message_status (id, message_id, user_id, status, created_at, updated_at) VALUES
('ms-001', 'msg-001', '550e8400-e29b-41d4-a716-446655440002', 'SEEN', '2024-01-20 08:30:00', '2024-01-20 08:32:00'),
('ms-002', 'msg-001', '550e8400-e29b-41d4-a716-446655440003', 'SEEN', '2024-01-20 08:30:00', '2024-01-20 08:35:00'),
('ms-003', 'msg-002', '550e8400-e29b-41d4-a716-446655440001', 'SEEN', '2024-01-20 08:32:00', '2024-01-20 08:33:00'),
('ms-004', 'msg-003', '550e8400-e29b-41d4-a716-446655440002', 'SEEN', '2024-01-20 09:15:00', '2024-01-20 09:17:00'),
('ms-005', 'msg-004', '550e8400-e29b-41d4-a716-446655440001', 'SEEN', '2024-01-20 09:17:00', '2024-01-20 09:18:00');

-- 6. BẢNG ATTACHMENTS
INSERT INTO attachments (id, message_id, file_url, file_type, file_size, original_file_name) VALUES
('att-001', 'msg-001', '/uploads/attachments/image1.jpg', 'image/jpeg', 1024000, 'photo1.jpg'),
('att-002', 'msg-002', '/uploads/attachments/document1.pdf', 'application/pdf', 2048000, 'document.pdf'),
('att-003', 'msg-003', '/uploads/attachments/video1.mp4', 'video/mp4', 5120000, 'video.mp4'),
('att-004', 'msg-004', '/uploads/attachments/image2.png', 'image/png', 1536000, 'screenshot.png'),
('att-005', 'msg-005', '/uploads/attachments/file1.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 3072000, 'report.docx');

-- 7. BẢNG FRIENDSHIPS
INSERT INTO friendships (id, user_id, friend_id, status, requested_at, accepted_at) VALUES
('fr-001', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', 'accepted', '2024-01-15 10:00:00', '2024-01-15 10:30:00'),
('fr-002', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440003', 'accepted', '2024-01-16 11:00:00', '2024-01-16 11:15:00'),
('fr-003', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440004', 'pending', '2024-01-17 12:00:00', NULL),
('fr-004', '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440005', 'accepted', '2024-01-18 13:00:00', '2024-01-18 13:45:00'),
('fr-005', '550e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440005', 'blocked', '2024-01-19 14:00:00', NULL);

-- 8. BẢNG POSTS
INSERT INTO posts (id, user_id, content, visibility, created_at, updated_at, deleted_at) VALUES
(1, '550e8400-e29b-41d4-a716-446655440001', 'Hôm nay là một ngày đẹp trời!', 'PUBLIC', '2024-01-20 08:00:00', '2024-01-20 08:00:00', NULL),
(2, '550e8400-e29b-41d4-a716-446655440002', 'Vừa hoàn thành xong dự án mới', 'FRIENDS', '2024-01-21 09:00:00', '2024-01-21 09:00:00', NULL),
(3, '550e8400-e29b-41d4-a716-446655440003', 'Đi du lịch cùng gia đình', 'PUBLIC', '2024-01-22 10:00:00', '2024-01-22 10:00:00', NULL),
(4, '550e8400-e29b-41d4-a716-446655440004', 'Chia sẻ công thức nấu ăn mới', 'FRIENDS', '2024-01-23 11:00:00', '2024-01-23 11:00:00', NULL),
(5, '550e8400-e29b-41d4-a716-446655440005', 'Cảm ơn mọi người đã ủng hộ', 'PRIVATE', '2024-01-24 12:00:00', '2024-01-24 12:00:00', NULL);

-- 9. BẢNG POST_COMMENTS
INSERT INTO post_comments (id, post_id, user_id, content, parent_comment_id, created_at, deleted_at) VALUES
(1, 1, '550e8400-e29b-41d4-a716-446655440002', 'Đúng vậy, thời tiết hôm nay thật đẹp!', NULL, '2024-01-20 08:30:00', NULL),
(2, 1, '550e8400-e29b-41d4-a716-446655440003', 'Chúc bạn một ngày tốt lành!', NULL, '2024-01-20 08:45:00', NULL),
(3, 2, '550e8400-e29b-41d4-a716-446655440001', 'Chúc mừng bạn!', NULL, '2024-01-21 09:15:00', NULL),
(4, 3, '550e8400-e29b-41d4-a716-446655440004', 'Chuyến đi thế nào?', NULL, '2024-01-22 10:30:00', NULL),
(5, 4, '550e8400-e29b-41d4-a716-446655440005', 'Công thức trông ngon quá!', NULL, '2024-01-23 11:30:00', NULL);

-- 10. BẢNG POST_REACTIONS
INSERT INTO post_reactions (id, post_id, user_id, reaction_type, created_at) VALUES
(1, 1, '550e8400-e29b-41d4-a716-446655440002', 'LIKE', '2024-01-20 08:25:00'),
(2, 1, '550e8400-e29b-41d4-a716-446655440003', 'LOVE', '2024-01-20 08:26:00'),
(3, 2, '550e8400-e29b-41d4-a716-446655440001', 'LIKE', '2024-01-21 09:10:00'),
(4, 3, '550e8400-e29b-41d4-a716-446655440004', 'WOW', '2024-01-22 10:15:00'),
(5, 4, '550e8400-e29b-41d4-a716-446655440005', 'LIKE', '2024-01-23 11:20:00');

-- 11. BẢNG POST_MEDIA
INSERT INTO post_media (id, post_id, media_url, media_type) VALUES
(1, 1, '/uploads/posts/image1.jpg', 'image/jpeg'),
(2, 2, '/uploads/posts/document1.pdf', 'application/pdf'),
(3, 3, '/uploads/posts/video1.mp4', 'video/mp4'),
(4, 4, '/uploads/posts/image2.png', 'image/png'),
(5, 5, '/uploads/posts/image3.jpg', 'image/jpeg');

-- 12. BẢNG NOTIFICATIONS
INSERT INTO notifications (id, receiver_id, type, content, is_read, created_at) VALUES
(1, '550e8400-e29b-41d4-a716-446655440001', 'REACTION_POST', 'Trần Thị Bình đã thích bài viết của bạn', 0, '2024-01-20 08:25:00'),
(2, '550e8400-e29b-41d4-a716-446655440001', 'COMMENT_POST', 'Lê Văn Cường đã bình luận bài viết của bạn', 0, '2024-01-20 08:30:00'),
(3, '550e8400-e29b-41d4-a716-446655440002', 'FRIEND_REQUEST', 'Phạm Thị Dung đã gửi lời mời kết bạn', 0, '2024-01-17 12:00:00'),
(4, '550e8400-e29b-41d4-a716-446655440003', 'FRIEND_ACCEPTED', 'Hoàng Văn Em đã chấp nhận lời mời kết bạn', 1, '2024-01-18 13:45:00'),
(5, '550e8400-e29b-41d4-a716-446655440004', 'REPLY_COMMENT', 'Hoàng Văn Em đã trả lời bình luận của bạn', 0, '2024-01-23 11:35:00');

-- 13. BẢNG PASSWORD_RESET_OTP
INSERT INTO password_reset_otp (id, otp, expiry_date, user_id) VALUES
(1, '123456', '2024-01-20 10:00:00', '550e8400-e29b-41d4-a716-446655440001'),
(2, '654321', '2024-01-21 11:00:00', '550e8400-e29b-41d4-a716-446655440002'),
(3, '789012', '2024-01-22 12:00:00', '550e8400-e29b-41d4-a716-446655440003'),
(4, '345678', '2024-01-23 13:00:00', '550e8400-e29b-41d4-a716-446655440004'),
(5, '901234', '2024-01-24 14:00:00', '550e8400-e29b-41d4-a716-446655440005');

-- 14. BẢNG PASSWORD_RESET_TOKEN
INSERT INTO password_reset_token (id, token, expiry_date, user_id) VALUES
(1, 'token-abc123', '2024-01-20 10:00:00', '550e8400-e29b-41d4-a716-446655440001'),
(2, 'token-def456', '2024-01-21 11:00:00', '550e8400-e29b-41d4-a716-446655440002'),
(3, 'token-ghi789', '2024-01-22 12:00:00', '550e8400-e29b-41d4-a716-446655440003'),
(4, 'token-jkl012', '2024-01-23 13:00:00', '550e8400-e29b-41d4-a716-446655440004'),
(5, 'token-mno345', '2024-01-24 14:00:00', '550e8400-e29b-41d4-a716-446655440005');

-- =====================================================
-- GHI CHÚ:
-- 1. Password được mã hóa bằng BCrypt với giá trị gốc là "password123"
-- 2. Các UUID được tạo theo format chuẩn
-- 3. Timestamps được tạo theo thứ tự logic
-- 4. Dữ liệu có mối quan hệ hợp lý giữa các bảng
-- 5. Các enum values được sử dụng đúng định dạng
-- ===================================================== 