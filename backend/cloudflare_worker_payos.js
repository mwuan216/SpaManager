/**
 * ==============================================================================
 * CLOUDFLARE WORKER XÁC THỰC THANH TOÁN payOS & PHÁT HÀNH KEY BẢN QUYỀN TỰ ĐỘNG
 * ==============================================================================
 * Hỗ trợ các ứng dụng:
 * 1. Văn Định Skinlab (SpaManager) - Kênh bán sỉ cho Sale (Chiết khấu 70%, tác giả nhận 30%)
 *    - Gói 1 Tháng (spa_1m): Giá sỉ 24.000 ₫ (Bán lẻ 80.000 ₫)
 *    - Gói 3 Tháng (spa_3m): Giá sỉ 54.000 ₫ (Bán lẻ 180.000 ₫)
 *    - Gói 6 Tháng (spa_6m): Giá sỉ 96.000 ₫ (Bán lẻ 320.000 ₫)
 *    - Gói 1 Năm (spa_1y): Giá sỉ 150.000 ₫ (Bán lẻ 500.000 ₫)
 *    - Gói Vĩnh Viễn (spa_life): Giá sỉ 450.000 ₫ (Bán lẻ 1.500.000 ₫)
 * 2. Sổ Thu Chi (sothuchi)
 * 3. WClone (wclone)
 *
 * CÁCH CẬP NHẬT TRÊN CLOUDFLARE WORKERS (wclon-payment):
 * 1. Mở https://dash.cloudflare.com/ -> Workers & Pages -> wclon-payment.
 * 2. Bấm "Edit code", dán toàn bộ nội dung file này vào và bấm "Deploy".
 * ==============================================================================
 */

const SECRET_SALT = "WCLONE_SALT_SECURE_2026_TIERS";

const TIERS = {
    // --------------------------------------------------------------------------
    // VĂN ĐỊNH SKINLAB (SPAMANAGER) - GÓI SỈ CHO SALE (SALE NHẬN 70%, BẠN NHẬN 30%)
    // --------------------------------------------------------------------------
    "spa_1m": {
        id: "spa_1m",
        app: "spamanager",
        tag: "1M",
        plan: "1_MONTH",
        retailPrice: 80000,
        minAmount: 24000, // 30% của 80k
        title: "Gói 1 Tháng (SpaManager)",
        description: "Khách lẻ: 80k - Sale thanh toán: 24k"
    },
    "spa_3m": {
        id: "spa_3m",
        app: "spamanager",
        tag: "3M",
        plan: "3_MONTHS",
        retailPrice: 180000,
        minAmount: 54000, // 30% của 180k
        title: "Gói 3 Tháng (SpaManager)",
        description: "Khách lẻ: 180k - Sale thanh toán: 54k"
    },
    "spa_6m": {
        id: "spa_6m",
        app: "spamanager",
        tag: "6M",
        plan: "6_MONTHS",
        retailPrice: 320000,
        minAmount: 96000, // 30% của 320k
        title: "Gói 6 Tháng (SpaManager)",
        description: "Khách lẻ: 320k - Sale thanh toán: 96k"
    },
    "spa_1y": {
        id: "spa_1y",
        app: "spamanager",
        tag: "1Y",
        plan: "1_YEAR",
        retailPrice: 500000,
        minAmount: 150000, // 30% của 500k
        title: "Gói 1 Năm (SpaManager)",
        description: "Khách lẻ: 500k - Sale thanh toán: 150k"
    },
    "spa_life": {
        id: "spa_life",
        app: "spamanager",
        tag: "LIFE",
        plan: "LIFETIME",
        retailPrice: 1500000,
        minAmount: 450000, // 30% của 1.5tr
        title: "Gói Vĩnh Viễn (SpaManager)",
        description: "Khách lẻ: 1.5tr - Sale thanh toán: 450k"
    },

    // --------------------------------------------------------------------------
    // SỔ THU CHI APP
    // --------------------------------------------------------------------------
    "tier_sothuchi_pro": {
        id: "tier_sothuchi_pro",
        app: "sothuchi",
        tag: "STC",
        minAmount: 59000,
        title: "Sổ Thu Chi Pro (Vĩnh viễn)"
    },
    "sothuchi": {
        id: "tier_sothuchi_pro",
        app: "sothuchi",
        tag: "STC",
        minAmount: 59000,
        title: "Sổ Thu Chi Pro (Vĩnh viễn)"
    },
    "stc": {
        id: "tier_sothuchi_pro",
        app: "sothuchi",
        tag: "STC",
        minAmount: 59000,
        title: "Sổ Thu Chi Pro (Vĩnh viễn)"
    },

    // --------------------------------------------------------------------------
    // WCLONE APP
    // --------------------------------------------------------------------------
    "tier_basic": {
        id: "tier_basic",
        app: "wclone",
        tag: "BASIC",
        minAmount: 59000,
        title: "Gói Cơ Bản (5 App)"
    },
    "tier_ultra": {
        id: "tier_ultra",
        app: "wclone",
        tag: "ULTRA",
        minAmount: 99000,
        title: "Gói Ultra Full App"
    },
    "tier_3": { id: "tier_basic", app: "wclone", tag: "BASIC", minAmount: 59000, title: "Gói Cơ Bản (5 App)" },
    "tier_5": { id: "tier_basic", app: "wclone", tag: "BASIC", minAmount: 59000, title: "Gói Cơ Bản (5 App)" },
    "tier_full": { id: "tier_ultra", app: "wclone", tag: "ULTRA", minAmount: 99000, title: "Gói Ultra Full App" }
};

