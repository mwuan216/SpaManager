package com.spacare.manager.core.license

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LicenseStatus(
    val isActivated: Boolean,
    val isTrialActive: Boolean,
    val isExpired: Boolean,
    val daysRemaining: Int,
    val hoursRemaining: Int,
    val activatedKey: String = "",
    val activatedDate: String = "",
    val expiresDate: String = "",
    val isLifetime: Boolean = true,
    val planName: String = "Bản Quyền Trọn Đời (PRO)",
    val deviceId: String = "",
    val firstLaunchDate: String = "",
    val statusTitle: String = "",
    val statusDescription: String = ""
)

class LicenseManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    init {
        // Khởi tạo thời điểm mở app lần đầu nếu chưa có
        if (!prefs.contains(KEY_FIRST_LAUNCH_MS)) {
            prefs.edit().putLong(KEY_FIRST_LAUNCH_MS, System.currentTimeMillis()).apply()
        }
    }

    /**
     * Lấy thông tin trạng thái bản quyền hiện tại
     */
    fun getLicenseStatus(): LicenseStatus {
        val isActivated = prefs.getBoolean(KEY_IS_ACTIVATED, false)
        val activatedKey = prefs.getString(KEY_ACTIVATED_KEY, "") ?: ""
        val activatedDate = prefs.getString(KEY_ACTIVATED_DATE, "") ?: ""
        val expiresDate = prefs.getString(KEY_EXPIRES_DATE, "") ?: ""
        val expiresMs = prefs.getLong(KEY_EXPIRES_MS, 0L)
        val isLifetime = prefs.getBoolean(KEY_IS_LIFETIME, true)
        val planName = prefs.getString(KEY_PLAN_NAME, "Bản Quyền Trọn Đời (PRO)") ?: "Bản Quyền Trọn Đời (PRO)"
        val firstLaunchMs = prefs.getLong(KEY_FIRST_LAUNCH_MS, System.currentTimeMillis())
        val deviceId = DeviceIdManager.getDeviceId(context)

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val firstLaunchDate = sdf.format(Date(firstLaunchMs))

        // 1. Trường hợp ĐÃ KÍCH HOẠT KEY
        if (isActivated) {
            val now = System.currentTimeMillis()

            // Nếu là gói có thời hạn (không phải trọn đời) -> kiểm tra hạn sử dụng
            if (!isLifetime && expiresMs > 0) {
                val remainingMs = expiresMs - now
                if (remainingMs <= 0) {
                    // Đã hết hạn gói
                    return LicenseStatus(
                        isActivated = false,
                        isTrialActive = false,
                        isExpired = true,
                        daysRemaining = 0,
                        hoursRemaining = 0,
                        activatedKey = activatedKey,
                        activatedDate = activatedDate,
                        expiresDate = expiresDate,
                        isLifetime = false,
                        planName = planName,
                        deviceId = deviceId,
                        firstLaunchDate = firstLaunchDate,
                        statusTitle = "Bản Quyền Đã Hết Hạn",
                        statusDescription = "Gói $planName đã hết hạn vào $expiresDate. Vui lòng nhập Key kích hoạt mới để tiếp tục sử dụng."
                    )
                } else {
                    // Còn hạn
                    val totalHours = remainingMs / (60 * 60 * 1000L)
                    val days = (totalHours / 24).toInt()
                    val hours = (totalHours % 24).toInt()

                    return LicenseStatus(
                        isActivated = true,
                        isTrialActive = false,
                        isExpired = false,
                        daysRemaining = days,
                        hoursRemaining = hours,
                        activatedKey = activatedKey,
                        activatedDate = activatedDate,
                        expiresDate = expiresDate,
                        isLifetime = false,
                        planName = planName,
                        deviceId = deviceId,
                        firstLaunchDate = firstLaunchDate,
                        statusTitle = "$planName (Còn $days ngày)",
                        statusDescription = "Bản quyền đã kích hoạt trên thiết bị này. Hạn sử dụng đến: $expiresDate."
                    )
                }
            }

            // Gói Trọn Đời (LIFETIME)
            return LicenseStatus(
                isActivated = true,
                isTrialActive = false,
                isExpired = false,
                daysRemaining = 0,
                hoursRemaining = 0,
                activatedKey = activatedKey,
                activatedDate = activatedDate,
                expiresDate = "Vĩnh viễn",
                isLifetime = true,
                planName = "Bản Quyền Trọn Đời (PRO)",
                deviceId = deviceId,
                firstLaunchDate = firstLaunchDate,
                statusTitle = "Bản Quyền Trọn Đời (PRO)",
                statusDescription = "Đã kích hoạt bản quyền vĩnh viễn với đầy đủ tính năng Văn Định Skinlab."
            )
        }

        // 2. Trường hợp CHƯA KÍCH HOẠT -> Kiểm tra thời hạn 15 ngày dùng thử
        val now = System.currentTimeMillis()
        val elapsedMs = now - firstLaunchMs
        val remainingMs = TRIAL_DURATION_MS - elapsedMs

        return if (remainingMs > 0) {
            val totalHours = remainingMs / (60 * 60 * 1000L)
            val days = (totalHours / 24).toInt()
            val hours = (totalHours % 24).toInt()

            LicenseStatus(
                isActivated = false,
                isTrialActive = true,
                isExpired = false,
                daysRemaining = days,
                hoursRemaining = hours,
                activatedKey = "",
                activatedDate = "",
                expiresDate = "",
                isLifetime = false,
                planName = "Gói Dùng Thử 15 Ngày",
                deviceId = deviceId,
                firstLaunchDate = firstLaunchDate,
                statusTitle = "Dùng Thử Miễn Phí (Còn $days ngày $hours giờ)",
                statusDescription = "Bạn đang trong thời gian 15 ngày dùng thử miễn phí. Sau khi hết hạn, vui lòng kích hoạt bản quyền để tiếp tục."
            )
        } else {
            LicenseStatus(
                isActivated = false,
                isTrialActive = false,
                isExpired = true,
                daysRemaining = 0,
                hoursRemaining = 0,
                activatedKey = "",
                activatedDate = "",
                expiresDate = "",
                isLifetime = false,
                planName = "Hết Hạn Dùng Thử",
                deviceId = deviceId,
                firstLaunchDate = firstLaunchDate,
                statusTitle = "Đã Hết Hạn Dùng Thử",
                statusDescription = "Gói dùng thử 15 ngày đã kết thúc. Vui lòng nhập Key kích hoạt bản quyền để tiếp tục sử dụng ứng dụng."
            )
        }
    }

    /**
     * Kích hoạt bản quyền qua API trực tuyến (Online) và khóa vào thiết bị này
     */
    suspend fun activateLicenseOnline(rawKey: String): ActivationResult {
        val cleanedKey = rawKey.trim().uppercase()
        val result = OnlineLicenseService.activateKey(context, cleanedKey)

        if (result.success) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val activatedDateStr = result.activatedAt.ifBlank { sdf.format(Date()) }
            val expiresDateStr = result.expiresAt.ifBlank { if (result.isLifetime) "Vĩnh viễn" else "" }

            val expiresMs = parseExpiresDateToMs(expiresDateStr, result.plan, result.isLifetime)
            val planTitle = formatPlanTitle(result.plan, result.isLifetime)

            prefs.edit()
                .putBoolean(KEY_IS_ACTIVATED, true)
                .putString(KEY_ACTIVATED_KEY, cleanedKey)
                .putString(KEY_ACTIVATED_DATE, activatedDateStr)
                .putString(KEY_EXPIRES_DATE, expiresDateStr)
                .putLong(KEY_EXPIRES_MS, expiresMs)
                .putBoolean(KEY_IS_LIFETIME, result.isLifetime)
                .putString(KEY_PLAN_NAME, planTitle)
                .putString(KEY_DEVICE_ID, DeviceIdManager.getDeviceId(context))
                .apply()
        }

        return result
    }

    /**
     * Xác thực và kích hoạt key bản quyền (hỗ trợ master key offline)
     */
    fun activateLicense(rawKey: String): Boolean {
        val cleanedKey = rawKey.trim().uppercase()
        if (OnlineLicenseService.isMasterKey(cleanedKey)) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            prefs.edit()
                .putBoolean(KEY_IS_ACTIVATED, true)
                .putString(KEY_ACTIVATED_KEY, cleanedKey)
                .putString(KEY_ACTIVATED_DATE, sdf.format(Date()))
                .putString(KEY_EXPIRES_DATE, "Vĩnh viễn")
                .putLong(KEY_EXPIRES_MS, 0L)
                .putBoolean(KEY_IS_LIFETIME, true)
                .putString(KEY_PLAN_NAME, "Bản Quyền Trọn Đời (PRO)")
                .putString(KEY_DEVICE_ID, DeviceIdManager.getDeviceId(context))
                .apply()
            return true
        }
        return false
    }

    /**
     * Hủy kích hoạt bản quyền trên máy
     */
    fun deactivateLicense() {
        prefs.edit()
            .putBoolean(KEY_IS_ACTIVATED, false)
            .remove(KEY_ACTIVATED_KEY)
            .remove(KEY_ACTIVATED_DATE)
            .remove(KEY_EXPIRES_DATE)
            .remove(KEY_EXPIRES_MS)
            .remove(KEY_IS_LIFETIME)
            .remove(KEY_PLAN_NAME)
            .remove(KEY_DEVICE_ID)
            .apply()
    }

    private fun parseExpiresDateToMs(expiresStr: String, plan: String, isLifetime: Boolean): Long {
        if (isLifetime || expiresStr.contains("vĩnh viễn", ignoreCase = true)) return 0L
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return try {
            val date = sdf.parse(expiresStr)
            date?.time ?: calculateFallbackExpiresMs(plan)
        } catch (e: Exception) {
            calculateFallbackExpiresMs(plan)
        }
    }

    private fun calculateFallbackExpiresMs(plan: String): Long {
        val now = System.currentTimeMillis()
        val days = when (plan.uppercase()) {
            "1_MONTH", "30_DAYS" -> 30L
            "3_MONTHS", "90_DAYS" -> 90L
            "6_MONTHS", "180_DAYS" -> 180L
            "1_YEAR", "365_DAYS" -> 365L
            else -> plan.toLongOrNull() ?: 30L
        }
        return now + (days * 24 * 60 * 60 * 1000L)
    }

    private fun formatPlanTitle(plan: String, isLifetime: Boolean): String {
        if (isLifetime) return "Bản Quyền Trọn Đời (PRO)"
        return when (plan.uppercase()) {
            "1_MONTH", "30_DAYS" -> "Gói 1 Tháng (PRO)"
            "3_MONTHS", "90_DAYS" -> "Gói 3 Tháng (PRO)"
            "6_MONTHS", "180_DAYS" -> "Gói 6 Tháng (PRO)"
            "1_YEAR", "365_DAYS" -> "Gói 1 Năm (PRO)"
            else -> "Gói Bản Quyền $plan"
        }
    }

    companion object {
        private const val PREF_NAME = "spacare_license_prefs"
        private const val KEY_FIRST_LAUNCH_MS = "key_first_launch_ms"
        private const val KEY_IS_ACTIVATED = "key_is_activated"
        private const val KEY_ACTIVATED_KEY = "key_activated_key"
        private const val KEY_ACTIVATED_DATE = "key_activated_date"
        private const val KEY_EXPIRES_DATE = "key_expires_date"
        private const val KEY_EXPIRES_MS = "key_expires_ms"
        private const val KEY_IS_LIFETIME = "key_is_lifetime"
        private const val KEY_PLAN_NAME = "key_plan_name"
        private const val KEY_DEVICE_ID = "key_device_id"

        // 15 ngày dùng thử = 15 * 24 * 60 * 60 * 1000 = 1_296_000_000 ms
        const val TRIAL_DURATION_MS = 15 * 24 * 60 * 60 * 1000L

        @Volatile
        private var instance: LicenseManager? = null

        fun getInstance(context: Context): LicenseManager {
            return instance ?: synchronized(this) {
                instance ?: LicenseManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
