package com.spacare.manager.core.notification

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.spacare.manager.domain.model.Appointment
import com.spacare.manager.domain.model.AppointmentStatus
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

object AppointmentReminderScheduler {

    private const val TAG = "ReminderScheduler"

    /**
     * Lên lịch thông báo nhắc hẹn trước [minutesBefore] phút
     */
    fun scheduleReminder(
        context: Context,
        appointment: Appointment,
        minutesBefore: Int = 15
    ) {
        // Chỉ nhắc hẹn cho lịch đã xác nhận hoặc chưa hủy/hoàn thành
        if (appointment.status == AppointmentStatus.CANCELLED || appointment.status == AppointmentStatus.COMPLETED) {
            cancelReminder(context, appointment.id)
            return
        }

        val appointmentMillis = parseAppointmentTimeMillis(appointment.date, appointment.startTime)
        if (appointmentMillis == null) {
            Log.w(TAG, "Không thể phân tích ngày giờ cuộc hẹn: ${appointment.date} ${appointment.startTime}")
            return
        }

        val targetReminderMillis = appointmentMillis - (minutesBefore * 60 * 1000L)
        val now = System.currentTimeMillis()
        val delayMillis = targetReminderMillis - now

        // Nếu cuộc hẹn đã qua hơn 30 phút thì không lên lịch nữa
        if (appointmentMillis + (30 * 60 * 1000L) < now) {
            Log.d(TAG, "Cuộc hẹn ${appointment.id} đã qua thời gian, bỏ qua.")
            return
        }

        val workData = Data.Builder()
            .putString("APPOINTMENT_ID", appointment.id)
            .putString("CLIENT_NAME", appointment.clientName)
            .putString("CLIENT_PHONE", appointment.clientPhone)
            .putString("SERVICE_NAME", appointment.serviceName)
            .putString("START_TIME", appointment.startTime)
            .putString("END_TIME", appointment.endTime)
            .putString("DATE", appointment.date)
            .putString("ROOM_NAME", appointment.roomName)
            .putString("STAFF_NAME", appointment.staffName)
            .putInt("MINUTES_BEFORE", minutesBefore)
            .build()

        val actualDelay = if (delayMillis > 0) delayMillis else 0L

        val workRequest = OneTimeWorkRequestBuilder<AppointmentReminderWorker>()
            .setInputData(workData)
            .setInitialDelay(actualDelay, TimeUnit.MILLISECONDS)
            .addTag("reminder_${appointment.id}")
            .build()

        val uniqueWorkName = "work_reminder_${appointment.id}"
        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Log.i(TAG, "Đã lên lịch nhắc hẹn cho ${appointment.clientName} lúc ${appointment.startTime} (sau ${actualDelay / 1000}s)")
    }

    fun cancelReminder(context: Context, appointmentId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("work_reminder_$appointmentId")
    }

    fun scheduleAllUpcoming(context: Context, appointments: List<Appointment>, minutesBefore: Int = 15) {
        appointments.forEach { appt ->
            scheduleReminder(context, appt, minutesBefore)
        }
    }

    private fun parseAppointmentTimeMillis(dateStr: String, timeStr: String): Long? {
        return try {
            val fullStr = "${dateStr.trim()} ${timeStr.trim()}"
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = sdf.parse(fullStr)
            date?.time
        } catch (e: Exception) {
            null
        }
    }
}