// Bộ nhớ đệm tạm thời cho đơn hàng gần nhất theo deviceId và tier
const activeOrdersCache = new Map();
// Cache lưu trữ bản quyền đã thanh toán để phục vụ khôi phục
const paidLicensesCache = new Map();

function formatVnDate(date) {
    const vnTime = new Date(date.getTime() + 7 * 3600 * 1000);
    const day = String(vnTime.getUTCDate()).padStart(2, "0");
    const month = String(vnTime.getUTCMonth() + 1).padStart(2, "0");
    const year = vnTime.getUTCFullYear();
    const hours = String(vnTime.getUTCHours()).padStart(2, "0");
    const mins = String(vnTime.getUTCMinutes()).padStart(2, "0");
    return `${day}/${month}/${year} ${hours}:${mins}`;
}

function getPlanDurationDays(plan) {
    const p = (plan || "").toString().trim().toUpperCase();
    switch (p) {
        case "1_MONTH": case "30_DAYS": case "SPA_1M": case "1M": return 30;
        case "3_MONTHS": case "90_DAYS": case "SPA_3M": case "3M": return 90;
        case "6_MONTHS": case "180_DAYS": case "SPA_6M": case "6M": return 180;
        case "1_YEAR": case "365_DAYS": case "SPA_1Y": case "1Y": return 365;
        default:
            const n = parseInt(p);
            return (!isNaN(n) && n > 0) ? n : 30;
    }
}

function isPlanProLifetime(plan) {
    if (!plan) return true;
    const p = plan.toString().trim().toUpperCase();
    return p === "LIFETIME" || p === "SPA_LIFE" || p === "PRO" || p === "VĨNH VIỄN" || p === "TRỌN ĐỜI";
}

