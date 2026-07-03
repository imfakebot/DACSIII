<div align="center">

<img src="docs/images/logo.png" alt="Datsan Logo" width="160"/>

# ⚽ DATSAN

### Nền tảng Đặt Sân Bóng Thông Minh

*Đặt sân nhanh · Thanh toán tiện · Quản lý dễ*

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

<br/>

<img src="docs/images/banner.png" alt="Datsan Banner" width="100%" style="border-radius: 16px;"/>

<br/>

</div>

---

## 📖 Giới thiệu

**Datsan** là ứng dụng di động Android giúp người dùng **tìm kiếm, đặt sân bóng đá** nhân tạo một cách nhanh chóng và tiện lợi. Ứng dụng cung cấp đầy đủ tính năng cho cả **Người chơi**, **Quản lý sân** và **Nhân viên**, từ đặt lịch, thanh toán trực tuyến đến quét QR check-in tại sân.

> 🎯 **Mục tiêu:** Xây dựng hệ sinh thái đặt sân bóng hoàn chỉnh, từ việc tìm sân gần nhất đến thanh toán và đánh giá sau khi sử dụng dịch vụ.

---

## ✨ Tính năng nổi bật

<table>
<tr>
<td width="50%">

### 👤 Dành cho Người chơi

| Tính năng | Mô tả |
|:--|:--|
| 🔐 **Xác thực đa dạng** | Email/OTP, Google OAuth |
| 🔍 **Tìm sân thông minh** | Theo GPS, khu vực, đánh giá |
| 📅 **Đặt sân linh hoạt** | Chọn khung giờ, xem slot trống |
| 💳 **Thanh toán VNPAY** | Tích hợp cổng thanh toán an toàn |
| 🎫 **Voucher & Ưu đãi** | Áp mã giảm giá khi thanh toán |
| 📜 **Lịch sử booking** | Theo dõi đơn đặt & hoá đơn |
| ⭐ **Đánh giá & Review** | Viết review sau khi sử dụng |
| 🔔 **Thông báo** | Nhận thông báo realtime |

</td>
<td width="50%">

### 🛡️ Dành cho Quản lý & Nhân viên

| Tính năng | Mô tả |
|:--|:--|
| 🏟️ **Quản lý sân** | CRUD sân, ảnh, tiện ích |
| 🏢 **Quản lý chi nhánh** | Thêm/sửa chi nhánh, địa chỉ |
| 📊 **Thống kê doanh thu** | Báo cáo theo thời gian |
| 📱 **QR Check-in** | Quét mã xác nhận khách hàng |
| 🎫 **Quản lý Voucher** | Tạo & phân phối mã giảm giá |
| 👥 **Quản lý nhân sự** | Tạo tài khoản nhân viên |
| 💬 **Phản hồi & Feedback** | Xử lý đánh giá từ khách |
| ⏰ **Khung giờ & Giá** | Cấu hình giá theo time slot |

</td>
</tr>
</table>

---

## 🛠️ Công nghệ sử dụng

<div align="center">

### Frontend (Android)

