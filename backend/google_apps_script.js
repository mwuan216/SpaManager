/**
 * ==============================================================================
 * HỆ THỐNG QUẢN LÝ BẢN QUYỀN ONLINE VĂN ĐỊNH SKINLAB (GOOGLE SHEETS & APPS SCRIPT)
 * ==============================================================================
 * Mục đích: Quản lý và bán Key kích hoạt bản quyền ứng dụng SpaManager.
 * Quy tắc: Mỗi Key chỉ được kích hoạt và sử dụng trên 1 thiết bị duy nhất.
 * Hỗ trợ: Key vĩnh viễn (LIFETIME) và Key theo thời hạn (1 tháng, 6 tháng, 1 năm).
 *
 * HƯỚNG DẪN TRIỂN KHAI NHANH:
 * 1. Mở Google Sheets mới (sheet.new).
 * 2. Vào Tiện ích mở rộng (Extensions) -> Apps Script.
 * 3. Dán toàn bộ nội dung file này vào và bấm Lưu (Ctrl + S).
 * 4. Chạy hàm "setupSheet" một lần để tạo bảng quản lý mẫu.
 * 5. Bấm "Triển khai" (Deploy) -> "Tùy chọn triển khai mới" (New deployment).
 *    - Chọn loại: Ứng dụng web (Web app).
 *    - Mô tả: "SpaCare License API".
 *    - Thực thi dưới dạng: Tôi (tài khoản Google của bạn).
 *    - Ai có quyền truy cập: Bất kỳ ai (Anyone).
 * 6. Copy URL Web App (dạng https://script.google.com/macros/s/.../exec) và
 *    dán vào ứng dụng SpaManager.
 * ==============================================================================
 */

const SHEET_NAME = "Licenses";

// Danh sách các cột
const COL_KEY = 1;          // Cột A: Key kích hoạt
const COL_PLAN = 2;         // Cột B: Gói (LIFETIME, 1_MONTH, 6_MONTHS, 1_YEAR, 30_DAYS...)
const COL_STATUS = 3;       // Cột C: Trạng thái (AVAILABLE, ACTIVATED, BLOCKED, EXPIRED)
const COL_DEVICE_ID = 4;    // Cột D: Mã thiết bị duy nhất (Device ID)
const COL_DEVICE_NAME = 5;  // Cột E: Tên thiết bị (Model)
const COL_CUSTOMER = 6;     // Cột F: Tên khách hàng / Tên Spa
const COL_CREATED_AT = 7;   // Cột G: Thời điểm tạo key
const COL_ACTIVATED_AT = 8; // Cột H: Thời điểm kích hoạt
const COL_EXPIRES_AT = 9;   // Cột I: Thời điểm hết hạn
const COL_NOTES = 10;       // Cột J: Ghi chú thêm

/**
 * Tạo Menu tiện ích ngay trên thanh công cụ của Google Sheets
 */
function onOpen() {
  const ui = SpreadsheetApp.getUi();
  ui.createMenu("💆 Quản Lý Key SpaCare")
    .addItem("➕ Tạo Key Mới Tự Động", "promptGenerateKeys")
    .addItem("🔄 Reset Thiết Bị (Cho phép đổi máy)", "resetSelectedDevice")
    .addItem("🔒 Khóa / Mở Khóa Key Đang Chọn", "toggleBlockSelectedKey")
    .addSeparator()
    .addItem("⚙️ Khởi Tạo / Định Dạng Lại Bảng", "setupSheet")
    .addToUi();
}

/**
 * Khởi tạo cấu trúc bảng tính với màu sắc chuẩn mực
 */
