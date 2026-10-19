package com.spacare.manager.core.license

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.util.Locale
import java.util.UUID

/**
 * Quản lý định danh duy nhất của thiết bị (Device ID & Model)
 * Đảm bảo mỗi thiết bị có một mã định danh ổn định không đổi.
 */
object DeviceIdManager {

    private const val PREF_DEVICE_ID = "spacare_device_fingerprint_pref"
    private const val KEY_PERSISTENT_UUID = "key_persistent_device_uuid"

    /**
     * Lấy mã định danh phần cứng duy nhất của thiết bị
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        val appContext = context.applicationContext

        // 1. Lấy Android ID hệ thống
        val androidId = try {
            Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            null
        }

        // Kiểm tra tính hợp lệ của Android ID (tránh lỗi bug 9774d56d682e549c trên một số máy cũ)
        if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
            return androidId.trim().uppercase()
        }

        // 2. Fallback sang UUID lưu bền vững trong SharedPreferences
        val prefs = appContext.getSharedPreferences(PREF_DEVICE_ID, Context.MODE_PRIVATE)
        val savedUuid = prefs.getString(KEY_PERSISTENT_UUID, null)
        if (!savedUuid.isNullOrBlank()) {
            return savedUuid
        }

        val newUuid = UUID.randomUUID().toString().replace("-", "").take(16).uppercase()
        prefs.edit().putString(KEY_PERSISTENT_UUID, newUuid).apply()
        return newUuid
    }

    /**
     * Lấy tên dòng máy thiết bị người dùng (ví dụ: Samsung SM-S918B, Xiaomi 2201116PG)
     */
    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.orEmpty().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        val model = Build.MODEL.orEmpty()
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model".trim()
        }.ifBlank { "Thiết bị Android" }
    }
}
