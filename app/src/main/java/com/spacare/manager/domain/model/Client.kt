package com.spacare.manager.domain.model

data class Client(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val avatarUrl: String? = null,
    val vipTier: VipTier = VipTier.STANDARD,
    val skinType: String,
    val skinConditionNotes: String,
    val allergyAlerts: List<String>,
    val contraindications: List<String>, // Chống chỉ định y khoa (ví dụ: đang mang thai, tiêm botox < 2 tuần)
    val treatmentPackages: List<TreatmentPackage>,
    val serviceHistory: List<ServiceRecord>,
    val beforeAfterComparisons: List<BeforeAfterComparison>,
    val sessionPhotos: List<SessionPhotoRecord> = emptyList()
)

enum class VipTier(val title: String) {
    STANDARD("Khách hàng"),
    VIP("Thân thiết")
}

data class TreatmentPackage(
    val id: String,
    val name: String,
    val totalSessions: Int,
    val usedSessions: Int,
    val remainingSessions: Int,
    val startDate: String,
    val expiryDate: String,
    val assignedTherapist: String,
    val intervalDays: Int = 7, // Chu kỳ điều trị (số ngày giữa các buổi, ví dụ 7, 10 ngày)
    val lastSessionDate: String? = null, // Ngày làm buổi gần nhất (dd/MM/yyyy)
    val nextRecommendedDate: String? = null // Ngày dự kiến làm buổi tiếp theo (dd/MM/yyyy)
)

data class BeforeAfterComparison(
    val id: String,
    val treatmentName: String,
    val beforeDate: String,
    val afterDate: String,
    val beforeImageUrl: String,
    val afterImageUrl: String,
    val sessionMilestone: String, // ví dụ: "Buổi 1 vs Buổi 7"
    val technicianName: String,
    val clinicalNotes: String
)

data class ServiceRecord(
    val id: String,
    val date: String,
    val serviceName: String,
    val technicianName: String,
    val room: String,
    val cost: Long,
    val feedback: String? = null
)

data class SessionPhotoRecord(
    val id: String,
    val sessionNumber: Int,
    val title: String,
    val date: String,
    val imageUrl: String,
    val notes: String = "",
    val technicianName: String = ""
)

