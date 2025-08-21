# API Chuyển tiếp và Xóa Tin nhắn

## 1. Chuyển tiếp tin nhắn (Forward Message)

### Endpoint
```
POST /api/messages/forward
```

### Request Body
```json
{
  "messageId": "string",
  "senderId": "string", 
  "targetConversationIds": ["string"]
}
```

### Parameters
- `messageId`: ID của tin nhắn cần chuyển tiếp
- `senderId`: ID của người gửi tin nhắn chuyển tiếp
- `targetConversationIds`: Danh sách ID của các cuộc trò chuyện đích

### Response
```json
{
  "status": {
    "code": "00",
    "message": "Chuyển tiếp tin nhắn thành công"
  },
  "data": [
    {
      "id": "string",
      "content": "string",
      "messageType": "TEXT",
      "sender": {
        "id": "string",
        "username": "string",
        "avatar": "string"
      },
      "createdAt": "2024-01-01T00:00:00",
      "edited": false,
      "seen": false,
      "recalled": false,
      "attachments": []
    }
  ]
}
```

### Mô tả
- Chuyển tiếp tin nhắn đến một hoặc nhiều cuộc trò chuyện
- Chỉ thành viên của cuộc trò chuyện đích mới có thể chuyển tiếp tin nhắn
- File đính kèm sẽ được chuyển tiếp cùng với tin nhắn
- Tạo MessageStatus cho tất cả thành viên trong cuộc trò chuyện đích

---

## 2. Xóa tin nhắn (Delete Message)

### Endpoint
```
DELETE /api/messages/{messageId}
```

### Parameters
- `messageId`: ID của tin nhắn cần xóa (path parameter)
- `userId`: ID của người dùng thực hiện xóa (query parameter)
- `conversationId`: ID của cuộc trò chuyện (query parameter)

### Response
```json
{
  "status": {
    "code": "00",
    "message": "Xóa tin nhắn thành công"
  },
  "data": "Tin nhắn đã được xóa"
}
```

### Mô tả
- Xóa tin nhắn chỉ cho người dùng cụ thể (soft delete)
- Tin nhắn vẫn tồn tại trong database nhưng được đánh dấu là đã xóa
- Chỉ thành viên của cuộc trò chuyện mới có thể xóa tin nhắn
- Gửi thông báo đến các thành viên khác trong cuộc trò chuyện

---

## 3. Xóa tin nhắn vĩnh viễn (Permanently Delete Message)

### Endpoint
```
DELETE /api/messages/{messageId}/permanent
```

### Parameters
- `messageId`: ID của tin nhắn cần xóa vĩnh viễn (path parameter)
- `userId`: ID của người dùng thực hiện xóa (query parameter)

### Response
```json
{
  "status": {
    "code": "00",
    "message": "Xóa vĩnh viễn tin nhắn thành công"
  },
  "data": "Tin nhắn đã được xóa vĩnh viễn"
}
```

### Mô tả
- Xóa tin nhắn vĩnh viễn khỏi database (hard delete)
- Chỉ người gửi tin nhắn mới có quyền xóa vĩnh viễn
- Xóa tất cả UserMessage và MessageStatus records liên quan
- Gửi thông báo đến tất cả thành viên trong cuộc trò chuyện

---

## Error Codes

| Code | Message |
|------|---------|
| 00 | Thành công |
| 01 | Không tìm thấy cuộc trò chuyện |
| 02 | Thiếu conversationId |
| 03 | Không có quyền thực hiện hành động |
| 04 | Không tìm thấy tin nhắn |

---

## WebSocket Topics

### Message Deleted
```
/topic/message-deleted/{userId}
```

### Message Permanently Deleted
```
/topic/message-permanently-deleted/{userId}
```

### Message Updated
```
/topic/message-updated/{userId}
```

---

## Ví dụ sử dụng

### Chuyển tiếp tin nhắn
```bash
curl -X POST http://localhost:8080/api/messages/forward \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": "msg-123",
    "senderId": "user-456",
    "targetConversationIds": ["conv-789", "conv-101"]
  }'
```

### Xóa tin nhắn
```bash
curl -X DELETE "http://localhost:8080/api/messages/msg-123?userId=user-456&conversationId=conv-789"
```

### Xóa tin nhắn vĩnh viễn
```bash
curl -X DELETE "http://localhost:8080/api/messages/msg-123/permanent?userId=user-456"
```
