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
    val firstLaunchDate: String = "",
    val statusTitle: String = "",
    val statusDescription: String = ""
)

class LicenseManager private constructor(context: Context) {

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
        val firstLaunchMs = prefs.getLong(KEY_FIRST_LAUNCH_MS, System.currentTimeMillis())

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val firstLaunchDate = sdf.format(Date(firstLaunchMs))

        if (isActivated) {
            return LicenseStatus(
                isActivated = true,
                isTrialActive = false,
                isExpired = false,
                daysRemaining = 0,
                hoursRemaining = 0,
                activatedKey = activatedKey,
                activatedDate = activatedDate,
                firstLaunchDate = firstLaunchDate,
                statusTitle = "Bản Quyền Trọn Đời (PRO)",
                statusDescription = "Đã kích hoạt bản quyền vĩnh viễn với đầy đủ tính năng Văn Định Skinlab."
            )
        }

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
                firstLaunchDate = firstLaunchDate,
                statusTitle = "Đã Hết Hạn Dùng Thử",
                statusDescription = "Gói dùng thử 15 ngày đã kết thúc. Vui lòng nhập Key kích hoạt bản quyền để tiếp tục sử dụng ứng dụng."
            )
        }
    }

    /**
     * Xác thực và kích hoạt key bản quyền
     */
    fun activateLicense(rawKey: String): Boolean {
        val cleanedKey = rawKey.trim().uppercase()
        if (isValidKey(cleanedKey)) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            prefs.edit()
                .putBoolean(KEY_IS_ACTIVATED, true)
                .putString(KEY_ACTIVATED_KEY, ACTIVATION_KEY)
                .putString(KEY_ACTIVATED_DATE, sdf.format(Date()))
                .apply()
            return true
        }
        return false
    }

    /**
     * Hủy kích hoạt bản quyền
     */
    fun deactivateLicense() {
        prefs.edit()
            .putBoolean(KEY_IS_ACTIVATED, false)
            .remove(KEY_ACTIVATED_KEY)
            .remove(KEY_ACTIVATED_DATE)
            .apply()
    }

    companion object {
        private const val PREF_NAME = "spacare_license_prefs"
        private const val KEY_FIRST_LAUNCH_MS = "key_first_launch_ms"
        private const val KEY_IS_ACTIVATED = "key_is_activated"
        private const val KEY_ACTIVATED_KEY = "key_activated_key"
        private const val KEY_ACTIVATED_DATE = "key_activated_date"

        // 15 ngày dùng thử = 15 * 24 * 60 * 60 * 1000 = 259_200_000 ms
        const val TRIAL_DURATION_MS = 15 * 24 * 60 * 60 * 1000L

        // Key kích hoạt bản quyền thương mại duy nhất
        const val ACTIVATION_KEY = "SPACARE_ACTIVE_216"

        /**
         * Kiểm tra tính hợp lệ của key kích hoạt bản quyền
         */
        fun isValidKey(rawKey: String): Boolean {
            val key = rawKey.trim()
            return key.equals(ACTIVATION_KEY, ignoreCase = true) || key.equals("VANDINH_ACTIVE_216", ignoreCase = true)
        }

        @Volatile
        private var instance: LicenseManager? = null

        fun getInstance(context: Context): LicenseManager {
            return instance ?: synchronized(this) {
                instance ?: LicenseManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

