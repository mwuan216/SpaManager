package com.spacare.manager.domain.model

enum class AppointmentStatus(val label: String) {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Đã đặt hẹn"),
    IN_SERVICE("Đang thực hiện"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy")
}

data class Appointment(
    val id: String,
    val clientName: String,
    val clientPhone: String,
    val serviceName: String,
    val durationMinutes: Int,
    val staffName: String,
    val roomName: String,
    val startTime: String, // format "14:30"
    val endTime: String,   // format "16:00"
    val date: String,      // format "16/10/2026"
    val status: AppointmentStatus,
    val price: Long,
    val reminderSent: Boolean = true,
    val isSessionDeducted: Boolean = false
)