function setupSheet() {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  let sheet = ss.getSheetByName(SHEET_NAME);
  if (!sheet) {
    sheet = ss.insertSheet(SHEET_NAME);
  }

  // Tiêu đề các cột
  const headers = [
    "Key Bản Quyền",
    "Gói Dịch Vụ",
    "Trạng Thái",
    "Mã Thiết Bị (Device ID)",
    "Tên Thiết Bị",
    "Khách Hàng / Spa",
    "Ngày Tạo",
    "Ngày Kích Hoạt",
    "Hạn Sử Dụng",
    "Ghi Chú"
  ];

  sheet.getRange(1, 1, 1, headers.length).setValues([headers]);

  // Định dạng Header
  const headerRange = sheet.getRange(1, 1, 1, headers.length);
  headerRange.setBackground("#4C6B58"); // Sage Green Primary
  headerRange.setFontColor("#FFFFFF");
  headerRange.setFontWeight("bold");
  headerRange.setHorizontalAlignment("center");
  sheet.setFrozenRows(1);

  // Đặt độ rộng cột
  sheet.setColumnWidth(COL_KEY, 220);
  sheet.setColumnWidth(COL_PLAN, 130);
  sheet.setColumnWidth(COL_STATUS, 130);
  sheet.setColumnWidth(COL_DEVICE_ID, 200);
  sheet.setColumnWidth(COL_DEVICE_NAME, 160);
  sheet.setColumnWidth(COL_CUSTOMER, 180);
  sheet.setColumnWidth(COL_CREATED_AT, 150);
  sheet.setColumnWidth(COL_ACTIVATED_AT, 150);
  sheet.setColumnWidth(COL_EXPIRES_AT, 150);
  sheet.setColumnWidth(COL_NOTES, 180);

  // Thêm một số key mẫu nếu chưa có dữ liệu
  if (sheet.getLastRow() <= 1) {
    addSampleKey(sheet, "VD-DEMO-LIFE-8888", "LIFETIME", "AVAILABLE", "Spa Mẫu Thử Nghiệm", "Key demo vĩnh viễn");
    addSampleKey(sheet, "VD-DEMO-1MTH-9999", "1_MONTH", "AVAILABLE", "Spa Khách Dùng Thử 1 Tháng", "Gói 1 tháng");
  }

  SpreadsheetApp.getActiveSpreadsheet().toast("Đã khởi tạo bảng quản lý License thành công!", "Thông Báo", 3);
}

function addSampleKey(sheet, key, plan, status, customer, notes) {
  const nowStr = Utilities.formatDate(new Date(), "Asia/Ho_Chi_Minh", "dd/MM/yyyy HH:mm");
  sheet.appendRow([
    key,
    plan,
    status,
    "",
    "",
    customer,
    nowStr,
    "",
    plan === "LIFETIME" ? "Vĩnh viễn" : "",
    notes
  ]);
}

/**
 * Hộp thoại tạo Key mới trực tiếp từ giao diện Google Sheets
 */
function promptGenerateKeys() {
  const ui = SpreadsheetApp.getUi();
  const countPrompt = ui.prompt("Tạo Key Bản Quyền", "Nhập số lượng key muốn tạo (vd: 1, 5, 10):", ui.ButtonSet.OK_CANCEL);
  if (countPrompt.getSelectedButton() !== ui.Button.OK) return;

  const count = parseInt(countPrompt.getResponseText().trim()) || 1;

  const planPrompt = ui.prompt(
    "Chọn Loại Gói",
    "Nhập mã gói:\n- LIFETIME (Vĩnh viễn)\n- 1_MONTH (1 tháng)\n- 6_MONTHS (6 tháng)\n- 1_YEAR (1 năm)\n(Mặc định: LIFETIME):",
    ui.ButtonSet.OK_CANCEL
  );
  if (planPrompt.getSelectedButton() !== ui.Button.OK) return;

  let plan = planPrompt.getResponseText().trim().toUpperCase();
  if (!plan) plan = "LIFETIME";

  const customerPrompt = ui.prompt("Ghi Chú Khách Hàng", "Nhập tên khách hàng hoặc Spa (tùy chọn):", ui.ButtonSet.OK_CANCEL);
  const customer = customerPrompt.getResponseText().trim();

  const generated = generateKeys(count, plan, customer, "Tạo từ Menu Sheet");
  ui.alert("Thành Công", "Đã tạo thành công " + generated.length + " key mới:\n\n" + generated.join("\n"), ui.ButtonSet.OK);
}

/**
 * Hàm sinh Key ngẫu nhiên không trùng lặp
 * Định dạng: VD-XXXX-XXXX-XXXX
 */
