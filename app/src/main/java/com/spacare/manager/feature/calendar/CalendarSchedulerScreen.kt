package com.spacare.manager.feature.calendar

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.Appointment
import com.spacare.manager.domain.model.AppointmentStatus
import java.text.NumberFormat
import java.util.Locale

enum class CalendarViewMode(val label: String) {
    TIMELINE("Dòng thời gian"),
    BY_STAFF("Theo Kỹ thuật viên"),
    BY_ROOM("Theo Phòng")
}

data class DayItem(
    val dayOfWeek: String,
    val dayNumber: String,
    val fullDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSchedulerScreen(
    appointments: List<Appointment>,
    onAppointmentClick: (Appointment) -> Unit = {},
    onAddAppointment: (Appointment) -> Unit = {},
    onUpdateAppointmentStatus: (String, AppointmentStatus) -> Unit = { _, _ -> },
    onDeleteAppointment: (String) -> Unit = {},
    onGoToCheckout: (Appointment) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedViewMode by remember { mutableStateOf(CalendarViewMode.TIMELINE) }
    var selectedDayIndex by remember { mutableIntStateOf(3) } // Thứ 6 ngày 16
    var selectedStaffFilter by remember { mutableStateOf<String?>(null) }
    var selectedRoomFilter by remember { mutableStateOf<String?>(null) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var activeAppointmentDetail by remember { mutableStateOf<Appointment?>(null) }

    val daysList = remember {
        listOf(
            DayItem("T3", "13", "13/10/2026"),
            DayItem("T4", "14", "14/10/2026"),
            DayItem("T5", "15", "15/10/2026"),
            DayItem("T6", "16", "16/10/2026"),
            DayItem("T7", "17", "17/10/2026"),
            DayItem("CN", "18", "18/10/2026"),
            DayItem("T2", "19", "19/10/2026")
        )
    }

    val selectedDate = daysList[selectedDayIndex].fullDate

    // Lọc cuộc hẹn theo ngày và theo chế độ xem
    val filteredAppointments = remember(appointments, selectedDayIndex, selectedViewMode, selectedStaffFilter, selectedRoomFilter) {
        appointments.filter { appt ->
            val matchDate = appt.date == selectedDate
            val matchMode = when (selectedViewMode) {
                CalendarViewMode.TIMELINE -> true
                CalendarViewMode.BY_STAFF -> selectedStaffFilter == null || appt.staffName == selectedStaffFilter
                CalendarViewMode.BY_ROOM -> selectedRoomFilter == null || appt.roomName == selectedRoomFilter
            }
            matchDate && matchMode
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Lịch Hẹn & Phân Bổ Phòng",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ngày $selectedDate • ${filteredAppointments.size} cuộc hẹn",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Tạo lịch", tint = SageGreenPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmBeigeBackground,
                    titleContentColor = CharcoalTextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = SageGreenPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo lịch hẹn mới")
            }
        },
        containerColor = WarmBeigeBackground
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Dải chọn ngày trong tuần
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(daysList.size) { index ->
                    val day = daysList[index]
                    val isSelected = index == selectedDayIndex

                    Surface(
                        modifier = Modifier
                            .width(52.dp)
                            .height(68.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedDayIndex = index
                            },
                        color = if (isSelected) SageGreenPrimary else WarmBeigeSurface,
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, WarmBeigeBorder) else null,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day.dayOfWeek,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) WarmBeigeSurface else CharcoalTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = day.dayNumber,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) WarmBeigeSurface else CharcoalTextPrimary
                            )
                        }
                    }
                }
            }

            // 2. Chuyển đổi chế độ xem (Sử dụng LazyRow cuộn ngang - Sửa triệt để lỗi ép tràn width làm méo chiều cao item)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(CalendarViewMode.entries) { mode ->
                    FilterChip(
                        selected = selectedViewMode == mode,
                        onClick = {
                            selectedViewMode = mode
                            selectedStaffFilter = null
                            selectedRoomFilter = null
                        },
                        label = { Text(mode.label, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenContainer,
                            selectedLabelColor = SageGreenOnContainer
                        )
                    )
                }
            }

            // Bộ lọc chi tiết phụ khi chọn Theo KTV hoặc Theo Phòng
            if (selectedViewMode == CalendarViewMode.BY_STAFF) {
                val staffList = listOf("Thảo My", "Hương Giang", "Lan Phương")
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    item {
                        InputChip(
                            selected = selectedStaffFilter == null,
                            onClick = { selectedStaffFilter = null },
                            label = { Text("Tất cả KTV") }
                        )
                    }
                    items(staffList) { staff ->
                        InputChip(
                            selected = selectedStaffFilter == staff,
                            onClick = { selectedStaffFilter = if (selectedStaffFilter == staff) null else staff },
                            label = { Text("KTV $staff") }
                        )
                    }
                }
            } else if (selectedViewMode == CalendarViewMode.BY_ROOM) {
                val roomList = listOf("Phòng VIP 02", "Phòng Trị Liệu Sen", "Phòng Chăm Sóc 03", "Phòng Laser 01")
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    item {
                        InputChip(
                            selected = selectedRoomFilter == null,
                            onClick = { selectedRoomFilter = null },
                            label = { Text("Tất cả phòng") }
                        )
                    }
                    items(roomList) { room ->
                        InputChip(
                            selected = selectedRoomFilter == room,
                            onClick = { selectedRoomFilter = if (selectedRoomFilter == room) null else room },
                            label = { Text(room) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Danh sách cuộc hẹn (Timeline slots)
            if (filteredAppointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = CharcoalTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Không có lịch hẹn nào trong ngày $selectedDate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CharcoalTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredAppointments, key = { it.id }) { appointment ->
                        AppointmentScheduleCard(
                            appointment = appointment,
                            onClick = { activeAppointmentDetail = appointment }
                        )
                    }
                }
            }
        }
    }

    // Dialog Tạo lịch hẹn mới
    if (showCreateDialog) {
        CreateAppointmentDialog(
            defaultDate = selectedDate,
            onDismissRequest = { showCreateDialog = false },
            onSave = { newAppt ->
                onAddAppointment(newAppt)
                showCreateDialog = false
            }
        )
    }

    // Dialog Xem chi tiết & Thao tác trạng thái cuộc hẹn
    activeAppointmentDetail?.let { appt ->
        AppointmentDetailDialog(
            appointment = appt,
            onDismissRequest = { activeAppointmentDetail = null },
            onUpdateStatus = { newStatus ->
                onUpdateAppointmentStatus(appt.id, newStatus)
                activeAppointmentDetail = null
            },
            onDelete = {
                onDeleteAppointment(appt.id)
                activeAppointmentDetail = null
            },
            onCheckout = {
                activeAppointmentDetail = null
                onGoToCheckout(appt)
            }
        )
    }
}

