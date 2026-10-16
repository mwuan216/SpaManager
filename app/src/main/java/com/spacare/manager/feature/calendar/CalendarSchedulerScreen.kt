package com.spacare.manager.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.Appointment
import com.spacare.manager.domain.model.AppointmentStatus

enum class CalendarViewMode {
    TIMELINE,
    BY_STAFF,
    BY_ROOM
}

data class DayItem(
    val dayOfWeek: String,
    val dayNumber: String,
    val isSelected: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSchedulerScreen(
    appointments: List<Appointment>,
    onAppointmentClick: (Appointment) -> Unit = {},
    onCreateAppointmentClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedViewMode by remember { mutableStateOf(CalendarViewMode.TIMELINE) }
    var selectedDayIndex by remember { mutableIntStateOf(3) } // Thứ 6 ngày 16

    val daysList = remember {
        listOf(
            DayItem("T3", "13"),
            DayItem("T4", "14"),
            DayItem("T5", "15"),
            DayItem("T6", "16", isSelected = true),
            DayItem("T7", "17"),
            DayItem("CN", "18"),
            DayItem("T2", "19")
        )
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
                            text = "Tháng 10, 2026 • 19 cuộc hẹn hôm nay",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FilterList, contentDescription = "Lọc")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
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
                onClick = onCreateAppointmentClick,
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
            // 1. Dải chọn ngày trong tuần (Date Selector Bar)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(daysList.size) { index ->
                    val day = daysList[index]
                    val isSelected = index == selectedDayIndex

                    Surface(
                        modifier = Modifier
                            .width(52.dp)
                            .height(68.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedDayIndex = index },
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

            // 2. Chuyển đổi chế độ xem (View Filter Chips: Dòng thời gian / Theo Kỹ thuật viên / Theo Phòng)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedViewMode == CalendarViewMode.TIMELINE,
                    onClick = { selectedViewMode = CalendarViewMode.TIMELINE },
                    label = { Text("Dòng thời gian") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SageGreenContainer,
                        selectedLabelColor = SageGreenOnContainer
                    )
                )
                FilterChip(
                    selected = selectedViewMode == CalendarViewMode.BY_STAFF,
                    onClick = { selectedViewMode = CalendarViewMode.BY_STAFF },
                    label = { Text("Theo Kỹ thuật viên") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SageGreenContainer,
                        selectedLabelColor = SageGreenOnContainer
                    )
                )
                FilterChip(
                    selected = selectedViewMode == CalendarViewMode.BY_ROOM,
                    onClick = { selectedViewMode = CalendarViewMode.BY_ROOM },
                    label = { Text("Theo Phòng") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SageGreenContainer,
                        selectedLabelColor = SageGreenOnContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Danh sách cuộc hẹn (Timeline slots)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(appointments) { appointment ->
                    AppointmentScheduleCard(
                        appointment = appointment,
                        onClick = { onAppointmentClick(appointment) }
                    )
                }
            }
        }
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
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cột thời gian bên trái
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(54.dp)
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
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalTextSecondary
                    )
                }
            }

            // Đường phân cách dọc
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(80.dp)
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

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CharcoalTextMuted,
                            modifier = Modifier.size(14.dp)
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
                            modifier = Modifier.size(14.dp)
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
                    Spacer(modifier = Modifier.height(6.dp))
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
                            text = "Đã gửi SMS / ZNS nhắc hẹn 2h trước",
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

