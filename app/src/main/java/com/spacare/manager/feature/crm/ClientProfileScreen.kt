package com.spacare.manager.feature.crm

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.*
import com.spacare.manager.feature.crm.components.ClientFormDialog
import com.spacare.manager.feature.crm.components.SessionPhotoProgressionCard
import com.spacare.manager.feature.crm.components.TakeOrUploadPhotoDialog
import com.spacare.manager.feature.crm.components.TreatmentPackageCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(
    client: Client,
    onBackClick: () -> Unit,
    onBookAppointmentClick: () -> Unit = {},
    onCheckoutClick: () -> Unit,
    onUpdateClient: (Client) -> Unit,
    onDeleteClient: (String) -> Unit,
    onCheckInSession: (packageId: String) -> Unit,
    onAddTreatmentPackage: (TreatmentPackage) -> Unit,
    onAddBeforeAfter: (BeforeAfterComparison) -> Unit = {},
    onAddAppointment: (Appointment) -> Unit = {},
    onAddSessionPhoto: (SessionPhotoRecord) -> Unit = {},
    onUpdateSessionPhoto: (SessionPhotoRecord) -> Unit = {},
    catalogPackages: List<SpaServicePackage> = emptyList(),
    technicians: List<TechnicianStaff> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAddPackageDialog by remember { mutableStateOf(false) }
    var showBookAppointmentDialog by remember { mutableStateOf(false) }
    var showAddSessionPhotoDialog by remember { mutableStateOf(false) }
    var photoToRetake by remember { mutableStateOf<SessionPhotoRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hồ Sơ Khách Hàng",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Chỉnh sửa")
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = AllergyAlertRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmBeigeBackground,
                    titleContentColor = CharcoalTextPrimary
                )
            )
        },
        containerColor = WarmBeigeBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Thẻ thông tin khách hàng & VIP Badge
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(SageGreenContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = client.fullName.split(" ").takeLast(2).map { it.first() }.joinToString(""),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = SageGreenPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = client.fullName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = client.phone,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CharcoalTextSecondary
                                )
                                if (client.email.isNotBlank()) {
                                    Text(
                                        text = client.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = SpaGoldContainer,
                                    shape = CircleShape
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = SpaGoldPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = client.vipTier.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SpaGoldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = WarmBeigeBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Nút hành động nhanh
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showBookAppointmentDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SageGreenPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Đặt lịch mới", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onCheckoutClick,
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreenPrimary)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Thanh toán", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // 2. Gói Liệu Trình
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Gói Liệu Trình",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                    }
                    TextButton(onClick = { showAddPackageDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = SageGreenPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm gói", color = SageGreenPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (client.treatmentPackages.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = WarmBeigeSurfaceVariant)
                    ) {
                        Text(
                            text = "Khách hàng chưa đăng ký gói liệu trình nào.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CharcoalTextMuted
                        )
                    }
                }
            } else {
                items(client.treatmentPackages.size) { index ->
                    val pkg = client.treatmentPackages[index]
                    TreatmentPackageCard(
                        pkg = pkg,
                        onUseSessionClick = {
                            onCheckInSession(pkg.id)
                        }
                    )
                }
            }

            // 3. Dịch Vụ Đã Làm
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Dịch Vụ Đã Làm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                }
            }

            if (client.serviceHistory.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = WarmBeigeSurfaceVariant)
                    ) {
                        Text(
                            text = "Chưa có lịch sử dịch vụ nào.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CharcoalTextMuted
                        )
                    }
                }
            } else {
                items(client.serviceHistory.size) { index ->
                    val record = client.serviceHistory[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = record.date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SageGreenPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = record.room,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalTextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = record.serviceName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "KTV: ${record.technicianName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CharcoalTextSecondary
                            )
                            if (record.feedback != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = WarmBeigeSurfaceVariant,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "“${record.feedback}”",
                                        modifier = Modifier.padding(8.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Ảnh Tình Trạng Khách Qua Các Buổi
            item {
                SessionPhotoProgressionCard(
                    photos = client.sessionPhotos,
                    onAddPhotoClick = {
                        photoToRetake = null
                        showAddSessionPhotoDialog = true
                    },
                    onRetakePhotoClick = { photo ->
                        photoToRetake = photo
                        showAddSessionPhotoDialog = true
                    }
                )
            }
        }
    }

    // Dialog Sửa khách hàng
    if (showEditDialog) {
        ClientFormDialog(
            initialClient = client,
            onDismissRequest = { showEditDialog = false },
            onSaveClient = { updatedClient ->
                onUpdateClient(updatedClient)
                showEditDialog = false
            }
        )
    }

    // Dialog Xác nhận Xóa khách hàng
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Xác nhận xóa hồ sơ") },
            text = { Text("Bạn có chắc chắn muốn xóa khách hàng \"${client.fullName}\"? Thao tác này không thể hoàn tác.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClient(client.id)
                        showDeleteConfirmDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AllergyAlertRed)
                ) {
                    Text("Xóa vĩnh viễn")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog Thêm gói liệu trình mới
    if (showAddPackageDialog) {
        var pkgName by remember { mutableStateOf("") }
        var totalSessions by remember { mutableStateOf("10") }
        var therapist by remember { mutableStateOf(technicians.firstOrNull()?.let { "KTV. ${it.name}" } ?: "") }

        AlertDialog(
            onDismissRequest = { showAddPackageDialog = false },
            title = { Text("Thêm Gói Liệu Trình Mới") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = pkgName,
                        onValueChange = { pkgName = it },
                        label = { Text("Tên gói liệu trình") },
                        placeholder = { Text("Vd: Căng bóng da Whitening Facial") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = totalSessions,
                        onValueChange = { totalSessions = it },
                        label = { Text("Tổng số buổi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = therapist,
                        onValueChange = { therapist = it },
                        label = { Text("KTV phụ trách") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sessions = totalSessions.toIntOrNull() ?: 10
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val now = Date()
                        val startDateStr = sdf.format(now)
                        val cal = java.util.Calendar.getInstance().apply {
                            time = now
                            add(java.util.Calendar.MONTH, 6)
                        }
                        val expiryDateStr = sdf.format(cal.time)

                        val newPackage = TreatmentPackage(
                            id = "PKG-" + (1000..9999).random(),
                            name = pkgName.ifBlank { "Liệu Trình Chuyên Sâu Spa" },
                            totalSessions = sessions,
                            usedSessions = 0,
                            remainingSessions = sessions,
                            startDate = startDateStr,
                            expiryDate = expiryDateStr,
                            assignedTherapist = therapist
                        )
                        onAddTreatmentPackage(newPackage)
                        showAddPackageDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Lưu gói", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddPackageDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Modal Chụp / Tải ảnh tiến trình từng buổi
    if (showAddSessionPhotoDialog) {
        TakeOrUploadPhotoDialog(
            existingPhoto = photoToRetake,
            onDismissRequest = {
                showAddSessionPhotoDialog = false
                photoToRetake = null
            },
            onSavePhoto = { record ->
                if (photoToRetake != null) {
                    onUpdateSessionPhoto(record)
                    Toast.makeText(context, "Đã cập nhật ảnh ${record.title}!", Toast.LENGTH_SHORT).show()
                } else {
                    onAddSessionPhoto(record)
                    Toast.makeText(context, "Đã lưu ảnh ${record.title} thành công!", Toast.LENGTH_SHORT).show()
                }
                showAddSessionPhotoDialog = false
                photoToRetake = null
            }
        )
    }

    // Modal Đặt Lịch Hẹn Ngay Tại Màn Khách Hàng
    if (showBookAppointmentDialog) {
        CreateAppointmentForClientDialog(
            client = client,
            catalogPackages = catalogPackages,
            technicians = technicians,
            onDismissRequest = { showBookAppointmentDialog = false },
            onSave = { newAppt ->
                onAddAppointment(newAppt)
                showBookAppointmentDialog = false
                Toast.makeText(context, "Đã tạo lịch hẹn mới cho ${client.fullName} lúc ${newAppt.startTime} ngày ${newAppt.date}!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentForClientDialog(
    client: Client,
    catalogPackages: List<SpaServicePackage>,
    technicians: List<TechnicianStaff>,
    onDismissRequest: () -> Unit,
    onSave: (Appointment) -> Unit
) {
    var serviceName by remember {
        mutableStateOf(
            client.treatmentPackages.firstOrNull { it.remainingSessions > 0 }?.name
                ?: catalogPackages.firstOrNull()?.name
                ?: ""
        )
    }
    var staffName by remember {
        mutableStateOf(
            technicians.firstOrNull()?.name ?: ""
        )
    }
    var roomName by remember { mutableStateOf("Phòng VIP 01") }
    var appointmentDate by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }
    var startTime by remember { mutableStateOf("09:30") }
    var durationMinutes by remember { mutableStateOf(75) }
    var isPackageSession by remember {
        mutableStateOf(client.treatmentPackages.any { it.name == serviceName && it.remainingSessions > 0 })
    }
    var customPriceStr by remember {
        mutableStateOf(
            if (isPackageSession) "0"
            else (catalogPackages.firstOrNull()?.price?.toString() ?: "0")
        )
    }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val roomOptions = listOf("Phòng VIP 01", "Phòng VIP 02", "Phòng Chăm Sóc 03", "Phòng Dưỡng Sinh Sen", "Phòng Laser 01")
    val timeOptions = listOf("08:30", "09:30", "10:30", "11:30", "13:30", "14:30", "15:30", "16:30", "17:30", "18:30")

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column {
                Text(
                    text = "Đặt Lịch Hẹn Mới",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Khách hàng: ${client.fullName} • ${client.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SageGreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = WarmBeigeSurface,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Chọn ngày hẹn
                OutlinedTextField(
                    value = appointmentDate,
                    onValueChange = { appointmentDate = it },
                    label = { Text("Ngày hẹn (dd/MM/yyyy)") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePickerDialog = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Chọn ngày", tint = SageGreenPrimary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Chọn giờ bắt đầu
                Text("Giờ bắt đầu hẹn:", style = MaterialTheme.typography.labelSmall, color = CharcoalTextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(timeOptions) { t ->
                        FilterChip(
                            selected = startTime == t,
                            onClick = { startTime = t },
                            label = { Text(t, fontSize = 12.sp) }
                        )
                    }
                }

                // Dịch vụ / Liệu trình
                Text("Dịch vụ thực hiện:", style = MaterialTheme.typography.labelSmall, color = CharcoalTextSecondary)
                if (client.treatmentPackages.isNotEmpty()) {
                    Text("• Gói liệu trình của khách:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SageGreenPrimary)
                    client.treatmentPackages.forEach { pkg ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (serviceName == pkg.name) 1.5.dp else 1.dp,
                                    color = if (serviceName == pkg.name) SageGreenPrimary else WarmBeigeBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    serviceName = pkg.name
                                    isPackageSession = true
                                },
                            color = if (serviceName == pkg.name) SageGreenContainer.copy(alpha = 0.5f) else WarmBeigeSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pkg.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text("Còn ${pkg.remainingSessions} buổi • Trừ gói (0đ)", style = MaterialTheme.typography.bodySmall, color = SageGreenPrimary)
                                }
                                if (serviceName == pkg.name) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SageGreenPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                if (catalogPackages.isNotEmpty()) {
                    Text("• Dịch vụ đơn lẻ khác:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CharcoalTextSecondary)
                    catalogPackages.forEach { cat ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (serviceName == cat.name) 1.5.dp else 1.dp,
                                    color = if (serviceName == cat.name) SageGreenPrimary else WarmBeigeBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    serviceName = cat.name
                                    isPackageSession = false
                                    customPriceStr = cat.price.toString()
                                    durationMinutes = cat.durationMinutes
                                },
                            color = if (serviceName == cat.name) SageGreenContainer.copy(alpha = 0.5f) else WarmBeigeSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cat.name, style = MaterialTheme.typography.labelMedium)
                                    Text("${cat.price / 1000}k • ${cat.durationMinutes}p", style = MaterialTheme.typography.bodySmall, color = CharcoalTextSecondary)
                                }
                                if (serviceName == cat.name) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SageGreenPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                if (client.treatmentPackages.isEmpty() && catalogPackages.isEmpty()) {
                    OutlinedTextField(
                        value = serviceName,
                        onValueChange = { serviceName = it },
                        label = { Text("Tên dịch vụ thực hiện") },
                        placeholder = { Text("Nhập tên dịch vụ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customPriceStr,
                        onValueChange = { customPriceStr = it },
                        label = { Text("Giá dịch vụ (đ)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Chọn KTV phụ trách
                if (technicians.isNotEmpty()) {
                    Text("Kỹ thuật viên phụ trách:", style = MaterialTheme.typography.labelSmall, color = CharcoalTextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val staffList = technicians.map { it.name }
                        items(staffList) { staff ->
                            FilterChip(
                                selected = staffName == staff,
                                onClick = { staffName = staff },
                                label = { Text(staff, fontSize = 12.sp) }
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = staffName,
                        onValueChange = { staffName = it },
                        label = { Text("Kỹ thuật viên phụ trách") },
                        placeholder = { Text("Nhập tên KTV (nếu có)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Chọn Phòng
                Text("Phòng trị liệu:", style = MaterialTheme.typography.labelSmall, color = CharcoalTextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(roomOptions) { room ->
                        FilterChip(
                            selected = roomName == room,
                            onClick = { roomName = room },
                            label = { Text(room, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val calculatedPrice = if (isPackageSession) 0L else (customPriceStr.toLongOrNull() ?: 0L)
                    val endHourInt = (startTime.split(":").firstOrNull()?.toIntOrNull() ?: 9) + 1
                    val endMinStr = startTime.split(":").getOrNull(1) ?: "00"
                    val calculatedEndTime = String.format("%02d:%s", endHourInt, endMinStr)

                    val newAppt = Appointment(
                        id = "APT-" + (1000..9999).random(),
                        clientName = client.fullName,
                        clientPhone = client.phone,
                        serviceName = serviceName,
                        durationMinutes = durationMinutes,
                        staffName = staffName,
                        roomName = roomName,
                        startTime = startTime,
                        endTime = calculatedEndTime,
                        date = appointmentDate.trim(),
                        status = AppointmentStatus.CONFIRMED,
                        price = calculatedPrice,
                        reminderSent = true
                    )
                    onSave(newAppt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Lưu & Đặt Lịch", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text("Hủy")
            }
        }
    )

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            appointmentDate = sdf.format(Date(millis))
                        }
                        showDatePickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary, contentColor = Color.White)
                ) {
                    Text("Chọn", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePickerDialog = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