@Composable
fun AppointmentScheduleCard(
    appointment: Appointment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium, // 16.dp
        colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min), // Tự động co giãn theo chiều cao nội dung thực tế
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cột thời gian bên trái
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(52.dp)
            ) {
                Text(
                    text = appointment.startTime,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SageGreenPrimary
                )
                Text(
                    text = appointment.endTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = WarmBeigeSurfaceVariant
                ) {
                    Text(
                        text = "${appointment.durationMinutes}p",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalTextSecondary
                    )
                }
            }

            // Đường phân cách dọc thích ứng linh hoạt
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .fillMaxHeight()
                    .background(WarmBeigeBorder)
            )

            // Nội dung chi tiết cuộc hẹn
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appointment.clientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )

                    // Trạng thái cuộc hẹn
                    Surface(
                        shape = CircleShape,
                        color = when (appointment.status) {
                            AppointmentStatus.IN_SERVICE -> SageGreenContainer
                            AppointmentStatus.CONFIRMED -> SpaGoldContainer
                            AppointmentStatus.PENDING -> AllergyAlertContainer
                            AppointmentStatus.COMPLETED -> WarmBeigeSurfaceVariant
                            AppointmentStatus.CANCELLED -> AllergyAlertContainer
                        }
                    ) {
                        Text(
                            text = appointment.status.label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = when (appointment.status) {
                                AppointmentStatus.IN_SERVICE -> SageGreenPrimary
                                AppointmentStatus.CONFIRMED -> SpaGoldPrimary
                                AppointmentStatus.PENDING -> AllergyAlertRed
                                AppointmentStatus.COMPLETED -> CharcoalTextSecondary
                                AppointmentStatus.CANCELLED -> AllergyAlertRed
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = appointment.serviceName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SageGreenPrimary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CharcoalTextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appointment.staffName,
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MeetingRoom,
                            contentDescription = null,
                            tint = CharcoalTextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appointment.roomName,
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary
                        )
                    }
                }

                if (appointment.reminderSent) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Đã gửi SMS / ZNS nhắc hẹn tự động",
                            style = MaterialTheme.typography.bodySmall,
                            color = SageGreenPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateAppointmentDialog(
    defaultDate: String,
    onDismissRequest: () -> Unit,
    onSave: (Appointment) -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var serviceName by remember { mutableStateOf("Liệu Trình Whitening Facial Căng Bóng") }
    var staffName by remember { mutableStateOf("Thảo My") }
    var roomName by remember { mutableStateOf("Phòng VIP 02") }
    var startTime by remember { mutableStateOf("10:00") }
    var duration by remember { mutableStateOf("75") }
    var priceText by remember { mutableStateOf("650000") }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Tạo Lịch Hẹn Mới",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SageGreenPrimary
                )

                Text(
                    text = "Ngày hẹn: $defaultDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Tên khách hàng *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("Số điện thoại *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = serviceName,
                    onValueChange = { serviceName = it },
                    label = { Text("Dịch vụ thực hiện") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Giờ hẹn") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Thời lượng (phút)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = staffName,
                        onValueChange = { staffName = it },
                        label = { Text("KTV phụ trách") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text("Phòng dịch vụ") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Chi phí dự kiến (0 đ nếu trừ gói)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hủy")
                    }

                    Button(
                        onClick = {
                            if (clientName.isNotBlank() && clientPhone.isNotBlank()) {
                                val dur = duration.toIntOrNull() ?: 60
                                val startHour = startTime.split(":").firstOrNull()?.toIntOrNull() ?: 10
                                val startMin = startTime.split(":").lastOrNull()?.toIntOrNull() ?: 0
                                val totalMin = startHour * 60 + startMin + dur
                                val endHour = (totalMin / 60) % 24
                                val endMinute = totalMin % 60
                                val endTimeFormatted = String.format("%02d:%02d", endHour, endMinute)

                                val newAppointment = Appointment(
                                    id = "APT-" + (1000..9999).random(),
                                    clientName = clientName.trim(),
                                    clientPhone = clientPhone.trim(),
                                    serviceName = serviceName.trim(),
                                    durationMinutes = dur,
                                    staffName = staffName.trim(),
                                    roomName = roomName.trim(),
                                    startTime = startTime.trim(),
                                    endTime = endTimeFormatted,
                                    date = defaultDate,
                                    status = AppointmentStatus.CONFIRMED,
                                    price = priceText.toLongOrNull() ?: 0L,
                                    reminderSent = true
                                )
                                onSave(newAppointment)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                        enabled = clientName.isNotBlank() && clientPhone.isNotBlank()
                    ) {
                        Text("Lưu lịch hẹn")
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentDetailDialog(
    appointment: Appointment,
    onDismissRequest: () -> Unit,
    onUpdateStatus: (AppointmentStatus) -> Unit,
    onDelete: () -> Unit,
    onCheckout: () -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column {
                Text(
                    text = appointment.clientName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
                Text(
                    text = "${appointment.startTime} - ${appointment.endTime} • ${appointment.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SageGreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dịch vụ: ${appointment.serviceName}", fontWeight = FontWeight.Medium)
                Text("KTV: ${appointment.staffName} | ${appointment.roomName}")
                Text("Chi phí: ${if (appointment.price == 0L) "Trừ gói dịch vụ" else currencyFormatter.format(appointment.price) + " đ"}")

                Spacer(modifier = Modifier.height(10.dp))
                Text("Cập nhật trạng thái:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { onUpdateStatus(AppointmentStatus.IN_SERVICE) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Đang làm", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onUpdateStatus(AppointmentStatus.COMPLETED) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Hoàn thành", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onCheckout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SpaGoldPrimary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chuyển sang Thu Ngân tạo VietQR")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = AllergyAlertRed)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Xóa lịch")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text("Đóng")
            }
        }
    )
}
