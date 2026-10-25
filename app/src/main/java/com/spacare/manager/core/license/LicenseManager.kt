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

            // Chống tua ngược đồng hồ thiết bị (Clock tampering protection)
            val lastSeenMs = prefs.getLong(KEY_LAST_VERIFIED_TIME_MS, 0L)
            if (now >= lastSeenMs - 3600_000L) {
                prefs.edit().putLong(KEY_LAST_VERIFIED_TIME_MS, maxOf(now, lastSeenMs)).apply()
            }

            // Tự động đối chiếu và tự sửa (Self-healing) nếu expiresMs bị lệch bất thường so với expiresDate ghi nhận
            var effectiveExpiresMs = expiresMs
            if (!isLifetime && expiresDate.isNotBlank() && !expiresDate.contains("vĩnh viễn", ignoreCase = true)) {
                val parsedDateMs = parseAnyDateStringToMs(expiresDate)
                if (parsedDateMs != null && parsedDateMs > 0L) {
                    if (effectiveExpiresMs <= 0L || kotlin.math.abs(effectiveExpiresMs - parsedDateMs) > 12 * 3600 * 1000L) {
                        effectiveExpiresMs = parsedDateMs
                        prefs.edit().putLong(KEY_EXPIRES_MS, parsedDateMs).apply()
                    }
                }
            }

            // Nếu là gói có thời hạn (không phải trọn đời) -> kiểm tra hạn sử dụng
            if (!isLifetime && effectiveExpiresMs > 0L) {
                val effectiveNow = if (now < lastSeenMs) lastSeenMs else now
                val remainingMs = effectiveExpiresMs - effectiveNow

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
                        statusDescription = "Gói $planName đã hết hạn vào ngày $expiresDate. Vui lòng nhập Key mới để tiếp tục sử dụng."
                    )
                } else {
                    // Còn hạn sử dụng: trừ đi chính xác số ngày & giờ đã trôi qua
                    val totalHours = remainingMs / (60 * 60 * 1000L)
                    val days = (totalHours / 24).toInt()
                    val hours = (totalHours % 24).toInt()
                    val timeRemainingText = when {
                        days > 0 && hours > 0 -> "Còn $days ngày $hours giờ"
                        days > 0 -> "Còn $days ngày"
                        else -> "Còn $hours giờ"
                    }

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
                        statusTitle = "$planName ($timeRemainingText)",
                        statusDescription = "Bản quyền hợp lệ trên thiết bị này. $timeRemainingText (Hạn dùng: $expiresDate)."
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
            if (OnlineLicenseService.isSaleSpecialKey(cleanedKey)) {
                com.spacare.manager.core.sale.SalePartnerManager.unlockSalePartner(context, cleanedKey)
            }

            val activatedMs = parseAnyDateStringToMs(result.activatedAt) ?: System.currentTimeMillis()
            val activatedDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(activatedMs))

            val expiresMs = parseExpiresDateToMs(
                expiresStr = result.expiresAt,
                plan = result.plan,
                isLifetime = result.isLifetime,
                serverExpiresMs = result.expiresMs,
                activatedAtStr = activatedDateStr,
                serverDaysRemaining = result.daysRemaining,
                serverHoursRemaining = result.hoursRemaining
            )
            val expiresDateStr = if (result.isLifetime) "Vĩnh viễn" else formatDisplayDate(expiresMs, result.expiresAt)
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
     * Khôi phục bản quyền tự động từ máy chủ theo mã thiết bị (Device ID)
     * Dùng khi người dùng xóa app cài lại hoặc xóa dữ liệu ứng dụng
     */
    suspend fun restoreLicenseOnline(): ActivationResult {
        val result = OnlineLicenseService.restoreLicense(context)

        if (result.success) {
            val activatedMs = parseAnyDateStringToMs(result.activatedAt) ?: System.currentTimeMillis()
            val activatedDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(activatedMs))

            val expiresMs = parseExpiresDateToMs(
                expiresStr = result.expiresAt,
                plan = result.plan,
                isLifetime = result.isLifetime,
                serverExpiresMs = result.expiresMs,
                activatedAtStr = activatedDateStr,
                serverDaysRemaining = result.daysRemaining,
                serverHoursRemaining = result.hoursRemaining
            )
            val expiresDateStr = if (result.isLifetime) "Vĩnh viễn" else formatDisplayDate(expiresMs, result.expiresAt)
            val planTitle = formatPlanTitle(result.plan, result.isLifetime)

            prefs.edit()
                .putBoolean(KEY_IS_ACTIVATED, true)
                .putString(KEY_ACTIVATED_KEY, result.key.ifBlank { "ĐÃ KHÔI PHỤC BẢN QUYỀN" })
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
        if (OnlineLicenseService.isSaleSpecialKey(cleanedKey)) {
            com.spacare.manager.core.sale.SalePartnerManager.unlockSalePartner(context, cleanedKey)
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            prefs.edit()
                .putBoolean(KEY_IS_ACTIVATED, true)
                .putString(KEY_ACTIVATED_KEY, cleanedKey)
                .putString(KEY_ACTIVATED_DATE, sdf.format(Date()))
                .putString(KEY_EXPIRES_DATE, "Vĩnh viễn")
                .putLong(KEY_EXPIRES_MS, 0L)
                .putBoolean(KEY_IS_LIFETIME, true)
                .putString(KEY_PLAN_NAME, "Bản Quyền Trọn Đời (PRO - Đối Tác Sale)")
                .putString(KEY_DEVICE_ID, DeviceIdManager.getDeviceId(context))
                .apply()
            return true
        }

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
            .remove(KEY_LAST_VERIFIED_TIME_MS)
            .apply()
    }

    fun parseExpiresDateToMs(
        expiresStr: String,
        plan: String,
        isLifetime: Boolean,
        serverExpiresMs: Long = 0L,
        activatedAtStr: String = "",
        serverDaysRemaining: Int = 0,
        serverHoursRemaining: Int = 0
    ): Long {
        if (isLifetime || expiresStr.contains("vĩnh viễn", ignoreCase = true)) return 0L

        // 1. Thử parse trực tiếp từ ngày hết hạn expiresStr (chuẩn xác nhất theo ngày hết hạn gốc)
        val parsedExpiresMs = parseAnyDateStringToMs(expiresStr)
        if (parsedExpiresMs != null && parsedExpiresMs > 0L) {
            return parsedExpiresMs
        }

        // 2. Thử lấy serverExpiresMs nếu hợp lệ trong tương lai
        if (serverExpiresMs > System.currentTimeMillis()) {
            return serverExpiresMs
        }

        // 3. Thử tính từ ngày kích hoạt gốc (activatedAtStr + thời hạn gói)
        val parsedActivatedMs = parseAnyDateStringToMs(activatedAtStr)
        if (parsedActivatedMs != null && parsedActivatedMs > 0L) {
            val durationMs = getPlanDurationMs(plan)
            return parsedActivatedMs + durationMs
        }

        // 4. Nếu server có trả về số ngày còn lại (serverDaysRemaining > 0)
        if (serverDaysRemaining > 0) {
            val totalRemainingMs = (serverDaysRemaining * 24L + serverHoursRemaining) * 3600_000L
            return System.currentTimeMillis() + totalRemainingMs
        }

        // 5. Nếu serverExpiresMs > 0 (dù có thể đã quá hạn)
        if (serverExpiresMs > 0L) {
            return serverExpiresMs
        }

        // 6. Dự phòng cuối cùng: tính từ thời điểm hiện tại
        return System.currentTimeMillis() + getPlanDurationMs(plan)
    }

    /**
     * Phân tích linh hoạt mọi định dạng ngày tháng chuỗi (tiếng Việt, ISO-8601, JavaScript Date.toString)
     * Trả về milliseconds epoch chính xác, không phụ thuộc Locale thiết bị
     */
    fun parseAnyDateStringToMs(dateStr: String): Long? {
        if (dateStr.isBlank() || dateStr.contains("vĩnh viễn", ignoreCase = true)) return null
        val clean = dateStr.trim()

        // 1. Regex dd/MM/yyyy với tùy chọn HH:mm[:ss]
        val dmyRegex = Regex("(\\d{1,2})/(\\d{1,2})/(\\d{4})")
        val dmyMatch = dmyRegex.find(clean)
        if (dmyMatch != null) {
            val day = dmyMatch.groupValues[1].toIntOrNull() ?: return null
            val month = dmyMatch.groupValues[2].toIntOrNull() ?: return null
            val year = dmyMatch.groupValues[3].toIntOrNull() ?: return null

            val timeRegex = Regex("(\\d{1,2}):(\\d{1,2})(?::(\\d{1,2}))?")
            val timeMatch = timeRegex.find(clean)
            val hour = timeMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 23
            val minute = timeMatch?.groupValues?.getOrNull(2)?.toIntOrNull() ?: 59
            val second = timeMatch?.groupValues?.getOrNull(3)?.toIntOrNull() ?: 59

            val cal = java.util.Calendar.getInstance()
            cal.set(year, month - 1, day, hour, minute, second)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        // 2. Regex yyyy-MM-dd với tùy chọn HH:mm[:ss]
        val ymdRegex = Regex("(\\d{4})-(\\d{1,2})-(\\d{1,2})")
        val ymdMatch = ymdRegex.find(clean)
        if (ymdMatch != null) {
            val year = ymdMatch.groupValues[1].toIntOrNull() ?: return null
            val month = ymdMatch.groupValues[2].toIntOrNull() ?: return null
            val day = ymdMatch.groupValues[3].toIntOrNull() ?: return null

            val timeRegex = Regex("(\\d{1,2}):(\\d{1,2})(?::(\\d{1,2}))?")
            val timeMatch = timeRegex.find(clean)
            val hour = timeMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 23
            val minute = timeMatch?.groupValues?.getOrNull(2)?.toIntOrNull() ?: 59
            val second = timeMatch?.groupValues?.getOrNull(3)?.toIntOrNull() ?: 59

            val cal = java.util.Calendar.getInstance()
            cal.set(year, month - 1, day, hour, minute, second)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        // 3. Regex định dạng tiếng Anh: Mon Day Year (VD: Nov 19 2026 hoặc Thu Nov 19 2026)
        val months = mapOf(
            "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4,
            "may" to 5, "jun" to 6, "jul" to 7, "aug" to 8,
            "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12
        )
        val engRegex = Regex("([a-zA-Z]{3})\\s+(\\d{1,2})\\s+(\\d{4})")
        val engMatch = engRegex.find(clean)
        if (engMatch != null) {
            val monthStr = engMatch.groupValues[1].lowercase()
            val month = months[monthStr]
            if (month != null) {
                val day = engMatch.groupValues[2].toIntOrNull() ?: return null
                val year = engMatch.groupValues[3].toIntOrNull() ?: return null

                val timeRegex = Regex("(\\d{1,2}):(\\d{1,2})(?::(\\d{1,2}))?")
                val timeMatch = timeRegex.find(clean)
                val hour = timeMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 23
                val minute = timeMatch?.groupValues?.getOrNull(2)?.toIntOrNull() ?: 59
                val second = timeMatch?.groupValues?.getOrNull(3)?.toIntOrNull() ?: 59

                val cal = java.util.Calendar.getInstance()
                cal.set(year, month - 1, day, hour, minute, second)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                return cal.timeInMillis
            }
        }

        // 4. Fallback các pattern chuẩn SimpleDateFormat
        val patterns = arrayOf(
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "EEE MMM dd yyyy HH:mm:ss 'GMT'Z",
            "EEE MMM dd yyyy HH:mm:ss"
        )
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                val date = sdf.parse(clean)
                if (date != null && date.time > 0L) {
                    return date.time
                }
            } catch (ignored: Exception) {}
        }

        return null
    }

    /**
     * Định dạng ngày hết hạn hiển thị sạch sẽ dạng dd/MM/yyyy HH:mm
     */
    fun formatDisplayDate(ms: Long, fallback: String = ""): String {
        if (ms <= 0L) {
            return if (fallback.contains("vĩnh viễn", ignoreCase = true) || fallback.isBlank()) "Vĩnh viễn" else fallback
        }
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(ms))
    }

    private fun getPlanDurationMs(plan: String): Long {
        val days = when (plan.uppercase()) {
            "1_MONTH", "30_DAYS", "SPA_1M", "1M" -> 30L
            "3_MONTHS", "90_DAYS", "SPA_3M", "3M" -> 90L
            "6_MONTHS", "180_DAYS", "SPA_6M", "6M" -> 180L
            "1_YEAR", "365_DAYS", "SPA_1Y", "1Y" -> 365L
            else -> plan.toLongOrNull() ?: 30L
        }
        return days * 24 * 60 * 60 * 1000L
    }

    private fun formatPlanTitle(plan: String, isLifetime: Boolean): String {
        if (isLifetime) return "Bản Quyền Trọn Đời (PRO)"
        return when (plan.uppercase()) {
            "1_MONTH", "30_DAYS", "SPA_1M", "1M" -> "Gói 1 Tháng (PRO)"
            "3_MONTHS", "90_DAYS", "SPA_3M", "3M" -> "Gói 3 Tháng (PRO)"
            "6_MONTHS", "180_DAYS", "SPA_6M", "6M" -> "Gói 6 Tháng (PRO)"
            "1_YEAR", "365_DAYS", "SPA_1Y", "1Y" -> "Gói 1 Năm (PRO)"
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
        private const val KEY_LAST_VERIFIED_TIME_MS = "key_last_verified_time_ms"

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
