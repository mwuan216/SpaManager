package com.spacare.manager.data.repository

import com.spacare.manager.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpaRepository {

    private val _currentClient = MutableStateFlow(
        Client(
            id = "KH-8829",
            fullName = "Nguyễn Thu Hà",
            phone = "0912 345 678",
            email = "thuha.nguyen@gmail.com",
            vipTier = VipTier.GOLD,
            skinType = "Da hỗn hợp thiên dầu, biểu bì mỏng",
            skinConditionNotes = "Vùng chữ T tiết dầu nhẹ, hai bên má có mao mạch nông. Hàng rào bảo vệ da đang phục hồi sau treatment trước đó.",
            allergyAlerts = listOf(
                "Dị ứng nồng độ Retinol > 0.5%",
                "Nhạy cảm với hương liệu nhân tạo (Fragrance)",
                "Kích ứng với Tinh dầu tràm trà đậm đặc"
            ),
            contraindications = listOf(
                "Không dùng máy sóng RF tần số cao vùng gò má",
                "Chống chỉ định peel da axit nồng độ cao"
            ),
            treatmentPackages = listOf(
                TreatmentPackage(
                    id = "PKG-01",
                    name = "Liệu Trình Căng Bóng Phục Hồi Whitening Facial",
                    totalSessions = 10,
                    usedSessions = 7,
                    remainingSessions = 3,
                    startDate = "12/08/2026",
                    expiryDate = "12/12/2026",
                    assignedTherapist = "KTV. Thảo My"
                ),
                TreatmentPackage(
                    id = "PKG-02",
                    name = "Triệt Lông Trẻ Hóa Diode Laser Master",
                    totalSessions = 8,
                    usedSessions = 6,
                    remainingSessions = 2,
                    startDate = "01/06/2026",
                    expiryDate = "30/11/2026",
                    assignedTherapist = "KTV. Lan Phương"
                )
            ),
            serviceHistory = listOf(
                ServiceRecord(
                    id = "SRV-07",
                    date = "09/10/2026 - 15:00",
                    serviceName = "Whitening Facial (Buổi 7) + Điện Di Tinh Chất Tế Bào Gốc",
                    technicianName = "Thảo My",
                    room = "Phòng VIP 02",
                    cost = 0L,
                    feedback = "Khách khen da mềm mịn, hết cảm giác châm chích."
                ),
                ServiceRecord(
                    id = "SRV-06",
                    date = "25/09/2026 - 14:00",
                    serviceName = "Whitening Facial (Buổi 6) + Cấp Ẩm O2 Jet",
                    technicianName = "Thảo My",
                    room = "Phòng Liệu Trình 01",
                    cost = 0L,
                    feedback = "Da phục hồi độ ẩm tốt, tone da sáng 1 nấc."
                ),
                ServiceRecord(
                    id = "SRV-05",
                    date = "10/09/2026 - 10:30",
                    serviceName = "Gội Đầu Dưỡng Sinh Thảo Dược Chuông Xoay 75p",
                    technicianName = "Hương Giang",
                    room = "Phòng Dưỡng Sinh Sen",
                    cost = 450000L,
                    feedback = "Rất hài lòng, thư giãn cổ vai gáy."
                )
            ),
            beforeAfterComparisons = listOf(
                BeforeAfterComparison(
                    id = "BA-01",
                    treatmentName = "Căng Bóng & Giảm Thâm Đỏ Sau Mụn",
                    beforeDate = "12/08/2026 (Buổi 1)",
                    afterDate = "09/10/2026 (Buổi 7)",
                    beforeImageUrl = "https://images.unsplash.com/photo-1512290900672-1f55b9e59d4c?auto=format&fit=crop&w=600&q=80",
                    afterImageUrl = "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?auto=format&fit=crop&w=600&q=80",
                    sessionMilestone = "Tiến độ: Buổi 1 ➔ Buổi 7",
                    technicianName = "Chuyên viên Thảo My",
                    clinicalNotes = "Sắc tố đỏ vùng má giảm 75%, bề mặt da mịn màng, lỗ chân lông thu nhỏ đáng kể. Khách không còn tình trạng kích ứng."
                )
            )
        )
    )
    val currentClient: Flow<Client> = _currentClient.asStateFlow()

    private val _appointments = MutableStateFlow(
        listOf(
            Appointment(
                id = "APT-101",
                clientName = "Nguyễn Thu Hà",
                clientPhone = "0912 345 678",
                serviceName = "Whitening Facial (Buổi 8/10)",
                durationMinutes = 90,
                staffName = "Thảo My",
                roomName = "Phòng VIP 02",
                startTime = "09:30",
                endTime = "11:00",
                date = "16/10/2026",
                status = AppointmentStatus.CONFIRMED,
                price = 0L,
                reminderSent = true
            ),
            Appointment(
                id = "APT-102",
                clientName = "Trần Bích Ngọc",
                clientPhone = "0988 776 655",
                serviceName = "Massage Body Đá Nóng Himalaya 90p",
                durationMinutes = 90,
                staffName = "Hương Giang",
                roomName = "Phòng Trị Liệu Sen",
                startTime = "10:00",
                endTime = "11:30",
                date = "16/10/2026",
                status = AppointmentStatus.IN_SERVICE,
                price = 850000L,
                reminderSent = true
            ),
            Appointment(
                id = "APT-103",
                clientName = "Lê Hoàng Yến",
                clientPhone = "0909 112 233",
                serviceName = "Chăm Sóc Da Chuyên Sâu Detox O2",
                durationMinutes = 75,
                staffName = "Lan Phương",
                roomName = "Phòng Chăm Sóc 03",
                startTime = "13:30",
                endTime = "14:45",
                date = "16/10/2026",
                status = AppointmentStatus.CONFIRMED,
                price = 650000L,
                reminderSent = true
            ),
            Appointment(
                id = "APT-104",
                clientName = "Phạm Mai Anh",
                clientPhone = "0933 456 789",
                serviceName = "Triệt Lông Diode Laser Trẻ Hóa",
                durationMinutes = 45,
                staffName = "Thảo My",
                roomName = "Phòng Laser 01",
                startTime = "14:00",
                endTime = "14:45",
                date = "16/10/2026",
                status = AppointmentStatus.PENDING,
                price = 500000L,
                reminderSent = false
            ),
            Appointment(
                id = "APT-105",
                clientName = "Vũ Thu Trang",
                clientPhone = "0977 123 456",
                serviceName = "Gội Đầu Dưỡng Sinh Thảo Dược 60p",
                durationMinutes = 60,
                staffName = "Hương Giang",
                roomName = "Phòng Dưỡng Sinh Sen",
                startTime = "15:30",
                endTime = "16:30",
                date = "16/10/2026",
                status = AppointmentStatus.CONFIRMED,
                price = 350000L,
                reminderSent = true
            )
        )
    )
    val appointments: Flow<List<Appointment>> = _appointments.asStateFlow()

    private val _currentInvoice = MutableStateFlow(
        CheckoutInvoice(
            invoiceId = "INV-20261016-01",
            invoiceCode = "SPA8829",
            clientName = "Nguyễn Thu Hà",
            clientPhone = "0912 345 678",
            items = listOf(
                OrderItem(
                    id = "ITEM-01",
                    name = "Whitening Facial Cao Cấp (Buổi 8/10)",
                    type = ItemType.PACKAGE,
                    quantity = 1,
                    unitPrice = 0L // Trừ vào gói
                ),
                OrderItem(
                    id = "ITEM-02",
                    name = "Mặt nạ vàng 24K khóa dưỡng ẩm chuyên sâu",
                    type = ItemType.SERVICE,
                    quantity = 1,
                    unitPrice = 350000L
                ),
                OrderItem(
                    id = "ITEM-03",
                    name = "Serum Tế Bào Gốc EGF Repair 30ml (Mỹ phẩm)",
                    type = ItemType.PRODUCT,
                    quantity = 1,
                    unitPrice = 1250000L
                ),
                OrderItem(
                    id = "ITEM-04",
                    name = "Kem chống nắng khoáng chất Mineral UV Shield 50ml",
                    type = ItemType.PRODUCT,
                    quantity = 1,
                    unitPrice = 650000L
                )
            ),
            discountAmount = 225000L, // Giảm 10% khách VIP Gold
            tipAmount = 0L,
            paymentStatus = PaymentStatus.WAITING_FOR_PAYMENT
        )
    )
    val currentInvoice: Flow<CheckoutInvoice> = _currentInvoice.asStateFlow()

    fun simulatePaymentSuccess() {
        val current = _currentInvoice.value
        _currentInvoice.value = current.copy(
            paymentStatus = PaymentStatus.PAYMENT_SUCCESS,
            transactionRef = "MB-${System.currentTimeMillis().toString().takeLast(6)}",
            paidAt = "16/10/2026 20:15"
        )
    }

    fun resetPayment() {
        val current = _currentInvoice.value
        _currentInvoice.value = current.copy(
            paymentStatus = PaymentStatus.WAITING_FOR_PAYMENT,
            transactionRef = null,
            paidAt = null
        )
    }

    val dashboardMetrics = DashboardMetrics(
        todayRevenue = 18650000L,
        todayGrowthPercent = 14.8f,
        weekRevenue = 112400000L,
        monthRevenue = 465800000L,
        totalBookingsToday = 19,
        roomOccupancyRate = 84.5f,
        topServices = listOf(
            TopServiceMetric("Liệu Trình Căng Bóng Whitening Facial", 42, 63000000L, 38f),
            TopServiceMetric("Gội Đầu Dưỡng Sinh Thảo Dược", 68, 27200000L, 22f),
            TopServiceMetric("Triệt Lông Công Nghệ Diode Laser", 28, 42000000L, 25f),
            TopServiceMetric("Massage Body Đá Nóng Himalaya", 18, 15300000L, 15f)
        ),
        revenueDistribution = RevenueDistribution(
            treatmentPackages = 265000000L, // 57%
            singleServices = 120800000L,     // 26%
            retailProducts = 80000000L       // 17%
        ),
        topPerformers = listOf(
            StaffPerformance("ST-01", "Thảo My", "Chuyên Viên Trị Liệu", 46, 32500000L, 4.95f),
            StaffPerformance("ST-02", "Hương Giang", "Kỹ Thuật Viên Dưỡng Sinh", 52, 24800000L, 4.90f),
            StaffPerformance("ST-03", "Lan Phương", "Chuyên Viên Laser & Phục Hồi", 38, 28100000L, 4.88f)
        )
    )
}

