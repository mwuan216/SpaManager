# Hướng Dẫn Hệ Thống Quản Lý & Phát Hành Key Bản Quyền (Văn Định Skinlab)

Hệ thống gồm 2 thành phần liên kết chặt chẽ:
1. **Google Sheets + Apps Script:** Quản lý danh sách Key, khóa cứng 1 Key / 1 Thiết bị (`Device ID`), reset máy khi khách đổi thiết bị.
2. **Cloudflare Worker + payOS:** Kênh phân phối cho Sale (Cộng Tác Viên). Sale nạp 30% giá sỉ qua VietQR payOS tự động, hưởng lợi nhuận 70%, hệ thống tự động sinh và bàn giao Key ngay lập tức trong 3 giây.

---

## 1. Bảng Giá & Chính Sách Chiết Khấu Đại Lý (Sale 70% - Bạn 30%)

| Gói Bản Quyền | Giá Bán Lẻ (Khách trả) | Tiền Sale Nạp (Bạn nhận 30%) | Lợi Nhuận Của Sale (70%) |
| :--- | :--- | :--- | :--- |
| **Gói 1 Tháng** | 80.000 ₫ | **24.000 ₫** | **56.000 ₫** |
| **Gói 3 Tháng** | 180.000 ₫ | **54.000 ₫** | **126.000 ₫** |
| **Gói 6 Tháng** | 320.000 ₫ | **96.000 ₫** | **224.000 ₫** |
| **Gói 1 Năm** | 500.000 ₫ | **150.000 ₫** | **350.000 ₫** |
| **Gói Vĩnh Viễn** | 1.500.000 ₫ | **450.000 ₫** | **1.050.000 ₫** |

---

## 2. Cách Hoạt Động Của Kênh Bán Key Cho Sale (Tự Động 100%)

### Bước 1: Bạn Cấp Mã Đối Tác Cho Người Sale
- Người Sale tải app Văn Định Skinlab về điện thoại.
- Vào mục **Cài đặt** > **Bản quyền** > Bấm **"Kích Hoạt Quyền Đối Tác Sale"**.
- Nhập một trong các mã hợp lệ:
  - `SALE_VANDINH_2026`
  - `VANDINH_SALE_VIP`
  - Hoặc mã cá nhân bắt đầu bằng `SALE-` hoặc `VD-SALE-` (Ví dụ: `SALE-TUAN`, `SALE-LAN`...).
- Ngay sau khi nhập đúng mã, app sẽ mở khóa giao diện **"Trung Tâm Đối Tác & Mua Key Đại Lý"**.

### Bước 2: Sale Mua Key Khi Có Khách
- Khi có khách muốn mua gói (ví dụ gói 1 Năm - 500.000 ₫):
- Sale mở app, chọn **Gói 1 Năm** > Bấm **"Mua Key"**.
- App hiển thị mã QR VietQR payOS với số tiền đúng 30%: **150.000 ₫** (chuyển về tài khoản MBBank `8500121062001 - BUI MINH QUAN`).
- Sale quét mã chuyển khoản qua app ngân hàng của họ.
- **Trong vòng 3 giây**, hệ thống payOS xác nhận tiền vào tài khoản:
  - Cloudflare Worker tự động sinh ra mã Key mới: `VD-1Y-XXXX-YYYY`.
  - App của Sale tự động nhận key, hiển thị Popup chúc mừng rực rỡ kèm nút **"Sao Chép Key"** và nút **"Gửi Cho Khách"** (soạn sẵn tin nhắn Zalo/SMS).
  - Key được lưu vĩnh viễn vào **"Kho Key Của Bạn"** trên máy Sale để quản lý lại bất cứ lúc nào.

### Bước 3: Khách Hàng Nhập Key Trên Máy Của Khách
- Khách hàng tải app về điện thoại của họ.
- Mở **Cài đặt** > **Bản quyền** > Dán mã key nhận từ Sale và bấm **"Kích Hoạt"**.
- Hệ thống tự động khóa mã máy của khách (`Device ID`) vào Key này. Người khác không thể dùng trộm key.

---

## 3. Cách Cập Nhật Cloudflare Worker payOS (Chỉ mất 1 phút)

1. Mở file [backend/cloudflare_worker_payos.js](file:///f:/Project/SpaManager/backend/cloudflare_worker_payos.js) và sao chép toàn bộ code.
2. Truy cập [Cloudflare Dashboard](https://dash.cloudflare.com/) > **Workers & Pages** > Chọn Worker của bạn (`wclon-payment`).
3. Bấm **Edit Code**, dán đè toàn bộ code mới vào và bấm **Deploy**.
4. (Tùy chọn) Trong mục **Settings** > **Variables and Secrets**, bạn có thể thêm biến `GOOGLE_SHEET_URL` (dán link Web App của Google Apps Script) để mỗi khi Sale mua key qua payOS, key sẽ tự động xuất hiện trên Google Sheet của bạn.

---

## 4. Cách Triển Khai Google Sheets Quản Lý Key

1. Mở Google Sheet mới tại [sheet.new](https://sheet.new).
2. Vào **Tiện ích mở rộng** > **Apps Script**.
3. Dán mã nguồn từ [backend/google_apps_script.js](file:///f:/Project/SpaManager/backend/google_apps_script.js) vào.
4. Chọn hàm `setupSheet` và bấm **Chạy (Run)** để khởi tạo bảng 10 cột.
5. Bấm **Triển khai (Deploy)** > **Tùy chọn triển khai mới (New deployment)**:
   - Loại: **Ứng dụng web (Web app)**
   - Thực thi dưới dạng: **Tôi (Me)**
   - Ai có quyền truy cập: **Bất kỳ ai (Anyone)**
6. Copy URL Web App và dán vào mục Cài đặt trong app SpaManager.