function generateKeys(count, plan, customerName, notes) {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  let sheet = ss.getSheetByName(SHEET_NAME);
  if (!sheet) {
    setupSheet();
    sheet = ss.getSheetByName(SHEET_NAME);
  }

  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Loại bỏ O, 0, I, 1 để tránh nhầm lẫn
  const generatedList = [];
  const nowStr = Utilities.formatDate(new Date(), "Asia/Ho_Chi_Minh", "dd/MM/yyyy HH:mm");

  for (let i = 0; i < count; i++) {
    let part1 = "", part2 = "", part3 = "";
    for (let j = 0; j < 4; j++) part1 += chars.charAt(Math.floor(Math.random() * chars.length));
    for (let j = 0; j < 4; j++) part2 += chars.charAt(Math.floor(Math.random() * chars.length));
    for (let j = 0; j < 4; j++) part3 += chars.charAt(Math.floor(Math.random() * chars.length));

    const key = "VD-" + part1 + "-" + part2 + "-" + part3;
    generatedList.push(key);

    const rowData = [
      key,
      plan,
      "AVAILABLE",
      "",
      "",
      customerName || "",
      nowStr,
      "",
      plan === "LIFETIME" ? "Vĩnh viễn" : "",
      notes || ""
    ];

    sheet.appendRow(rowData);
  }

  return generatedList;
}

/**
 * Reset thiết bị cho dòng đang chọn (Cho phép khách hàng đổi sang điện thoại mới)
 */
function resetSelectedDevice() {
  const ui = SpreadsheetApp.getUi();
  const sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  const row = sheet.getActiveCell().getRow();

  if (row <= 1) {
    ui.alert("Vui lòng chọn dòng chứa Key bạn muốn reset!");
    return;
  }

  const key = sheet.getRange(row, COL_KEY).getValue();
  const confirm = ui.alert("Xác Nhận Reset Thiết Bị", "Bạn có chắc chắn muốn xóa liên kết thiết bị cho Key: " + key + "?\nKhách hàng sẽ có thể kích hoạt key này trên thiết bị mới.", ui.ButtonSet.YES_NO);
  if (confirm !== ui.Button.YES) return;

  sheet.getRange(row, COL_DEVICE_ID).setValue("");
  sheet.getRange(row, COL_DEVICE_NAME).setValue("");
  sheet.getRange(row, COL_STATUS).setValue("AVAILABLE");

  ui.alert("Đã reset thiết bị thành công! Key " + key + " hiện có thể dùng trên thiết bị khác.");
}

/**
 * Khóa hoặc Mở khóa Key đang chọn
 */
function toggleBlockSelectedKey() {
  const sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  const row = sheet.getActiveCell().getRow();
  if (row <= 1) return;

  const currentStatus = sheet.getRange(row, COL_STATUS).getValue();
  const newStatus = (currentStatus === "BLOCKED") ? "AVAILABLE" : "BLOCKED";
  sheet.getRange(row, COL_STATUS).setValue(newStatus);

  SpreadsheetApp.getActiveSpreadsheet().toast("Đã chuyển trạng thái Key sang: " + newStatus, "Thông Báo", 3);
}

/**
 * ==============================================================================
 * XỬ LÝ API WEB (HTTP GET & POST)
 * ==============================================================================
 */
function doGet(e) {
  return handleApiRequest(e);
}

function doPost(e) {
  return handleApiRequest(e);
}

