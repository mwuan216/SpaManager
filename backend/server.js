/**
 * SpaCare Manager - Backend API & Real-time VietQR Webhook Service
 * Cung cấp RESTful API, Server-Sent Events (SSE) và Webhook xử lý biến động số dư ngân hàng
 */

const express = require('express');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// In-memory Database Mockup
let clients = {
  "KH-8829": {
    id: "KH-8829",
    fullName: "Nguyễn Thu Hà",
    phone: "0912 345 678",
    email: "thuha.nguyen@gmail.com",
    vipTier: "Hội Viên Vàng (VIP)",
    skinType: "Da hỗn hợp thiên dầu, biểu bì mỏng",
    allergyAlerts: [
      "Dị ứng Retinol > 0.5%",
      "Nhạy cảm với hương liệu nhân tạo",
      "Kích ứng với Tinh dầu tràm trà đậm đặc"
    ],
    contraindications: [
      "Không dùng máy sóng RF tần số cao vùng gò má",
      "Chống chỉ định peel da axit nồng độ cao"
    ],
    treatmentPackages: [
      {
        id: "PKG-01",
        name: "Liệu Trình Căng Bóng Phục Hồi Whitening Facial",
        totalSessions: 10,
        usedSessions: 7,
        remainingSessions: 3,
        expiryDate: "12/12/2026",
        assignedTherapist: "KTV. Thảo My"
      }
    ]
  }
};

let invoices = {
  "SPA8829": {
    invoiceCode: "SPA8829",
    clientId: "KH-8829",
    clientName: "Nguyễn Thu Hà",
    subtotal: 2250000,
    discountAmount: 225000,
    finalTotal: 2025000,
    status: "WAITING_FOR_PAYMENT", // WAITING_FOR_PAYMENT | PAID | FAILED
    transactionRef: null,
    paidAt: null
  }
};

// Lưu các SSE client đang lắng nghe trạng thái thanh toán hóa đơn
let sseSubscribers = {};

// -------------------------------------------------------------
// 1. API CRM Khách Hàng
// -------------------------------------------------------------
app.get('/api/v1/clients/:id', (req, res) => {
  const client = clients[req.params.id];
  if (!client) {
    return res.status(404).json({ error: "Không tìm thấy khách hàng" });
  }
  return res.json({ success: true, data: client });
});

// Trừ buổi liệu trình (Check-in session)
app.post('/api/v1/clients/:id/packages/:packageId/use-session', (req, res) => {
  const client = clients[req.params.id];
  if (!client) return res.status(404).json({ error: "Không tìm thấy khách" });

  const pkg = client.treatmentPackages.find(p => p.id === req.params.packageId);
  if (!pkg || pkg.remainingSessions <= 0) {
    return res.status(400).json({ error: "Gói dịch vụ đã hết buổi khả dụng" });
  }

  pkg.usedSessions += 1;
  pkg.remainingSessions -= 1;

  res.json({
    success: true,
    message: `Đã điểm danh buổi thành công. Còn lại ${pkg.remainingSessions}/${pkg.totalSessions} buổi.`,
    data: pkg
  });
});

// -------------------------------------------------------------
// 2. API Quản Lý Lịch Hẹn (Calendar & Schedule)
// -------------------------------------------------------------
app.get('/api/v1/appointments', (req, res) => {
  const { date } = req.query;
  // Trả về danh sách lịch hẹn trong ngày
  res.json({
    success: true,
    data: [
      {
        id: "APT-101",
        clientName: "Nguyễn Thu Hà",
        serviceName: "Whitening Facial (Buổi 8/10)",
        startTime: "09:30",
        endTime: "11:00",
        room: "Phòng VIP 02",
        therapist: "Thảo My",
        status: "CONFIRMED"
      },
      {
        id: "APT-102",
        clientName: "Trần Bích Ngọc",
        serviceName: "Massage Body Đá Nóng Himalaya 90p",
        startTime: "10:00",
        endTime: "11:30",
        room: "Phòng Trị Liệu Sen",
        therapist: "Hương Giang",
        status: "IN_SERVICE"
      }
    ]
  });
});

// -------------------------------------------------------------
// 3. API Thu Ngân & VietQR Thanh Toán Thời Gian Thực
// -------------------------------------------------------------

