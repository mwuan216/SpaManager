# SpaCare Manager 💆‍♀️🌿
### Ứng Dụng Quản Lý Spa, Thẩm Mỹ Viện & Wellness Mobile-First Chuyên Nghiệp

![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Jetpack%20Compose-green)
![Design](https://img.shields.io/badge/Design-Material%203%20(M3)-sage)
![Language](https://img.shields.io/badge/Language-Kotlin%202.0-purple)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture%20%2B%20MVI%2FMVVM-blue)

**SpaCare Manager** là giải pháp phần mềm quản lý tối ưu hóa cho màn hình di động và tablet quầy lễ tân của các cơ sở Spa, Thẩm mỹ viện và Viện chăm sóc sức khỏe sắc đẹp cao cấp. Ứng dụng được thiết kế theo chuẩn **Material Design 3**, mang phong cách tinh tế, ấm áp và thanh lịch với bảng màu tự nhiên (*Warm Beige & Sage Green*).

---

## 🌟 Tính Năng Nổi Bật

### 1. 📇 Spa CRM & Quản Lý Liệu Trình Chuyên Sâu
- **Hồ sơ khách hàng chuẩn y khoa:** Quản lý thông tin liên hệ, phân loại thành viên (VIP Gold, Diamond), phân loại da và bệnh lý biểu bì.
- **Cảnh báo Dị ứng & Chống chỉ định:** Hiển thị trực quan cảnh báo đỏ (*Allergy Alert Card*) ngăn ngừa tuyệt đối việc dùng nhầm hoạt chất hoặc công nghệ máy gây kích ứng.
- **Bộ đếm buổi liệu trình (Session Counter):** Theo dõi tiến độ các gói dịch vụ (ví dụ: *Gói 10 buổi Whitening Facial - Còn 3 buổi*), hỗ trợ điểm danh một chạm.
- **Thư viện Before & After:** So sánh ảnh chụp trước và sau điều trị song song (*Side-by-Side*) theo mốc thời gian kèm nhận xét lâm sàng của chuyên viên.

### 2. 💳 Thu Ngân Thông Minh & Dynamic VietQR
- **Tự động sinh mã VietQR Napas 247:** Điền sẵn chính xác số tiền, số tài khoản và nội dung chuyển khoản tương thích hơn 40 app ngân hàng Việt Nam.
- **Lắng nghe Webhook thời gian thực:** Màn hình lễ tân tự động cập nhật khi tiền vào tài khoản ngân hàng thông qua Server-Sent Events (SSE).
- **Phản hồi xúc giác & âm thanh:** Hiệu ứng checkmark thành công, rung haptic và âm thanh báo nhận tiền tức thì.

### 3. 📅 Lịch Hẹn & Điều Phối Phòng Trị Liệu
- **Lưới lịch hẹn đa chế độ:** Xem theo *Dòng thời gian (Timeline)*, *Theo Kỹ thuật viên (Staff)* hoặc *Theo Phòng dịch vụ (Room)* để tránh trùng phòng.
- **Tự động hóa nhắc hẹn (Automated Reminders):** Tích hợp `WorkManager` chạy ngầm kích hoạt gửi SMS Brandname hoặc Zalo ZNS trước giờ hẹn 2 tiếng và 24 tiếng.

### 4. 📊 Báo Cáo Doanh Thu & Hiệu Suất KTV
- **Chỉ số thời gian thực:** Tổng hợp doanh thu hôm nay, tuần này, tháng này và tỷ lệ công suất phòng (Occupancy Rate).
- **Cơ cấu nguồn thu:** Biểu đồ phân bổ tỷ trọng giữa Gói liệu trình trả trước (57%), Dịch vụ lẻ (26%) và Mỹ phẩm bán lẻ mang về (17%).
- **Bảng vinh danh KTV:** Đánh giá số lượt khách phục vụ, doanh thu mang về và xếp hạng sao trung bình.

---

## 🎨 Hệ Thống Thiết Kế (Design System)

| Token | Mã Màu Hex | Ứng Dụng |
|---|---|---|
| **Warm Beige Background** | `#FAF8F5` | Nền ứng dụng thư thái, dễ chịu |
| **Warm Beige Surface** | `#FFFFFF` | Nền thẻ Card bo góc mềm mại 16dp |
| **Sage Green Primary** | `#4C6B58` | Màu thương hiệu chính, nút thao tác |
| **Charcoal Text Primary** | `#222624` | Chữ độ tương phản cao, chống mỏi mắt |
| **Spa Gold** | `#997A48` | Huy hiệu VIP, gói liệu trình cao cấp |
| **Allergy Alert Red** | `#C74337` | Cảnh báo dị ứng mỹ phẩm & chống chỉ định |
| **Success Emerald** | `#2E7D32` | Trạng thái thanh toán VietQR thành công |

---

## 📂 Cấu Trúc Mã Nguồn (Clean Architecture)

```
SpaManager/
├── app/
│   ├── src/main/java/com/spacare/manager/
│   │   ├── MainActivity.kt                      // Khởi chạy ứng dụng
│   │   ├── ui/SpaMainNavigation.kt              // Thanh điều hướng 4 tab
│   │   ├── core/
│   │   │   ├── designsystem/theme/              // Color, Shape, Type, Theme M3
│   │   │   └── notification/                    // AppointmentReminderWorker (WorkManager)
│   │   ├── domain/model/                        // Client, Appointment, Checkout, Dashboard
│   │   ├── data/repository/                     // SpaRepository & StateFlow
│   │   └── feature/
│   │       ├── crm/                             // Profile, Dị ứng, Gói, Before & After
│   │       ├── calendar/                        // Lịch hẹn & Phân bổ phòng
│   │       ├── checkout/                        // Thu ngân & VietQR Generator
│   │       └── dashboard/                       // Báo cáo doanh thu & KPIs
├── backend/                                     // Mock API Server, SSE & Webhook Handler
│   ├── server.js
│   └── openapi_specification.json               // OpenAPI 3.0 Specs
├── gradle/libs.versions.toml                    // Version Catalog
└── build.gradle.kts
```

---

## 🚀 Hướng Dẫn Cài Đặt & Build APK

### 1. Build APK Debug bằng Gradle Wrapper:
```powershell
# Chạy build APK trực tiếp từ dòng lệnh
.\gradlew assembleDebug
```
File APK sau khi build hoàn tất sẽ nằm tại:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Chạy ứng dụng trên Android Studio:
1. Mở thư mục dự án bằng Android Studio (Ladybug 2024.2+ hoặc mới hơn).
2. Chờ Gradle đồng bộ dependencies.
3. Chọn thiết bị ảo (Tablet hoặc Điện thoại) và nhấn **Run (Shift + F10)**.

### 3. Chạy Mock Backend & Test Webhook VietQR:
```bash
cd backend
npm install
npm start
```
Thử kích hoạt webhook giả lập tiền vào:
```bash
curl -X POST http://localhost:3000/api/v1/webhooks/bank-payment \
  -H "Content-Type: application/json" \
  -d '{"content": "Thanh toan SPA8829", "transferAmount": 2025000, "referenceCode": "MB129482"}'
```

---

## 📄 Bản Quyền & Giấy Phép
Dự án được phát triển phục vụ ngành Spa & Thẩm mỹ viện cao cấp. Giữ toàn quyền tác giả © 2026 SpaCare Manager.

