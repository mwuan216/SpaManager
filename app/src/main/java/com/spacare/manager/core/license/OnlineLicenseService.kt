package com.spacare.manager.core.license

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class ActivationResult(
    val success: Boolean,
    val message: String,
    val key: String = "",
    val plan: String = "LIFETIME",
    val isLifetime: Boolean = true,
    val activatedAt: String = "",
    val expiresAt: String = "",
    val expiresMs: Long = 0L,
    val daysRemaining: Int = 0,
    val hoursRemaining: Int = 0,
    val deviceId: String = "",
    val isExpired: Boolean = false,
    val isRestored: Boolean = false
)

object OnlineLicenseService {

    // URL mặc định của hệ thống Google Apps Script Web App
    // Quản trị viên có thể đổi URL này trong mục Cài đặt -> Bản quyền
    const val DEFAULT_SERVER_URL = "https://script.google.com/macros/s/AKfycbycE-sample-vandinh-license/exec"

    // Gateway Cloudflare Worker mặc định hỗ trợ payOS và lưu trữ key trực tiếp
    const val DEFAULT_CLOUDFLARE_GATEWAY_URL = "https://wclon-payment.mwuan216.workers.dev"

    private const val PREF_SERVER = "spacare_license_server_pref"
    private const val KEY_CUSTOM_SERVER_URL = "key_custom_server_url"

