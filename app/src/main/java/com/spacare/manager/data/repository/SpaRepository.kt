package com.spacare.manager.data.repository

import com.spacare.manager.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class SpaRepository {

    // Danh sách khách hàng đầy đủ
    private val _clients = MutableStateFlow(
        listOf(
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
                        clinicalNotes = "Sắc tố đỏ vùng má giảm 75%, bề mặt da mịn màng, lỗ chân lông thu nhỏ đáng kể."
                    )
                )
            ),
            Client(
                id = "KH-8830",
                fullName = "Trần Bích Ngọc",
                phone = "0988 776 655",
                email = "bichngoc.tran@yahoo.com",
                vipTier = VipTier.DIAMOND,
                skinType = "Da khô thiếu nước, xuất hiện nếp nhăn tĩnh",
                skinConditionNotes = "Vùng khóe mắt và rãnh cười có dấu hiệu lão hóa sớm, cần liệu trình cấp ẩm chuyên sâu và collagen tái sinh.",
                allergyAlerts = listOf(
                    "Dị ứng với phấn hoa cúc Chamomile",
                    "Kích ứng với cồn khô (Alcohol Denat)"
                ),
                contraindications = listOf(
                    "Không dùng lực massage quá mạnh vùng cổ do huyết áp thấp"
                ),
                treatmentPackages = listOf(
                    TreatmentPackage(
                        id = "PKG-03",
                        name = "Trẻ Hóa Nâng Cơ RF Hifu Collagen 360",
                        totalSessions = 5,
                        usedSessions = 2,
                        remainingSessions = 3,
                        startDate = "01/09/2026",
                        expiryDate = "01/03/2027",
                        assignedTherapist = "KTV. Hương Giang"
                    )
                ),
                serviceHistory = listOf(
                    ServiceRecord(
                        id = "SRV-10",
                        date = "10/10/2026 - 10:00",
                        serviceName = "Massage Body Đá Nóng Himalaya 90p",
                        technicianName = "Hương Giang",
                        room = "Phòng Trị Liệu Sen",
                        cost = 850000L,
                        feedback = "Rất thoải mái, dịch vụ chu đáo."
                    )
                ),
                beforeAfterComparisons = emptyList()
            ),
            Client(
                id = "KH-8831",
                fullName = "Lê Hoàng Yến",
                phone = "0909 112 233",
                email = "hoangyen.le@gmail.com",
                vipTier = VipTier.SILVER,
                skinType = "Da dầu mụn bọc, lỗ chân lông to",
                skinConditionNotes = "Đang trong giai đoạn gom cồi mụn viêm, kết hợp chiếu ánh sáng sinh học Biolight giảm thâm đỏ.",
                allergyAlerts = listOf(
                    "Dị ứng kháng sinh nhóm Sulfa bôi ngoài da"
                ),
                contraindications = listOf(
                    "Tuyệt đối không lấy nhân mụn chưa gom cồi"
                ),
                treatmentPackages = listOf(
                    TreatmentPackage(
                        id = "PKG-04",
                        name = "Trị Mụn Chuyên Sâu Detox O2 & Biolight",
                        totalSessions = 6,
                        usedSessions = 5,
                        remainingSessions = 1,
                        startDate = "15/07/2026",
                        expiryDate = "15/11/2026",
                        assignedTherapist = "KTV. Lan Phương"
                    )
                ),
                serviceHistory = emptyList(),
                beforeAfterComparisons = emptyList()
            ),
            Client(
                id = "KH-8832",
                fullName = "Vũ Thu Trang",
                phone = "0977 123 456",
                email = "thutrang.vu@gmail.com",
                vipTier = VipTier.STANDARD,
                skinType = "Da thường, ít khuyết điểm",
                skinConditionNotes = "Đến chăm sóc thư giãn và duy trì độ ẩm tự nhiên, gội đầu dưỡng sinh thảo dược.",
                allergyAlerts = emptyList(),
                contraindications = emptyList(),
                treatmentPackages = emptyList(),
                serviceHistory = emptyList(),
                beforeAfterComparisons = emptyList()
            )
        )
    )
    val clients: Flow<List<Client>> = _clients.asStateFlow()

    // Khách hàng đang được chọn để xem chi tiết
    private val _selectedClientId = MutableStateFlow<String?>("KH-8829")
    val selectedClientId: Flow<String?> = _selectedClientId.asStateFlow()

    fun selectClient(id: String?) {
        _selectedClientId.value = id
    }

    // --- CRUD KHÁCH HÀNG ---
    fun addClient(client: Client) {
        val currentList = _clients.value.toMutableList()
        currentList.add(0, client)
        _clients.value = currentList
        _selectedClientId.value = client.id
    }

    fun updateClient(client: Client) {
        val currentList = _clients.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == client.id }
        if (index != -1) {
            currentList[index] = client
            _clients.value = currentList
        }
    }

    fun deleteClient(clientId: String) {
        val currentList = _clients.value.toMutableList()
        currentList.removeAll { it.id == clientId }
        _clients.value = currentList
        if (_selectedClientId.value == clientId) {
            _selectedClientId.value = currentList.firstOrNull()?.id
        }
    }

    // Check-in trừ buổi liệu trình
    fun checkInSession(clientId: String, packageId: String) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val updatedPackages = client.treatmentPackages.map { pkg ->
            if (pkg.id == packageId && pkg.remainingSessions > 0) {
                pkg.copy(
                    usedSessions = pkg.usedSessions + 1,
                    remainingSessions = pkg.remainingSessions - 1
                )
            } else pkg
        }

        val updatedHistory = client.serviceHistory.toMutableList()
        val pkg = client.treatmentPackages.find { it.id == packageId }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        updatedHistory.add(
            0,
            ServiceRecord(
                id = "SRV-${System.currentTimeMillis().toString().takeLast(4)}",
                date = dateFormat.format(Date()),
                serviceName = "${pkg?.name ?: "Liệu trình"} (Buổi ${(pkg?.usedSessions ?: 0) + 1})",
                technicianName = pkg?.assignedTherapist ?: "Chuyên viên",
                room = "Phòng Điều Trị VIP",
                cost = 0L,
                feedback = "Khách điểm danh sử dụng buổi liệu trình."
            )
        )

        updateClient(client.copy(treatmentPackages = updatedPackages, serviceHistory = updatedHistory))
    }

    // Thêm gói dịch vụ mới cho khách
    fun addTreatmentPackage(clientId: String, pkg: TreatmentPackage) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val updatedPackages = client.treatmentPackages + pkg
        updateClient(client.copy(treatmentPackages = updatedPackages))
    }

    // Thêm ảnh Before & After mới
    fun addBeforeAfter(clientId: String, record: BeforeAfterComparison) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val updatedRecords = client.beforeAfterComparisons + record
        updateClient(client.copy(beforeAfterComparisons = updatedRecords))
    }

    // --- QUẢN LÝ LỊCH HẸN (CALENDAR) ---
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

    fun addAppointment(appointment: Appointment) {
        val current = _appointments.value.toMutableList()
        current.add(appointment)
        // Sắp xếp theo giờ bắt đầu
        current.sortBy { it.startTime }
        _appointments.value = current
    }

    fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
        val current = _appointments.value.toMutableList()
        val index = current.indexOfFirst { it.id == appointmentId }
        if (index != -1) {
            current[index] = current[index].copy(status = status)
            _appointments.value = current
        }
    }

    fun deleteAppointment(appointmentId: String) {
        val current = _appointments.value.toMutableList()
        current.removeAll { it.id == appointmentId }
        _appointments.value = current
    }

    // --- THU NGÂN & TẠO MÃ VIETQR ---
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
                    unitPrice = 0L
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
            discountAmount = 225000L,
            tipAmount = 0L,
            paymentStatus = PaymentStatus.WAITING_FOR_PAYMENT,
            selectedBank = SupportedBanks[0]
        )
    )
    val currentInvoice: Flow<CheckoutInvoice> = _currentInvoice.asStateFlow()

    fun updateBank(bank: BankOption, accountNumber: String, accountHolder: String) {
        _currentInvoice.value = _currentInvoice.value.copy(
            selectedBank = bank,
            accountNumber = accountNumber,
            accountHolder = accountHolder
        )
    }

    fun markInvoiceAsPaid() {
        val current = _currentInvoice.value
        val now = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        _currentInvoice.value = current.copy(
            paymentStatus = PaymentStatus.PAYMENT_SUCCESS,
            transactionRef = "CK-${System.currentTimeMillis().toString().takeLast(6)}",
            paidAt = now
        )
    }

    fun resetInvoice() {
        val code = "SPA" + (1000..9999).random()
        _currentInvoice.value = CheckoutInvoice(
            invoiceId = "INV-${System.currentTimeMillis()}",
            invoiceCode = code,
            clientName = "Khách Hàng Mới",
            clientPhone = "0900 000 000",
            items = listOf(
                OrderItem("SRV-NEW", "Dịch vụ Spa & Trị liệu thư giãn", ItemType.SERVICE, 1, 500000L)
            ),
            discountAmount = 0L,
            paymentStatus = PaymentStatus.WAITING_FOR_PAYMENT
        )
    }

    fun createInvoiceFromAppointment(appointment: Appointment) {
        val code = "SPA" + (1000..9999).random()
        _currentInvoice.value = CheckoutInvoice(
            invoiceId = "INV-${System.currentTimeMillis()}",
            invoiceCode = code,
            clientName = appointment.clientName,
            clientPhone = appointment.clientPhone,
            items = listOf(
                OrderItem(
                    id = appointment.id,
                    name = appointment.serviceName,
                    type = if (appointment.price == 0L) ItemType.PACKAGE else ItemType.SERVICE,
                    quantity = 1,
                    unitPrice = appointment.price
                )
            ),
            discountAmount = 0L,
            paymentStatus = PaymentStatus.WAITING_FOR_PAYMENT
        )
    }

    // --- BÁO CÁO DOANH THU & CHỈ SỐ ---
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
            treatmentPackages = 265000000L,
            singleServices = 120800000L,
            retailProducts = 80000000L
        ),
        topPerformers = listOf(
            StaffPerformance("ST-01", "Thảo My", "Chuyên Viên Trị Liệu", 46, 32500000L, 4.95f),
            StaffPerformance("ST-02", "Hương Giang", "Kỹ Thuật Viên Dưỡng Sinh", 52, 24800000L, 4.90f),
            StaffPerformance("ST-03", "Lan Phương", "Chuyên Viên Laser & Phục Hồi", 38, 28100000L, 4.88f)
        )
    )
}
