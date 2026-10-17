package com.spacare.manager.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spacare.manager.domain.model.*

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val avatarUrl: String?,
    val vipTier: String,
    val skinType: String,
    val skinConditionNotes: String,
    val allergyAlerts: List<String>,
    val contraindications: List<String>,
    val treatmentPackages: List<TreatmentPackage>,
    val serviceHistory: List<ServiceRecord>,
    val beforeAfterComparisons: List<BeforeAfterComparison>,
    val sessionPhotos: List<SessionPhotoRecord>
) {
    fun toDomain(): Client {
        val tier = try {
            VipTier.valueOf(vipTier)
        } catch (e: Exception) {
            VipTier.STANDARD
        }
        return Client(
            id = id,
            fullName = fullName,
            phone = phone,
            email = email,
            avatarUrl = avatarUrl,
            vipTier = tier,
            skinType = skinType,
            skinConditionNotes = skinConditionNotes,
            allergyAlerts = allergyAlerts,
            contraindications = contraindications,
            treatmentPackages = treatmentPackages,
            serviceHistory = serviceHistory,
            beforeAfterComparisons = beforeAfterComparisons,
            sessionPhotos = sessionPhotos
        )
    }

    companion object {
        fun fromDomain(client: Client): ClientEntity {
            return ClientEntity(
                id = client.id,
                fullName = client.fullName,
                phone = client.phone,
                email = client.email,
                avatarUrl = client.avatarUrl,
                vipTier = client.vipTier.name,
                skinType = client.skinType,
                skinConditionNotes = client.skinConditionNotes,
                allergyAlerts = client.allergyAlerts,
                contraindications = client.contraindications,
                treatmentPackages = client.treatmentPackages,
                serviceHistory = client.serviceHistory,
                beforeAfterComparisons = client.beforeAfterComparisons,
                sessionPhotos = client.sessionPhotos
            )
        }
    }
}

