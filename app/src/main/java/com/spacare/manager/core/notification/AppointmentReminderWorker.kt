package com.spacare.manager.core.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

/**
 * Worker chạy ngầm phụ trách gửi thông báo đẩy (Push Notification) trên thiết bị
 * và mô phỏng gửi tin nhắn SMS/Zalo ZNS nhắc hẹn trước giờ trị liệu.
 */
class AppointmentReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val appointmentId = inputData.getString("APPOINTMENT_ID") ?: "APT-${System.currentTimeMillis()}"
        val clientName = inputData.getString("CLIENT_NAME") ?: "Khách hàng"
        val clientPhone = inputData.getString("CLIENT_PHONE") ?: ""
        val serviceName = inputData.getString("SERVICE_NAME") ?: "Liệu trình chăm sóc da"
        val startTime = inputData.getString("START_TIME") ?: "Sắp tới"
        val date = inputData.getString("DATE") ?: "Hôm nay"
        val roomName = inputData.getString("ROOM_NAME") ?: "Phòng trị liệu"
        val staffName = inputData.getString("STAFF_NAME") ?: "KTV phụ trách"
        val minutesBefore = inputData.getInt("MINUTES_BEFORE", 15)

        Log.d(TAG, "Kích hoạt thông báo nhắc hẹn cho cuộc hẹn $appointmentId ($clientName lúc $startTime)")

        return try {
            // 1. Phát thông báo cục bộ thực sự (Android System Push Notification)
            NotificationHelper.showAppointmentApproachingNotification(
                context = applicationContext,
                appointmentId = appointmentId,
                clientName = clientName,
                clientPhone = clientPhone,
                serviceName = serviceName,
                startTime = startTime,
                date = date,
                roomName = roomName,
                staffName = staffName,
                minutesBefore = minutesBefore
            )

            // 2. Mô phỏng tích hợp SMS Brandname / Zalo ZNS
            delay(400)
            Log.i(TAG, "Đã gửi thông báo nhắc hẹn thành công cho khách hàng $clientName ($clientPhone) lúc $startTime.")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi hiển thị thông báo nhắc hẹn", e)
            Result.retry()
        }
    }

    companion object {
        const val TAG = "AppointmentReminderWorker"
    }
}