export default {
    async fetch(request, env, ctx) {
        // Xử lý CORS để app di động và web đều gọi được
        if (request.method === "OPTIONS") {
            return new Response(null, {
                headers: {
                    "Access-Control-Allow-Origin": "*",
                    "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
                    "Access-Control-Allow-Headers": "Content-Type, Authorization, x-client-id, x-api-key"
                }
            });
        }

        const url = new URL(request.url);
        const path = url.pathname.replace(/\/$/, "");

        // =========================================================================
        // 1. ENDPOINT: /create-payment (Tạo yêu cầu thanh toán qua payOS)
        // =========================================================================
        if (path === "/create-payment" || path === "/api/create-payment") {
            const deviceId = (url.searchParams.get("deviceId") || "").trim().toUpperCase();
            const app = (url.searchParams.get("app") || "").trim().toLowerCase();
            const requestedTierId = (url.searchParams.get("tier") || (app === "spamanager" ? "spa_1y" : (app === "sothuchi" ? "tier_sothuchi_pro" : "tier_basic"))).trim().toLowerCase();

            if (!deviceId || deviceId.length < 4) {
                return jsonResponse({
                    success: false,
                    error: "Mã thiết bị (deviceId) không hợp lệ."
                }, 400);
            }

            const tier = TIERS[requestedTierId] || (app === "spamanager" ? TIERS["spa_1y"] : (app === "sothuchi" ? TIERS["tier_sothuchi_pro"] : TIERS["tier_basic"]));

            // Kiểm tra cấu hình payOS
            if (!env.PAYOS_CLIENT_ID || !env.PAYOS_API_KEY || !env.PAYOS_CHECKSUM_KEY) {
                return jsonResponse({
                    success: false,
                    error: "Chưa cấu hình PAYOS_CLIENT_ID, PAYOS_API_KEY, PAYOS_CHECKSUM_KEY trên Cloudflare Worker Settings."
                }, 500);
            }

            try {
                // Sinh mã đơn hàng số nguyên ngẫu nhiên (11-12 chữ số)
                const timestampSec = Math.floor(Date.now() / 1000);
                const randomPart = Math.floor(Math.random() * 90 + 10);
                const orderCode = Number(`${timestampSec}${randomPart}`);

                // Chuẩn hóa nội dung chuyển khoản (chỉ chữ và số, tối đa 25 ký tự theo chuẩn payOS)
                const cleanDeviceId = deviceId.replace(/[^A-Za-z0-9]/g, "").substring(0, 10);
                let description;
                if (tier.app === "spamanager" || app === "spamanager") {
                    description = `SPA ${tier.tag} ${cleanDeviceId}`.substring(0, 25);
                } else if (tier.app === "sothuchi" || app === "sothuchi") {
                    description = `STC ${cleanDeviceId}`.substring(0, 25);
                } else {
                    description = `WCLONE ${tier.tag} ${cleanDeviceId}`.substring(0, 25);
                }

                const cancelUrl = "https://vandinh.vn/cancel";
                const returnUrl = "https://vandinh.vn/success";

                // Ký dữ liệu chuẩn theo thứ tự alphabet: amount, cancelUrl, description, orderCode, returnUrl
                const signatureData = `amount=${tier.minAmount}&cancelUrl=${cancelUrl}&description=${description}&orderCode=${orderCode}&returnUrl=${returnUrl}`;
                const signature = await hmacSha256(env.PAYOS_CHECKSUM_KEY.trim(), signatureData);

                // Gọi API payOS để tạo Payment Link
                const payosRes = await fetch("https://api-merchant.payos.vn/v2/payment-requests", {
                    method: "POST",
                    headers: {
                        "x-client-id": env.PAYOS_CLIENT_ID.trim(),
                        "x-api-key": env.PAYOS_API_KEY.trim(),
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        orderCode: orderCode,
                        amount: tier.minAmount,
                        description: description,
                        cancelUrl: cancelUrl,
                        returnUrl: returnUrl,
                        signature: signature
                    })
                });

                const payosData = await payosRes.json();

                if (!payosRes.ok || payosData.code !== "00") {
                    return jsonResponse({
                        success: false,
                        error: `Lỗi từ payOS (${payosData.code}): ${payosData.desc || "Không thể tạo yêu cầu thanh toán."}`
                    }, 502);
                }

                // Lưu cache orderCode cho thiết bị này
                const cacheKey = `${deviceId}_${tier.id}`;
                activeOrdersCache.set(cacheKey, {
                    orderCode: orderCode,
                    createdAt: Date.now()
                });

                return jsonResponse({
                    success: true,
                    orderCode: orderCode,
                    amount: tier.minAmount,
                    retailPrice: tier.retailPrice || tier.minAmount,
                    profit: (tier.retailPrice ? (tier.retailPrice - tier.minAmount) : 0),
                    description: description,
                    tier: tier.id,
                    tierTitle: tier.title,
                    plan: tier.plan || "LIFETIME",
                    checkoutUrl: payosData.data.checkoutUrl,
                    qrCode: payosData.data.qrCode,
                    accountNumber: payosData.data.accountNumber || "8500121062001",
                    accountName: payosData.data.accountName || "BUI MINH QUAN",
                    bin: payosData.data.bin || "970422"
                });

            } catch (err) {
                return jsonResponse({
                    success: false,
                    error: "Lỗi tạo đơn thanh toán payOS: " + err.message
                }, 500);
            }
        }

        // =========================================================================
        // 2. ENDPOINT: /check-payment (Kiểm tra trạng thái thanh toán & Cấp License Key)
        // =========================================================================
        if (path === "/check-payment" || path === "/api/check-payment") {
            const deviceId = (url.searchParams.get("deviceId") || "").trim().toUpperCase();
            const app = (url.searchParams.get("app") || "").trim().toLowerCase();
            const requestedTierId = (url.searchParams.get("tier") || (app === "spamanager" ? "spa_1y" : (app === "sothuchi" ? "tier_sothuchi_pro" : "tier_basic"))).trim().toLowerCase();
            let orderCode = url.searchParams.get("orderCode");

            if (!deviceId || deviceId.length < 4) {
                return jsonResponse({
                    success: false,
                    error: "Mã thiết bị (deviceId) không hợp lệ."
                }, 400);
            }

            const tier = TIERS[requestedTierId] || (app === "spamanager" ? TIERS["spa_1y"] : (app === "sothuchi" ? TIERS["tier_sothuchi_pro"] : TIERS["tier_basic"]));

            // 1. Kiểm tra qua payOS
            if (env.PAYOS_CLIENT_ID && env.PAYOS_API_KEY) {
                if (!orderCode) {
                    const cached = activeOrdersCache.get(`${deviceId}_${tier.id}`);
                    if (cached) {
                        orderCode = cached.orderCode;
                    }
                }

                const numericOrderCode = Number(String(orderCode).replace(/\D/g, ""));
                if (numericOrderCode && !isNaN(numericOrderCode)) {
                    try {
                        const checkRes = await fetch(`https://api-merchant.payos.vn/v2/payment-requests/${numericOrderCode}`, {
                            method: "GET",
                            headers: {
                                "x-client-id": env.PAYOS_CLIENT_ID.trim(),
                                "x-api-key": env.PAYOS_API_KEY.trim()
                            }
                        });

                        const checkData = await checkRes.json();

                        if (checkRes.ok && checkData.code === "00" && checkData.data) {
                            const status = checkData.data.status;

                            if (status === "PAID") {
                                // Đã thanh toán thành công -> Tự động sinh Key bản quyền
                                let generatedKey = "";
                                if (tier.app === "spamanager" || app === "spamanager") {
                                    // Sinh key SpaManager dạng VD-TAG-XXXX-YYYY (VD: VD-1Y-7A8B-9C2D)
                                    generatedKey = generateSpaKey(tier.tag);
                                } else {
                                    generatedKey = await createLicenseKey(deviceId, tier.id, tier.tag);
                                }

                                // Lưu cache
                                paidLicensesCache.set(`${deviceId}_${orderCode}`, {
                                    tier: tier.id,
                                    tag: tier.tag,
                                    plan: tier.plan || "LIFETIME",
                                    key: generatedKey,
                                    paidAt: Date.now()
                                });

                                // Lưu vào Cloudflare KV
                                const keyRecord = {
                                    key: generatedKey,
                                    tier: tier.id,
                                    plan: tier.plan || "LIFETIME",
                                    status: "AVAILABLE",
                                    orderCode: numericOrderCode,
                                    purchasedByDeviceId: deviceId,
                                    deviceId: null,
                                    deviceName: null,
                                    activatedAt: null,
                                    expiresAt: null,
                                    expiresMs: 0,
                                    createdAt: Date.now()
                                };

                                if (env.WCLONE_KV) {
                                    try {
                                        await env.WCLONE_KV.put(`order_${numericOrderCode}`, JSON.stringify(keyRecord));
                                        await env.WCLONE_KV.put(`key_${generatedKey}`, JSON.stringify(keyRecord));
                                    } catch (e) {}
                                }
                                paidLicensesCache.set(`key_${generatedKey}`, keyRecord);

                                // Đồng bộ tự động sang Google Sheet nếu có GOOGLE_SHEET_URL
                                if (env.GOOGLE_SHEET_URL && (tier.app === "spamanager" || app === "spamanager")) {
                                    try {
                                        const syncUrl = `${env.GOOGLE_SHEET_URL}?action=add_key&key=${encodeURIComponent(generatedKey)}&plan=${encodeURIComponent(tier.plan || "LIFETIME")}&notes=${encodeURIComponent("Sale_PayOS_" + numericOrderCode)}`;
                                        ctx.waitUntil(fetch(syncUrl));
                                    } catch (e) {}
                                }

                                return jsonResponse({
                                    success: true,
                                    paid: true,
                                    gateway: "payOS",
                                    tier: tier.id,
                                    tierTitle: tier.title,
                                    plan: tier.plan || "LIFETIME",
                                    key: generatedKey,
                                    orderCode: numericOrderCode,
                                    amount: checkData.data.amountPaid || checkData.data.amount,
                                    transaction: {
                                        amount: checkData.data.amountPaid || checkData.data.amount,
                                        date: checkData.data.createdAt,
                                        content: checkData.data.description
                                    },
                                    message: `Đã xác nhận thanh toán thành công qua payOS! Key bản quyền của bạn đã được khởi tạo.`
                                });
                            } else if (status === "CANCELLED") {
                                return jsonResponse({
                                    success: true,
                                    paid: false,
                                    status: "CANCELLED",
                                    message: "Đơn thanh toán này đã bị hủy trên payOS."
                                });
                            } else {
                                return jsonResponse({
                                    success: true,
                                    paid: false,
                                    status: "PENDING",
                                    message: "Đang chờ nhận chuyển khoản qua payOS (MBBank)..."
                                });
                            }
                        }
                    } catch (err) {
                        console.error("Lỗi gọi payOS check: ", err);
                    }
                }
            }

            return jsonResponse({
                success: true,
                paid: false,
                status: "PENDING",
                message: "Hệ thống đang chờ ngân hàng xác nhận giao dịch chuyển khoản."
            });
        }

        // =========================================================================
        // 3. ENDPOINTS XÁC THỰC BẢN QUYỀN (ACTIVATE / RESTORE / VERIFY)
        // =========================================================================
        const actionParam = (url.searchParams.get("action") || "").trim().toLowerCase();

        // 3.1. KÍCH HOẠT KEY (ACTIVATE)
        if (path === "/activate-license" || path === "/api/activate-license" || ((path === "/api/license" || path === "/license") && actionParam === "activate")) {
            const rawKey = (url.searchParams.get("key") || "").trim().toUpperCase();
            const deviceId = (url.searchParams.get("deviceId") || "").trim().toUpperCase();
            const deviceName = (url.searchParams.get("deviceName") || "").trim();

            if (!rawKey) {
                return jsonResponse({ success: false, message: "Vui lòng nhập Key kích hoạt." }, 400);
            }
            if (!deviceId || deviceId.length < 4) {
                return jsonResponse({ success: false, message: "Mã thiết bị (deviceId) không hợp lệ." }, 400);
            }

            // A. Tra cứu trong Cloudflare KV / Cache
            let record = null;
            if (env.WCLONE_KV) {
                try {
                    record = await env.WCLONE_KV.get(`key_${rawKey}`, { type: "json" });
                } catch (e) {}
            }
            if (!record) {
                record = paidLicensesCache.get(`key_${rawKey}`);
            }

            // B. Nếu không tìm thấy trong KV nhưng có cấu hình Google Sheets -> Tra cứu Google Sheet
            if (!record && env.GOOGLE_SHEET_URL) {
                try {
                    const sheetRes = await fetch(`${env.GOOGLE_SHEET_URL}?action=activate&key=${encodeURIComponent(rawKey)}&deviceId=${encodeURIComponent(deviceId)}&deviceName=${encodeURIComponent(deviceName)}`);
                    const sheetData = await sheetRes.json();
                    return jsonResponse(sheetData);
                } catch (e) {}
            }

            if (!record) {
                return jsonResponse({
                    success: false,
                    message: "Key kích hoạt không tồn tại trên hệ thống. Vui lòng kiểm tra lại chính xác."
                }, 404);
            }

            const isLifetime = isPlanProLifetime(record.plan);

            // C. Key đã được kích hoạt trước đó
            if (record.status === "ACTIVATED") {
                // Trùng thiết bị -> Cho phép kích hoạt lại / Khôi phục
                if (record.deviceId === deviceId) {
                    const nowMs = Date.now();
                    if (!isLifetime && record.expiresMs && nowMs > record.expiresMs) {
                        record.status = "EXPIRED";
                        if (env.WCLONE_KV) {
                            try { await env.WCLONE_KV.put(`key_${rawKey}`, JSON.stringify(record)); } catch (e) {}
                        }
                        return jsonResponse({
                            success: false,
                            isExpired: true,
                            message: `Key bản quyền này đã hết hạn sử dụng vào ngày ${record.expiresAt}. Vui lòng mua Key mới.`
                        });
                    }

                    const diffMs = isLifetime ? 0 : Math.max(0, record.expiresMs - nowMs);
                    const daysRemaining = isLifetime ? 0 : Math.floor(diffMs / (24 * 3600 * 1000));
                    const hoursRemaining = isLifetime ? 0 : Math.floor((diffMs % (24 * 3600 * 1000)) / (3600 * 1000));

                    return jsonResponse({
                        success: true,
                        restored: true,
                        message: isLifetime
                            ? "Xác thực bản quyền Trọn Đời thành công trên thiết bị này!"
                            : `Xác thực gói ${record.plan} thành công! Còn ${daysRemaining} ngày sử dụng (đến ${record.expiresAt}).`,
                        key: record.key,
                        plan: record.plan,
                        isLifetime: isLifetime,
                        activatedAt: record.activatedAt,
                        expiresAt: record.expiresAt,
                        expiresMs: record.expiresMs,
                        daysRemaining: daysRemaining,
                        hoursRemaining: hoursRemaining,
                        deviceId: deviceId
                    });
                }

                // Khác thiết bị -> Chặn
                const prevDev = record.deviceName ? `'${record.deviceName}'` : "thiết bị khác";
                return jsonResponse({
                    success: false,
                    message: `Key này đã được kích hoạt trên ${prevDev} vào ngày ${record.activatedAt}. Mỗi key chỉ dùng được trên 1 thiết bị duy nhất!`
                }, 403);
            }

            // D. Key chưa kích hoạt (AVAILABLE) -> Kích hoạt lần đầu
            if (record.status === "AVAILABLE") {
                const now = new Date();
                const nowMs = now.getTime();
                const activatedAtStr = formatVnDate(now);

                let expiresAtStr = "Vĩnh viễn";
                let expiresMs = 0;
                let daysRemaining = 0;
                let hoursRemaining = 0;

                if (!isLifetime) {
                    const days = getPlanDurationDays(record.plan);
                    const expDate = new Date(nowMs + days * 24 * 3600 * 1000);
                    expiresAtStr = formatVnDate(expDate);
                    expiresMs = expDate.getTime();
                    daysRemaining = days;
                }

                record.status = "ACTIVATED";
                record.deviceId = deviceId;
                record.deviceName = deviceName;
                record.activatedAt = activatedAtStr;
                record.expiresAt = expiresAtStr;
                record.expiresMs = expiresMs;

                if (env.WCLONE_KV) {
                    try {
                        await env.WCLONE_KV.put(`key_${rawKey}`, JSON.stringify(record));
                        await env.WCLONE_KV.put(`device_${deviceId}`, JSON.stringify(record));
                    } catch (e) {}
                }
                paidLicensesCache.set(`key_${rawKey}`, record);
                paidLicensesCache.set(`device_${deviceId}`, record);

                // Đồng bộ sang Google Sheet nếu có
                if (env.GOOGLE_SHEET_URL) {
                    try {
                        const syncUrl = `${env.GOOGLE_SHEET_URL}?action=activate&key=${encodeURIComponent(rawKey)}&deviceId=${encodeURIComponent(deviceId)}&deviceName=${encodeURIComponent(deviceName)}`;
                        ctx.waitUntil(fetch(syncUrl));
                    } catch (e) {}
                }

                return jsonResponse({
                    success: true,
                    message: isLifetime
                        ? "Kích hoạt bản quyền Trọn Đời (PRO) thành công trên thiết bị này!"
                        : `Kích hoạt gói ${record.plan} thành công! Còn ${daysRemaining} ngày sử dụng (đến ${expiresAtStr}).`,
                    key: record.key,
                    plan: record.plan,
                    isLifetime: isLifetime,
                    activatedAt: activatedAtStr,
                    expiresAt: expiresAtStr,
                    expiresMs: expiresMs,
                    daysRemaining: daysRemaining,
                    hoursRemaining: hoursRemaining,
                    deviceId: deviceId
                });
            }

            return jsonResponse({
                success: false,
                message: `Key đang ở trạng thái: ${record.status}. Không thể kích hoạt.`
            }, 400);
        }

        // 3.2. KHÔI PHỤC BẢN QUYỀN (RESTORE)
        if (path === "/restore-license" || path === "/api/restore-license" || ((path === "/api/license" || path === "/license") && actionParam === "restore")) {
            const deviceId = (url.searchParams.get("deviceId") || "").trim().toUpperCase();
            if (!deviceId || deviceId.length < 4) {
                return jsonResponse({
                    success: false,
                    error: "Mã thiết bị (deviceId) không hợp lệ."
                }, 400);
            }

            // 1. Kiểm tra Cloudflare KV theo Device ID
            let saved = null;
            if (env.WCLONE_KV) {
                try {
                    saved = await env.WCLONE_KV.get(`device_${deviceId}`, { type: "json" });
                } catch (e) {}
            }
            if (!saved) {
                saved = paidLicensesCache.get(`device_${deviceId}`);
            }

            // 2. Nếu tìm thấy bản quyền đã kích hoạt trên thiết bị này
            if (saved && saved.key) {
                const isLifetime = isPlanProLifetime(saved.plan);
                const nowMs = Date.now();

                // Kiểm tra hết hạn
                if (!isLifetime && saved.expiresMs && nowMs > saved.expiresMs) {
                    return jsonResponse({
                        success: false,
                        restored: false,
                        isExpired: true,
                        message: `Gói bản quyền của thiết bị này đã hết hạn sử dụng vào ngày ${saved.expiresAt}. Vui lòng mua hoặc gia hạn Key mới.`
                    });
                }

                const diffMs = isLifetime ? 0 : Math.max(0, (saved.expiresMs || nowMs) - nowMs);
                const daysRemaining = isLifetime ? 0 : Math.floor(diffMs / (24 * 3600 * 1000));
                const hoursRemaining = isLifetime ? 0 : Math.floor((diffMs % (24 * 3600 * 1000)) / (3600 * 1000));

                return jsonResponse({
                    success: true,
                    paid: true,
                    restored: true,
                    message: isLifetime
                        ? "Khôi phục bản quyền Trọn Đời thành công từ đám mây!"
                        : `Khôi phục gói ${saved.plan} thành công! Còn ${daysRemaining} ngày sử dụng (đến ${saved.expiresAt}).`,
                    key: saved.key,
                    plan: saved.plan || "LIFETIME",
                    isLifetime: isLifetime,
                    activatedAt: saved.activatedAt || "",
                    expiresAt: saved.expiresAt || (isLifetime ? "Vĩnh viễn" : ""),
                    expiresMs: saved.expiresMs || 0,
                    daysRemaining: daysRemaining,
                    hoursRemaining: hoursRemaining,
                    deviceId: deviceId
                });
            }

            // 3. Fallback qua Google Sheet nếu có
            if (env.GOOGLE_SHEET_URL) {
                try {
                    const sheetRes = await fetch(`${env.GOOGLE_SHEET_URL}?action=restore&deviceId=${encodeURIComponent(deviceId)}`);
                    const sheetData = await sheetRes.json();
                    if (sheetData && sheetData.success) {
                        return jsonResponse(sheetData);
                    }
                } catch (e) {}
            }

            return jsonResponse({
                success: false,
                paid: false,
                restored: false,
                message: "Không tìm thấy thông tin bản quyền đã kích hoạt cho thiết bị này."
            });
        }

        // =========================================================================
        // 4. TRANG CHỦ / KIỂM TRA TRẠNG THÁI
        // =========================================================================
        return jsonResponse({
            service: "Văn Định Skinlab & Sổ Thu Chi & WClone Payment Gateway Worker",
            provider: env.PAYOS_CLIENT_ID ? "payOS" : "Unconfigured",
            supportedApps: ["spamanager", "sothuchi", "wclone"],
            spaManagerTiers: {
                "spa_1m": "24,000 VND (Gói 1 tháng)",
                "spa_3m": "54,000 VND (Gói 3 tháng)",
                "spa_6m": "96,000 VND (Gói 6 tháng)",
                "spa_1y": "150,000 VND (Gói 1 năm)",
                "spa_life": "450,000 VND (Gói Vĩnh Viễn)"
            },
            status: "ready",
            time: new Date().toISOString()
        });
    }
};

