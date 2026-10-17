package com.spacare.manager.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spacare.manager.domain.model.TechnicianStaff

@Entity(tableName = "technicians")
data class TechnicianEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String,
    val specialty: String,
    val rating: Float,
    val isActive: Boolean
) {
    fun toDomain(): TechnicianStaff {
        return TechnicianStaff(
            id = id,
            name = name,
            phone = phone,
            specialty = specialty,
            rating = rating,
            isActive = isActive
        )
    }

    companion object {
        fun fromDomain(staff: TechnicianStaff): TechnicianEntity {
            return TechnicianEntity(
                id = staff.id,
                name = staff.name,
                phone = staff.phone,
                specialty = staff.specialty,
                rating = staff.rating,
                isActive = staff.isActive
            )
        }
    }
}

