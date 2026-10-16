package com.spacare.manager.data.repository

import android.content.Context
import com.spacare.manager.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class SpaRepository(context: Context? = null) {

    private val prefs = context?.applicationContext?.getSharedPreferences(
        "spacare_usage_mode_prefs",
        Context.MODE_PRIVATE
    )

    private val _isRealUsageMode = MutableStateFlow(
        prefs?.getBoolean("key_is_real_usage", false) ?: false
    )
    val isRealUsageMode: Flow<Boolean> = _isRealUsageMode.asStateFlow()

    // --- CẤU HÌNH CỬA HÀNG & TÀI KHOẢN NGÂN HÀNG SPA ---
    private val _storeProfile = MutableStateFlow(
        StoreProfile(
            name = "SpaCare Wellness Center",
            hotline = "1900 6868",
            address = "128 Phan Xích Long, P. 2, Q. Phú Nhuận, TP.HCM",
            selectedBank = SupportedBanks[0], // Vietcombank làm gợi ý ban đầu
            accountNumber = "1023456789",
            accountHolder = "SPA CARE WELLNESS CENTER",
            defaultMemo = "SPACARE"
        )
    )
    val storeProfile: Flow<StoreProfile> = _storeProfile.asStateFlow()

    fun updateStoreProfile(profile: StoreProfile) {
        _storeProfile.value = profile
        // Đồng bộ ngay sang hóa đơn thu ngân hiện tại
        _currentInvoice.value = _currentInvoice.value.copy(
            selectedBank = profile.selectedBank,
            accountNumber = profile.accountNumber,
            accountHolder = profile.accountHolder,
            defaultMemo = profile.defaultMemo
        )
    }

    // --- QUẢN LÝ MENU GÓI LIỆU TRÌNH ---
    private val _catalogPackages = MutableStateFlow(
        listOf(
            SpaServicePackage(
                id = "PKG-CAT-01",
                name = "Liệu Trình Căng Bóng Whitening Facial",
                totalSessions = 10,
                price = 6500000L,
                durationMinutes = 75,
                description = "Điện di tinh chất tế bào gốc, cấy collagen tươi và khóa ẩm O2 Jet."
            ),
            SpaServicePackage(
                id = "PKG-CAT-02",
                name = "Triệt Lông Diode Laser Trẻ Hóa Master",
                totalSessions = 8,
                price = 4200000L,
                durationMinutes = 45,
                description = "Công nghệ Diode Laser bước sóng 808nm triệt sạch lông và se khít lỗ chân lông."
            ),
            SpaServicePackage(
                id = "PKG-CAT-03",
                name = "Gội Đầu Dưỡng Sinh Thảo Dược Chuông Xoay",
                totalSessions = 5,
                price = 1800000L,
                durationMinutes = 60,
                description = "Gội nước thảo mộc nấu tươi, massage ấn huyệt cổ vai gáy và chuông xoay Tây Tạng."
            ),
            SpaServicePackage(
                id = "PKG-CAT-04",
                name = "Nâng Cơ Trẻ Hóa Hifu 360 Siêu Âm Hội Tụ",
                totalSessions = 3,
                price = 8900000L,
                durationMinutes = 90,
                description = "Sóng siêu âm hội tụ vi điểm kích thích tăng sinh collagen tầng sâu, thon gọn viền hàm."
            )
        )
    )
    val catalogPackages: Flow<List<SpaServicePackage>> = _catalogPackages.asStateFlow()

    fun addCatalogPackage(pkg: SpaServicePackage) {
        _catalogPackages.value = listOf(pkg) + _catalogPackages.value
    }

    fun updateCatalogPackage(pkg: SpaServicePackage) {
        _catalogPackages.value = _catalogPackages.value.map { if (it.id == pkg.id) pkg else it }
    }

    fun deleteCatalogPackage(packageId: String) {
        _catalogPackages.value = _catalogPackages.value.filterNot { it.id == packageId }
    }

    // --- QUẢN LÝ ĐỘI NGŨ KỸ THUẬT VIÊN ---
    private val _technicians = MutableStateFlow(
        listOf(
            TechnicianStaff(
                id = "TECH-01",
                name = "Thảo My",
                phone = "0912 111 222",
                specialty = "Chuyên viên Điều Trị & Phục Hồi Da",
                rating = 4.95f,
                isActive = true
            ),
            TechnicianStaff(
                id = "TECH-02",
                name = "Hương Giang",
                phone = "0912 333 444",
                specialty = "KTV Dưỡng Sinh & Trị Liệu Cổ Vai Gáy",
                rating = 4.90f,
                isActive = true
            ),
            TechnicianStaff(
                id = "TECH-03",
                name = "Lan Phương",
                phone = "0912 555 666",
                specialty = "Chuyên viên Laser & Trẻ Hóa Da Công Nghệ Cao",
                rating = 4.88f,
                isActive = true
            )
        )
    )
    val technicians: Flow<List<TechnicianStaff>> = _technicians.asStateFlow()

    fun addTechnician(tech: TechnicianStaff) {
        _technicians.value = _technicians.value + tech
    }

    fun updateTechnician(tech: TechnicianStaff) {
        _technicians.value = _technicians.value.map { if (it.id == tech.id) tech else it }
    }

    fun deleteTechnician(id: String) {
        _technicians.value = _technicians.value.filterNot { it.id == id }
    }

    // --- DANH SÁCH KHÁCH HÀNG (CRM) ---
    private val demoClients = listOf(
            Client(
                id = "KH-8829",
                fullName = "Nguyễn Thu Hà",
                phone = "0912 345 678",
                email = "thuha.nguyen@gmail.com",
                vipTier = VipTier.VIP,
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
                ),
                sessionPhotos = listOf(
                    SessionPhotoRecord(
                        id = "SP-01",
                        sessionNumber = 1,
                        title = "Buổi 1: Trước điều trị",
                        date = "12/08/2026",
                        imageUrl = "https://images.unsplash.com/photo-1512290900672-1f55b9e59d4c?auto=format&fit=crop&w=600&q=80",
                        notes = "Bề mặt da sần sùi, mao mạch nổi rõ, vùng má đỏ ửng sau kích ứng.",
                        technicianName = "Thảo My"
                    ),
                    SessionPhotoRecord(
                        id = "SP-02",
                        sessionNumber = 3,
                        title = "Buổi 3: Sau phục hồi O2",
                        date = "28/08/2026",
                        imageUrl = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=600&q=80",
                        notes = "Nền da dịu đỏ 40%, hàng rào bảo vệ da bắt đầu dày lên, độ ẩm cải thiện.",
                        technicianName = "Thảo My"
                    ),
                    SessionPhotoRecord(
                        id = "SP-03",
                        sessionNumber = 5,
                        title = "Buổi 5: Điện di tế bào gốc",
                        date = "18/09/2026",
                        imageUrl = "https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?auto=format&fit=crop&w=600&q=80",
                        notes = "Da sáng bật tone nhẹ, hết cảm giác ngứa căng, mờ thâm vi điểm vùng má.",
                        technicianName = "Thảo My"
                    ),
                    SessionPhotoRecord(
                        id = "SP-04",
                        sessionNumber = 7,
                        title = "Buổi 7: Căng bóng Whitening",
                        date = "09/10/2026",
                        imageUrl = "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?auto=format&fit=crop&w=600&q=80",
                        notes = "Độ đàn hồi tăng rõ rệt, da căng bóng tự nhiên, sắc tố đỏ giảm 80%.",
                        technicianName = "Thảo My"
                    )
                )
            ),
            Client(
                id = "KH-8830",
                fullName = "Trần Bích Ngọc",
                phone = "0988 776 655",
                email = "bichngoc.tran@yahoo.com",
                vipTier = VipTier.VIP,
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
                beforeAfterComparisons = emptyList(),
                sessionPhotos = listOf(
                    SessionPhotoRecord(
                        id = "SP-11",
                        sessionNumber = 1,
                        title = "Buổi 1: Thăm khám ban đầu",
                        date = "01/09/2026",
                        imageUrl = "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?auto=format&fit=crop&w=600&q=80",
                        notes = "Da khô ráp, khóe mắt xuất hiện nếp nhăn tĩnh sâu khi cười.",
                        technicianName = "Hương Giang"
                    ),
                    SessionPhotoRecord(
                        id = "SP-12",
                        sessionNumber = 2,
                        title = "Buổi 2: Sau nâng cơ RF",
                        date = "22/09/2026",
                        imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80",
                        notes = "Cơ mặt săn chắc hơn, rãnh cười đầy lên khoảng 30%.",
                        technicianName = "Hương Giang"
                    )
                )
            ),
            Client(
                id = "KH-8831",
                fullName = "Lê Hoàng Yến",
                phone = "0909 112 233",
                email = "hoangyen.le@gmail.com",
                vipTier = VipTier.STANDARD,
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

    private val _clients = MutableStateFlow(
        if (_isRealUsageMode.value) emptyList() else demoClients
    )
    val clients: Flow<List<Client>> = _clients.asStateFlow()

    private val _selectedClientId = MutableStateFlow<String?>(null)
    val selectedClientId: Flow<String?> = _selectedClientId.asStateFlow()

    fun selectClient(id: String?) {
        _selectedClientId.value = id
    }

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
            _selectedClientId.value = null
        }
    }

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

    fun addTreatmentPackage(clientId: String, pkg: TreatmentPackage) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val updatedPackages = client.treatmentPackages + pkg
        updateClient(client.copy(treatmentPackages = updatedPackages))
    }

    fun addBeforeAfter(clientId: String, record: BeforeAfterComparison) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val updatedRecords = client.beforeAfterComparisons + record
        updateClient(client.copy(beforeAfterComparisons = updatedRecords))
    }

    fun addSessionPhoto(clientId: String, record: SessionPhotoRecord) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val filtered = client.sessionPhotos.filterNot { it.id == record.id }
        updateClient(client.copy(sessionPhotos = (filtered + record).sortedBy { it.sessionNumber }))
    }

    fun updateSessionPhoto(clientId: String, record: SessionPhotoRecord) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val updated = client.sessionPhotos.map { if (it.id == record.id) record else it }
        updateClient(client.copy(sessionPhotos = updated))
    }

    /**
     * Tự động trừ 1 buổi trong gói liệu trình của khách hàng khi hoàn thành lịch hẹn
     */
    fun deductPackageSessionForClient(clientName: String, clientPhone: String, serviceName: String): Boolean {
        val client = _clients.value.find {
            it.phone.replace(" ", "") == clientPhone.replace(" ", "") ||
            it.fullName.trim().equals(clientName.trim(), ignoreCase = true)
        } ?: return false

        // Tìm gói liệu trình phù hợp
        val matchedPackage = client.treatmentPackages.firstOrNull { pkg ->
            pkg.remainingSessions > 0 && (
                serviceName.contains(pkg.name, ignoreCase = true) ||
                pkg.name.contains(serviceName.replace(Regex("\\(Buổi.*?\\)"), "").trim(), ignoreCase = true) ||
                (serviceName.contains("Facial", ignoreCase = true) && pkg.name.contains("Facial", ignoreCase = true)) ||
                (serviceName.contains("Laser", ignoreCase = true) && pkg.name.contains("Laser", ignoreCase = true)) ||
                (serviceName.contains("Mụn", ignoreCase = true) && pkg.name.contains("Mụn", ignoreCase = true)) ||
                (serviceName.contains("Dưỡng Sinh", ignoreCase = true) && pkg.name.contains("Dưỡng Sinh", ignoreCase = true))
            )
        } ?: client.treatmentPackages.firstOrNull { it.remainingSessions > 0 }

        if (matchedPackage != null) {
            checkInSession(client.id, matchedPackage.id)
            return true
        }
        return false
    }


    // --- QUẢN LÝ LỊCH HẸN (CALENDAR) ---
    private val demoAppointments = listOf(
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

    private val _appointments = MutableStateFlow(
        if (_isRealUsageMode.value) emptyList() else demoAppointments
    )
    val appointments: Flow<List<Appointment>> = _appointments.asStateFlow()

    fun addAppointment(appointment: Appointment) {
        val current = _appointments.value.toMutableList()
        current.add(appointment)
        current.sortBy { it.startTime }
        _appointments.value = current
    }

    fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
        val current = _appointments.value.toMutableList()
        val index = current.indexOfFirst { it.id == appointmentId }
        if (index != -1) {
            val appt = current[index]
            current[index] = appt.copy(status = status)
            _appointments.value = current

            // Khi bấm hoàn thành các gói trừ dịch vụ thì tự trừ buổi của khách hàng đi
            if (status == AppointmentStatus.COMPLETED) {
                deductPackageSessionForClient(appt.clientName, appt.clientPhone, appt.serviceName)
            }
        }
    }

    fun deleteAppointment(appointmentId: String) {
        val current = _appointments.value.toMutableList()
        current.removeAll { it.id == appointmentId }
        _appointments.value = current
    }

    // --- LỊCH SỬ GIAO DỊCH & HÓA ĐƠN ---
    private val demoTransactions = listOf(
        InvoiceTransactionRecord(
                id = "TX-01",
                invoiceCode = "SPA8829",
                clientName = "Nguyễn Thu Hà",
                clientPhone = "0912 345 678",
                serviceName = "Whitening Facial Cao Cấp (Buổi 8/10)",
                technicianName = "Thảo My",
                time = "09:30",
                date = "16/10/2026",
                amount = 2025000L,
                paymentMethod = "VietQR Chuyển Khoản",
                isPaid = true
            ),
            InvoiceTransactionRecord(
                id = "TX-02",
                invoiceCode = "SPA102",
                clientName = "Trần Bích Ngọc",
                clientPhone = "0988 776 655",
                serviceName = "Massage Body Đá Nóng Himalaya 90p",
                technicianName = "Hương Giang",
                time = "10:00",
                date = "16/10/2026",
                amount = 850000L,
                paymentMethod = "VietQR Chuyển Khoản",
                isPaid = true
            ),
            InvoiceTransactionRecord(
                id = "TX-03",
                invoiceCode = "SPA103",
                clientName = "Lê Hoàng Yến",
                clientPhone = "0909 112 233",
                serviceName = "Chăm Sóc Da Chuyên Sâu Detox O2",
                technicianName = "Lan Phương",
                time = "13:30",
                date = "16/10/2026",
                amount = 650000L,
                paymentMethod = "VietQR Chuyển Khoản",
                isPaid = false
            ),
            InvoiceTransactionRecord(
                id = "TX-04",
                invoiceCode = "SPA104",
                clientName = "Phạm Mai Anh",
                clientPhone = "0933 456 789",
                serviceName = "Triệt Lông Diode Laser Trẻ Hóa",
                technicianName = "Thảo My",
                time = "14:00",
                date = "16/10/2026",
                amount = 500000L,
                paymentMethod = "Tiền mặt",
                isPaid = false
            ),
            InvoiceTransactionRecord(
                id = "TX-05",
                invoiceCode = "SPA105",
                clientName = "Vũ Thu Trang",
                clientPhone = "0977 123 456",
                serviceName = "Gội Đầu Dưỡng Sinh Thảo Dược 60p",
                technicianName = "Hương Giang",
                time = "15:30",
                date = "16/10/2026",
                amount = 350000L,
                paymentMethod = "VietQR Chuyển Khoản",
                isPaid = false
            )
        )

    private val _transactions = MutableStateFlow(
        if (_isRealUsageMode.value) emptyList() else demoTransactions
    )
    val transactions: Flow<List<InvoiceTransactionRecord>> = _transactions.asStateFlow()

    fun getTransactionsForDate(dateStr: String): List<InvoiceTransactionRecord> {
        return _transactions.value.filter { it.date == dateStr }
    }

    /**
     * Bắt đầu sử dụng thực tế: Xóa toàn bộ dữ liệu mẫu (khách hàng, lịch hẹn, giao dịch)
     */
    fun startRealUsage() {
        prefs?.edit()?.putBoolean("key_is_real_usage", true)?.apply()
        _isRealUsageMode.value = true
        _clients.value = emptyList()
        _selectedClientId.value = null
        _appointments.value = emptyList()
        _transactions.value = emptyList()
    }

    /**
     * Khôi phục dữ liệu mẫu thử nghiệm ban đầu
     */
    fun restoreDemoData() {
        prefs?.edit()?.putBoolean("key_is_real_usage", false)?.apply()
        _isRealUsageMode.value = false
        _clients.value = demoClients
        _appointments.value = demoAppointments
        _transactions.value = demoTransactions
    }

    // --- THU NGÂN & TẠO MÃ VIETQR (LẤY TÀI KHOẢN TỪ STORE PROFILE) ---
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
            selectedBank = SupportedBanks[0], // Vietcombank
            accountNumber = "1023456789",
            accountHolder = "SPA CARE WELLNESS CENTER",
            defaultMemo = "SPACARE",
            appointmentId = "APT-101"
        )
    )
    val currentInvoice: Flow<CheckoutInvoice> = _currentInvoice.asStateFlow()

    fun updateBank(bank: BankOption, accountNumber: String, accountHolder: String, defaultMemo: String = "SPACARE") {
        _currentInvoice.value = _currentInvoice.value.copy(
            selectedBank = bank,
            accountNumber = accountNumber,
            accountHolder = accountHolder,
            defaultMemo = defaultMemo
        )
        // Đồng thời cập nhật vào cấu hình cửa hàng
        _storeProfile.value = _storeProfile.value.copy(
            selectedBank = bank,
            accountNumber = accountNumber,
            accountHolder = accountHolder,
            defaultMemo = defaultMemo
        )
    }

    fun markInvoiceAsPaid() {
        val current = _currentInvoice.value
        val now = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val timeOnly = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val dateOnly = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        _currentInvoice.value = current.copy(
            paymentStatus = PaymentStatus.PAYMENT_SUCCESS,
            transactionRef = "CK-${System.currentTimeMillis().toString().takeLast(6)}",
            paidAt = now
        )

        // Các lịch hẹn đang làm, đã hẹn sau khi chuyển sang thanh toán và xác nhận nhận tiền thì tự động update thành hoàn thành
        current.appointmentId?.let { apptId ->
            updateAppointmentStatus(apptId, AppointmentStatus.COMPLETED)
        } ?: run {
            val matchedAppt = _appointments.value.find {
                it.clientName == current.clientName && it.date == dateOnly &&
                it.status != AppointmentStatus.COMPLETED && it.status != AppointmentStatus.CANCELLED
            }
            if (matchedAppt != null) {
                updateAppointmentStatus(matchedAppt.id, AppointmentStatus.COMPLETED)
            }
        }

        // Lưu vào lịch sử giao dịch hóa đơn
        val newTx = InvoiceTransactionRecord(
            id = "TX-${System.currentTimeMillis()}",
            invoiceCode = current.invoiceCode,
            clientName = current.clientName,
            clientPhone = current.clientPhone,
            serviceName = current.items.joinToString(", ") { it.name },
            technicianName = "Thảo My",
            time = timeOnly,
            date = dateOnly,
            amount = current.finalTotal,
            paymentMethod = "VietQR Chuyển Khoản",
            isPaid = true
        )
        _transactions.value = listOf(newTx) + _transactions.value
    }

    fun resetInvoice() {
        val code = "SPA" + (1000..9999).random()
        val store = _storeProfile.value
        _currentInvoice.value = CheckoutInvoice(
            invoiceId = "INV-${System.currentTimeMillis()}",
            invoiceCode = code,
            clientName = "Khách Hàng Mới",
            clientPhone = "0900 000 000",
            items = listOf(
                OrderItem("SRV-NEW", "Dịch vụ Spa & Trị liệu thư giãn", ItemType.SERVICE, 1, 500000L)
            ),
            discountAmount = 0L,
            paymentStatus = PaymentStatus.WAITING_FOR_PAYMENT,
            selectedBank = store.selectedBank,
            accountNumber = store.accountNumber,
            accountHolder = store.accountHolder,
            defaultMemo = store.defaultMemo,
            appointmentId = null
        )
    }

    fun createInvoiceFromAppointment(appointment: Appointment) {
        val code = "SPA" + (1000..9999).random()
        val store = _storeProfile.value
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
            paymentStatus = PaymentStatus.WAITING_FOR_PAYMENT,
            selectedBank = store.selectedBank,
            accountNumber = store.accountNumber,
            accountHolder = store.accountHolder,
            defaultMemo = store.defaultMemo,
            appointmentId = appointment.id
        )
    }

    // --- BÁO CÁO DOANH THU ĐỘNG THEO NGÀY ---
    fun getMetricsForDate(dateStr: String): DashboardMetrics {
        val dayAppts = _appointments.value.filter { it.date == dateStr }
        val bookingCount = dayAppts.size
        val dayTxs = _transactions.value.filter { it.date == dateStr && it.isPaid }

        val dayRevenue = if (_isRealUsageMode.value) {
            if (dayTxs.isNotEmpty()) dayTxs.sumOf { it.amount }
            else dayAppts.filter { it.status == AppointmentStatus.COMPLETED }.sumOf { it.price }
        } else {
            if (dayAppts.isEmpty()) 0L else dayAppts.sumOf { it.price } + 8200000L
        }

        val totalPaidRevenue = _transactions.value.filter { it.isPaid }.sumOf { it.amount }

        return DashboardMetrics(
            todayRevenue = dayRevenue,
            todayGrowthPercent = if (dayRevenue > 0) 14.8f else 0f,
            weekRevenue = if (_isRealUsageMode.value) totalPaidRevenue else 112400000L,
            monthRevenue = if (_isRealUsageMode.value) totalPaidRevenue else 465800000L,
            totalBookingsToday = bookingCount,
            roomOccupancyRate = if (bookingCount > 0) (bookingCount * 17.5f).coerceAtMost(100f) else 0f,
            topServices = if (_isRealUsageMode.value && dayAppts.isEmpty() && dayTxs.isEmpty()) {
                emptyList()
            } else {
                listOf(
                    TopServiceMetric("Liệu Trình Căng Bóng Whitening Facial", 42, 63000000L, 38f),
                    TopServiceMetric("Gội Đầu Dưỡng Sinh Thảo Dược", 68, 27200000L, 22f),
                    TopServiceMetric("Triệt Lông Công Nghệ Diode Laser", 28, 42000000L, 25f),
                    TopServiceMetric("Massage Body Đá Nóng Himalaya", 18, 15300000L, 15f)
                )
            },
            revenueDistribution = if (_isRealUsageMode.value && totalPaidRevenue == 0L && dayRevenue == 0L) {
                RevenueDistribution(0L, 0L, 0L)
            } else {
                RevenueDistribution(
                    treatmentPackages = 265000000L,
                    singleServices = 120800000L,
                    retailProducts = 80000000L
                )
            },
            topPerformers = if (_isRealUsageMode.value && dayAppts.isEmpty() && dayTxs.isEmpty()) {
                emptyList()
            } else {
                listOf(
                    StaffPerformance("ST-01", "Thảo My", "Chuyên Viên Trị Liệu", 46, 32500000L, 4.95f),
                    StaffPerformance("ST-02", "Hương Giang", "Kỹ Thuật Viên Dưỡng Sinh", 52, 24800000L, 4.90f),
                    StaffPerformance("ST-03", "Lan Phương", "Chuyên Viên Laser & Phục Hồi", 38, 28100000L, 4.88f)
                )
            }
        )
    }
}
