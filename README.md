# Datsan (Đặt Sân) - Nền tảng Đặt Sân Bóng Trực Tuyến

Datsan là một ứng dụng di động trên nền tảng Android được xây dựng nhằm giải quyết bài toán đặt sân bóng đá nhân tạo một cách nhanh chóng, tiện lợi. Dự án cung cấp cả phân hệ cho Người dùng (User) và Quản lý/Admin (Staff/Branch Manager), tích hợp thanh toán trực tuyến và quét mã QR.

## 🚀 Tính năng nổi bật

### Dành cho Người dùng (User)
- **Đăng nhập & Xác thực:** Hỗ trợ đăng nhập bằng Email/Mật khẩu với mã OTP, hoặc Đăng nhập nhanh qua Google OAuth.
- **Tìm kiếm sân:** Tìm kiếm sân theo khoảng cách (GPS), tỉnh/thành phố, khu vực và đánh giá.
- **Xem chi tiết & Đặt sân:** Xem hình ảnh, tiện ích, giá tiền theo khung giờ. Chọn khung giờ linh hoạt.
- **Thanh toán trực tuyến:** Tích hợp cổng thanh toán VNPAY, an toàn và tự động cập nhật trạng thái.
- **Ưu đãi & Voucher:** Áp dụng mã giảm giá khi thanh toán.
- **Lịch sử đặt sân:** Theo dõi các hoá đơn, trạng thái thanh toán và thông tin check-in.
- **Đánh giá (Review):** Viết đánh giá, bình luận và xem đánh giá từ người dùng khác sau khi sử dụng dịch vụ.

### Dành cho Quản lý & Nhân viên (Admin/Manager/Staff)
- **Quản lý sân & Chi nhánh:** Thêm, sửa, xoá sân bóng, quản lý hình ảnh, tiện ích và các khung giờ.
- **Quản lý Đặt sân:** Xem danh sách hoá đơn, trạng thái sân theo thời gian thực.
- **Quét mã QR (QR Scanner):** Nhân viên tại sân có thể check-in cho khách hàng một cách nhanh chóng bằng cách quét mã QR trên hoá đơn của khách.
- **Thống kê (Statistics):** Báo cáo doanh thu, số lượng đơn đặt sân.
- **Quản lý Vouchers & Người dùng:** Cấp phát voucher, quản lý tài khoản nhân viên và người dùng (Khóa/Mở khóa tài khoản).

## 🛠️ Công nghệ sử dụng (Tech Stack)

### Ứng dụng Android (Frontend)
- **Ngôn ngữ:** Kotlin
- **Giao diện (UI):** Jetpack Compose (100% Declarative UI)
- **Kiến trúc:** MVVM (Model-View-ViewModel) + Clean Architecture principles
- **Asynchronous & State Management:** Kotlin Coroutines & StateFlow/SharedFlow
- **Mạng & Gọi API:** Retrofit2, OkHttp3 (có Logging Interceptor)
- **Điều hướng:** Jetpack Navigation Compose
- **Tải ảnh:** Coil
- **Quét mã QR:** ML Kit (Barcode Scanning) / CameraX
- **Bản đồ / Vị trí:** Google Play Services Location

### Backend (Tham khảo - Repository riêng)
- **Framework:** NestJS (TypeScript)
- **Cơ sở dữ liệu:** MySQL với TypeORM
- **Thanh toán:** VNPAY API
- **Realtime:** Socket.IO

## ⚙️ Hướng dẫn cài đặt & Chạy ứng dụng

### 1. Yêu cầu hệ thống
- Android Studio (Phiên bản mới nhất, khuyên dùng bản Iguana hoặc Jellyfish).
- JDK 17.
- Máy ảo (Emulator) hoặc thiết bị thật chạy Android 8.0 (API 26) trở lên.

### 2. Cấu hình môi trường (local.properties)
Do dự án sử dụng các API key và đường dẫn backend riêng, bạn cần tạo/chỉnh sửa file `local.properties` ở thư mục gốc của project với nội dung tương tự như sau:

```properties
# URL của Backend Server (đã public hoặc ngrok nếu chạy local)
API_BASE_URL=https://<your-backend-url>/
# API backend gốc để replace (nếu cần xử lý URL ảnh nội bộ)
API_BACKEND=http://localhost:3000

# Client ID cho tính năng Google Login
GOOGLE_WEB_CLIENT_ID=<your-google-oauth-client-id>.apps.googleusercontent.com

# Deeplink cho việc đặt lại mật khẩu từ email
API_DEEPlINK_FORGOT_PASSWORD=dacsii\://reset-password
```

### 3. Build và Chạy
- Mở project bằng Android Studio.
- Chờ Gradle đồng bộ (`Sync Project with Gradle Files`).
- Bấm nút **Run** (Shift + F10) để cài đặt và chạy ứng dụng trên thiết bị của bạn.

## 📁 Cấu trúc thư mục (Packages)

- `ui/`: Chứa toàn bộ giao diện Jetpack Compose, chia theo tính năng (`auth`, `home`, `admin`, `profile`, `staff`, `component`).
- `data/`: Chứa `model` (DTOs), `network` (Retrofit APIs), và `repository` (Xử lý dữ liệu).
- `viewmodel/` (đang dịch chuyển về `ui/`): Quản lý State và Logic UI.
- `utils/`: Chứa các hàm tiện ích (Image Utils, JWT, Validation, OpenVnPay,...).
- `navigation/`: Cấu hình điều hướng của ứng dụng.
- `di/`: Quản lý Dependency Injection (Hilt/Manual DI tuỳ cấu hình).

## 🤝 Đóng góp (Contributing)
1. Fork dự án
2. Tạo nhánh tính năng mới (`git checkout -b feature/AmazingFeature`)
3. Commit thay đổi (`git commit -m 'feat: Add some AmazingFeature'`)
4. Đẩy lên nhánh (`git push origin feature/AmazingFeature`)
5. Mở một Pull Request
