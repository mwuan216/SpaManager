package com.spacare.manager.feature.calendar

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.spacare.manager.R
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.core.notification.AppointmentReminderScheduler
import com.spacare.manager.core.notification.NotificationHelper
import com.spacare.manager.domain.model.Appointment
import com.spacare.manager.domain.model.AppointmentStatus
import com.spacare.manager.domain.model.Client
import com.spacare.manager.domain.model.SpaServicePackage
import com.spacare.manager.domain.model.TechnicianStaff
import com.spacare.manager.domain.model.TreatmentPackage
import com.spacare.manager.domain.model.VipTier
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    technicians: List<TechnicianStaff> = emptyList(),
    clients: List<Client> = emptyList(),
    catalogPackages: List<SpaServicePackage> = emptyList(),
    onAppointmentClick: (Appointment) -> Unit = {},
    onAddAppointment: (Appointment) -> Unit = {},
    onAddNewClient: ((Client) -> Unit)? = null,
    onUpdateAppointmentStatus: (String, AppointmentStatus) -> Unit = { _, _ -> },
    onDeleteAppointment: (String) -> Unit = {},
    onGoToCheckout: (Appointment) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedViewMode by remember { mutableStateOf(CalendarViewMode.TIMELINE) }
    val todayDateStr = remember {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.format(Date())
    }
    var selectedDateStr by remember { mutableStateOf(todayDateStr) }
    var showCalendarDatePicker by remember { mutableStateOf(false) }

    var selectedStaffFilter by remember { mutableStateOf<String?>(null) }

    val clientsNeedingRoutineReminder = remember(clients) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val now = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val list = mutableListOf<Triple<Client, TreatmentPackage, String>>()
        clients.forEach { client ->
            client.treatmentPackages.filter { it.remainingSessions > 0 && !it.nextRecommendedDate.isNullOrBlank() }.forEach { pkg ->
                try {
                    val nextDate = sdf.parse(pkg.nextRecommendedDate!!)
                    if (nextDate != null) {
                        val diffDays = ((nextDate.time - now) / (1000 * 60 * 60 * 24)).toInt()
                        if (diffDays in -7..1) {
                            val statusText = when {
                                diffDays < 0 -> "Quá hạn ${-diffDays} ngày"
                                diffDays == 0 -> "Hôm nay"
                                diffDays == 1 -> "Ngày mai (${pkg.nextRecommendedDate})"
                                else -> "Trong ${diffDays} ngày"
                            }
                            list.add(Triple(client, pkg, statusText))
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parse error
                }
            }
        }
        list
    }
    var selectedRoomFilter by remember { mutableStateOf<String?>(null) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var activeAppointmentDetail by remember { mutableStateOf<Appointment?>(null) }

    // Dải 7 ngày xung quanh ngày đang chọn (-3 ngày đến +3 ngày)
    val daysList = remember(selectedDateStr) {
        val fullSdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dayMonthSdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        val currentDate = try {
            fullSdf.parse(selectedDateStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }
        val cal = Calendar.getInstance()
        (-3..3).map { offset ->
            cal.time = currentDate
            cal.add(Calendar.DAY_OF_YEAR, offset)
            val dow = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "T2"
                Calendar.TUESDAY -> "T3"
                Calendar.WEDNESDAY -> "T4"
                Calendar.THURSDAY -> "T5"
                Calendar.FRIDAY -> "T6"
                Calendar.SATURDAY -> "T7"
                Calendar.SUNDAY -> "CN"
                else -> ""
            }
            val dayMonth = dayMonthSdf.format(cal.time)
            val full = fullSdf.format(cal.time)
            DayItem(
                dayOfWeek = dow,
                dayNumber = dayMonth,
                fullDate = full
            )
        }
    }

    val selectedDate = selectedDateStr

    // Lọc cuộc hẹn theo ngày và theo chế độ xem
    val filteredAppointments = remember(appointments, selectedDate, selectedViewMode, selectedStaffFilter, selectedRoomFilter) {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.clickable { showCalendarDatePicker = true }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Lịch Hẹn & Phân Bổ Phòng",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Chọn ngày",
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Ngày $selectedDate • ${filteredAppointments.size} cuộc hẹn",
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalTextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showNotificationDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Trung tâm thông báo",
                            tint = SageGreenPrimary
                        )
                    }
                    IconButton(onClick = { showCalendarDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Chọn ngày bất kỳ", tint = SageGreenPrimary)
                    }
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
                    val isSelected = day.fullDate == selectedDateStr

                    Surface(
                        modifier = Modifier
                            .width(62.dp)
                            .height(68.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedDateStr = day.fullDate
                            },
                        color = if (isSelected) SageGreenPrimary else WarmBeigeSurface,
                        contentColor = if (isSelected) Color.White else CharcoalTextPrimary,
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
                                color = if (isSelected) Color.White.copy(alpha = 0.95f) else CharcoalTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = day.dayNumber,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else CharcoalTextPrimary
                            )
                        }
                    }
                }
            }

            // Banner Nhắc Lịch Liệu Trình (Chu kỳ 7-10 ngày)
            if (clientsNeedingRoutineReminder.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showNotificationDialog = true },
                    color = SageGreenContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SageGreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "🔔 Có ${clientsNeedingRoutineReminder.size} khách cần nhắc lịch liệu trình",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SageGreenPrimary
                                )
                                Text(
                                    text = "Chu kỳ 7-10 ngày • Chạm để xem danh sách & gọi/nhắn tin",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SageGreenPrimary
                        )
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
                val staffList = remember(technicians, appointments) {
                    (technicians.map { it.name } + appointments.map { it.staffName }).filter { it.isNotBlank() }.distinct()
                }
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

    // Modal DatePickerDialog chọn ngày bất kỳ (quá khứ / hiện tại / tương lai)
    if (showCalendarDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = remember(selectedDateStr) {
                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    sdf.parse(selectedDateStr)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showCalendarDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            selectedDateStr = sdf.format(Date(millis))
                        }
                        showCalendarDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Chọn ngày", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCalendarDatePicker = false }) {
                    Text("Hủy", color = CharcoalTextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = WarmBeigeSurface
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = WarmBeigeSurface,
                    titleContentColor = CharcoalTextPrimary,
                    headlineContentColor = SageGreenPrimary,
                    weekdayContentColor = CharcoalTextSecondary,
                    subheadContentColor = CharcoalTextPrimary,
                    yearContentColor = CharcoalTextPrimary,
                    currentYearContentColor = SageGreenPrimary,
                    selectedYearContainerColor = SageGreenPrimary,
                    selectedYearContentColor = Color.White,
                    dayContentColor = CharcoalTextPrimary,
                    disabledDayContentColor = CharcoalTextMuted,
                    selectedDayContainerColor = SageGreenPrimary,
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = SageGreenPrimary,
                    todayContentColor = SageGreenPrimary
                )
            )
        }
    }

    // Dialog Tạo lịch hẹn mới
    if (showCreateDialog) {
        CreateAppointmentDialog(
            defaultDate = selectedDate,
            technicians = technicians,
            clients = clients,
            catalogPackages = catalogPackages,
            onDismissRequest = { showCreateDialog = false },
            onSave = { newAppt ->
                onAddAppointment(newAppt)
                selectedDateStr = newAppt.date // Tự động chuyển view sang ngày vừa tạo
                showCreateDialog = false
                Toast.makeText(context, "Đã tạo lịch hẹn thành công cho ngày ${newAppt.date}", Toast.LENGTH_SHORT).show()
            },
            onAddNewClient = onAddNewClient
        )
    }

    // Dialog Trung tâm thông báo nhắc hẹn
    if (showNotificationDialog) {
        AppointmentNotificationCenterDialog(
            appointments = appointments,
            selectedDate = selectedDate,
            routineReminders = clientsNeedingRoutineReminder,
            onDismissRequest = { showNotificationDialog = false },
            onTriggerNotification = { appt ->
                NotificationHelper.showAppointmentApproachingNotification(
                    context = context,
                    appointmentId = appt.id,
                    clientName = appt.clientName,
                    clientPhone = appt.clientPhone,
                    serviceName = appt.serviceName,
                    startTime = appt.startTime,
                    date = appt.date,
                    roomName = appt.roomName,
                    staffName = appt.staffName,
                    minutesBefore = 15
                )
                Toast.makeText(context, "Đã phát thông báo nhắc hẹn cho ${appt.clientName}!", Toast.LENGTH_SHORT).show()
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
                activeAppointmentDetail = appt.copy(status = newStatus)
                val statusLabel = when (newStatus) {
                    AppointmentStatus.PENDING -> "Chờ duyệt"
                    AppointmentStatus.CONFIRMED -> "Đã xác nhận"
                    AppointmentStatus.IN_SERVICE -> "Đang làm"
                    AppointmentStatus.COMPLETED -> "Hoàn thành & tự động trừ 1 buổi liệu trình"
                    AppointmentStatus.CANCELLED -> "Đã hủy hẹn"
                }
                Toast.makeText(context, "Đã cập nhật: $statusLabel", Toast.LENGTH_SHORT).show()
            },
            onDelete = {
                onDeleteAppointment(appt.id)
                activeAppointmentDetail = null
                Toast.makeText(context, "Đã xóa lịch hẹn", Toast.LENGTH_SHORT).show()
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
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

                    if (appointment.price <= 0L) {
                        Surface(
                            shape = CircleShape,
                            color = SpaGoldContainer
                        ) {
                            Text(
                                text = "Trừ gói",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpaGoldPrimary
                            )
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentDialog(
    defaultDate: String,
    technicians: List<TechnicianStaff> = emptyList(),
    clients: List<Client> = emptyList(),
    catalogPackages: List<SpaServicePackage> = emptyList(),
    onDismissRequest: () -> Unit,
    onSave: (Appointment) -> Unit,
    onAddNewClient: ((Client) -> Unit)? = null
) {
    var appointmentDate by remember { mutableStateOf(defaultDate) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var selectedClient by remember { mutableStateOf<Client?>(null) }
    var saveAsNewClient by remember { mutableStateOf(false) }

    var serviceName by remember { mutableStateOf("") }
    var staffName by remember(technicians) { mutableStateOf(technicians.firstOrNull()?.name ?: "") }
    var roomName by remember { mutableStateOf("Phòng VIP 01") }
    var startTime by remember { mutableStateOf("10:00") }
    var duration by remember { mutableStateOf("60") }
    var priceText by remember { mutableStateOf("") }

    val commonTimes = listOf("08:30", "09:30", "10:30", "11:30", "13:30", "14:30", "15:30", "16:30", "17:30", "18:30")
    val roomOptions = listOf("Phòng VIP 01", "Phòng VIP 02", "Phòng Chăm Sóc 03", "Phòng Dưỡng Sinh Sen", "Phòng Laser 01")

    // Lọc nhanh danh sách khách hàng đang có sẵn theo từ khóa tên hoặc SĐT
    val trimmedSearch = clientName.trim()
    val suggestedClients = remember(trimmedSearch, clients, selectedClient) {
        if (trimmedSearch.isEmpty() || (selectedClient != null && selectedClient?.fullName == clientName)) {
            emptyList()
        } else {
            clients.filter {
                it.fullName.contains(trimmedSearch, ignoreCase = true) ||
                it.phone.contains(trimmedSearch)
            }.take(5)
        }
    }

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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tạo Lịch Hẹn Mới",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SageGreenPrimary
                )

                // Ô chọn ngày hẹn thực hiện dịch vụ
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, WarmBeigeBorder, RoundedCornerShape(8.dp))
                        .clickable { showDatePickerDialog = true }
                        .padding(12.dp),
                    color = WarmBeigeSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SageGreenPrimary)
                            Column {
                                Text("Ngày thực hiện dịch vụ", style = MaterialTheme.typography.bodySmall, color = CharcoalTextSecondary)
                                Text(appointmentDate, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = SageGreenPrimary)
                            }
                        }
                        TextButton(onClick = { showDatePickerDialog = true }) {
                            Text("Đổi ngày", color = SageGreenPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 1. TÊN KHÁCH HÀNG & GỢI Ý NHANH
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { newName ->
                            clientName = newName
                            if (selectedClient != null && selectedClient?.fullName != newName) {
                                selectedClient = null
                                saveAsNewClient = true
                            }
                        },
                        label = { Text("Tên khách hàng *") },
                        placeholder = { Text("Gõ tên để tìm khách quen hoặc nhập khách mới") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SageGreenPrimary) },
                        trailingIcon = {
                            if (selectedClient != null) {
                                IconButton(onClick = {
                                    selectedClient = null
                                    clientName = ""
                                    clientPhone = ""
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Bỏ chọn", tint = CharcoalTextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Gợi ý danh sách khách hàng có sẵn khi gõ
                    if (suggestedClients.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SageGreenPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            color = WarmBeigeSurfaceVariant,
                            shadowElevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text(
                                    "Khách hàng có sẵn trong hệ thống (chạm để chọn):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SageGreenPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                suggestedClients.forEach { c ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                selectedClient = c
                                                clientName = c.fullName
                                                clientPhone = c.phone
                                                saveAsNewClient = false
                                            }
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(c.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CharcoalTextPrimary)
                                            Text(c.phone, style = MaterialTheme.typography.bodySmall, color = CharcoalTextSecondary)
                                        }
                                        val activePkgs = c.treatmentPackages.filter { it.remainingSessions > 0 }
                                        if (activePkgs.isNotEmpty()) {
                                            Surface(
                                                color = SageGreenContainer,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    "Còn ${activePkgs.sumOf { it.remainingSessions }} buổi",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SageGreenPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = WarmBeigeBorder.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }

                    // Trạng thái đã chọn khách có sẵn
                    if (selectedClient != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = SageGreenContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SageGreenPrimary, modifier = Modifier.size(16.dp))
                                    Text(
                                        "Đã liên kết hồ sơ: ${selectedClient!!.fullName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SageGreenPrimary
                                    )
                                }
                                Text(
                                    selectedClient!!.skinType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CharcoalTextSecondary
                                )
                            }
                        }
                    }
                }

                // 2. SỐ ĐIỆN THOẠI
                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("Số điện thoại *") },
                    placeholder = { Text("Tự điền khi chọn khách quen") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SageGreenPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. OPTION LƯU KHÁCH MỚI NẾU CHƯA CÓ TRONG HỆ THỐNG
                if (selectedClient == null && clientName.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { saveAsNewClient = !saveAsNewClient },
                        shape = RoundedCornerShape(8.dp),
                        color = WarmBeigeSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = saveAsNewClient,
                                onCheckedChange = { saveAsNewClient = it },
                                colors = CheckboxDefaults.colors(checkedColor = SageGreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    "Lưu khách hàng mới vào danh bạ CRM",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )
                                Text(
                                    "Tự động tạo hồ sơ khách hàng để theo dõi lịch sử và liệu trình",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CharcoalTextSecondary
                                )
                            }
                        }
                    }
                }

                // 4. CHỌN GÓI LIỆU TRÌNH CỦA KHÁCH (NẾU CÓ)
                if (selectedClient != null && selectedClient!!.treatmentPackages.any { it.remainingSessions > 0 }) {
                    val clientPkgs = selectedClient!!.treatmentPackages.filter { it.remainingSessions > 0 }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Gói liệu trình của khách (Trừ buổi - 0đ):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenPrimary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(clientPkgs) { pkg ->
                                val isSelected = serviceName == pkg.name
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        serviceName = pkg.name
                                        priceText = "0"
                                        if (pkg.assignedTherapist.isNotBlank()) {
                                            staffName = pkg.assignedTherapist.replace("KTV.", "").trim()
                                        }
                                    },
                                    label = {
                                        Column {
                                            Text(pkg.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                                            Text("Còn ${pkg.remainingSessions}/${pkg.totalSessions} buổi • Trừ gói", fontSize = 10.sp, color = if (isSelected) SageGreenPrimary else CharcoalTextMuted)
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SageGreenContainer,
                                        selectedLabelColor = SageGreenPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // 5. CHỌN NHANH DỊCH VỤ ĐÃ SETUP (CATALOG)
                if (catalogPackages.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Dịch vụ trong danh mục đã setup:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextSecondary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(catalogPackages) { cat ->
                                val isSelected = serviceName == cat.name
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        serviceName = cat.name
                                        duration = cat.durationMinutes.toString()
                                        priceText = cat.price.toString()
                                    },
                                    label = {
                                        Column {
                                            Text(cat.name, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                            Text("${cat.price / 1000}k • ${cat.durationMinutes}p", fontSize = 10.sp, color = if (isSelected) SageGreenPrimary else CharcoalTextMuted)
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SageGreenContainer,
                                        selectedLabelColor = SageGreenPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Tên dịch vụ
                OutlinedTextField(
                    value = serviceName,
                    onValueChange = { serviceName = it },
                    label = { Text("Dịch vụ thực hiện") },
                    placeholder = { Text("Chọn ở trên hoặc nhập dịch vụ tùy biến") },
                    leadingIcon = { Icon(Icons.Default.Spa, contentDescription = null, tint = SageGreenPrimary) },
                    modifier = Modifier.fillMaxWidth()
                )

                // 6. GIỜ HẸN & THỜI LƯỢNG
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Khung giờ phổ biến:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextSecondary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(commonTimes) { t ->
                            val isSelected = startTime == t
                            FilterChip(
                                selected = isSelected,
                                onClick = { startTime = t },
                                label = { Text(t, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreenContainer,
                                    selectedLabelColor = SageGreenPrimary
                                )
                            )
                        }
                    }
                }

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

                // 7. KỸ THUẬT VIÊN
                if (technicians.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Chọn nhanh KTV:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextSecondary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(technicians) { tech ->
                                val isSelected = staffName == tech.name
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { staffName = tech.name },
                                    label = { Text(tech.name, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SageGreenContainer,
                                        selectedLabelColor = SageGreenPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // 8. PHÒNG THỰC HIỆN
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Chọn nhanh phòng:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextSecondary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(roomOptions) { room ->
                            val isSelected = roomName == room
                            FilterChip(
                                selected = isSelected,
                                onClick = { roomName = room },
                                label = { Text(room, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreenContainer,
                                    selectedLabelColor = SageGreenPrimary
                                )
                            )
                        }
                    }
                }

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

                // 9. CHI PHÍ DỰ KIẾN
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Chi phí dự kiến (0 đ nếu trừ gói)") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = SageGreenPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

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
                                // Nếu người dùng chọn lưu khách hàng mới
                                if (saveAsNewClient && selectedClient == null) {
                                    val phoneClean = clientPhone.trim()
                                    val alreadyExists = clients.any { it.phone == phoneClean }
                                    if (!alreadyExists) {
                                        val newClient = Client(
                                        id = "KH-" + (1000..9999).random(),
                                        fullName = clientName.trim(),
                                        phone = clientPhone.trim(),
                                        email = "",
                                        avatarUrl = null,
                                        vipTier = VipTier.STANDARD,
                                        skinType = "Chưa phân loại",
                                        skinConditionNotes = "Tạo tự động từ lịch hẹn ngày $appointmentDate",
                                        allergyAlerts = emptyList(),
                                        contraindications = emptyList(),
                                        treatmentPackages = emptyList(),
                                        serviceHistory = emptyList(),
                                        beforeAfterComparisons = emptyList()
                                    )
                                    onAddNewClient?.invoke(newClient)
                                    }
                                }

                                val dur = duration.toIntOrNull() ?: 60
                                val startHour = startTime.split(":").firstOrNull()?.toIntOrNull() ?: 10
                                val startMin = startTime.split(":").lastOrNull()?.toIntOrNull() ?: 0
                                val totalMin = startHour * 60 + startMin + dur
                                val endHour = (totalMin / 60) % 24
                                val endMinute = totalMin % 60
                                val endTimeFormatted = String.format("%02d:%02d", endHour, endMinute)

                                val finalPrice = priceText.toLongOrNull() ?: 0L

                                val newAppointment = Appointment(
                                    id = "APT-" + (1000..9999).random(),
                                    clientName = clientName.trim(),
                                    clientPhone = clientPhone.trim(),
                                    serviceName = serviceName.trim().ifBlank { "Dịch Vụ Spa" },
                                    durationMinutes = dur,
                                    staffName = staffName.trim(),
                                    roomName = roomName.trim(),
                                    startTime = startTime.trim(),
                                    endTime = endTimeFormatted,
                                    date = appointmentDate,
                                    status = AppointmentStatus.CONFIRMED,
                                    price = finalPrice,
                                    reminderSent = true
                                )
                                onSave(newAppointment)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SageGreenPrimary,
                            contentColor = Color.White
                        ),
                        enabled = clientName.isNotBlank() && clientPhone.isNotBlank()
                    ) {
                        Text("Lưu lịch hẹn", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = remember(appointmentDate) {
                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    sdf.parse(appointmentDate)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            }
        )
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Xác nhận", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePickerDialog = false }) {
                    Text("Hủy", color = CharcoalTextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = WarmBeigeSurface
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = WarmBeigeSurface,
                    titleContentColor = CharcoalTextPrimary,
                    headlineContentColor = SageGreenPrimary,
                    weekdayContentColor = CharcoalTextSecondary,
                    subheadContentColor = CharcoalTextPrimary,
                    yearContentColor = CharcoalTextPrimary,
                    currentYearContentColor = SageGreenPrimary,
                    selectedYearContainerColor = SageGreenPrimary,
                    selectedYearContentColor = Color.White,
                    dayContentColor = CharcoalTextPrimary,
                    disabledDayContentColor = CharcoalTextMuted,
                    selectedDayContainerColor = SageGreenPrimary,
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = SageGreenPrimary,
                    todayContentColor = SageGreenPrimary
                )
            )
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
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appointment.clientName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                    // Status Badge
                    Surface(
                        shape = CircleShape,
                        color = when (appointment.status) {
                            AppointmentStatus.CONFIRMED -> SageGreenContainer
                            AppointmentStatus.IN_SERVICE -> SpaGoldContainer
                            AppointmentStatus.COMPLETED -> SuccessGreenContainer
                            AppointmentStatus.CANCELLED -> AllergyAlertRed.copy(alpha = 0.15f)
                            AppointmentStatus.PENDING -> WarmBeigeSurfaceVariant
                        }
                    ) {
                        Text(
                            text = when (appointment.status) {
                                AppointmentStatus.CONFIRMED -> "Đã xác nhận"
                                AppointmentStatus.IN_SERVICE -> "Đang làm"
                                AppointmentStatus.COMPLETED -> "Hoàn thành"
                                AppointmentStatus.CANCELLED -> "Đã hủy"
                                AppointmentStatus.PENDING -> "Chờ duyệt"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (appointment.status) {
                                AppointmentStatus.CONFIRMED -> SageGreenPrimary
                                AppointmentStatus.IN_SERVICE -> SpaGoldPrimary
                                AppointmentStatus.COMPLETED -> SuccessGreen
                                AppointmentStatus.CANCELLED -> AllergyAlertRed
                                AppointmentStatus.PENDING -> CharcoalTextSecondary
                            }
                        )
                    }
                }
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
                Text("Cập nhật trạng thái thật:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                // Hàng 1: Đã hẹn & Đang làm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onUpdateStatus(AppointmentStatus.CONFIRMED) },
                        modifier = Modifier.weight(1f),
                        colors = if (appointment.status == AppointmentStatus.CONFIRMED) {
                            ButtonDefaults.outlinedButtonColors(containerColor = SageGreenContainer)
                        } else ButtonDefaults.outlinedButtonColors(),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Đã hẹn",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (appointment.status == AppointmentStatus.CONFIRMED) SageGreenPrimary else CharcoalTextPrimary
                        )
                    }

                    Button(
                        onClick = { onUpdateStatus(AppointmentStatus.IN_SERVICE) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (appointment.status == AppointmentStatus.IN_SERVICE) SpaGoldPrimary else WarmBeigeSurfaceVariant,
                            contentColor = if (appointment.status == AppointmentStatus.IN_SERVICE) Color.White else CharcoalTextPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Đang làm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Hàng 2: Hoàn thành & Hủy hẹn
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onUpdateStatus(AppointmentStatus.COMPLETED) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (appointment.status == AppointmentStatus.COMPLETED) SuccessGreen else SuccessGreenContainer,
                            contentColor = if (appointment.status == AppointmentStatus.COMPLETED) Color.White else SuccessGreen
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hoàn thành", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onUpdateStatus(AppointmentStatus.CANCELLED) },
                        modifier = Modifier.weight(1f),
                        colors = if (appointment.status == AppointmentStatus.CANCELLED) {
                            ButtonDefaults.outlinedButtonColors(containerColor = AllergyAlertRed.copy(alpha = 0.1f), contentColor = AllergyAlertRed)
                        } else ButtonDefaults.outlinedButtonColors(contentColor = AllergyAlertRed),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Hủy hẹn", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Với các lịch hẹn trừ gói dịch vụ: Bỏ hẳn option chuyển sang thu ngân QR
                val isPackageSession = appointment.price <= 0L
                val canCheckout = !isPackageSession && appointment.status != AppointmentStatus.COMPLETED && appointment.status != AppointmentStatus.CANCELLED

                if (isPackageSession) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = SpaGoldContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = SpaGoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lịch hẹn trừ gói dịch vụ (Không phát sinh thu ngân QR)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SpaGoldPrimary
                            )
                        }
                    }
                } else if (canCheckout) {
                    Button(
                        onClick = onCheckout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpaGoldPrimary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Chuyển sang Thu Ngân tạo VietQR",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = WarmBeigeSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (appointment.status == AppointmentStatus.COMPLETED) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (appointment.status == AppointmentStatus.COMPLETED) SuccessGreen else AllergyAlertRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (appointment.status == AppointmentStatus.COMPLETED)
                                    "Lịch hẹn đã hoàn thành (Không cần tạo QR)"
                                else
                                    "Lịch hẹn đã hủy (Không thể tạo QR)",
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Nút phát thông báo nhắc nhở ngay
                OutlinedButton(
                    onClick = {
                        NotificationHelper.showAppointmentApproachingNotification(
                            context = context,
                            appointmentId = appointment.id,
                            clientName = appointment.clientName,
                            clientPhone = appointment.clientPhone,
                            serviceName = appointment.serviceName,
                            startTime = appointment.startTime,
                            date = appointment.date,
                            roomName = appointment.roomName,
                            staffName = appointment.staffName,
                            minutesBefore = 15
                        )
                        Toast.makeText(context, "Đã gửi thông báo nhắc hẹn cho ${appointment.clientName}!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreenPrimary),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gửi thông báo nhắc giờ hẹn ngay", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        containerColor = WarmBeigeSurface,
        titleContentColor = CharcoalTextPrimary,
        textContentColor = CharcoalTextPrimary,
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

@Composable
fun AppointmentNotificationCenterDialog(
    appointments: List<Appointment>,
    selectedDate: String,
    routineReminders: List<Triple<Client, TreatmentPackage, String>> = emptyList(),
    onDismissRequest: () -> Unit,
    onTriggerNotification: (Appointment) -> Unit
) {
    val context = LocalContext.current
    var selectedLeadTime by remember { mutableStateOf(15) }
    val hasPermission = remember { NotificationHelper.hasNotificationPermission(context) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = WarmBeigeSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SageGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Trung Tâm Nhắc Hẹn",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                    Text(
                        text = "Thông báo khi sắp tới giờ hẹn",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Trạng thái quyền thông báo
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (hasPermission) SageGreenContainer.copy(alpha = 0.5f) else SpaGoldContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (hasPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasPermission) SageGreenPrimary else SpaGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (hasPermission) "Quyền thông báo: ĐÃ KÍCH HOẠT" else "Quyền thông báo: CHƯA BẬT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (hasPermission) SageGreenPrimary else SpaGoldPrimary
                            )
                            Text(
                                text = if (hasPermission)
                                    "Hệ thống sẽ rung và hiển thị banner thông báo khi sắp đến giờ phục vụ khách."
                                else
                                    "Vui lòng bật quyền thông báo trong cài đặt để nhận thông báo đẩy.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Nút test thông báo mẫu
                Button(
                    onClick = {
                        NotificationHelper.showTestNotification(context)
                        Toast.makeText(context, "Đã gửi thông báo thử nghiệm lên thanh trạng thái!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🔔 Gửi Thử Thông Báo Mẫu Ngay", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                HorizontalDivider(color = WarmBeigeBorder)

                // Cấu hình thời gian báo trước
                Text(
                    text = "Thời gian nhắc trước giờ hẹn:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15 to "Trước 15p", 30 to "Trước 30p", 60 to "Trước 1h").forEach { (mins, label) ->
                        FilterChip(
                            selected = selectedLeadTime == mins,
                            onClick = { selectedLeadTime = mins },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (routineReminders.isNotEmpty()) {
                    HorizontalDivider(color = WarmBeigeBorder)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SageGreenPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Khách đến hạn làm liệu trình (${routineReminders.size} khách):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenPrimary
                        )
                    }

                    routineReminders.forEach { (c, pkg, statusText) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = WarmBeigeSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmBeigeBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(c.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CharcoalTextPrimary)
                                        Text("${pkg.name} • Còn ${pkg.remainingSessions} buổi", style = MaterialTheme.typography.bodySmall, color = CharcoalTextSecondary, fontSize = 11.sp)
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = if (statusText.contains("Quá hạn")) AllergyAlertContainer else SageGreenContainer
                                    ) {
                                        Text(
                                            text = statusText,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (statusText.contains("Quá hạn")) AllergyAlertRed else SageGreenPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${c.phone}"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {}
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Gọi", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val smsText = "Dạ chào chị ${c.fullName}, Spa xin phép nhắc chị sắp đến lịch chăm sóc da buổi tiếp theo của gói ${pkg.name}. Chị nhắn lại em để em giữ lịch cho mình nhé ạ!"
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("sms:${c.phone}")).apply {
                                                    putExtra("sms_body", smsText)
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {}
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Nhắn tin", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            NotificationHelper.showRoutineReminderNotification(
                                                context = context,
                                                packageId = pkg.id,
                                                clientName = c.fullName,
                                                clientPhone = c.phone,
                                                packageName = pkg.name,
                                                intervalDays = pkg.intervalDays,
                                                remainingSessions = pkg.remainingSessions,
                                                recommendedDate = pkg.nextRecommendedDate ?: "Hôm nay"
                                            )
                                            Toast.makeText(context, "Đã gửi thông báo nhắc lịch cho ${c.fullName}!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary, contentColor = Color.White),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Báo máy", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = WarmBeigeBorder)

                // Danh sách lịch hẹn hôm nay cần nhắc nhở
                Text(
                    text = "Danh sách lịch hẹn ngày $selectedDate:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )

                val todayAppointments = appointments.filter { it.date == selectedDate }
                if (todayAppointments.isEmpty()) {
                    Text(
                        text = "Không có cuộc hẹn nào trong ngày này.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalTextMuted
                    )
                } else {
                    todayAppointments.forEach { appt ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WarmBeigeSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${appt.startTime} • ${appt.clientName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalTextPrimary
                                    )
                                    Text(
                                        text = "${appt.serviceName} (${appt.roomName})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Button(
                                    onClick = { onTriggerNotification(appt) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SageGreenContainer,
                                        contentColor = SageGreenPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Báo ngay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary, contentColor = Color.White)
            ) {
                Text("Đóng")
            }
        }
    )
}