function handleApiRequest(e) {
  const params = (e && e.parameter) ? e.parameter : {};
  const action = (params.action || "").trim().toLowerCase();

  // Đảm bảo trả về JSON
  const createJsonResponse = function(data) {
    return ContentService.createTextOutput(JSON.stringify(data))
      .setMimeType(ContentService.MimeType.JSON);
  };

  try {
    if (action === "ping") {
      return createJsonResponse({
        success: true,
        message: "Hệ thống xác thực bản quyền Văn Định Skinlab đang hoạt động tốt.",
        serverTime: new Date().toISOString()
      });
    }

    if (action === "activate" || action === "verify") {
      const key = (params.key || "").trim().toUpperCase();
      const deviceId = (params.deviceId || "").trim();
      const deviceName = (params.deviceName || "Unknown Device").trim();

      if (!key) {
        return createJsonResponse({ success: false, message: "Thiếu mã Key bản quyền." });
      }
      if (!deviceId) {
        return createJsonResponse({ success: false, message: "Không thể định danh thiết bị (Thiếu Device ID)." });
      }

      return createJsonResponse(processKeyActivation(key, deviceId, deviceName, action === "activate"));
    }

    if (action === "add_key") {
      const key = (params.key || "").trim().toUpperCase();
      const plan = (params.plan || "LIFETIME").trim().toUpperCase();
      const notes = (params.notes || "Sale Mua Qua payOS").trim();
      const customer = (params.customer || "Khách Hàng Của Sale").trim();

      if (!key) {
        return createJsonResponse({ success: false, message: "Thiếu mã Key." });
      }

      const ss = SpreadsheetApp.getActiveSpreadsheet();
      let sheet = ss.getSheetByName(SHEET_NAME);
      if (!sheet) {
        setupSheet();
        sheet = ss.getSheetByName(SHEET_NAME);
      }

      const nowStr = Utilities.formatDate(new Date(), "Asia/Ho_Chi_Minh", "dd/MM/yyyy HH:mm");
      sheet.appendRow([
        key,
        plan,
        "AVAILABLE",
        "",
        "",
        customer,
        nowStr,
        "",
        plan === "LIFETIME" ? "Vĩnh viễn" : "",
        notes
      ]);

      return createJsonResponse({
        success: true,
        message: "Đã lưu key mới từ payOS vào Google Sheet thành công",
        key: key,
        plan: plan
      });
    }

    return createJsonResponse({
      success: false,
      message: "Action không hợp lệ. Hỗ trợ: 'ping', 'activate', 'verify', 'add_key'."
    });

  } catch (err) {
    return createJsonResponse({
      success: false,
      message: "Lỗi xử lý máy chủ: " + err.toString()
    });
  }
}

/**
 * Logic cốt lõi: Kiểm tra và khóa Key vào đúng 1 thiết bị
 */
function processKeyActivation(key, deviceId, deviceName, isActivateAction) {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  let sheet = ss.getSheetByName(SHEET_NAME);
  if (!sheet) {
    setupSheet();
    sheet = ss.getSheetByName(SHEET_NAME);
  }

  const data = sheet.getDataRange().getValues();
  let foundRow = -1;
  let rowData = null;

  for (let i = 1; i < data.length; i++) {
    const currentKey = (data[i][COL_KEY - 1] || "").toString().trim().toUpperCase();
    if (currentKey === key) {
      foundRow = i + 1; // 1-indexed trong Sheets
      rowData = data[i];
      break;
    }
  }

  // 1. Không tìm thấy Key
  if (foundRow === -1) {
    return {
      success: false,
      message: "Key kích hoạt không tồn tại trên hệ thống. Vui lòng kiểm tra lại chính xác."
    };
  }

  const plan = (rowData[COL_PLAN - 1] || "LIFETIME").toString().trim().toUpperCase();
  const status = (rowData[COL_STATUS - 1] || "AVAILABLE").toString().trim().toUpperCase();
  const linkedDeviceId = (rowData[COL_DEVICE_ID - 1] || "").toString().trim();
  const linkedDeviceName = (rowData[COL_DEVICE_NAME - 1] || "").toString().trim();
  const activatedAt = (rowData[COL_ACTIVATED_AT - 1] || "").toString().trim();
  let expiresAt = (rowData[COL_EXPIRES_AT - 1] || "").toString().trim();

  // 2. Key bị quản trị viên khóa / thu hồi
  if (status === "BLOCKED") {
    return {
      success: false,
      message: "Key bản quyền này đã bị khóa hoặc thu hồi bởi quản trị viên. Vui lòng liên hệ hỗ trợ."
    };
  }

  const isLifetime = (plan === "LIFETIME" || plan === "TRỌN ĐỜI" || plan === "PRO");

  // 3. Key ĐÃ ĐƯỢC KÍCH HOẠT TRƯỚC ĐÓ
  if (status === "ACTIVATED") {
    // A. Nếu thiết bị gửi lên TRÙNG với thiết bị đã đăng ký
    if (linkedDeviceId === deviceId) {
      // Kiểm tra ngày hết hạn
      if (!isLifetime && expiresAt) {
        const isExpired = checkIfExpired(expiresAt);
        if (isExpired) {
          sheet.getRange(foundRow, COL_STATUS).setValue("EXPIRED");
          return {
            success: false,
            isExpired: true,
            message: "Key bản quyền của bạn đã hết hạn sử dụng vào ngày " + expiresAt + ". Vui lòng gia hạn để tiếp tục."
          };
        }
      }

      return {
        success: true,
        message: "Xác thực bản quyền thành công!",
        plan: plan,
        isLifetime: isLifetime,
        activatedAt: activatedAt,
        expiresAt: expiresAt || (isLifetime ? "Vĩnh viễn" : ""),
        deviceId: deviceId
      };
    }

    // B. Nếu thiết bị gửi lên KHÁC với thiết bị đã kích hoạt -> CHẶN NGAY
    const prevDevice = linkedDeviceName ? ("'" + linkedDeviceName + "'") : "thiết bị khác";
    const prevDate = activatedAt ? (" vào ngày " + activatedAt) : "";
    return {
      success: false,
      message: "Key này đã được kích hoạt trên " + prevDevice + prevDate + ". Mỗi key chỉ dùng được trên 1 thiết bị duy nhất!"
    };
  }

  // 4. Key CHƯA KÍCH HOẠT (AVAILABLE)
  if (status === "AVAILABLE") {
    const now = new Date();
    const nowStr = Utilities.formatDate(now, "Asia/Ho_Chi_Minh", "dd/MM/yyyy HH:mm");

    // Tính ngày hết hạn theo loại gói
    let expiresStr = "Vĩnh viễn";
    if (!isLifetime) {
      const expDate = calculateExpirationDate(now, plan);
      expiresStr = Utilities.formatDate(expDate, "Asia/Ho_Chi_Minh", "dd/MM/yyyy HH:mm");
    }

    // Cập nhật dòng trên sheet: Khóa ngay thiết bị này vào Key
    sheet.getRange(foundRow, COL_STATUS).setValue("ACTIVATED");
    sheet.getRange(foundRow, COL_DEVICE_ID).setValue(deviceId);
    sheet.getRange(foundRow, COL_DEVICE_NAME).setValue(deviceName);
    sheet.getRange(foundRow, COL_ACTIVATED_AT).setValue(nowStr);
    sheet.getRange(foundRow, COL_EXPIRES_AT).setValue(expiresStr);

    return {
      success: true,
      message: isLifetime
        ? "Kích hoạt bản quyền trọn đời (PRO) thành công trên thiết bị này!"
        : "Kích hoạt bản quyền thành công! Hạn dùng đến: " + expiresStr,
      plan: plan,
      isLifetime: isLifetime,
      activatedAt: nowStr,
      expiresAt: expiresStr,
      deviceId: deviceId
    };
  }

  // 5. Trạng thái khác (ví dụ EXPIRED)
  return {
    success: false,
    message: "Key bản quyền đang ở trạng thái: " + status + ". Không thể kích hoạt."
  };
}

