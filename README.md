🧾 Báo cáo kiến trúc dự án message_service
   Dự án được tổ chức theo mô hình Clean Architecture / Layered Architecture, với package gốc là com.example.message_service. Mỗi package đảm nhiệm một vai trò rõ ràng trong hệ thống:

com.example.message_service

├── components          # Tiện ích JWT, xử lý khóa, đẩy tin nhắn nội bộ
├── config              # Cấu hình: bảo mật, WebSocket, Redis, multipart
├── controller          # Controller xử lý REST API và WebSocket
├── dto                 # DTO: request/response để trao đổi dữ liệu API
│   ├── request         # Dữ liệu đầu vào từ client
│   └── response        # Dữ liệu phản hồi về client
├── exception           # Xử lý ngoại lệ tùy chỉnh, toàn cục
├── infrastructure      # Các lớp trung gian: JWT filter, WebSocket interceptor, UserContext
├── model               # Entity (JPA) đại diện cho bảng dữ liệu
├── repository          # Tầng DAO: giao tiếp với CSDL (JpaRepository, custom query)
├── service             # Logic nghiệp vụ chính: nhắn tin, hội thoại, bạn bè,...
├── util                # Các tiện ích hỗ trợ: xử lý thời gian, định dạng văn bản,...

   Nhóm chức năng	Mô tả
   🔐 Xác thực JWT	Hỗ trợ bảo mật xác thực bằng JWT thông qua:
   • JwtTokenUtil, KeyProvider
   • JwtTokenFilter, SecurityConfig
   💬 Giao tiếp thời gian thực (WebSocket)	Cho phép nhắn tin thời gian thực với cấu hình và xác thực:
   • WebSocketConfig, WebSocketAuthChannelInterceptor
   • WebSocketController
   📨 Tin nhắn & Hội thoại	Xử lý gửi/nhận tin nhắn, quản lý cuộc trò chuyện:
   • MessageService, MessageController
   • ConversationService, ConversationController
   👥 Quản lý bạn bè	Kết bạn, duyệt yêu cầu, hủy kết bạn:
   • FriendshipService, FriendshipController
   ⚡ Redis Token Cache	Lưu trữ và truy xuất token/phiên bằng Redis:
   • RedisConfig, RedisToken
   📄 Swagger API Docs	Cung cấp tài liệu API theo chuẩn OpenAPI:
   • File docx/swagger.yaml
   💾 Dữ liệu mẫu	Cấu trúc CSDL khởi tạo có sẵn:
   • File SQL: database/message.sql
   • register.html, profile.html, index.html
   🐳 Hỗ trợ Docker	Dễ dàng triển khai môi trường toàn diện:
   • Dockerfile, docker-compose.yml hỗ trợ build và run backend, Redis, MySQL

Tách riêng WebSocket thành module độc lập (cleaner boundary).

Bổ sung các tính năng nâng cao: gọi video (WebRTC), xác thực hai lớp (2FA), mã hóa E2E.

Tích hợp frontend hiện đại hơn (React/Vue) thay thế Thymeleaf.

Viết test (unit + integration) cho các service chính.

CI/CD với GitHub Actions hoặc GitLab CI.