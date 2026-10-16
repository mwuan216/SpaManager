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
                statusDescription = "Đã kích hoạt bản quyền vĩnh viễn với đầy đủ tính năng SpaCare Manager."
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
                statusDescription = "Bạn đang trong thời gian 3 ngày dùng thử miễn phí. Sau khi hết hạn, vui lòng kích hoạt bản quyền để tiếp tục."
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
                statusDescription = "Gói dùng thử 3 ngày đã kết thúc. Vui lòng nhập Key kích hoạt bản quyền để tiếp tục sử dụng ứng dụng."
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
                .putString(KEY_ACTIVATED_KEY, cleanedKey)
                .putString(KEY_ACTIVATED_DATE, sdf.format(Date()))
                .apply()
            return true
        }
        return false
    }

    /**
     * Hủy kích hoạt để quay lại trạng thái dùng thử (phục vụ test)
     */
    fun deactivateLicense() {
        prefs.edit()
            .putBoolean(KEY_IS_ACTIVATED, false)
            .remove(KEY_ACTIVATED_KEY)
            .remove(KEY_ACTIVATED_DATE)
            .apply()
    }

    /**
     * Đặt trạng thái dùng thử về hết hạn (phục vụ test màn hình khóa kích hoạt)
     */
    fun simulateTrialExpired() {
        prefs.edit()
            .putBoolean(KEY_IS_ACTIVATED, false)
            .remove(KEY_ACTIVATED_KEY)
            .remove(KEY_ACTIVATED_DATE)
            .putLong(KEY_FIRST_LAUNCH_MS, System.currentTimeMillis() - (TRIAL_DURATION_MS + 1000L))
            .apply()
    }

    /**
     * Đặt lại thời gian dùng thử đủ 3 ngày mới (phục vụ test)
     */
    fun resetTrial() {
        prefs.edit()
            .putBoolean(KEY_IS_ACTIVATED, false)
            .remove(KEY_ACTIVATED_KEY)
            .remove(KEY_ACTIVATED_DATE)
            .putLong(KEY_FIRST_LAUNCH_MS, System.currentTimeMillis())
            .apply()
    }

    companion object {
        private const val PREF_NAME = "spacare_license_prefs"
        private const val KEY_FIRST_LAUNCH_MS = "key_first_launch_ms"
        private const val KEY_IS_ACTIVATED = "key_is_activated"
        private const val KEY_ACTIVATED_KEY = "key_activated_key"
        private const val KEY_ACTIVATED_DATE = "key_activated_date"

        // 3 ngày = 3 * 24 * 60 * 60 * 1000 = 259_200_000 ms
        const val TRIAL_DURATION_MS = 3 * 24 * 60 * 60 * 1000L

        // Danh sách Master Key VIP/PRO chính thức được tạo sẵn để chia sẻ cho bạn bè
        val PRESET_FRIEND_KEYS = listOf(
            "SPACARE-VIP-8888-9999",
            "SPACARE-PRO-2026-8888",
            "SPACARE-FRIEND-7777-6666",
            "SPACARE-GOLD-9999-1111",
            "SPACARE-PREMIUM-5555-4444",
            "SPACARE-MASTER-3333-2222",
            "SPACARE-SPA-6868-8686",
            "SPACARE-LIFETIME-1234-5678"
        )

        /**
         * Kiểm tra tính hợp lệ của key:
         * 1. Nằm trong danh sách Master Key được cung cấp sẵn
         * 2. Hoặc thỏa mãn thuật toán checksum mở rộng: SPACARE-[TAG]-[XXXX]-[YYYY]
         */
        fun isValidKey(rawKey: String): Boolean {
            val key = rawKey.trim().uppercase()
            if (PRESET_FRIEND_KEYS.contains(key)) return true

            // Thuật toán kiểm tra mã checksum động: SPACARE-[PREFIX]-[XXXX]-[YYYY]
            val regex = Regex("^SPACARE-[A-Z0-9]{3,8}-(\\d{4})-(\\d{4})$")
            val match = regex.find(key)
            if (match != null) {
                val randPart = match.groupValues[1].toIntOrNull() ?: return false
                val checksum = match.groupValues[2].toIntOrNull() ?: return false
                val expectedChecksum = (randPart * 7 + 1234) % 9000 + 1000
                return checksum == expectedChecksum
            }

            return false
        }

        /**
         * Tạo Key kích hoạt bản quyền động mới để người dùng có thể bấm tạo và gửi ngay cho bạn bè
         */
        fun generateNewFriendKey(): String {
            val randPart = (1000..9999).random()
            val checksum = (randPart * 7 + 1234) % 9000 + 1000
            return "SPACARE-FRND-$randPart-$checksum"
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