/**
 * Tính ngày hết hạn dựa theo mã gói
 */
function calculateExpirationDate(fromDate, plan) {
  const d = new Date(fromDate.getTime());
  switch (plan) {
    case "1_MONTH":
    case "30_DAYS":
      d.setDate(d.getDate() + 30);
      break;
    case "3_MONTHS":
    case "90_DAYS":
      d.setDate(d.getDate() + 90);
      break;
    case "6_MONTHS":
    case "180_DAYS":
      d.setDate(d.getDate() + 180);
      break;
    case "1_YEAR":
    case "365_DAYS":
      d.setDate(d.getDate() + 365);
      break;
    default:
      // Nếu nhập một con số cụ thể, vd "15" -> 15 ngày
      const numDays = parseInt(plan);
      if (!isNaN(numDays) && numDays > 0) {
        d.setDate(d.getDate() + numDays);
      } else {
        d.setDate(d.getDate() + 365); // Mặc định 1 năm nếu không rõ
      }
      break;
  }
  return d;
}

/**
 * Kiểm tra chuỗi ngày dd/MM/yyyy HH:mm xem đã qua thời điểm hiện tại chưa
 */
function checkIfExpired(dateStr) {
  if (!dateStr || dateStr.toLowerCase().indexOf("vĩnh viễn") >= 0) return false;
  try {
    const parts = dateStr.split(" ");
    const dParts = parts[0].split("/");
    const tParts = (parts[1] || "00:00").split(":");
    const day = parseInt(dParts[0]);
    const month = parseInt(dParts[1]) - 1;
    const year = parseInt(dParts[2]);
    const hour = parseInt(tParts[0]);
    const min = parseInt(tParts[1]);

    const targetDate = new Date(year, month, day, hour, min);
    return new Date().getTime() > targetDate.getTime();
  } catch (e) {
    return false;
  }
}
