# Test Post API

## 1. Tạo Post đơn giản (không có file)

```bash
curl -X POST http://localhost:8080/api/posts/create-simple \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "content": "Đây là nội dung post đầu tiên",
    "visibility": "PUBLIC"
  }'
```

## 2. Tạo Post với file (multipart/form-data)

```bash
curl -X POST http://localhost:8080/api/posts/create \
  -F "metadata={\"userId\":\"user123\",\"content\":\"Post với hình ảnh\",\"visibility\":\"PUBLIC\"}" \
  -F "files=@/path/to/image1.jpg" \
  -F "files=@/path/to/image2.jpg"
```

## 3. Lấy Post theo ID

```bash
curl -X GET http://localhost:8080/api/posts/1
```

## 4. Lấy tất cả Posts

```bash
curl -X GET "http://localhost:8080/api/posts/all?page=0&size=10"
```

## 5. Lấy Posts của User

```bash
curl -X GET "http://localhost:8080/api/posts/user/user123?page=0&size=10"
```

## 6. Cập nhật Post đơn giản

```bash
curl -X PUT http://localhost:8080/api/posts/1/simple?userId=user123 \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Nội dung đã được cập nhật",
    "visibility": "PRIVATE"
  }'
```

## 7. Cập nhật Post với file

```bash
curl -X PUT http://localhost:8080/api/posts/1?userId=user123 \
  -F "metadata={\"content\":\"Post với file mới\",\"visibility\":\"PUBLIC\"}" \
  -F "files=@/path/to/new-image.jpg"
```

## 8. Xóa Post

```bash
curl -X DELETE "http://localhost:8080/api/posts/1?userId=user123"
```

## 9. Xóa Media của Post

```bash
curl -X DELETE "http://localhost:8080/api/posts/1/media/1?userId=user123"
```

## 10. Tìm kiếm Posts

```bash
curl -X GET "http://localhost:8080/api/posts/search?keyword=test&page=0&size=10"
```

## JavaScript/Fetch Examples

### Tạo Post đơn giản
```javascript
fetch('/api/posts/create-simple', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        userId: 'user123',
        content: 'Nội dung post',
        visibility: 'PUBLIC'
    })
})
.then(response => response.json())
.then(data => console.log(data));
```

### Tạo Post với file
```javascript
const formData = new FormData();
formData.append('metadata', JSON.stringify({
    userId: 'user123',
    content: 'Post với file',
    visibility: 'PUBLIC'
}));
formData.append('files', fileInput.files[0]);

fetch('/api/posts/create', {
    method: 'POST',
    body: formData
})
.then(response => response.json())
.then(data => console.log(data));
```

## Response Format

```json
{
    "code": "00",
    "message": "Tạo post thành công",
    "data": {
        "id": 1,
        "user": {
            "senderId": "user123",
            "nameSender": "username"
        },
        "content": "Nội dung post",
        "visibility": "PUBLIC",
        "createdAt": "2025-01-06T10:15:28",
        "updatedAt": "2025-01-06T10:15:28",
        "reactionCount": 0,
        "commentCount": 0,
        "reactions": [],
        "comments": [],
        "mediaUrls": ["/uploads/file1.jpg", "/uploads/file2.jpg"]
    }
}
``` 