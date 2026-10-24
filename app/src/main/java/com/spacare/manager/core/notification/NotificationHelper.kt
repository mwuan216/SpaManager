package com.spacare.manager.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.spacare.manager.MainActivity
import com.spacare.manager.R

object NotificationHelper {

    const val CHANNEL_ID = "spacare_appointment_reminders"
    const val CHANNEL_NAME = "Nhắc Nhở Lịch Hẹn Văn Định Skinlab"
    const val CHANNEL_DESC = "Thông báo khi lịch hẹn trị liệu sắp đến giờ phục vụ"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = Color.parseColor("#2D5A43") // SageGreenPrimary
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 350, 200, 350)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun showAppointmentApproachingNotification(
        context: Context,
        appointmentId: String,
        clientName: String,
        clientPhone: String,
        serviceName: String,
        startTime: String,
        date: String,
        roomName: String,
        staffName: String,
        minutesBefore: Int = 15
    ) {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NOTIFICATION_APPOINTMENT_ID", appointmentId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            appointmentId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIconBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
        } catch (e: Exception) {
            null
        }

        val title = "⏰ Sắp Đến Giờ Hẹn ($startTime): $clientName"
        val shortContent = "$serviceName • $roomName • KTV $staffName"
        val bigText = StringBuilder().apply {
            append("• Khách hàng: $clientName ($clientPhone)\n")
            append("• Dịch vụ: $serviceName\n")
            append("• Thời gian: $startTime ngày $date\n")
            append("• Phòng: $roomName | KTV: $staffName\n")
            append("👉 Vui lòng chuẩn bị phòng và đón tiếp khách hàng chu đáo nhé!")
        }.toString()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .apply {
                if (largeIconBitmap != null) {
                    setLargeIcon(largeIconBitmap)
                }
            }
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(bigText)
                    .setSummaryText("Văn Định Skinlab • Còn ~$minutesBefore phút")
            )
            .setColor(Color.parseColor("#2D5A43"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notifId = appointmentId.hashCode()
            notificationManager.notify(notifId, notification)
        } catch (e: SecurityException) {
            // Permission revoked
        }
    }

    fun showRoutineReminderNotification(
        context: Context,
        packageId: String,
        clientName: String,
        clientPhone: String,
        packageName: String,
        intervalDays: Int,
        remainingSessions: Int,
        recommendedDate: String
    ) {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            packageId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIconBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
        } catch (e: Exception) {
            null
        }

        val title = "🌿 Nhắc Lịch Liệu Trình: $clientName"
        val shortContent = "Gói: $packageName • Chu kỳ $intervalDays ngày (Còn $remainingSessions buổi)"
        val bigText = StringBuilder().apply {
            append("• Khách hàng: $clientName ($clientPhone)\n")
            append("• Gói liệu trình: $packageName\n")
            append("• Chu kỳ điều trị: $intervalDays ngày làm 1 lần\n")
            append("• Số buổi còn lại: $remainingSessions buổi\n")
            append("• Ngày dự kiến làm buổi tiếp theo: $recommendedDate\n")
            append("👉 Đã đến hạn nhắc hẹn! Hãy gọi điện hoặc gửi tin nhắn cho khách nhé.")
        }.toString()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .apply {
                if (largeIconBitmap != null) {
                    setLargeIcon(largeIconBitmap)
                }
            }
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(bigText)
                    .setSummaryText("Văn Định Skinlab • Nhắc Lịch Liệu Trình")
            )
            .setColor(Color.parseColor("#2D5A43"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(packageId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission revoked
        }
    }

    fun showTestNotification(context: Context) {
        showAppointmentApproachingNotification(
            context = context,
            appointmentId = "TEST-" + System.currentTimeMillis() % 10000,
            clientName = "Nguyễn Thu Hà",
            clientPhone = "0912 345 678",
            serviceName = "Chăm Sóc Da Căng Bóng Whitening Facial",
            startTime = "14:30",
            date = "Hôm nay",
            roomName = "Phòng VIP 01",
            staffName = "Thảo My",
            minutesBefore = 15
        )
    }
}
