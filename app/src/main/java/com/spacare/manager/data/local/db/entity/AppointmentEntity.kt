package com.spacare.manager.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spacare.manager.domain.model.Appointment
import com.spacare.manager.domain.model.AppointmentStatus

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey
    val id: String,
    val clientName: String,
    val clientPhone: String,
    val serviceName: String,
    val durationMinutes: Int,
    val staffName: String,
    val roomName: String,
    val startTime: String,
    val endTime: String,
    val date: String,
    val status: String,
    val price: Long,
    val reminderSent: Boolean,
    val isSessionDeducted: Boolean = false
) {
    fun toDomain(): Appointment {
        val apptStatus = try {
            AppointmentStatus.valueOf(status)
        } catch (e: Exception) {
            AppointmentStatus.CONFIRMED
        }
        return Appointment(
            id = id,
            clientName = clientName,
            clientPhone = clientPhone,
            serviceName = serviceName,
            durationMinutes = durationMinutes,
            staffName = staffName,
            roomName = roomName,
            startTime = startTime,
            endTime = endTime,
            date = date,
            status = apptStatus,
            price = price,
            reminderSent = reminderSent,
            isSessionDeducted = isSessionDeducted
        )
    }

    companion object {
        fun fromDomain(appointment: Appointment): AppointmentEntity {
            return AppointmentEntity(
                id = appointment.id,
                clientName = appointment.clientName,
                clientPhone = appointment.clientPhone,
                serviceName = appointment.serviceName,
                durationMinutes = appointment.durationMinutes,
                staffName = appointment.staffName,
                roomName = appointment.roomName,
                startTime = appointment.startTime,
                endTime = appointment.endTime,
                date = appointment.date,
                status = appointment.status.name,
                price = appointment.price,
                reminderSent = appointment.reminderSent,
                isSessionDeducted = appointment.isSessionDeducted
            )
        }
    }
}

