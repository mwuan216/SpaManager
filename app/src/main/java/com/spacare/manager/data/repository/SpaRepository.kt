package com.spacare.manager.data.repository

import android.content.Context
import com.spacare.manager.data.backup.BackupManager
import com.spacare.manager.data.backup.BackupStats
import com.spacare.manager.data.local.db.SpaDatabase
import com.spacare.manager.data.local.db.entity.*
import com.spacare.manager.domain.model.*
import com.spacare.manager.core.notification.AppointmentReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SpaRepository(context: Context? = null) {

    private val appContext = context?.applicationContext
    private val database: SpaDatabase? = context?.let { SpaDatabase.getInstance(it) }
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val prefs = context?.applicationContext?.getSharedPreferences(
        "spacare_usage_mode_prefs",
        Context.MODE_PRIVATE
    )

    private val _isRealUsageMode = MutableStateFlow(
        prefs?.getBoolean("key_is_real_usage", false) ?: false
    )
    val isRealUsageMode: Flow<Boolean> = _isRealUsageMode.asStateFlow()

    // --- CẤU HÌNH CỬA HÀNG & TÀI KHOẢN NGÂN HÀNG SPA ---
    private val defaultStoreProfile = StoreProfile(
        name = "Văn Định Skinlab",
        hotline = "1900 6868",
        address = "128 Phan Xích Long, P. 2, Q. Phú Nhuận, TP.HCM",
        selectedBank = SupportedBanks[0], // Vietcombank làm gợi ý ban đầu
        accountNumber = "1023456789",
        accountHolder = "VAN DINH SKINLAB",
        defaultMemo = "VANDINH"
    )

    private val _storeProfile = MutableStateFlow(defaultStoreProfile)
    val storeProfile: Flow<StoreProfile> = _storeProfile.asStateFlow()

    fun updateStoreProfile(profile: StoreProfile) {
        _storeProfile.value = profile
        // Đồng bộ ngay sang hóa đơn thu ngân hiện tại
        _currentInvoice.value = _currentInvoice.value?.copy(
            selectedBank = profile.selectedBank,
            accountNumber = profile.accountNumber,
            accountHolder = profile.accountHolder,
            defaultMemo = profile.defaultMemo
        )
        repositoryScope.launch {
            database?.storeProfileDao()?.insertOrUpdate(StoreProfileEntity.fromDomain(profile))
        }
    }

    // --- QUẢN LÝ MENU GÓI LIỆU TRÌNH ---
    private val defaultCatalogPackages = listOf(
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

    private val _catalogPackages = MutableStateFlow(
        if (_isRealUsageMode.value) emptyList() else defaultCatalogPackages
    )
    val catalogPackages: Flow<List<SpaServicePackage>> = _catalogPackages.asStateFlow()

    fun addCatalogPackage(pkg: SpaServicePackage) {
        _catalogPackages.value = listOf(pkg) + _catalogPackages.value
        repositoryScope.launch {
            database?.catalogPackageDao()?.insert(CatalogPackageEntity.fromDomain(pkg))
        }
    }

    fun updateCatalogPackage(pkg: SpaServicePackage) {
        _catalogPackages.value = _catalogPackages.value.map { if (it.id == pkg.id) pkg else it }
        repositoryScope.launch {
            database?.catalogPackageDao()?.update(CatalogPackageEntity.fromDomain(pkg))
        }
    }

    fun deleteCatalogPackage(packageId: String) {
        _catalogPackages.value = _catalogPackages.value.filterNot { it.id == packageId }
        repositoryScope.launch {
            database?.catalogPackageDao()?.deleteById(packageId)
        }
    }

    // --- QUẢN LÝ ĐỘI NGŨ KỸ THUẬT VIÊN ---
    private val defaultTechnicians = listOf(
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

    private val _technicians = MutableStateFlow(
        if (_isRealUsageMode.value) emptyList() else defaultTechnicians
    )
    val technicians: Flow<List<TechnicianStaff>> = _technicians.asStateFlow()

    fun addTechnician(tech: TechnicianStaff) {
        _technicians.value = _technicians.value + tech
        repositoryScope.launch {
            database?.technicianDao()?.insert(TechnicianEntity.fromDomain(tech))
        }
    }

    fun updateTechnician(tech: TechnicianStaff) {
        _technicians.value = _technicians.value.map { if (it.id == tech.id) tech else it }
        repositoryScope.launch {
            database?.technicianDao()?.update(TechnicianEntity.fromDomain(tech))
        }
    }

    fun deleteTechnician(id: String) {
        _technicians.value = _technicians.value.filterNot { it.id == id }
        repositoryScope.launch {
            database?.technicianDao()?.deleteById(id)
        }
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
                        assignedTherapist = "KTV. Thảo My",
                        intervalDays = 5,
                        lastSessionDate = "17/10/2026",
                        nextRecommendedDate = "24/10/2026"
                    ),
                    TreatmentPackage(
                        id = "PKG-02",
                        name = "Triệt Lông Trẻ Hóa Diode Laser Master",
                        totalSessions = 8,
                        usedSessions = 6,
                        remainingSessions = 2,
                        startDate = "01/06/2026",
                        expiryDate = "30/11/2026",
                        assignedTherapist = "KTV. Lan Phương",
                        intervalDays = 10,
                        lastSessionDate = "15/10/2026",
                        nextRecommendedDate = "25/10/2026"
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
            ),
            Client(
                id = "KH-8833",
                fullName = "Phạm Mai Anh",
                phone = "0933 456 789",
                email = "maianh.pham@gmail.com",
                vipTier = VipTier.STANDARD,
                skinType = "Da nhạy cảm, nang lông dày sẫm màu",
                skinConditionNotes = "Đang trong liệu trình triệt lông Diode Laser công nghệ cao, kết hợp trẻ hóa làm sáng vùng da dưới cánh tay.",
                allergyAlerts = listOf("Dị ứng gel bôi trơn gốc sáp ong"),
                contraindications = listOf("Không tắm nước quá nóng trong vòng 24h sau bắn laser"),
                treatmentPackages = listOf(
                    TreatmentPackage(
                        id = "PKG-05",
                        name = "Triệt Lông Diode Laser Trẻ Hóa",
                        totalSessions = 10,
                        usedSessions = 3,
                        remainingSessions = 7,
                        startDate = "10/08/2026",
                        expiryDate = "10/08/2027",
                        assignedTherapist = "KTV. Thảo My"
                    )
                ),
                serviceHistory = emptyList(),
                beforeAfterComparisons = emptyList()
            ),
            Client(
                id = "KH-8834",
                fullName = "Đặng Kim Ngân",
                phone = "0938 222 333",
                email = "kimngan.dang@outlook.com",
                vipTier = VipTier.VIP,
                skinType = "Da xỉn màu, thiếu sức sống do thức khuya",
                skinConditionNotes = "Cần tăng cường cấy tinh chất Nano Collagen tươi phục hồi độ sáng và trẻ hóa tầng sâu biểu bì.",
                allergyAlerts = emptyList(),
                contraindications = emptyList(),
                treatmentPackages = listOf(
                    TreatmentPackage(
                        id = "PKG-06",
                        name = "Cấy Trắng Da Nano Collagen Tươi",
                        totalSessions = 5,
                        usedSessions = 1,
                        remainingSessions = 4,
                        startDate = "01/10/2026",
                        expiryDate = "01/04/2027",
                        assignedTherapist = "KTV. Lan Phương"
                    )
                ),
                serviceHistory = emptyList(),
                beforeAfterComparisons = emptyList()
            ),
            Client(
                id = "KH-8835",
                fullName = "Hoàng Yến Nhi",
                phone = "0903 888 999",
                email = "yennhi.hoang@gmail.com",
                vipTier = VipTier.STANDARD,
                skinType = "Da dầu mụn ẩn vùng trán và cằm",
                skinConditionNotes = "Da nhiễm bụi mịn đô thị, độc tố chì từ mỹ phẩm trang điểm. Cần thải độc chì định kỳ 2 tuần/lần.",
                allergyAlerts = listOf("Kích ứng với cồn mỹ phẩm nồng độ cao"),
                contraindications = emptyList(),
                treatmentPackages = emptyList(),
                serviceHistory = emptyList(),
                beforeAfterComparisons = emptyList()
            ),
            Client(
                id = "KH-8836",
                fullName = "Ngô Bảo Châu",
                phone = "0918 666 777",
                email = "baochau.ngo@gmail.com",
                vipTier = VipTier.VIP,
                skinType = "Da khô toàn thân, cháy nắng nhẹ sau du lịch biển",
                skinConditionNotes = "Liệu trình tắm trắng phi thuyền collagen kết hợp ủ bùn khoáng thiên nhiên.",
                allergyAlerts = emptyList(),
                contraindications = emptyList(),
                treatmentPackages = listOf(
                    TreatmentPackage(
                        id = "PKG-07",
                        name = "Tắm Trắng Phi Thuyền Hoàng Gia",
                        totalSessions = 4,
                        usedSessions = 2,
                        remainingSessions = 2,
                        startDate = "15/09/2026",
                        expiryDate = "15/12/2026",
                        assignedTherapist = "KTV. Lan Phương"
                    )
                ),
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
        repositoryScope.launch {
            database?.clientDao()?.insert(ClientEntity.fromDomain(client))
        }
    }

    fun updateClient(client: Client) {
        val currentList = _clients.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == client.id }
        if (index != -1) {
            currentList[index] = client
            _clients.value = currentList
        }
        repositoryScope.launch {
            database?.clientDao()?.insert(ClientEntity.fromDomain(client))
        }
    }

    fun deleteClient(clientId: String) {
        val currentList = _clients.value.toMutableList()
        currentList.removeAll { it.id == clientId }
        _clients.value = currentList
        if (_selectedClientId.value == clientId) {
            _selectedClientId.value = null
        }
        repositoryScope.launch {
            database?.clientDao()?.deleteById(clientId)
        }
    }

    fun checkInSession(
        clientId: String,
        packageId: String,
        intervalDays: Int = 5,
        customNextDate: String? = null
    ) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val today = Date()
        val todayDateStr = dateOnlyFormat.format(today)

        val updatedPackages = client.treatmentPackages.map { pkg ->
            if (pkg.id == packageId && pkg.remainingSessions > 0) {
                val effectiveInterval = if (intervalDays > 0) intervalDays else pkg.intervalDays
                val calculatedNextDate = customNextDate ?: run {
                    val cal = Calendar.getInstance().apply {
                        time = today
                        add(Calendar.DAY_OF_YEAR, effectiveInterval)
                    }
                    dateOnlyFormat.format(cal.time)
                }
                val newRemaining = pkg.remainingSessions - 1
                pkg.copy(
                    usedSessions = pkg.usedSessions + 1,
                    remainingSessions = newRemaining,
                    intervalDays = effectiveInterval,
                    lastSessionDate = todayDateStr,
                    nextRecommendedDate = if (newRemaining > 0) calculatedNextDate else null
                )
            } else pkg
        }

        val updatedHistory = client.serviceHistory.toMutableList()
        val pkg = client.treatmentPackages.find { it.id == packageId }
        updatedHistory.add(
            0,
            ServiceRecord(
                id = "SRV-${System.currentTimeMillis().toString().takeLast(4)}",
                date = dateFormat.format(today),
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
        val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val effectiveNextDate = pkg.nextRecommendedDate ?: run {
            try {
                val cal = Calendar.getInstance()
                val parsedStart = dateOnlyFormat.parse(pkg.startDate)
                if (parsedStart != null) cal.time = parsedStart
                cal.add(Calendar.DAY_OF_YEAR, pkg.intervalDays.coerceAtLeast(1))
                dateOnlyFormat.format(cal.time)
            } catch (e: Exception) {
                null
            }
        }
        val finalPkg = pkg.copy(nextRecommendedDate = effectiveNextDate)
        val updatedPackages = client.treatmentPackages + finalPkg
        updateClient(client.copy(treatmentPackages = updatedPackages))
    }

    /**
     * Cập nhật chu kỳ điều trị và ngày hẹn tiếp theo của gói liệu trình (cài đặt thủ công)
     */
    fun updatePackageCycle(
        clientId: String,
        packageId: String,
        intervalDays: Int,
        nextRecommendedDate: String?
    ) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val updatedPackages = client.treatmentPackages.map { pkg ->
            if (pkg.id == packageId) {
                pkg.copy(
                    intervalDays = intervalDays.coerceAtLeast(1),
                    nextRecommendedDate = nextRecommendedDate
                )
            } else pkg
        }
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

    fun deleteTreatmentPackage(clientId: String, packageId: String) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val updatedPackages = client.treatmentPackages.filterNot { it.id == packageId }
        updateClient(client.copy(treatmentPackages = updatedPackages))
    }

    fun deleteServiceRecord(clientId: String, serviceRecordId: String) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val updatedHistory = client.serviceHistory.filterNot { it.id == serviceRecordId }
        updateClient(client.copy(serviceHistory = updatedHistory))
    }

    fun deleteSessionPhoto(clientId: String, photoId: String) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val updatedPhotos = client.sessionPhotos.filterNot { it.id == photoId }
        updateClient(client.copy(sessionPhotos = updatedPhotos))
    }

    fun deleteBeforeAfter(clientId: String, comparisonId: String) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val updatedRecords = client.beforeAfterComparisons.filterNot { it.id == comparisonId }
        updateClient(client.copy(beforeAfterComparisons = updatedRecords))
    }

    fun addServiceRecord(clientId: String, record: ServiceRecord) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val updatedHistory = listOf(record) + client.serviceHistory
        updateClient(client.copy(serviceHistory = updatedHistory))
    }

    /**
     * Tự động ghi nhận dịch vụ đã làm vào hồ sơ khách hàng khi hoàn thành lịch hẹn / thanh toán
     */
    fun recordServiceUsage(
        clientName: String,
        clientPhone: String,
        serviceName: String,
        technicianName: String,
        room: String = "Phòng Dịch Vụ",
        cost: Long = 0L,
        feedback: String? = null,
        dateStr: String? = null
    ) {
        val cleanPhone = clientPhone.replace(" ", "").trim()
        val client = _clients.value.find {
            (cleanPhone.isNotBlank() && it.phone.replace(" ", "").trim() == cleanPhone) ||
            it.fullName.trim().equals(clientName.trim(), ignoreCase = true)
        }
        val effectiveDate = dateStr ?: SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault()).format(Date())
        val newRecord = ServiceRecord(
            id = "SRV-${System.currentTimeMillis()}-${(100..999).random()}",
            date = effectiveDate,
            serviceName = serviceName,
            technicianName = technicianName.ifBlank { "Chuyên viên" },
            room = room.ifBlank { "Phòng Dịch Vụ" },
            cost = cost,
            feedback = feedback ?: "Khách hoàn thành dịch vụ."
        )

        if (client != null) {
            val alreadyExists = client.serviceHistory.any {
                it.serviceName == serviceName && (it.date == effectiveDate || (effectiveDate.length >= 10 && it.date.startsWith(effectiveDate.take(10))))
            }
            if (!alreadyExists) {
                val updatedHistory = listOf(newRecord) + client.serviceHistory
                updateClient(client.copy(serviceHistory = updatedHistory))
            }
        } else if (clientName.isNotBlank()) {
            val newClient = Client(
                id = "CLI-${System.currentTimeMillis()}",
                fullName = clientName.trim(),
                phone = clientPhone.trim(),
                email = "",
                vipTier = VipTier.STANDARD,
                skinType = "Chưa phân loại",
                skinConditionNotes = "Khách hàng được tạo tự động khi hoàn thành dịch vụ.",
                allergyAlerts = emptyList(),
                contraindications = emptyList(),
                treatmentPackages = emptyList(),
                serviceHistory = listOf(newRecord),
                beforeAfterComparisons = emptyList(),
                sessionPhotos = emptyList()
            )
            addClient(newClient)
        }
    }

    /**
     * Tự động trừ 1 buổi trong gói liệu trình của khách hàng khi hoàn thành lịch hẹn
     */
    fun deductPackageSessionForClient(clientName: String, clientPhone: String, serviceName: String): Boolean {
        val client = _clients.value.find {
            it.phone.replace(" ", "") == clientPhone.replace(" ", "") ||
            it.fullName.trim().equals(clientName.trim(), ignoreCase = true)
        } ?: return false

        // Tìm gói liệu trình phù hợp với tên dịch vụ
        val matchedPackage = client.treatmentPackages.firstOrNull { pkg ->
            pkg.remainingSessions > 0 && (
                serviceName.contains(pkg.name, ignoreCase = true) ||
                pkg.name.contains(serviceName.replace(Regex("\\(Buổi.*?\\)"), "").trim(), ignoreCase = true) ||
                (serviceName.contains("Facial", ignoreCase = true) && pkg.name.contains("Facial", ignoreCase = true)) ||
                (serviceName.contains("Laser", ignoreCase = true) && pkg.name.contains("Laser", ignoreCase = true)) ||
                (serviceName.contains("Mụn", ignoreCase = true) && pkg.name.contains("Mụn", ignoreCase = true)) ||
                (serviceName.contains("Dưỡng Sinh", ignoreCase = true) && pkg.name.contains("Dưỡng Sinh", ignoreCase = true))
            )
        }

        if (matchedPackage != null) {
            checkInSession(client.id, matchedPackage.id)
            return true
        }
        return false
    }


    // --- QUẢN LÝ LỊCH HẸN (CALENDAR) ---
    private fun buildDemoAppointments(): List<Appointment> {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, 2) // +1 from today
        val tomorrowStr = sdf.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, 1) // +2 from today
        val dayAfterTomorrowStr = sdf.format(cal.time)

        val list = mutableListOf<Appointment>()

        // 1. LỊCH HẸN HÔM NAY (TODAY) - Đa dạng trạng thái để test tất cả các luồng xử lý
        list.add(
            Appointment(
                id = "APT-TODAY-01",
                clientName = "Vũ Thu Trang",
                clientPhone = "0977 123 456",
                serviceName = "Gội Đầu Dưỡng Sinh Thảo Dược 60p",
                durationMinutes = 60,
                staffName = "Hương Giang",
                roomName = "Phòng Dưỡng Sinh Sen",
                startTime = "08:30",
                endTime = "09:30",
                date = todayStr,
                status = AppointmentStatus.COMPLETED,
                price = 350000L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-TODAY-02",
                clientName = "Nguyễn Thu Hà",
                clientPhone = "0912 345 678",
                serviceName = "Whitening Facial (Buổi 8/10)",
                durationMinutes = 90,
                staffName = "Thảo My",
                roomName = "Phòng VIP 02",
                startTime = "09:30",
                endTime = "11:00",
                date = todayStr,
                status = AppointmentStatus.IN_SERVICE,
                price = 0L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-TODAY-03",
                clientName = "Trần Bích Ngọc",
                clientPhone = "0988 776 655",
                serviceName = "Massage Body Đá Nóng Himalaya 90p",
                durationMinutes = 90,
                staffName = "Hương Giang",
                roomName = "Phòng Trị Liệu Sen",
                startTime = "10:00",
                endTime = "11:30",
                date = todayStr,
                status = AppointmentStatus.IN_SERVICE,
                price = 850000L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-TODAY-04",
                clientName = "Đặng Kim Ngân",
                clientPhone = "0938 222 333",
                serviceName = "Cấy Trắng Da Nano Collagen Tươi",
                durationMinutes = 60,
                staffName = "Lan Phương",
                roomName = "Phòng VIP 01",
                startTime = "11:30",
                endTime = "12:30",
                date = todayStr,
                status = AppointmentStatus.CONFIRMED,
                price = 1200000L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-TODAY-05",
                clientName = "Lê Hoàng Yến",
                clientPhone = "0909 112 233",
                serviceName = "Chăm Sóc Da Chuyên Sâu Detox O2",
                durationMinutes = 75,
                staffName = "Lan Phương",
                roomName = "Phòng Chăm Sóc 03",
                startTime = "13:30",
                endTime = "14:45",
                date = todayStr,
                status = AppointmentStatus.CONFIRMED,
                price = 650000L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-TODAY-06",
                clientName = "Phạm Mai Anh",
                clientPhone = "0933 456 789",
                serviceName = "Triệt Lông Diode Laser Trẻ Hóa",
                durationMinutes = 45,
                staffName = "Thảo My",
                roomName = "Phòng Laser 01",
                startTime = "14:00",
                endTime = "14:45",
                date = todayStr,
                status = AppointmentStatus.PENDING,
                price = 500000L,
                reminderSent = false
            )
        )
        list.add(
            Appointment(
                id = "APT-TODAY-07",
                clientName = "Trần Bích Ngọc",
                clientPhone = "0988 776 655",
                serviceName = "Trẻ Hóa Nâng Cơ RF Hifu Collagen 360 (Buổi 3/5)",
                durationMinutes = 60,
                staffName = "Hương Giang",
                roomName = "Phòng Trị Liệu Sen",
                startTime = "15:30",
                endTime = "16:30",
                date = todayStr,
                status = AppointmentStatus.CONFIRMED,
                price = 0L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-TODAY-08",
                clientName = "Vũ Thu Trang",
                clientPhone = "0977 123 456",
                serviceName = "Massage Cổ Vai Gáy Dược Liệu",
                durationMinutes = 60,
                staffName = "Hương Giang",
                roomName = "Phòng Dưỡng Sinh Sen",
                startTime = "16:30",
                endTime = "17:30",
                date = todayStr,
                status = AppointmentStatus.PENDING,
                price = 300000L,
                reminderSent = false
            )
        )
        list.add(
            Appointment(
                id = "APT-TODAY-09",
                clientName = "Hoàng Yến Nhi",
                clientPhone = "0903 888 999",
                serviceName = "Thải Độc Chì Chăm Sóc Da Căng Bóng",
                durationMinutes = 75,
                staffName = "Thảo My",
                roomName = "Phòng Chăm Sóc 03",
                startTime = "18:00",
                endTime = "19:15",
                date = todayStr,
                status = AppointmentStatus.CANCELLED,
                price = 450000L,
                reminderSent = false
            )
        )

        // 2. LỊCH HẸN NGÀY MAI (TOMORROW)
        list.add(
            Appointment(
                id = "APT-TOM-01",
                clientName = "Nguyễn Thu Hà",
                clientPhone = "0912 345 678",
                serviceName = "Liệu Trình Phục Hồi Hàng Rào Bảo Vệ Da",
                durationMinutes = 90,
                staffName = "Thảo My",
                roomName = "Phòng VIP 02",
                startTime = "09:00",
                endTime = "10:30",
                date = tomorrowStr,
                status = AppointmentStatus.CONFIRMED,
                price = 750000L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-TOM-02",
                clientName = "Vũ Thu Trang",
                clientPhone = "0977 123 456",
                serviceName = "Gội Đầu Dưỡng Sinh Thảo Dược 60p",
                durationMinutes = 60,
                staffName = "Hương Giang",
                roomName = "Phòng Dưỡng Sinh Sen",
                startTime = "10:30",
                endTime = "11:30",
                date = tomorrowStr,
                status = AppointmentStatus.PENDING,
                price = 350000L,
                reminderSent = false
            )
        )
        list.add(
            Appointment(
                id = "APT-TOM-03",
                clientName = "Lê Hoàng Yến",
                clientPhone = "0909 112 233",
                serviceName = "Trị Mụn Chuyên Sâu Detox O2 & Biolight (Buổi 6/6)",
                durationMinutes = 75,
                staffName = "Lan Phương",
                roomName = "Phòng Chăm Sóc 03",
                startTime = "13:30",
                endTime = "14:45",
                date = tomorrowStr,
                status = AppointmentStatus.CONFIRMED,
                price = 0L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-TOM-04",
                clientName = "Phạm Mai Anh",
                clientPhone = "0933 456 789",
                serviceName = "Triệt Lông Toàn Thân Diode Laser",
                durationMinutes = 60,
                staffName = "Thảo My",
                roomName = "Phòng Laser 01",
                startTime = "15:00",
                endTime = "16:00",
                date = tomorrowStr,
                status = AppointmentStatus.CONFIRMED,
                price = 1800000L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-TOM-05",
                clientName = "Trần Bích Ngọc",
                clientPhone = "0988 776 655",
                serviceName = "Massage Body Đá Nóng Himalaya 90p",
                durationMinutes = 90,
                staffName = "Hương Giang",
                roomName = "Phòng Trị Liệu Sen",
                startTime = "16:30",
                endTime = "18:00",
                date = tomorrowStr,
                status = AppointmentStatus.CONFIRMED,
                price = 850000L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-TOM-06",
                clientName = "Đặng Kim Ngân",
                clientPhone = "0938 222 333",
                serviceName = "Chăm Sóc Da Căng Bóng Thủy Điểm",
                durationMinutes = 60,
                staffName = "Lan Phương",
                roomName = "Phòng VIP 01",
                startTime = "18:30",
                endTime = "19:30",
                date = tomorrowStr,
                status = AppointmentStatus.PENDING,
                price = 950000L,
                reminderSent = false
            )
        )

        // 3. LỊCH HẸN HÔM QUA (YESTERDAY)
        list.add(
            Appointment(
                id = "APT-YES-01",
                clientName = "Nguyễn Thu Hà",
                clientPhone = "0912 345 678",
                serviceName = "Whitening Facial (Buổi 7/10)",
                durationMinutes = 90,
                staffName = "Thảo My",
                roomName = "Phòng VIP 02",
                startTime = "09:00",
                endTime = "10:30",
                date = yesterdayStr,
                status = AppointmentStatus.COMPLETED,
                price = 0L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-YES-02",
                clientName = "Trần Bích Ngọc",
                clientPhone = "0988 776 655",
                serviceName = "Massage Body Thư Giãn Tinh Dầu",
                durationMinutes = 90,
                staffName = "Hương Giang",
                roomName = "Phòng Trị Liệu Sen",
                startTime = "10:30",
                endTime = "12:00",
                date = yesterdayStr,
                status = AppointmentStatus.COMPLETED,
                price = 650000L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-YES-03",
                clientName = "Lê Hoàng Yến",
                clientPhone = "0909 112 233",
                serviceName = "Trị Mụn Chuyên Sâu Detox O2",
                durationMinutes = 75,
                staffName = "Lan Phương",
                roomName = "Phòng Chăm Sóc 03",
                startTime = "14:00",
                endTime = "15:15",
                date = yesterdayStr,
                status = AppointmentStatus.COMPLETED,
                price = 550000L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-YES-04",
                clientName = "Vũ Thu Trang",
                clientPhone = "0977 123 456",
                serviceName = "Gội Đầu Dưỡng Sinh Thảo Dược 60p",
                durationMinutes = 60,
                staffName = "Hương Giang",
                roomName = "Phòng Dưỡng Sinh Sen",
                startTime = "16:00",
                endTime = "17:00",
                date = yesterdayStr,
                status = AppointmentStatus.COMPLETED,
                price = 350000L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-YES-05",
                clientName = "Ngô Bảo Châu",
                clientPhone = "0918 666 777",
                serviceName = "Tắm Trắng Phi Thuyền Hoàng Gia",
                durationMinutes = 60,
                staffName = "Lan Phương",
                roomName = "Phòng VIP 01",
                startTime = "17:30",
                endTime = "18:30",
                date = yesterdayStr,
                status = AppointmentStatus.CANCELLED,
                price = 1500000L,
                reminderSent = false
            )
        )

        // 4. LỊCH HẸN NGÀY KIA / NGÀY MỐT (DAY AFTER TOMORROW)
        list.add(
            Appointment(
                id = "APT-DAT-01",
                clientName = "Vũ Thu Trang",
                clientPhone = "0977 123 456",
                serviceName = "Chăm Sóc Da Căng Bóng Thủy Điểm",
                durationMinutes = 90,
                staffName = "Thảo My",
                roomName = "Phòng VIP 02",
                startTime = "09:30",
                endTime = "11:00",
                date = dayAfterTomorrowStr,
                status = AppointmentStatus.CONFIRMED,
                price = 950000L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-DAT-02",
                clientName = "Trần Bích Ngọc",
                clientPhone = "0988 776 655",
                serviceName = "Massage Body Đá Nóng Himalaya 90p",
                durationMinutes = 90,
                staffName = "Hương Giang",
                roomName = "Phòng Trị Liệu Sen",
                startTime = "14:00",
                endTime = "15:30",
                date = dayAfterTomorrowStr,
                status = AppointmentStatus.CONFIRMED,
                price = 850000L,
                reminderSent = true
            )
        )
        list.add(
            Appointment(
                id = "APT-DAT-03",
                clientName = "Đặng Kim Ngân",
                clientPhone = "0938 222 333",
                serviceName = "Cấy Trắng Da Nano Collagen Tươi",
                durationMinutes = 60,
                staffName = "Lan Phương",
                roomName = "Phòng VIP 01",
                startTime = "16:00",
                endTime = "17:00",
                date = dayAfterTomorrowStr,
                status = AppointmentStatus.PENDING,
                price = 1200000L,
                reminderSent = false
            )
        )

        // 5. NGÀY CỐ ĐỊNH "16/10/2026" (Nếu hôm nay không trùng với 16/10/2026)
        if (todayStr != "16/10/2026") {
            list.add(
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
                )
            )
            list.add(
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
                )
            )
            list.add(
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
                )
            )
            list.add(
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
                )
            )
            list.add(
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
        }

        return list
    }

    private val demoAppointments = buildDemoAppointments()

    private val _appointments = MutableStateFlow(
        if (_isRealUsageMode.value) emptyList() else demoAppointments
    )
    val appointments: Flow<List<Appointment>> = _appointments.asStateFlow()

    fun addAppointment(appointment: Appointment) {
        val current = _appointments.value.toMutableList()
        current.add(appointment)
        current.sortBy { it.startTime }
        _appointments.value = current
        repositoryScope.launch {
            database?.appointmentDao()?.insert(AppointmentEntity.fromDomain(appointment))
        }
        appContext?.let { ctx ->
            AppointmentReminderScheduler.scheduleReminder(ctx, appointment)
        }
    }

    fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
        val current = _appointments.value.toMutableList()
        val index = current.indexOfFirst { it.id == appointmentId }
        if (index != -1) {
            val appt = current[index]
            val updated = appt.copy(status = status)
            current[index] = updated
            _appointments.value = current
            repositoryScope.launch {
                database?.appointmentDao()?.insert(AppointmentEntity.fromDomain(updated))
            }

            // Khi bấm hoàn thành dịch vụ: Tự động trừ gói hoặc cập nhật vào Dịch Vụ Đã Làm của khách hàng
            if (status == AppointmentStatus.COMPLETED && !appt.isSessionDeducted) {
                var wasDeducted = false
                if (appt.price <= 0L) {
                    wasDeducted = deductPackageSessionForClient(appt.clientName, appt.clientPhone, appt.serviceName)
                }

                // Nếu không trừ được gói (hoặc là dịch vụ có phí), ghi nhận vào lịch sử dịch vụ của khách
                if (!wasDeducted) {
                    recordServiceUsage(
                        clientName = appt.clientName,
                        clientPhone = appt.clientPhone,
                        serviceName = appt.serviceName,
                        technicianName = appt.staffName,
                        room = appt.roomName,
                        cost = appt.price,
                        feedback = "Khách hoàn thành dịch vụ theo lịch hẹn.",
                        dateStr = "${appt.date} - ${appt.startTime}"
                    )
                }

                val marked = updated.copy(isSessionDeducted = true)
                current[index] = marked
                _appointments.value = current
                repositoryScope.launch {
                    database?.appointmentDao()?.insert(AppointmentEntity.fromDomain(marked))
                }
            }

            // Hủy nhắc hẹn nếu lịch đã hoàn thành hoặc đã hủy
            appContext?.let { ctx ->
                if (status == AppointmentStatus.CANCELLED || status == AppointmentStatus.COMPLETED) {
                    AppointmentReminderScheduler.cancelReminder(ctx, appointmentId)
                }
            }
        }
    }

    fun deleteAppointment(appointmentId: String) {
        val current = _appointments.value.toMutableList()
        current.removeAll { it.id == appointmentId }
        _appointments.value = current
        repositoryScope.launch {
            database?.appointmentDao()?.deleteById(appointmentId)
        }
        appContext?.let { ctx ->
            AppointmentReminderScheduler.cancelReminder(ctx, appointmentId)
        }
    }

    // --- LỊCH SỬ GIAO DỊCH & HÓA ĐƠN ---
    private fun buildDemoTransactions(): List<InvoiceTransactionRecord> {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        val list = mutableListOf<InvoiceTransactionRecord>()

        // 1. Giao dịch Hôm nay (TODAY)
        list.add(
            InvoiceTransactionRecord(
                id = "TX-TODAY-01",
                invoiceCode = "SPA-8832",
                clientName = "Vũ Thu Trang",
                clientPhone = "0977 123 456",
                serviceName = "Gội Đầu Dưỡng Sinh Thảo Dược 60p",
                technicianName = "Hương Giang",
                time = "08:30",
                date = todayStr,
                amount = 350000L,
                paymentMethod = "VietQR Chuyển Khoản",
                isPaid = true
            )
        )
        list.add(
            InvoiceTransactionRecord(
                id = "TX-TODAY-02",
                invoiceCode = "SPA-8829",
                clientName = "Nguyễn Thu Hà",
                clientPhone = "0912 345 678",
                serviceName = "Serum EGF Repair 30ml + Combo Dưỡng Ẩm",
                technicianName = "Thảo My",
                time = "09:30",
                date = todayStr,
                amount = 1600000L,
                paymentMethod = "VietQR Chuyển Khoản",
                isPaid = true
            )
        )
        list.add(
            InvoiceTransactionRecord(
                id = "TX-TODAY-03",
                invoiceCode = "SPA-8830",
                clientName = "Trần Bích Ngọc",
                clientPhone = "0988 776 655",
                serviceName = "Massage Body Đá Nóng Himalaya 90p",
                technicianName = "Hương Giang",
                time = "10:00",
                date = todayStr,
                amount = 850000L,
                paymentMethod = "VietQR Chuyển Khoản",
                isPaid = false
            )
        )
        list.add(
            InvoiceTransactionRecord(
                id = "TX-TODAY-04",
                invoiceCode = "SPA-8831",
                clientName = "Lê Hoàng Yến",
                clientPhone = "0909 112 233",
                serviceName = "Chăm Sóc Da Chuyên Sâu Detox O2",
                technicianName = "Lan Phương",
                time = "13:30",
                date = todayStr,
                amount = 650000L,
                paymentMethod = "VietQR Chuyển Khoản",
                isPaid = false
            )
        )

        // 2. Giao dịch Hôm qua (YESTERDAY)
        list.add(
            InvoiceTransactionRecord(
                id = "TX-YES-01",
                invoiceCode = "SPA-8820",
                clientName = "Trần Bích Ngọc",
                clientPhone = "0988 776 655",
                serviceName = "Massage Body Thư Giãn Tinh Dầu",
                technicianName = "Hương Giang",
                time = "10:30",
                date = yesterdayStr,
                amount = 650000L,
                paymentMethod = "VietQR Chuyển Khoản",
                isPaid = true
            )
        )
        list.add(
            InvoiceTransactionRecord(
                id = "TX-YES-02",
                invoiceCode = "SPA-8821",
                clientName = "Lê Hoàng Yến",
                clientPhone = "0909 112 233",
                serviceName = "Trị Mụn Chuyên Sâu Detox O2",
                technicianName = "Lan Phương",
                time = "14:00",
                date = yesterdayStr,
                amount = 550000L,
                paymentMethod = "VietQR Chuyển Khoản",
                isPaid = true
            )
        )
        list.add(
            InvoiceTransactionRecord(
                id = "TX-YES-03",
                invoiceCode = "SPA-8822",
                clientName = "Vũ Thu Trang",
                clientPhone = "0977 123 456",
                serviceName = "Gội Đầu Dưỡng Sinh Thảo Dược 60p",
                technicianName = "Hương Giang",
                time = "16:00",
                date = yesterdayStr,
                amount = 350000L,
                paymentMethod = "Tiền mặt",
                isPaid = true
            )
        )

        // 3. Dữ liệu ngày cố định 16/10/2026 (Nếu hôm nay khác 16/10/2026)
        if (todayStr != "16/10/2026") {
            list.add(
                InvoiceTransactionRecord(
                    id = "TX-01",
                    invoiceCode = "SPA8829",
                    clientName = "Nguyễn Thu Hà",
                    clientPhone = "0912 345 678",
                    serviceName = "Combo Whitening Facial + Mỹ phẩm EGF",
                    technicianName = "Thảo My",
                    time = "11:00",
                    date = "16/10/2026",
                    amount = 2025000L,
                    paymentMethod = "VietQR Chuyển Khoản",
                    isPaid = true
                )
            )
            list.add(
                InvoiceTransactionRecord(
                    id = "TX-02",
                    invoiceCode = "SPA102",
                    clientName = "Trần Bích Ngọc",
                    clientPhone = "0988 776 655",
                    serviceName = "Massage Body Đá Nóng Himalaya 90p",
                    technicianName = "Hương Giang",
                    time = "11:30",
                    date = "16/10/2026",
                    amount = 850000L,
                    paymentMethod = "VietQR Chuyển Khoản",
                    isPaid = true
                )
            )
            list.add(
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
                )
            )
            list.add(
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
                )
            )
            list.add(
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
        }

        return list
    }

    private val demoTransactions = buildDemoTransactions()

    private val _transactions = MutableStateFlow(
        if (_isRealUsageMode.value) emptyList() else demoTransactions
    )
    val transactions: Flow<List<InvoiceTransactionRecord>> = _transactions.asStateFlow()

    fun getTransactionsForDate(dateStr: String): List<InvoiceTransactionRecord> {
        return _transactions.value.filter { it.date == dateStr }
    }

    /**
     * Bắt đầu sử dụng thực tế: Xóa toàn bộ dữ liệu mẫu (khách hàng, lịch hẹn, giao dịch, gói dịch vụ, KTV)
     */
    fun startRealUsage() {
        prefs?.edit()?.putBoolean("key_is_real_usage", true)?.apply()
        _isRealUsageMode.value = true
        _clients.value = emptyList()
        _selectedClientId.value = null
        _appointments.value = emptyList()
        _transactions.value = emptyList()
        _catalogPackages.value = emptyList()
        _technicians.value = emptyList()
        _currentInvoice.value = null
        repositoryScope.launch {
            database?.clientDao()?.deleteAll()
            database?.appointmentDao()?.deleteAll()
            database?.transactionDao()?.deleteAll()
            database?.catalogPackageDao()?.deleteAll()
            database?.technicianDao()?.deleteAll()
        }
    }

    // --- THU NGÂN & TẠO MÃ VIETQR (LẤY TÀI KHOẢN TỪ STORE PROFILE) ---
    private val _currentInvoice = MutableStateFlow<CheckoutInvoice?>(
        if (_isRealUsageMode.value) null else CheckoutInvoice(
            invoiceId = "INV-20261016-01",
            invoiceCode = "SPA8829",
            clientName = "Nguyễn Thu Hà",
            clientPhone = "0912 345 678",
            items = listOf(
                OrderItem(
                    id = "ITEM-01",
                    name = "Whitening Facial Cao Cấp",
                    type = ItemType.SERVICE,
                    quantity = 1,
                    unitPrice = 650000L
                )
            ),
            discountAmount = 0L,
            tipAmount = 0L,
            paymentStatus = PaymentStatus.WAITING_FOR_PAYMENT,
            selectedBank = defaultStoreProfile.selectedBank,
            accountNumber = defaultStoreProfile.accountNumber,
            accountHolder = defaultStoreProfile.accountHolder,
            defaultMemo = defaultStoreProfile.defaultMemo,
            appointmentId = null
        )
    )
    val currentInvoice: Flow<CheckoutInvoice?> = _currentInvoice.asStateFlow()

    init {
        if (database != null) {
            repositoryScope.launch {
                val clientCount = database.clientDao().getCount()
                val isReal = _isRealUsageMode.value
                if (clientCount == 0 && !isReal) {
                    // Cài đặt lần đầu: Nạp dữ liệu mẫu vào Room Database để trải nghiệm UI
                    database.clientDao().insertAll(demoClients.map { ClientEntity.fromDomain(it) })
                    database.appointmentDao().insertAll(demoAppointments.map { AppointmentEntity.fromDomain(it) })
                    database.transactionDao().insertAll(demoTransactions.map { TransactionEntity.fromDomain(it) })
                    database.storeProfileDao().insertOrUpdate(StoreProfileEntity.fromDomain(defaultStoreProfile))
                    database.catalogPackageDao().insertAll(defaultCatalogPackages.map { CatalogPackageEntity.fromDomain(it) })
                    database.technicianDao().insertAll(defaultTechnicians.map { TechnicianEntity.fromDomain(it) })
                } else {
                    // Đảm bảo cấu hình cửa hàng luôn có sẵn
                    if (database.storeProfileDao().getStoreProfileSync() == null) {
                        database.storeProfileDao().insertOrUpdate(StoreProfileEntity.fromDomain(defaultStoreProfile))
                    }
                }

                // Lắng nghe thay đổi từ Room Database để cập nhật đồng bộ các StateFlow
                launch {
                    database.clientDao().getAllClients().collect { list ->
                        _clients.value = list.map { it.toDomain() }
                    }
                }
                launch {
                    database.appointmentDao().getAllAppointments().collect { list ->
                        val domainList = list.map { it.toDomain() }
                        _appointments.value = domainList
                        appContext?.let { ctx ->
                            AppointmentReminderScheduler.scheduleAllUpcoming(ctx, domainList)
                        }
                    }
                }
                launch {
                    database.transactionDao().getAllTransactions().collect { list ->
                        _transactions.value = list.map { it.toDomain() }
                    }
                }
                launch {
                    database.storeProfileDao().getStoreProfile().collect { entity ->
                        entity?.let {
                            val profile = it.toDomain()
                            _storeProfile.value = profile
                            _currentInvoice.value = _currentInvoice.value?.copy(
                                selectedBank = profile.selectedBank,
                                accountNumber = profile.accountNumber,
                                accountHolder = profile.accountHolder,
                                defaultMemo = profile.defaultMemo
                            )
                        }
                    }
                }
                launch {
                    database.catalogPackageDao().getAllPackages().collect { list ->
                        _catalogPackages.value = list.map { it.toDomain() }
                    }
                }
                launch {
                    database.technicianDao().getAllTechnicians().collect { list ->
                        _technicians.value = list.map { it.toDomain() }
                    }
                }
            }
        }
    }

    fun updateBank(bank: BankOption, accountNumber: String, accountHolder: String, defaultMemo: String = "VANDINH") {
        _currentInvoice.value = _currentInvoice.value?.copy(
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
        val current = _currentInvoice.value ?: return
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
                it.clientName.equals(current.clientName, ignoreCase = true) &&
                it.status != AppointmentStatus.COMPLETED && it.status != AppointmentStatus.CANCELLED
            }
            if (matchedAppt != null) {
                updateAppointmentStatus(matchedAppt.id, AppointmentStatus.COMPLETED)
            }
        }

        // Lấy đúng tên KTV phụ trách từ cuộc hẹn / invoice thay vì hardcode
        val assignedTech = current.staffName
            ?: current.appointmentId?.let { id -> _appointments.value.find { it.id == id }?.staffName }
            ?: "KTV Phục Vụ"

        // Ngày ghi nhận giao dịch tài chính luôn là ngày thực tế xác nhận thu tiền (dateOnly)
        val txDate = dateOnly

        // Cập nhật hoặc lưu mới vào lịch sử giao dịch hóa đơn
        val currentTxList = _transactions.value.toMutableList()
        val existingIndex = currentTxList.indexOfFirst {
            it.invoiceCode == current.invoiceCode ||
            (current.appointmentId != null && it.clientName.equals(current.clientName, ignoreCase = true) && !it.isPaid)
        }

        val newTx = InvoiceTransactionRecord(
            id = if (existingIndex != -1) currentTxList[existingIndex].id else "TX-${System.currentTimeMillis()}",
            invoiceCode = current.invoiceCode,
            clientName = current.clientName,
            clientPhone = current.clientPhone,
            serviceName = current.items.joinToString(", ") { it.name }.ifBlank { "Dịch Vụ Spa" },
            technicianName = assignedTech,
            time = timeOnly,
            date = txDate,
            amount = current.finalTotal,
            paymentMethod = "VietQR Chuyển Khoản",
            isPaid = true
        )

        if (existingIndex != -1) {
            currentTxList[existingIndex] = newTx
            _transactions.value = currentTxList
        } else {
            _transactions.value = listOf(newTx) + currentTxList
        }

        repositoryScope.launch {
            database?.transactionDao()?.insert(TransactionEntity.fromDomain(newTx))
        }
    }

    /**
     * Ghi nhận giao dịch thu tiền nhanh qua mã VietQR Cửa hàng
     */
    fun recordStoreQrPayment(amount: Long, memo: String = ""): InvoiceTransactionRecord {
        val timeOnly = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val dateOnly = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val code = "QR" + (1000..9999).random()

        val newTx = InvoiceTransactionRecord(
            id = "TX-${System.currentTimeMillis()}",
            invoiceCode = code,
            clientName = "Khách Quầy (QR Cửa Hàng)",
            clientPhone = "",
            serviceName = if (memo.isNotBlank()) "Thanh toán VietQR ($memo)" else "Thanh toán VietQR Cửa Hàng",
            technicianName = "Quầy Thu Ngân",
            time = timeOnly,
            date = dateOnly,
            amount = amount,
            paymentMethod = "VietQR Cửa Hàng",
            isPaid = true
        )
        _transactions.value = listOf(newTx) + _transactions.value
        repositoryScope.launch {
            database?.transactionDao()?.insert(TransactionEntity.fromDomain(newTx))
        }
        return newTx
    }

    fun deleteTransaction(transactionId: String) {
        val current = _transactions.value.toMutableList()
        current.removeAll { it.id == transactionId }
        _transactions.value = current
        repositoryScope.launch {
            database?.transactionDao()?.deleteById(transactionId)
        }
    }

    fun resetInvoice() {
        _currentInvoice.value = null
    }

    fun createInvoiceForClient(client: Client, serviceName: String = "Dịch Vụ Spa", amount: Long = 0L) {
        val code = "SPA" + (1000..9999).random()
        val store = _storeProfile.value
        _currentInvoice.value = CheckoutInvoice(
            invoiceId = "INV-${System.currentTimeMillis()}",
            invoiceCode = code,
            clientName = client.fullName,
            clientPhone = client.phone,
            items = listOf(
                OrderItem(
                    id = "ITEM-${System.currentTimeMillis()}",
                    name = serviceName,
                    type = ItemType.SERVICE,
                    quantity = 1,
                    unitPrice = amount
                )
            ),
            discountAmount = 0L,
            paymentStatus = PaymentStatus.WAITING_FOR_PAYMENT,
            selectedBank = store.selectedBank,
            accountNumber = store.accountNumber,
            accountHolder = store.accountHolder,
            defaultMemo = store.defaultMemo,
            appointmentId = null,
            staffName = null
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
            appointmentId = appointment.id,
            staffName = appointment.staffName
        )
    }

    // --- BÁO CÁO DOANH THU ĐỘNG THEO NGÀY ---
    fun getMetricsForDate(dateStr: String): DashboardMetrics {
        val dayAppts = _appointments.value.filter { it.date == dateStr }
        val bookingCount = dayAppts.size
        val dayTxs = _transactions.value.filter { it.date == dateStr && it.isPaid }

        val isReal = _isRealUsageMode.value

        // Doanh thu từ các giao dịch đã thanh toán trong ngày
        val txRevenue = dayTxs.sumOf { it.amount }

        // Doanh thu từ các lịch hẹn đã hoàn thành mà chưa có giao dịch hóa đơn đi kèm (ví dụ thanh toán tiền mặt trực tiếp)
        val completedApptsWithoutTx = dayAppts.filter { appt ->
            appt.status == AppointmentStatus.COMPLETED && appt.price > 0L &&
            _transactions.value.none { tx ->
                tx.isPaid && (
                    (tx.clientPhone.isNotBlank() && tx.clientPhone == appt.clientPhone) ||
                    tx.clientName.equals(appt.clientName, ignoreCase = true)
                )
            }
        }
        val totalActualDayRevenue = txRevenue + completedApptsWithoutTx.sumOf { it.price }

        val dayRevenue = if (isReal) {
            totalActualDayRevenue
        } else {
            if (dayAppts.isNotEmpty()) {
                val apptsWithoutTx = dayAppts.filter { appt ->
                    dayTxs.none { (it.clientPhone.isNotBlank() && it.clientPhone == appt.clientPhone) || it.clientName.equals(appt.clientName, ignoreCase = true) }
                }
                apptsWithoutTx.sumOf { it.price } + 8200000L + txRevenue
            } else {
                totalActualDayRevenue
            }
        }

        val totalPaidRevenue = _transactions.value.filter { it.isPaid }.sumOf { it.amount }

        val topServices = if (isReal) {
            val completedAppts = _appointments.value.filter { it.status == AppointmentStatus.COMPLETED && it.serviceName.isNotBlank() }
            val paidTxs = _transactions.value.filter { it.isPaid && it.serviceName.isNotBlank() }
            val allServices = completedAppts.map { it.serviceName to it.price } + paidTxs.map { it.serviceName to it.amount }
            val groups = allServices.groupBy { it.first }
            val totalRev = allServices.sumOf { it.second }.coerceAtLeast(1L)
            groups.entries.map { entry ->
                val name = entry.key
                val items = entry.value
                val count = items.size
                val rev = items.sumOf { p -> p.second }
                val pct = (rev.toFloat() / totalRev) * 100f
                TopServiceMetric(name, count, rev, pct)
            }.sortedByDescending { it.totalRevenue }.take(5)
        } else {
            listOf(
                TopServiceMetric("Liệu Trình Căng Bóng Whitening Facial", 42, 63000000L, 38f),
                TopServiceMetric("Gội Đầu Dưỡng Sinh Thảo Dược", 68, 27200000L, 22f),
                TopServiceMetric("Triệt Lông Công Nghệ Diode Laser", 28, 42000000L, 25f),
                TopServiceMetric("Massage Body Đá Nóng Himalaya", 18, 15300000L, 15f)
            )
        }

        val revenueDistribution = if (isReal) {
            var pkgs = 0L
            var singles = 0L
            var products = 0L
            _transactions.value.filter { it.isPaid }.forEach { tx ->
                val name = tx.serviceName.lowercase()
                when {
                    name.contains("gói") || name.contains("liệu trình") || name.contains("facial") || name.contains("hifu") || name.contains("laser") -> pkgs += tx.amount
                    name.contains("mỹ phẩm") || name.contains("kem") || name.contains("serum") || name.contains("sản phẩm") -> products += tx.amount
                    else -> singles += tx.amount
                }
            }
            RevenueDistribution(pkgs, singles, products)
        } else {
            var extraPkgs = 0L
            var extraSingles = 0L
            var extraProducts = 0L
            _transactions.value.filter { it.isPaid }.forEach { tx ->
                val name = tx.serviceName.lowercase()
                when {
                    name.contains("gói") || name.contains("liệu trình") || name.contains("facial") || name.contains("hifu") || name.contains("laser") -> extraPkgs += tx.amount
                    name.contains("mỹ phẩm") || name.contains("kem") || name.contains("serum") || name.contains("sản phẩm") -> extraProducts += tx.amount
                    else -> extraSingles += tx.amount
                }
            }
            RevenueDistribution(
                treatmentPackages = 265000000L + extraPkgs,
                singleServices = 120800000L + extraSingles,
                retailProducts = 80000000L + extraProducts
            )
        }

        val topPerformers = if (isReal) {
            val completedAppts = _appointments.value.filter { it.status == AppointmentStatus.COMPLETED && it.staffName.isNotBlank() }
            val paidTxs = _transactions.value.filter { it.isPaid && it.technicianName.isNotBlank() && it.technicianName != "Quầy Thu Ngân" }
            val staffPairs = completedAppts.map { it.staffName to it.price } + paidTxs.map { it.technicianName to it.amount }
            staffPairs.groupBy { it.first }
                .entries
                .mapIndexed { idx, entry ->
                    val staffName = entry.key
                    val items = entry.value
                    val rev = items.sumOf { p -> p.second }
                    StaffPerformance(
                        staffId = "ST-0${idx + 1}",
                        staffName = staffName,
                        role = "Kỹ Thuật Viên",
                        totalCustomersServed = items.size,
                        revenueGenerated = rev,
                        rating = 5.0f
                    )
                }
                .sortedByDescending { it.revenueGenerated }
                .take(5)
        } else {
            listOf(
                StaffPerformance("ST-01", "Thảo My", "Chuyên Viên Trị Liệu", 46, 32500000L, 4.95f),
                StaffPerformance("ST-02", "Hương Giang", "Kỹ Thuật Viên Dưỡng Sinh", 52, 24800000L, 4.90f),
                StaffPerformance("ST-03", "Lan Phương", "Chuyên Viên Laser & Phục Hồi", 38, 28100000L, 4.88f)
            )
        }

        val txsNotCoveredInAppts = dayTxs.filter { tx ->
            dayAppts.none { appt ->
                (appt.clientPhone.isNotBlank() && appt.clientPhone == tx.clientPhone) ||
                appt.clientName.equals(tx.clientName, ignoreCase = true)
            }
        }
        val totalServed = bookingCount + txsNotCoveredInAppts.size

        return DashboardMetrics(
            todayRevenue = dayRevenue,
            todayGrowthPercent = if (dayRevenue > 0) 14.8f else 0f,
            weekRevenue = if (isReal) totalPaidRevenue else (112400000L + totalPaidRevenue),
            monthRevenue = if (isReal) totalPaidRevenue else (465800000L + totalPaidRevenue),
            totalBookingsToday = totalServed,
            roomOccupancyRate = if (totalServed > 0) (totalServed * 17.5f).coerceAtMost(100f) else 0f,
            topServices = topServices,
            revenueDistribution = revenueDistribution,
            topPerformers = topPerformers
        )
    }

    // --- SAO LƯU & KHÔI PHỤC DỮ LIỆU (EXPORT / IMPORT JSON) ---
    suspend fun exportBackup(context: Context): Result<File> {
        val db = database ?: return Result.failure(IllegalStateException("Cơ sở dữ liệu chưa sẵn sàng"))
        return BackupManager.exportBackup(context, db)
    }

    fun shareBackup(context: Context, backupFile: File) {
        BackupManager.shareBackupFile(context, backupFile)
    }

    suspend fun importBackup(context: Context, jsonContent: String): Result<BackupStats> {
        val db = database ?: return Result.failure(IllegalStateException("Cơ sở dữ liệu chưa sẵn sàng"))
        val result = BackupManager.importBackup(context, db, jsonContent)
        if (result.isSuccess) {
            prefs?.edit()?.putBoolean("key_is_real_usage", true)?.apply()
            _isRealUsageMode.value = true
        }
        return result
    }
}