    /**
     * Lấy URL máy chủ xác thực hiện tại (Ưu tiên URL tùy chỉnh do người dùng nhập trong Cài Đặt)
     */
    fun getServerUrl(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREF_SERVER, Context.MODE_PRIVATE)
        val customUrl = prefs.getString(KEY_CUSTOM_SERVER_URL, null)
        return if (!customUrl.isNullOrBlank()) customUrl.trim() else ""
    }

    /**
     * Lưu URL máy chủ xác thực mới
     */
    fun saveServerUrl(context: Context, url: String) {
        val prefs = context.applicationContext.getSharedPreferences(PREF_SERVER, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_SERVER_URL, url.trim()).apply()
    }

    /**
     * Kích hoạt key online qua Google Apps Script Web App hoặc Cloudflare Worker
     */
    suspend fun activateKey(
        context: Context,
        rawKey: String
    ): ActivationResult = withContext(Dispatchers.IO) {
        val cleanedKey = rawKey.trim().uppercase()
        val deviceId = DeviceIdManager.getDeviceId(context)
        val deviceName = DeviceIdManager.getDeviceName()

        // 1. Hỗ trợ Key Master dự phòng khẩn cấp cục bộ cho quản trị viên
        if (isMasterKey(cleanedKey)) {
            return@withContext ActivationResult(
                success = true,
                message = "Đã kích hoạt bản quyền Vĩnh Viễn bằng Key Master Quản Trị!",
                plan = "LIFETIME",
                isLifetime = true,
                activatedAt = "Ngay bây giờ",
                expiresAt = "Vĩnh viễn",
                deviceId = deviceId
            )
        }

        // 2. Thử kích hoạt qua Google Apps Script nếu đã cấu hình URL riêng
        val serverUrl = getServerUrl(context)
        if (serverUrl.isNotBlank()) {
            try {
                val builtUrl = Uri.parse(serverUrl).buildUpon()
                    .appendQueryParameter("action", "activate")
                    .appendQueryParameter("key", cleanedKey)
                    .appendQueryParameter("deviceId", deviceId)
                    .appendQueryParameter("deviceName", deviceName)
                    .build()
                    .toString()

                val responseJsonStr = executeHttpRequestWithRedirect(builtUrl)
                val res = parseActivationResponse(responseJsonStr)
                if (res.success || res.isExpired) {
                    return@withContext res
                }
            } catch (e: Exception) {
                // Tiếp tục thử Cloudflare Worker
            }
        }

        // 3. Kích hoạt qua Cloudflare Worker mặc định
        try {
            val workerUrl = Uri.parse("$DEFAULT_CLOUDFLARE_GATEWAY_URL/activate-license").buildUpon()
                .appendQueryParameter("key", cleanedKey)
                .appendQueryParameter("deviceId", deviceId)
                .appendQueryParameter("deviceName", deviceName)
                .build()
                .toString()

            val responseJsonStr = executeHttpRequestWithRedirect(workerUrl)
            parseActivationResponse(responseJsonStr)
        } catch (e: Exception) {
            ActivationResult(
                success = false,
                message = "Không thể kết nối máy chủ xác thực: ${e.localizedMessage ?: "Vui lòng kiểm tra kết nối mạng Internet."}"
            )
        }
    }

    /**
     * Kiểm tra trạng thái bản quyền hiện tại trên server
     */
    suspend fun verifyCurrentKey(
        context: Context,
        key: String
    ): ActivationResult = withContext(Dispatchers.IO) {
        if (isMasterKey(key)) {
            return@withContext ActivationResult(
                success = true,
                message = "Master Key hợp lệ",
                isLifetime = true
            )
        }

        val deviceId = DeviceIdManager.getDeviceId(context)
        val deviceName = DeviceIdManager.getDeviceName()

        val serverUrl = getServerUrl(context)
        if (serverUrl.isNotBlank()) {
            try {
                val builtUrl = Uri.parse(serverUrl).buildUpon()
                    .appendQueryParameter("action", "verify")
                    .appendQueryParameter("key", key.trim().uppercase())
                    .appendQueryParameter("deviceId", deviceId)
                    .appendQueryParameter("deviceName", deviceName)
                    .build()
                    .toString()

                val responseJsonStr = executeHttpRequestWithRedirect(builtUrl)
                return@withContext parseActivationResponse(responseJsonStr)
            } catch (e: Exception) {}
        }

        try {
            val workerUrl = Uri.parse("$DEFAULT_CLOUDFLARE_GATEWAY_URL/verify-license").buildUpon()
                .appendQueryParameter("key", key.trim().uppercase())
                .appendQueryParameter("deviceId", deviceId)
                .appendQueryParameter("deviceName", deviceName)
                .build()
                .toString()

            val responseJsonStr = executeHttpRequestWithRedirect(workerUrl)
            parseActivationResponse(responseJsonStr)
        } catch (e: Exception) {
            // Lỗi mạng khi verify ngầm -> không chặn người dùng nếu đã active trước đó
            ActivationResult(success = true, message = "Không có kết nối mạng để verify ngầm")
        }
    }

    /**
     * Khôi phục bản quyền tự động cho thiết bị này khi cài lại app hoặc xóa dữ liệu
     * Giữ nguyên vẹn gói đã mua và số ngày còn lại gốc
     */
    suspend fun restoreLicense(context: Context): ActivationResult = withContext(Dispatchers.IO) {
        val deviceId = DeviceIdManager.getDeviceId(context)

        // 1. Thử khôi phục từ Google Apps Script Web App (nếu có cấu hình)
        val serverUrl = getServerUrl(context)
        if (serverUrl.isNotBlank()) {
            try {
                val restoreUrl = Uri.parse(serverUrl).buildUpon()
                    .appendQueryParameter("action", "restore")
                    .appendQueryParameter("deviceId", deviceId)
                    .build()
                    .toString()

                val responseJsonStr = executeHttpRequestWithRedirect(restoreUrl)
                val res = parseActivationResponse(responseJsonStr)
                if (res.success || res.isExpired) {
                    return@withContext res
                }
            } catch (e: Exception) {
                // Tiếp tục thử Cloudflare Worker
            }
        }

        // 2. Thử khôi phục từ Cloudflare Worker payOS
        try {
            val workerUrl = "$DEFAULT_CLOUDFLARE_GATEWAY_URL/restore-license?deviceId=$deviceId"
            val responseJsonStr = executeHttpRequestWithRedirect(workerUrl)
            val res = parseActivationResponse(responseJsonStr)
            if (res.success || res.isExpired) {
                return@withContext res
            }
        } catch (e: Exception) {
            // Không khôi phục được từ worker
        }

        ActivationResult(
            success = false,
            message = "Không tìm thấy thông tin bản quyền đã kích hoạt cho thiết bị này. Vui lòng nhập mã Key để kích hoạt."
        )
    }

    /**
     * Kiểm tra kết nối tới Server Google Apps Script (Ping)
     */
    suspend fun testServerConnection(url: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val pingUrl = Uri.parse(url).buildUpon()
                .appendQueryParameter("action", "ping")
                .build()
                .toString()

            val responseJsonStr = executeHttpRequestWithRedirect(pingUrl)
            val json = JSONObject(responseJsonStr)
            val isSuccess = json.optBoolean("success", false)
            val msg = json.optString("message", "Kết nối thành công")
            Pair(isSuccess, msg)
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Không thể kết nối tới URL")
        }
    }

    /**
     * Thực hiện HTTP GET xử lý redirect (Google Apps Script luôn trả về 302 redirect)
     */
    private fun executeHttpRequestWithRedirect(urlString: String): String {
        var currentUrl = URL(urlString)
        var redirectCount = 0
        val maxRedirects = 6

        while (redirectCount < maxRedirects) {
            val connection = (currentUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "SpaCare-Android/1.0")
                setRequestProperty("Accept", "application/json")
            }

            val statusCode = connection.responseCode
            if (statusCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                statusCode == HttpURLConnection.HTTP_MOVED_PERM ||
                statusCode == 307 || statusCode == 308
            ) {
                val redirectLocation = connection.getHeaderField("Location")
                    ?: throw IOException("Máy chủ yêu cầu chuyển hướng nhưng không có Location")
                currentUrl = URL(redirectLocation)
                redirectCount++
                connection.disconnect()
                continue
            }

            val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val content = stream?.bufferedReader()?.use { it.readText() } ?: ""
            connection.disconnect()

            if (content.isBlank() && statusCode !in 200..299) {
                throw IOException("Lỗi máy chủ phản hồi mã HTTP: $statusCode")
            }
            return content
        }

        throw IOException("Vượt quá số lần chuyển hướng cho phép (Redirect Loop)")
    }

    /**
     * Phân tích JSON trả về từ Google Apps Script
     */
    private fun parseActivationResponse(jsonStr: String): ActivationResult {
        return try {
            val json = JSONObject(jsonStr)
            val success = json.optBoolean("success", false)
            val isRestored = json.optBoolean("restored", false)
            val isExpired = json.optBoolean("isExpired", false)
            val message = json.optString("message", if (success) "Thao tác thành công" else "Kích hoạt thất bại")
            val key = json.optString("key", "")
            val plan = json.optString("plan", "LIFETIME")
            val isLifetime = json.optBoolean(
                "isLifetime",
                plan.equals("LIFETIME", ignoreCase = true) || plan.equals("SPA_LIFE", ignoreCase = true) || plan.equals("PRO", ignoreCase = true)
            )
            val activatedAt = json.optString("activatedAt", "")
            val expiresAt = json.optString("expiresAt", if (isLifetime) "Vĩnh viễn" else "")
            val expiresMs = json.optLong("expiresMs", 0L)
            val daysRemaining = json.optInt("daysRemaining", 0)
            val hoursRemaining = json.optInt("hoursRemaining", 0)
            val deviceId = json.optString("deviceId", "")

            ActivationResult(
                success = success,
                message = message,
                key = key,
                plan = plan,
                isLifetime = isLifetime,
                activatedAt = activatedAt,
                expiresAt = expiresAt,
                expiresMs = expiresMs,
                daysRemaining = daysRemaining,
                hoursRemaining = hoursRemaining,
                deviceId = deviceId,
                isExpired = isExpired,
                isRestored = isRestored
            )
        } catch (e: Exception) {
            ActivationResult(
                success = false,
                message = "Dữ liệu máy chủ không đúng định dạng: ${jsonStr.take(100)}"
            )
        }
    }

    /**
     * Kiểm tra master key quản trị viên
     */
    fun isMasterKey(key: String): Boolean {
        val trimmed = key.trim().uppercase()
        return trimmed == "SPACARE_ACTIVE_216" ||
                trimmed == "VANDINH_ACTIVE_216" ||
                trimmed == "VANDINH_MASTER_2026"
    }
}
