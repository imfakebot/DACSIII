## 📝 Mô tả thay đổi
(Điền vào đây: Vừa sửa cái gì? Có thêm tính năng mới hay fix bug? UI hay Logic?)

## 🔗 Link tới Kanban Issue
(Gắn ID của thẻ task vào đây để tự động đóng thẻ khi Merge. VD: `Closes #1`, `Fixes #4`)

## 🛠 Checklist trước khi gửi PR (Bắt buộc)
**🔹 Cấu hình & Môi trường:**
- [] Tôi đã tự giải quyết **Conflict** với nhánh main.
- [] Tôi đã gỡ sạch các file có tên tiếng Việt có dấu (như `ảnh.xml`).
- [] Code build thành công, không văng lỗi đỏ.

**🔹 Tích hợp API (Backend NestJS):**
- [] Tôi đã đổi IP sang **10.0.2.2** để test mượt trên máy ảo.
- [] Đã xử lý lưu **Token (DataStore/SharedPreferences)** nếu làm tính năng Đăng nhập.
- [] Đã xử lý đủ 3 trạng thái mạng: **Loading** (xoay vòng), **Success**, và **Error** (Hiện Toast/Snackbar báo lỗi).

**🔹 Chuẩn Code (Compose & MVVM):**
- [] Tuân thủ MVVM: Logic gọi API nằm ở **ViewModel**, tuyệt đối KHÔNG gọi trực tiếp trong file giao diện (`*Screen.kt`).
- [] Đã sử dụng localization (không dùng string hardcoded).
- [] Code tuân theo coding standards của project (Kotlin/Android guidelines).

## 📸 Ảnh minh họa giao diện mới (Hoặc Video quay màn hình)
(Kéo thả ảnh demo vào đây. Nếu UI đè chữ, vỡ khung -> Reject PR!)