# Tóm tắt sửa lỗi API Posts

## Vấn đề gốc
- Frontend gọi API `/api/posts/17/like` nhưng backend không có endpoint này
- Lỗi 500 do mapping không đúng giữa frontend và backend

## Các sửa lỗi đã thực hiện

### 1. Backend - Sửa mapping trong Services

#### PostService.java
- Thêm `setAvatarSender()` trong `convertToResponse()`
- Thêm null check cho `displayName`: `post.getUser().getDisplayName() != null ? post.getUser().getDisplayName() : "Người dùng"`

#### PostCommentService.java  
- Thêm `setAvatarSender()` trong `convertToResponse()`
- Sửa `getNameSender()` từ `getUsername()` thành `getDisplayName()`
- Thêm null check cho `displayName`

#### PostReactionService.java
- Thêm `setAvatarSender()` trong `convertToResponse()`
- Sửa `getNameSender()` từ `getUsername()` thành `getDisplayName()`
- Thêm null check cho `displayName`

### 2. Frontend - Sửa API endpoints

#### postService.js
- **toggleLike()**: Thay đổi từ `/posts/${postId}/like` thành:
  - Kiểm tra reaction: `/post-reactions/check`
  - Tạo reaction: `/post-reactions/create`
  - Xóa reaction: `/post-reactions/delete`

- **getLikes()**: Thay đổi từ `/posts/${postId}/likes` thành `/post-reactions/post/${postId}`

- **getComments()**: Thay đổi từ `/posts/${postId}/comments` thành `/post-comments/post/${postId}`

- **deletePost()**: Thêm `userId` parameter

- **updatePost()**: 
  - Thêm `userId` parameter
  - Hỗ trợ cả multipart và simple endpoints
  - Sử dụng `/posts/${postId}/simple` cho update không có file

## Endpoints mapping

### Posts
- `GET /api/posts/all` - Lấy tất cả posts
- `GET /api/posts/{postId}` - Lấy post theo ID
- `POST /api/posts/create` - Tạo post với file
- `POST /api/posts/create-simple` - Tạo post không có file
- `PUT /api/posts/{postId}` - Cập nhật post với file
- `PUT /api/posts/{postId}/simple` - Cập nhật post không có file
- `DELETE /api/posts/{postId}` - Xóa post

### Reactions
- `POST /api/post-reactions/create` - Tạo reaction
- `PUT /api/post-reactions/update` - Cập nhật reaction
- `DELETE /api/post-reactions/delete` - Xóa reaction
- `GET /api/post-reactions/post/{postId}` - Lấy reactions của post
- `GET /api/post-reactions/check` - Kiểm tra user đã reaction chưa
- `GET /api/post-reactions/count/{postId}` - Đếm reactions

### Comments
- `POST /api/post-comments/create` - Tạo comment
- `PUT /api/post-comments/update` - Cập nhật comment
- `DELETE /api/post-comments/delete` - Xóa comment
- `GET /api/post-comments/post/{postId}` - Lấy comments của post

## Cấu trúc Response

### PostResponse
```json
{
  "id": 1,
  "user": {
    "senderId": "user-id",
    "nameSender": "Tên người dùng",
    "avatarSender": "avatar-url"
  },
  "content": "Nội dung bài viết",
  "visibility": "PUBLIC",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00",
  "reactionCount": 5,
  "commentCount": 3,
  "reactions": [...],
  "comments": [...],
  "mediaUrls": [...]
}
```

### SenderResponse
```json
{
  "senderId": "user-id",
  "nameSender": "Tên người dùng", 
  "avatarSender": "avatar-url"
}
```

## Lưu ý
- Tất cả endpoints đều trả về `ApiResponse<T>` với cấu trúc:
  ```json
  {
    "status": {
      "success": true,
      "code": "00",
      "displayMessage": "Thành công"
    },
    "data": {...}
  }
  ```
- User ID được lấy từ `localStorage` trong frontend
- Các field null được xử lý với giá trị mặc định