/**
 * Sinh Key Văn Định Skinlab ngẫu nhiên bảo mật (tránh O, 0, I, 1)
 * VD: VD-1Y-8A2B-9C4D hoặc VD-LIFE-7K3M-2N5P
 */
function generateSpaKey(tag) {
    const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    let p1 = "", p2 = "";
    for (let i = 0; i < 4; i++) p1 += chars.charAt(Math.floor(Math.random() * chars.length));
    for (let i = 0; i < 4; i++) p2 += chars.charAt(Math.floor(Math.random() * chars.length));
    return `VD-${tag}-${p1}-${p2}`;
}

/**
 * Tính HMAC-SHA256 theo chuẩn Web Crypto API
 */
async function hmacSha256(keyStr, messageStr) {
    const encoder = new TextEncoder();
    const key = await crypto.subtle.importKey(
        "raw",
        encoder.encode(keyStr),
        { name: "HMAC", hash: "SHA-256" },
        false,
        ["sign"]
    );
    const signature = await crypto.subtle.sign(
        "HMAC",
        key,
        encoder.encode(messageStr)
    );
    return Array.from(new Uint8Array(signature))
        .map(b => b.toString(16).padStart(2, "0"))
        .join("");
}

/**
 * Sinh key bản quyền cho Sổ Thu Chi & WClone
 */
async function createLicenseKey(deviceId, tierId, keyTag) {
    const rawInput = `${deviceId.toLowerCase()}:${tierId}:${SECRET_SALT}`;
    const hashBuffer = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(rawInput));
    const hashHex = Array.from(new Uint8Array(hashBuffer)).map(b => b.toString(16).padStart(2, "0")).join("");
    const p1 = hashHex.substring(0, 4).toUpperCase();
    const p2 = hashHex.substring(8, 12).toUpperCase();
    if (tierId === "tier_sothuchi_pro" || keyTag === "STC") {
        return `STC-PRO-${p1}-${p2}`;
    }
    return `WCLONE-${keyTag}-${p1}-${p2}`;
}

function jsonResponse(data, status = 200) {
    return new Response(JSON.stringify(data, null, 2), {
        status,
        headers: {
            "Content-Type": "application/json; charset=utf-8",
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
            "Access-Control-Allow-Headers": "Content-Type, Authorization, x-client-id, x-api-key"
        }
    });
}
