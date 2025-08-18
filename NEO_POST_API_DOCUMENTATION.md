# NeoPost API Documentation

## Tổng quan
Tài liệu này mô tả tất cả các API liên quan đến quản lý bài post, comment, reply và reaction trong hệ thống.

## Base URL
```
http://localhost:8080/api/neo-posts
```

## Authentication
Tất cả API đều yêu cầu JWT token trong header:
```
Authorization: Bearer <JWT_TOKEN>
```

## 1. Tạo bài viết mới

### Endpoint
```
POST /api/neo-posts
```

### Content-Type
```
multipart/form-data
```

### Request Body
- **request** (JSON string): Thông tin bài viết
- **mediaFiles** (file[], optional): Mảng các file ảnh/video

#### CreatePostRequest Schema
```json
{
  "content": "string",
  "visibility": "PUBLIC|PRIVATE"
}
```

#### Trường dữ liệu
| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| content | string | Có | Nội dung bài viết |
| visibility | enum | Có | Quyền riêng tư: PUBLIC hoặc PRIVATE |

### Response
```json
{
  "code": "00",
  "message": "Tạo bài viết thành công",
  "data": {
    "id": "uuid",
    "user": {
      "id": "uuid",
      "displayName": "string",
      "avatarUrl": "string"
    },
    "content": "string",
    "visibility": "PUBLIC|PRIVATE",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00",
    "deletedAt": null,
    "mediaUrls": [
      "/uploads/neo-posts/uuid_filename.jpg",
      "/uploads/neo-posts/uuid_filename.png"
    ],
    "comments": [],
    "reactions": [],
    "commentCount": 0,
    "reactionCount": 0
  }
}
```

## 2. Cập nhật bài viết