| Công nghệ | Phiên bản | Vai trò |
|:--:|:--:|:--|
| ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white) | 2.3 | Ngôn ngữ chính |
| ![Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white) | BOM 2026.06 | 100% Declarative UI |
| ![Hilt](https://img.shields.io/badge/Hilt-3DDC84?style=flat-square&logo=android&logoColor=white) | 2.59 | Dependency Injection |
| ![Retrofit](https://img.shields.io/badge/Retrofit-48B983?style=flat-square&logo=square&logoColor=white) | 3.0 | HTTP Client & API |
| ![Coil](https://img.shields.io/badge/Coil-2.7-blue?style=flat-square) | 2.7 | Image Loading |
| ![CameraX](https://img.shields.io/badge/CameraX-1.6-orange?style=flat-square) | 1.6 | QR Scanner |
| ![Navigation](https://img.shields.io/badge/Navigation_Compose-2.10-green?style=flat-square) | 2.10 | Điều hướng màn hình |

### Backend

| Công nghệ | Vai trò |
|:--:|:--|
| ![NestJS](https://img.shields.io/badge/NestJS-E0234E?style=flat-square&logo=nestjs&logoColor=white) | API Framework |
| ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white) | Cơ sở dữ liệu |
| ![TypeORM](https://img.shields.io/badge/TypeORM-FE0803?style=flat-square&logo=typeorm&logoColor=white) | ORM |
| ![VNPAY](https://img.shields.io/badge/VNPAY-005BAA?style=flat-square&logoColor=white) | Cổng thanh toán |
| ![Socket.IO](https://img.shields.io/badge/Socket.IO-010101?style=flat-square&logo=socket.io&logoColor=white) | Realtime |

</div>

---

## 🏗️ Kiến trúc ứng dụng

```
📦 com.tanh.datsan
├── 🔧 core/              # Core utilities & base classes
├── 📡 data/
│   ├── model/             # Data models & DTOs
│   ├── network/           # Retrofit API services
│   └── repository/        # Repository pattern (data access)
├── 💉 di/                 # Hilt Dependency Injection modules
├── 🧭 navigation/         # App-level navigation graph
└── 🎨 ui/
    ├── admin/             # 🛡️ Admin screens (field, booking, voucher, ...)
    ├── auth/              # 🔐 Login, Register, OTP, Forgot Password
    ├── component/         # 🧩 Reusable UI components
    ├── feedback/          # 💬 Feedback screens
    ├── home/
    │   ├── booking/       # 📅 Booking flow & success
    │   ├── detail/        # 📋 Field detail & booking sheet
    │   ├── history/       # 📜 Booking history
    │   ├── main/          # 🏠 Home & search
    │   ├── notification/  # 🔔 Notifications
    │   ├── review/        # ⭐ Reviews (write, view all)
    │   └── voucher/       # 🎫 Voucher listing
    ├── profile/           # 👤 User profile & settings
    ├── staff/             # 📱 QR Scanner & check-in
    └── theme/             # 🎨 Colors, Typography, Theme
```

> **Kiến trúc:** MVVM (Model–View–ViewModel) kết hợp Clean Architecture.  
> Mỗi feature module chứa `Screen` (Composable) + `ViewModel` + `UiState` riêng biệt.

---

## ⚙️ Cài đặt & Chạy ứng dụng

### Yêu cầu

| Yêu cầu | Phiên bản tối thiểu |
|:--|:--|
| Android Studio | Meerkat (2025.x) trở lên |
| JDK | 17 |
| Android SDK | API 33 (Android 13) |
| Target SDK | API 37 |
| Gradle | 9.6 |

### Bước 1 — Clone repository

```bash
git clone https://github.com/imfakebot/DACSIII.git
cd DACSIII
```

### Bước 2 — Cấu hình `local.properties`

Tạo file `local.properties` ở thư mục gốc:

```properties
# Backend URL (production hoặc ngrok)
API_BASE_URL=https://your-backend-url.com/

# Backend URL gốc (dùng cho xử lý URL ảnh nội bộ)
API_BACKEND=http://localhost:3000

# Google OAuth Client ID
GOOGLE_WEB_CLIENT_ID=your-client-id.apps.googleusercontent.com

# Deeplink cho quên mật khẩu
API_DEEPlINK_FORGOT_PASSWORD=dacsii\://reset-password
```

### Bước 3 — Build & Run

```bash
# Sync Gradle
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Hoặc mở bằng Android Studio → Run ▶️
```

---

## 📂 Tài liệu liên quan

| Tài liệu | Mô tả |
|:--|:--|
| [`gradle/libs.versions.toml`](gradle/libs.versions.toml) | Quản lý phiên bản dependencies |
| [`app/build.gradle.kts`](app/build.gradle.kts) | Cấu hình build ứng dụng |
| [`LICENSE`](LICENSE) | Giấy phép MIT |

---

## 🤝 Đóng góp

Chúng tôi hoan nghênh mọi đóng góp! Hãy làm theo các bước sau:

```bash
# 1. Fork & Clone
git clone https://github.com/<your-username>/DACSIII.git

# 2. Tạo nhánh mới
git checkout -b feature/your-amazing-feature

# 3. Commit theo Conventional Commits
git commit -m "feat: add amazing feature"

# 4. Push & tạo Pull Request
git push origin feature/your-amazing-feature
```

---

## 📝 License

Dự án được phân phối theo giấy phép **MIT**. Xem file [`LICENSE`](LICENSE) để biết thêm chi tiết.

---

<div align="center">

**Datsan** — *Book. Play. Repeat.* ⚽

Made with ❤️ by [DACSIII Team](https://github.com/imfakebot/DACSIII)

</div>
