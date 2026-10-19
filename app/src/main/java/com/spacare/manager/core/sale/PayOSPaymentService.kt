package com.spacare.manager.core.sale

import android.content.Context
import android.net.Uri
import com.spacare.manager.core.license.DeviceIdManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class SaleTier(
    val id: String,
    val title: String,
    val durationLabel: String,
    val retailPrice: Long,      // Giá bán cho khách lẻ (100%)
    val wholesalePrice: Long,   // Giá Sale thanh toán nhận key (30%)
    val profit: Long,           // Lợi nhuận Sale giữ lại (70%)
    val tag: String,
    val plan: String
)

data class CreatePaymentResponse(
    val success: Boolean,
    val orderCode: Long = 0L,
    val amount: Long = 0L,
    val retailPrice: Long = 0L,
    val profit: Long = 0L,
    val description: String = "",
    val qrCode: String = "",
    val checkoutUrl: String = "",
    val accountNumber: String = "8500121062001",
    val accountName: String = "BUI MINH QUAN",
    val bin: String = "970422",
    val error: String? = null
)

data class CheckPaymentResponse(
    val success: Boolean,
    val paid: Boolean = false,
    val key: String = "",
    val plan: String = "",
    val orderCode: Long = 0L,
    val amount: Long = 0L,
    val status: String = "PENDING",
    val message: String = ""
)

object PayOSPaymentService {

    // Cloudflare Worker URL của tác giả tích hợp payOS
    const val PAYMENT_WORKER_URL = "https://wclon-payment.mwuan216.workers.dev"

    // Danh sách 5 gói bản quyền chính thức dành cho Sale
    val SALE_TIERS = listOf(
        SaleTier(
            id = "spa_1m",
            title = "Gói 1 Tháng",
            durationLabel = "30 ngày sử dụng",
            retailPrice = 80000L,
            wholesalePrice = 24000L,  // 30%
            profit = 56000L,          // 70%
            tag = "1M",
            plan = "1_MONTH"
        ),
        SaleTier(
            id = "spa_3m",
            title = "Gói 3 Tháng",
            durationLabel = "90 ngày sử dụng",
            retailPrice = 180000L,
            wholesalePrice = 54000L,  // 30%
            profit = 126000L,         // 70%
            tag = "3M",
            plan = "3_MONTHS"
        ),
        SaleTier(
            id = "spa_6m",
            title = "Gói 6 Tháng",
            durationLabel = "180 ngày sử dụng",
            retailPrice = 320000L,
            wholesalePrice = 96000L,  // 30%
            profit = 224000L,         // 70%
            tag = "6M",
            plan = "6_MONTHS"
        ),
        SaleTier(
            id = "spa_1y",
            title = "Gói 1 Năm",
            durationLabel = "365 ngày sử dụng",
            retailPrice = 500000L,
            wholesalePrice = 150000L, // 30%
            profit = 350000L,         // 70%
            tag = "1Y",
            plan = "1_YEAR"
        ),
        SaleTier(
            id = "spa_life",
            title = "Gói Vĩnh Viễn",
            durationLabel = "Trọn đời không giới hạn",
            retailPrice = 1500000L,
            wholesalePrice = 450000L, // 30%
            profit = 1050000L,        // 70%
            tag = "LIFE",
            plan = "LIFETIME"
        )
    )

    /**
     * Tạo yêu cầu thanh toán mua key cho Sale qua payOS
     */
    suspend fun createPayment(context: Context, tier: SaleTier): CreatePaymentResponse = withContext(Dispatchers.IO) {
        val deviceId = DeviceIdManager.getDeviceId(context)
        try {
            val urlStr = Uri.parse(PAYMENT_WORKER_URL).buildUpon()
                .appendPath("create-payment")
                .appendQueryParameter("app", "spamanager")
                .appendQueryParameter("tier", tier.id)
                .appendQueryParameter("deviceId", deviceId)
                .build()
                .toString()

            val jsonStr = executeHttpGet(urlStr)
            val json = JSONObject(jsonStr)

            val success = json.optBoolean("success", false)
            if (success) {
                CreatePaymentResponse(
                    success = true,
                    orderCode = json.optLong("orderCode", 0L),
                    amount = json.optLong("amount", tier.wholesalePrice),
                    retailPrice = json.optLong("retailPrice", tier.retailPrice),
                    profit = json.optLong("profit", tier.profit),
                    description = json.optString("description", "SPA ${tier.tag} $deviceId"),
                    qrCode = json.optString("qrCode", ""),
                    checkoutUrl = json.optString("checkoutUrl", ""),
                    accountNumber = json.optString("accountNumber", "8500121062001"),
                    accountName = json.optString("accountName", "BUI MINH QUAN"),
                    bin = json.optString("bin", "970422")
                )
            } else {
                CreatePaymentResponse(
                    success = false,
                    error = json.optString("error", "Không thể tạo đơn thanh toán payOS.")
                )
            }
        } catch (e: Exception) {
            CreatePaymentResponse(
                success = false,
                error = "Lỗi kết nối cổng thanh toán: ${e.localizedMessage ?: "Vui lòng kiểm tra Internet"}"
            )
        }
    }

    /**
     * Kiểm tra trạng thái thanh toán & lấy key tự động
     */
    suspend fun checkPayment(context: Context, tierId: String, orderCode: Long): CheckPaymentResponse = withContext(Dispatchers.IO) {
        val deviceId = DeviceIdManager.getDeviceId(context)
        try {
            val urlStr = Uri.parse(PAYMENT_WORKER_URL).buildUpon()
                .appendPath("check-payment")
                .appendQueryParameter("app", "spamanager")
                .appendQueryParameter("tier", tierId)
                .appendQueryParameter("orderCode", orderCode.toString())
                .appendQueryParameter("deviceId", deviceId)
                .build()
                .toString()

            val jsonStr = executeHttpGet(urlStr)
            val json = JSONObject(jsonStr)

            val success = json.optBoolean("success", false)
            val paid = json.optBoolean("paid", false)
            val key = json.optString("key", "")
            val plan = json.optString("plan", "LIFETIME")
            val status = json.optString("status", if (paid) "PAID" else "PENDING")
            val message = json.optString("message", "")
            val amount = json.optLong("amount", 0L)

            CheckPaymentResponse(
                success = success,
                paid = paid,
                key = key,
                plan = plan,
                orderCode = orderCode,
                amount = amount,
                status = status,
                message = message
            )
        } catch (e: Exception) {
            CheckPaymentResponse(
                success = false,
                paid = false,
                status = "ERROR",
                message = e.localizedMessage ?: "Lỗi kiểm tra trạng thái"
            )
        }
    }

    private fun executeHttpGet(urlString: String): String {
        val url = URL(urlString)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12000
            readTimeout = 12000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "SpaManager-Android/1.0")
        }
        val statusCode = conn.responseCode
        val stream = if (statusCode in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
        conn.disconnect()
        if (text.isBlank() && statusCode !in 200..299) {
            throw IOException("Máy chủ trả về mã HTTP $statusCode")
        }
        return text
    }
}
