package com.spacare.manager.core.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

/**
 * Worker chạy ngầm phụ trách gửi thông báo đẩy (Push Notification)
 * hoặc tin nhắn SMS/Zalo ZNS nhắc hẹn trước 2 tiếng / 24 tiếng cho khách hàng.
 */
class AppointmentReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val clientName = inputData.getString("CLIENT_NAME") ?: "Quý khách"
        val clientPhone = inputData.getString("CLIENT_PHONE") ?: ""
        val serviceName = inputData.getString("SERVICE_NAME") ?: "dịch vụ"
        val appointmentTime = inputData.getString("APPOINTMENT_TIME") ?: "hôm nay"

        Log.d(TAG, "Bắt đầu tiến trình gửi tin nhắn nhắc hẹn...")

        return try {
            // 1. Giả lập gọi API Zalo Cloud (ZNS) hoặc SMS Brandname Gateway (eSMS / SpeedSMS)
            delay(800) // Giả lập độ trễ mạng

            val reminderPayload = mapOf(
                "phone" to clientPhone,
                "template_id" to "ZNS_SPA_REMINDER_T2",
                "params" to mapOf(
                    "customer_name" to clientName,
                    "service_name" to serviceName,
                    "time" to appointmentTime,
                    "spa_hotline" to "1900 6868",
                    "spa_address" to "128 Phan Xích Long, Phú Nhuận, TP.HCM"
                )
            )

            Log.i(TAG, "Đã gửi thành công SMS / ZNS nhắc hẹn cho $clientName ($clientPhone): $reminderPayload")

            // 2. Phát thông báo local push notification trên thiết bị lễ tân
            showLocalNotification(clientName, serviceName, appointmentTime)

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi kích hoạt nhắc hẹn tự động", e)
            Result.retry()
        }
    }

    private fun showLocalNotification(clientName: String, service: String, time: String) {
        // Mockup Notification Manager
        Log.i(TAG, "[NOTIFICATION MANAGER] Nhắc nhở lễ tân: Khách $clientName có lịch $service vào lúc $time.")
    }

    companion object {
        const val TAG = "AppointmentReminderWorker"
    }
}

