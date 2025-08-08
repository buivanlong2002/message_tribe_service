# API Documentation

## 1. UserController (9 APIs)
- **POST** `/api/users/avatar` - Upload avatar
- **GET** `/api/users/profile` - Lấy profile người dùng hiện tại
- **PUT** `/api/users/profile` - Cập nhật thông tin người dùng
- **GET** `/api/users/{userId}` - Lấy người dùng theo ID
- **GET** `/api/users/search` - Tìm người dùng theo email
- **GET** `/api/users/find` - Tìm chính xác người dùng theo email
- **PUT** `/api/users/change-password` - Đổi mật khẩu
- **GET** `/api/users/search-by-name` - Tìm người dùng theo tên hiển thị

---

## 2. PostController (11 APIs)
- **POST** `/api/posts/create` - Tạo post với file
- **POST** `/api/posts/create-simple` - Tạo post đơn giản
- **GET** `/api/posts/{postId}` - Lấy post theo ID
- **GET** `/api/posts/all` - Lấy tất cả posts với pagination
- **GET** `/api/posts/user/{userId}` - Lấy posts của user
- **GET** `/api/posts/visibility/{visibility}` - Lấy posts theo visibility
- **PUT** `/api/posts/{postId}` - Cập nhật post với file
- **PUT** `/api/posts/{postId}/simple` - Cập nhật post đơn giản
- **DELETE** `/api/posts/{postId}` - Xóa post
- **GET** `/api/posts/search` - Tìm kiếm posts
- **DELETE** `/api/posts/{postId}/media/{mediaId}` - Xóa media của post

---

## 3. MessageController (9 APIs)
- **POST** `/api/messages/send` - Gửi tin nhắn mới
- **GET** `/api/messages/get-by-conversation` - Lấy tin nhắn theo conversation
- **POST** `/api/messages/get-by-id` - Lấy tin nhắn theo ID
- **POST** `/api/messages/get-by-sender` - Lấy tin nhắn theo người gửi
- **POST** `/api/messages/edit` - Chỉnh sửa tin nhắn
- **PUT** `/api/messages/{id}/seen` - Đánh dấu đã xem
- **PUT** `/api/messages/{id}/recall` - Thu hồi tin nhắn
- **GET** `/api/messages/search` - Tìm kiếm tin nhắn

---

## 4. ConversationController (6 APIs)
- **POST** `/api/conversations/create-group` - Tạo nhóm mới
- **POST** `/api/conversations/one-to-one` - Tạo hoặc lấy cuộc trò chuyện 1-1
- **PUT** `/api/conversations/{conversationId}/update` - Cập nhật conversation
- **PUT** `/api/conversations/{conversationId}/archive` - Lưu trữ conversation
- **POST** `/api/conversations/groups/{id}/avatar` - Upload avatar nhóm
- **GET** `/api/conversations/user/{userId}` - Lấy conversations của user

---

## 5. AuthController (6 APIs)
- **POST** `/api/auth/login` - Đăng nhập
- **POST** `/api/auth/register` - Đăng ký
- **GET** `/api/auth/{id}` - Lấy user theo ID
- **POST** `/api/auth/logout` - Đăng xuất
- **POST** `/api/auth/forgot-password` - Quên mật khẩu
- **POST** `/api/auth/reset-password` - Đặt lại mật khẩu

---

## 6. FriendshipController (8 APIs)
- **POST** `/api/friendships/send` - Gửi lời mời kết bạn
- **POST** `/api/friendships/accept` - Chấp nhận lời mời kết bạn
- **POST** `/api/friendships/reject` - Từ chối lời mời kết bạn
- **GET** `/api/friendships/friends` - Lấy danh sách bạn bè
- **GET** `/api/friendships/friend-requests` - Lấy lời mời kết bạn đang chờ
- **GET** `/api/friendships/blocked-users` - Lấy danh sách người bị chặn
- **DELETE** `/api/friendships/unblock` - Bỏ chặn người dùng
- **DELETE** `/api/friendships/unfriend` - Hủy kết bạn
- **GET** `/api/friendships/sent-requests` - Lấy lời mời đã gửi

---

## 7. PostCommentController (8 APIs)
- **POST** `/api/post-comments/create` - Tạo comment mới
- **GET** `/api/post-comments/{commentId}` - Lấy comment theo ID
- **GET** `/api/post-comments/post/{postId}` - Lấy comments của post
- **GET** `/api/post-comments/{commentId}/replies` - Lấy replies của comment
- **GET** `/api/post-comments/user/{userId}` - Lấy comments của user
- **PUT** `/api/post-comments/{commentId}` - Cập nhật comment
- **DELETE** `/api/post-comments/{commentId}` - Xóa comment
- **GET** `/api/post-comments/search` - Tìm kiếm comments

---

## 8. PostReactionController (7 APIs)
- **POST** `/api/post-reactions/create` - Tạo reaction mới
- **PUT** `/api/post-reactions/update` - Cập nhật reaction
- **DELETE** `/api/post-reactions/delete` - Xóa reaction
- **GET** `/api/post-reactions/post/{postId}` - Lấy reactions của post
- **GET** `/api/post-reactions/user` - Lấy reaction của user trên post
- **GET** `/api/post-reactions/check` - Kiểm tra user đã reaction chưa
- **GET** `/api/post-reactions/count/{postId}` - Đếm số reactions của post

---

## 9. ConversationMemberController (3 APIs)
- **POST** `/api/conversation-members/add` - Thêm thành viên vào conversation
- **POST** `/api/conversation-members/members-by-conversation` - Lấy danh sách thành viên
- **POST** `/api/conversation-members/remove` - Xóa thành viên khỏi conversation

---

## 10. MessageStatusController (5 APIs)
- **GET** `/api/message-statuses/message/{messageId}` - Lấy trạng thái của tin nhắn
- **GET** `/api/message-statuses/user/{userId}/status/{status}` - Lấy trạng thái theo user và status
- **POST** `/api/message-statuses` - Thêm trạng thái mới
- **PUT** `/api/message-statuses/{messageStatusId}` - Cập nhật trạng thái
- **POST** `/api/message-statuses/mark-all-seen` - Đánh dấu tất cả đã xem

---

## 11. AttachmentController (3 APIs)
- **GET** `/api/attachments/message/{messageId}` - Lấy attachments của message
- **POST** `/api/attachments` - Thêm attachment mới
- **GET** `/api/attachments/conversation/{conversationId}` - Lấy attachments của conversation

---

## 12. WebSocketController (2 APIs - WebSocket)
- `/app/conversations/get` - Lấy danh sách conversations qua WebSocket
- `/app/messages/get` - Lấy messages qua WebSocket

---

## 13. NotificationController (4 APIs)
- **GET** `/api/notifications/user/{userId}` - Lấy thông báo của user
- **GET** `/api/notifications/user/{userId}/unread` - Lấy thông báo chưa đọc
- **PUT** `/api/notifications/{notificationId}/read` - Đánh dấu đã đọc
- **POST** `/api/notifications` - Tạo thông báo mới

---

## 14. ViewController (4 APIs - View pages)
- **GET** `/api/login` - Trang đăng nhập
- **GET** `/api/register` - Trang đăng ký
- **GET** `/api/profile` - Trang profile
- **GET** `/api/index` - Trang chủ

---

**Tổng cộng:** 89 API endpoints bao gồm REST APIs và WebSocket endpoints.  
Cung cấp đầy đủ chức năng cho hệ thống **Messaging** + **Social Networking**.