// Tạo VietQR Payload chuẩn NAPAS 247
app.post('/api/v1/checkout/create-vietqr', (req, res) => {
  const { invoiceCode, amount, bankBin = "970422", accountNumber = "0888999888" } = req.body;
  const qrUrl = `https://img.vietqr.io/image/${bankBin}-${accountNumber}-compact2.png?amount=${amount}&addInfo=${invoiceCode}&accountName=SPA%20CARE%20WELLNESS%20CENTER`;

  res.json({
    success: true,
    data: {
      bankBin,
      bankName: "MBBank",
      accountNumber,
      accountHolder: "SPA CARE WELLNESS CENTER",
      amount,
      referenceCode: invoiceCode,
      qrImageUrl: qrUrl
    }
  });
});

// Server-Sent Events (SSE) để Tablet Lễ Tân nhận cập nhật thanh toán tức thì
app.get('/api/v1/checkout/stream-status/:invoiceCode', (req, res) => {
  const invoiceCode = req.params.invoiceCode;

  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');

  if (!sseSubscribers[invoiceCode]) {
    sseSubscribers[invoiceCode] = [];
  }
  sseSubscribers[invoiceCode].push(res);

  // Gửi trạng thái ban đầu
  const currentInvoice = invoices[invoiceCode];
  res.write(`data: ${JSON.stringify(currentInvoice || { status: 'UNKNOWN' })}\n\n`);

  req.on('close', () => {
    sseSubscribers[invoiceCode] = sseSubscribers[invoiceCode].filter(client => client !== res);
  });
});

/**
 * Webhook Nhận Biến Động Số Dư (Tương thích VietQR / Casso / SePAY / PayOS)
 * Khi tiền nảy vào tài khoản ngân hàng của Spa, hệ thống ngân hàng bắn Webhook tới đây.
 */
app.post('/api/v1/webhooks/bank-payment', (req, res) => {
  /*
    Payload mẫu từ Casso / SePAY:
    {
      "gateway": "MBBank",
      "transactionDate": "2026-10-16 20:15:00",
      "accountNumber": "0888999888",
      "transferType": "in",
      "transferAmount": 2025000,
      "accumulated": 458900000,
      "code": null,
      "content": "Thanh toan SPA8829",
      "referenceCode": "MB129482"
    }
  */
  const { content, transferAmount, referenceCode } = req.body;
  console.log(`[BANK WEBHOOK] Nhận giao dịch mới: ${transferAmount}đ - Nội dung: "${content}"`);

  // Trích xuất mã hóa đơn từ nội dung chuyển khoản (regex tìm SPA...)
  const match = content.match(/SPA\d+/i);
  if (match) {
    const invoiceCode = match[0].toUpperCase();
    if (invoices[invoiceCode]) {
      invoices[invoiceCode].status = "PAID";
      invoices[invoiceCode].transactionRef = referenceCode || `MB-${Date.now().toString().slice(-6)}`;
      invoices[invoiceCode].paidAt = new Date().toISOString();

      // Bắn tín hiệu tức thì tới màn hình thu ngân của Lễ Tân qua SSE
      if (sseSubscribers[invoiceCode]) {
        sseSubscribers[invoiceCode].forEach(client => {
          client.write(`data: ${JSON.stringify(invoices[invoiceCode])}\n\n`);
        });
      }

      console.log(`[PAYMENT NOTIFICATION] Khách hàng hóa đơn ${invoiceCode} đã thanh toán thành công!`);
    }
  }

  res.json({ success: true, message: "Webhook processed" });
});

// -------------------------------------------------------------
// 4. API Báo Cáo Doanh Thu & Hiệu Suất (Dashboard Metrics)
// -------------------------------------------------------------
app.get('/api/v1/dashboard/metrics', (req, res) => {
  res.json({
    success: true,
    data: {
      todayRevenue: 18650000,
      growthPercent: 14.8,
      weekRevenue: 112400000,
      monthRevenue: 465800000,
      roomOccupancyRate: 84.5,
      revenueDistribution: {
        treatmentPackages: 265000000,
        singleServices: 120800000,
        retailProducts: 80000000
      }
    }
  });
});

app.listen(PORT, () => {
  console.log(`[SpaCare Backend Server] Đang chạy tại http://localhost:${PORT}`);
});

