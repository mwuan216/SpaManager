package com.spacare.manager.domain.model

data class StoreProfile(
    val name: String = "SpaCare Wellness Center",
    val hotline: String = "1900 6868",
    val address: String = "128 Phan Xích Long, Phú Nhuận, TP.HCM",
    val selectedBank: BankOption = SupportedBanks[1], // Vietcombank làm mặc định gợi ý, hoặc người dùng chọn
    val accountNumber: String = "1023456789",
    val accountHolder: String = "NGUYEN THI SPA MANAGER"
)

data class SpaServicePackage(
    val id: String,
    val name: String,
    val totalSessions: Int,
    val price: Long,
    val durationMinutes: Int,
    val description: String
)

data class TechnicianStaff(
    val id: String,
    val name: String,
    val phone: String,
    val specialty: String,
    val rating: Float = 5.0f,
    val isActive: Boolean = true
)