### Endpoint
```
PUT /api/neo-posts/{postId}
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| postId | string | ID của bài viết cần cập nhật |

### Request Body
```json
{
  "content": "string",
  "visibility": "PUBLIC|PRIVATE",
  "mediaUrls": [
    "string"
  ]
}
```

#### Trường dữ liệu
| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| content | string | Có | Nội dung mới của bài viết |
| visibility | enum | Có | Quyền riêng tư mới |
| mediaUrls | string[] | Không | Danh sách URL media mới |

### Response
```json
{
  "code": "00",
  "message": "Cập nhật bài viết thành công",
  "data": {
    "id": "uuid",
    "user": {
      "id": "uuid",
      "displayName": "string",
      "avatarUrl": "string"
    },
    "content": "string",
    "visibility": "PUBLIC|PRIVATE",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00",
    "deletedAt": null,
    "mediaUrls": [
      "string"
    ],
    "comments": [
      {
        "id": "uuid",
        "user": {
          "id": "uuid",
          "displayName": "string",
          "avatarUrl": "string"
        },
        "content": "string",
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00",
        "replies": [
          {
            "id": "uuid",
            "user": {
              "id": "uuid",
              "displayName": "string",
              "avatarUrl": "string"
            },
            "content": "string",
            "createdAt": "2024-01-01T00:00:00",
            "updatedAt": "2024-01-01T00:00:00"
          }
        ],
        "replyCount": 1
      }
    ],
    "reactions": [
      {
        "id": "uuid",
        "user": {
          "id": "uuid",
          "displayName": "string",
          "avatarUrl": "string"
        },
        "type": "LIKE|LOVE|HAHA|WOW|SAD|ANGRY",
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "commentCount": 1,
    "reactionCount": 1
  }
}
```

## 3. Xóa bài viết

### Endpoint
```
DELETE /api/neo-posts/{postId}
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| postId | string | ID của bài viết cần xóa |

### Response
```json
{
  "code": "00",
  "message": "Xóa bài viết thành công",
  "data": null
}
```

## 4. Lấy danh sách bài viết public (Newsfeed)

### Endpoint
```
GET /api/neo-posts/newsfeed
```

### Query Parameters
| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| page | integer | Không | Số trang (mặc định: 0) |
| size | integer | Không | Số bài viết mỗi trang (mặc định: 10) |

### Response
```json
{
  "code": "00",
  "message": "Lấy danh sách bài viết thành công",
  "data": {
    "content": [
      {
        "id": "uuid",
        "user": {
          "id": "uuid",
          "displayName": "string",
          "avatarUrl": "string"
        },
        "content": "string",
        "visibility": "PUBLIC",
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00",
        "deletedAt": null,
        "mediaUrls": [
          "string"
        ],
        "comments": [
          {
            "id": "uuid",
            "user": {
              "id": "uuid",
              "displayName": "string",
              "avatarUrl": "string"
            },
            "content": "string",
            "createdAt": "2024-01-01T00:00:00",
            "updatedAt": "2024-01-01T00:00:00",
            "replies": [
              {
                "id": "uuid",
                "user": {
                  "id": "uuid",
                  "displayName": "string",
                  "avatarUrl": "string"
                },
                "content": "string",
                "createdAt": "2024-01-01T00:00:00",
                "updatedAt": "2024-01-01T00:00:00"
              }
            ],
            "replyCount": 1
          }
        ],
        "reactions": [
          {
            "id": "uuid",
            "user": {
              "id": "uuid",
              "displayName": "string",
              "avatarUrl": "string"
            },
            "type": "LIKE|LOVE|HAHA|WOW|SAD|ANGRY",
            "createdAt": "2024-01-01T00:00:00"
          }
        ],
        "commentCount": 1,
        "reactionCount": 1
      }
    ],
    "pageable": {
      "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
      },
      "offset": 0,
      "pageNumber": 0,
      "pageSize": 10,
      "paged": true,
      "unpaged": false
    },
    "last": false,
    "totalElements": 25,
    "totalPages": 3,
    "size": 10,
    "number": 0,
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "numberOfElements": 10,
    "first": true,
    "empty": false
  }
}
```

## 5. Lấy bài viết public của user khác

### Endpoint
```
GET /api/neo-posts/user/{userId}/public
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| userId | string | ID của user cần lấy bài viết |

### Query Parameters
| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| page | integer | Không | Số trang (mặc định: 0) |
| size | integer | Không | Số bài viết mỗi trang (mặc định: 10) |

### Response
Tương tự như Newsfeed API, nhưng chỉ trả về bài viết PUBLIC của user được chỉ định.

## 6. Lấy bài viết của user hiện tại (public + private)

### Endpoint
```
GET /api/neo-posts/my-posts
```

### Query Parameters
| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| page | integer | Không | Số trang (mặc định: 0) |
| size | integer | Không | Số bài viết mỗi trang (mặc định: 10) |

### Response
Tương tự như Newsfeed API, nhưng trả về tất cả bài viết (PUBLIC + PRIVATE) của user hiện tại.

## 7. Tạo comment

### Endpoint
```
POST /api/neo-posts/{postId}/comments
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| postId | string | ID của bài viết cần comment |

### Request Body
```json
{
  "content": "string"
}
```

#### Trường dữ liệu
| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| content | string | Có | Nội dung comment |

### Response
```json
{
  "code": "00",
  "message": "Tạo comment thành công",
  "data": {
    "id": "uuid",
    "user": {
      "id": "uuid",
      "displayName": "string",
      "avatarUrl": "string"
    },
    "content": "string",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00",
    "replies": [],
    "replyCount": 0
  }
}
```

## 8. Cập nhật comment

### Endpoint
```
PUT /api/neo-posts/comments/{commentId}
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| commentId | string | ID của comment cần cập nhật |

### Request Body
```json
{
  "content": "string"
}
```

### Response
Tương tự như tạo comment.

## 9. Xóa comment

### Endpoint
```
DELETE /api/neo-posts/comments/{commentId}
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| commentId | string | ID của comment cần xóa |

### Response
```json
{
  "code": "00",
  "message": "Xóa comment thành công",
  "data": null
}
```

## 10. Tạo reply

### Endpoint
```
POST /api/neo-posts/comments/{commentId}/replies
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| commentId | string | ID của comment cần reply |

### Request Body
```json
{
  "content": "string"
}
```

### Response
```json
{
  "code": "00",
  "message": "Tạo reply thành công",
  "data": {
    "id": "uuid",
    "user": {
      "id": "uuid",
      "displayName": "string",
      "avatarUrl": "string"
    },
    "content": "string",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

## 11. Cập nhật reply

### Endpoint
```
PUT /api/neo-posts/replies/{replyId}
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| replyId | string | ID của reply cần cập nhật |

### Request Body
```json
{
  "content": "string"
}
```

### Response
Tương tự như tạo reply.

## 12. Xóa reply

### Endpoint
```
DELETE /api/neo-posts/replies/{replyId}
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| replyId | string | ID của reply cần xóa |

### Response
```json
{
  "code": "00",
  "message": "Xóa reply thành công",
  "data": null
}
```

## 13. Tạo reaction

### Endpoint
```
POST /api/neo-posts/{postId}/reactions
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| postId | string | ID của bài viết cần reaction |

### Request Body
```json
{
  "type": "LIKE|LOVE|HAHA|WOW|SAD|ANGRY"
}
```

#### Trường dữ liệu
| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| type | enum | Có | Loại reaction |

### Response
```json
{
  "code": "00",
  "message": "Tạo reaction thành công",
  "data": {
    "id": "uuid",
    "user": {
      "id": "uuid",
      "displayName": "string",
      "avatarUrl": "string"
    },
    "type": "LIKE|LOVE|HAHA|WOW|SAD|ANGRY",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

## 14. Cập nhật reaction

### Endpoint
```
PUT /api/neo-posts/reactions/{reactionId}
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| reactionId | string | ID của reaction cần cập nhật |

### Request Body
```json
{
  "type": "LIKE|LOVE|HAHA|WOW|SAD|ANGRY"
}
```

### Response
Tương tự như tạo reaction.

## 15. Xóa reaction

### Endpoint
```
DELETE /api/neo-posts/reactions/{reactionId}
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| reactionId | string | ID của reaction cần xóa |

### Response
```json
{
  "code": "00",
  "message": "Xóa reaction thành công",
  "data": null
}
```

## 16. Kiểm tra quyền sở hữu

### 16.1. Kiểm tra quyền sở hữu bài viết

### Endpoint
```
GET /api/neo-posts/{postId}/check-ownership
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| postId | string | ID của bài viết cần kiểm tra |

### Response
```json
{
  "code": "00",
  "message": "Kiểm tra quyền sở hữu thành công",
  "data": true
}
```

### 16.2. Kiểm tra quyền sở hữu comment

### Endpoint
```
GET /api/neo-posts/comments/{commentId}/check-ownership
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| commentId | string | ID của comment cần kiểm tra |

### Response
```json
{
  "code": "00",
  "message": "Kiểm tra quyền sở hữu thành công",
  "data": true
}
```

### 16.3. Kiểm tra quyền sở hữu reply

### Endpoint
```
GET /api/neo-posts/replies/{replyId}/check-ownership
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| replyId | string | ID của reply cần kiểm tra |

### Response
```json
{
  "code": "00",
  "message": "Kiểm tra quyền sở hữu thành công",
  "data": true
}
```

### 16.4. Kiểm tra quyền sở hữu reaction

### Endpoint
```
GET /api/neo-posts/reactions/{reactionId}/check-ownership
```

### Path Parameters
| Trường | Kiểu | Mô tả |
|--------|------|-------|
| reactionId | string | ID của reaction cần kiểm tra |

### Response
```json
{
  "code": "00",
  "message": "Kiểm tra quyền sở hữu thành công",
  "data": true
}
```

## Mã lỗi chung

| Mã | Mô tả |
|----|-------|
| 00 | Thành công |
| 01 | Lỗi chung (Bad Request) |
| 99 | Lỗi Internal Server Error |

## HTTP Status Codes

| Status Code | Mô tả |
|-------------|-------|
| 200 | Thành công |
| 400 | Bad Request - Dữ liệu không hợp lệ |
| 401 | Unauthorized - Chưa đăng nhập |
| 403 | Forbidden - Không có quyền truy cập |
| 500 | Internal Server Error - Lỗi hệ thống |

## Lưu ý quan trọng

1. **File upload**: Chỉ hỗ trợ multipart/form-data cho API tạo bài viết
2. **Quyền truy cập**: Tất cả API đều yêu cầu user đăng nhập với role USER
3. **Phân trang**: Các API danh sách đều hỗ trợ phân trang với page và size
4. **Media files**: Được lưu tại `/uploads/neo-posts/` và truy cập qua URL tương ứng
5. **Timestamps**: Tất cả thời gian đều theo định dạng ISO 8601
6. **UUID**: Tất cả ID đều là UUID string
7. **Nested data**: Response bao gồm đầy đủ thông tin user, comments, replies, reactions
8. **Counts**: Mỗi bài viết đều có commentCount và reactionCount để hiển thị nhanh
9. **Error handling**: Tất cả API đều trả về ApiResponse với code và message rõ ràng
10. **Ownership check**: Các API check ownership trả về boolean trực tiếp trong data field
