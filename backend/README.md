# Hướng Dẫn Thiết Lập Hệ Thống Quản Lý & Bán Key Online (Văn Định Skinlab)

Hệ thống quản lý key bản quyền sử dụng **Google Sheets + Google Apps Script Web App**:
- **Chi phí:** Hoàn toàn **0đ (Miễn phí 100%)** trọn đời từ hạ tầng Google Cloud.
- **Bảo mật:** Mỗi Key được khóa cứng vào **duy nhất 1 thiết bị (Device ID)** khi kích hoạt lần đầu.
- **Tiện ích:** Quản trị viên có thể xem, tạo key, quản lý khách hàng, gia hạn hoặc reset máy trực tiếp trên ứng dụng Google Sheets trên điện thoại hoặc máy tính.

---

## 1. Các bước triển khai (Chỉ mất 2 phút)

### Bước 1: Tạo Google Sheet mới
1. Truy cập [sheet.new](https://sheet.new) trên trình duyệt (hoặc tạo file Google Sheets mới trên Google Drive của bạn).
2. Đổi tên file bảng tính thành: `Văn Định Skinlab - Quản Lý Key Bản Quyền`.

### Bước 2: Dán mã nguồn Apps Script
1. Trên thanh menu Google Sheets, chọn **Tiện ích mở rộng (Extensions)** > **Apps Script**.
2. Xóa hết mã mặc định trong tệp `Mã.gs` (`Code.gs`).
3. Mở file [google_apps_script.js](file:///f:/Project/SpaManager/backend/google_apps_script.js), sao chép toàn bộ nội dung và dán vào.
4. Bấm biểu tượng **Lưu (Save)** (hoặc phím tắt `Ctrl + S`).

### Bước 3: Khởi tạo bảng dữ liệu
1. Ở thanh công cụ phía trên trong Apps Script, chọn hàm **`setupSheet`** và bấm **Chạy (Run)**.
2. Nếu Google yêu cầu ủy quyền (Authorization Required):
   - Chọn tài khoản Google của bạn -> Bấm **Nâng cao (Advanced)** -> Bấm **Đi tới ... (Không an toàn)** -> Bấm **Cho phép (Allow)**.
3. Quay lại trang Google Sheets, bạn sẽ thấy bảng tính đã được tạo sẵn 10 cột tiêu chuẩn với màu sắc Sage Green sang trọng cùng các phím menu tiện ích.

### Bước 4: Triển khai thành Web App (Lấy API URL)
1. Ở góc trên bên phải màn hình Apps Script, bấm nút **Triển khai (Deploy)** > **Tùy chọn triển khai mới (New deployment)**.
2. Bấm vào biểu tượng bánh răng **Chọn loại** > Chọn **Ứng dụng web (Web app)**.
3. Điền các trường:
   - **Mô tả (Description):** `SpaCare License API`
   - **Thực thi dưới dạng (Execute as):** `Tôi (tài khoản của bạn)` *(Me)*
   - **Ai có quyền truy cập (Who has access):** **`Bất kỳ ai (Anyone)`** *(Quan trọng: Phải chọn Anyone để ứng dụng Android có thể gửi request xác thực key)*
4. Bấm **Triển khai (Deploy)**.
5. Google sẽ cấp cho bạn một đường dẫn dạng:
   ```
   https://script.google.com/macros/s/AKfycbx.../exec
   ```
6. **Sao chép URL này** để nhập vào ứng dụng SpaManager hoặc gắn làm URL mặc định!

---

## 2. Cách Sử Dụng Hàng Ngày Để Bán Key Cho Khách

### A. Tạo Key mới để bán:
1. Mở trang Google Sheet của bạn trên điện thoại hoặc máy tính.
2. Trên thanh menu, bấm **💆 Quản Lý Key SpaCare** > **➕ Tạo Key Mới Tự Động**.
3. Nhập số lượng key muốn tạo (vd: `1`, `5`, `10`).
4. Chọn loại gói:
   - `LIFETIME`: Bản quyền vĩnh viễn (Trọn đời).
   - `1_MONTH`: Hạn dùng 30 ngày kể từ lúc khách kích hoạt.
   - `6_MONTHS`: Hạn dùng 180 ngày.
   - `1_YEAR`: Hạn dùng 365 ngày.
5. Nhập tên khách / tên Spa mua.
6. Hệ thống sẽ sinh key định dạng an toàn: `VD-ABCD-EFGH-1234`. Bạn chỉ cần copy key này gửi cho khách hàng.

### B. Khi Khách Hàng Nhập Key Trên Điện Thoại:
- Ứng dụng SpaManager sẽ lấy Mã định danh thiết bị (`Device ID`) và Tên dòng máy (`Device Name`) gửi lên Google Sheet.
- Dòng chứa key đó trên Google Sheet sẽ tự động chuyển sang:
  - Cột Trạng Thái: `ACTIVATED`
  - Cột Mã Thiết Bị: Được điền tự động mã máy của khách.
  - Cột Ngày Kích Hoạt & Hạn Sử Dụng: Được tự động tính và điền.
- Nếu người khác cố tình lấy key này nhập vào máy khác: Ứng dụng sẽ báo ngay lỗi *"Key này đã được kích hoạt trên thiết bị khác..."* và không cho sử dụng.

### C. Khách Hàng Đổi Điện Thoại Mới:
- Khi khách hàng đổi máy và muốn dùng tiếp key cũ:
- Bạn chỉ cần mở Google Sheet, chọn dòng của key đó, bấm **💆 Quản Lý Key SpaCare** > **🔄 Reset Thiết Bị (Cho phép đổi máy)**.
- Dòng đó sẽ được giải phóng thiết bị cũ để khách hàng kích hoạt lại trên máy mới một cách tiện lợi!

### D. Khóa Key Khi Cần Thiết:
- Nếu khách hàng hoàn tiền hoặc hủy hợp đồng:
- Chọn dòng key đó, bấm **🔒 Khóa / Mở Khóa Key Đang Chọn** (Trạng thái chuyển sang `BLOCKED`). Khi app kết nối mạng, key này sẽ bị thu hồi quyền truy cập.
