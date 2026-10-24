package com.spacare.manager.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Spa
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
import com.spacare.manager.domain.model.TreatmentPackage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentPackageCard(
    pkg: TreatmentPackage,
    onUseSessionClick: () -> Unit = {},
    onCheckInSessionWithCycle: ((intervalDays: Int, nextDate: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showCheckInDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // 16.dp
        colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SageGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = pkg.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                        Text(
                            text = "KTV phụ trách: ${pkg.assignedTherapist}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bộ đếm buổi liệu trình (Remaining session counter)
            Surface(
                shape = MaterialTheme.shapes.small,
                color = WarmBeigeSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Số buổi còn lại",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalTextSecondary
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${pkg.remainingSessions}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = SageGreenPrimary
                            )
                            Text(
                                text = " / ${pkg.totalSessions} buổi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CharcoalTextSecondary,
                                modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                            )
                        }
                    }

                    val isCompleted = pkg.remainingSessions <= 0 || pkg.usedSessions >= pkg.totalSessions
                    Surface(
                        shape = CircleShape,
                        color = when {
                            isCompleted -> SuccessGreenContainer
                            pkg.remainingSessions <= 2 -> AllergyAlertContainer
                            else -> SageGreenContainer
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = when {
                                    isCompleted -> "Hoàn thành"
                                    pkg.remainingSessions <= 2 -> "Sắp hết liệu trình"
                                    else -> "Đang kích hoạt"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    isCompleted -> SuccessGreen
                                    pkg.remainingSessions <= 2 -> AllergyAlertRed
                                    else -> SageGreenPrimary
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Thanh tiến độ trực quan (Progress Indicator)
            val isCompleted = pkg.remainingSessions <= 0 || pkg.usedSessions >= pkg.totalSessions
            val progress = if (isCompleted) 1.0f else (pkg.usedSessions.toFloat() / pkg.totalSessions.toFloat().coerceAtLeast(1f))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (isCompleted) SuccessGreen else SageGreenPrimary,
                trackColor = WarmBeigeSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Chu kỳ và lịch hẹn tiếp theo
            if (!isCompleted) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SageGreenContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Chu kỳ: ${pkg.intervalDays} ngày/buổi",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = SageGreenPrimary
                            )
                        }

                        Text(
                            text = if (!pkg.nextRecommendedDate.isNullOrBlank()) "Lịch tới: ${pkg.nextRecommendedDate}" else "Chưa xếp lịch",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hạn dùng: ${pkg.expiryDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalTextMuted
                )

                if (isCompleted) {
                    Surface(
                        shape = CircleShape,
                        color = SuccessGreenContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Đã hoàn thành gói",
                                style = MaterialTheme.typography.labelMedium,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            if (onCheckInSessionWithCycle != null) {
                                showCheckInDialog = true
                            } else {
                                onUseSessionClick()
                            }
                        },
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SageGreenPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Điểm danh buổi",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Modal Điểm Danh Buổi & Thiết Lập Chu Kỳ Lịch Tiếp Theo
    if (showCheckInDialog) {
        val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
        var selectedInterval by remember { mutableStateOf(pkg.intervalDays) }
        val cycleOptions = remember { listOf(5, 7, 10, 14, 21, 30) }

        val calculatedNextDate = remember(selectedInterval) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, selectedInterval)
            sdf.format(cal.time)
        }

        AlertDialog(
            onDismissRequest = { showCheckInDialog = false },
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
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SageGreenPrimary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "Điểm Danh Buổi Liệu Trình",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                        Text(
                            text = "Buổi ${pkg.usedSessions + 1} / ${pkg.totalSessions} (${pkg.name})",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Khách thực hiện buổi chăm sóc hôm nay. Hệ thống sẽ tự động trừ 1 buổi và lên lịch nhắc buổi tiếp theo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalTextSecondary
                    )

                    HorizontalDivider(color = WarmBeigeBorder)

                    Text(
                        text = "Chọn chu kỳ làm buổi tiếp theo:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(cycleOptions) { days ->
                            val isSelected = selectedInterval == days
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedInterval = days },
                                label = { Text("$days ngày", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreenContainer,
                                    selectedLabelColor = SageGreenPrimary
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = WarmBeigeSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SageGreenPrimary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Ngày hẹn tiếp theo: $calculatedNextDate",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SageGreenPrimary
                                )
                            }
                            Text(
                                text = "Hệ thống sẽ nhắc bạn trước ngày $calculatedNextDate để bạn liên hệ dặn dò khách hàng.",
                                style = MaterialTheme.typography.labelSmall,
                                color = CharcoalTextSecondary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCheckInSessionWithCycle?.invoke(selectedInterval, calculatedNextDate)
                        showCheckInDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xác nhận điểm danh", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCheckInDialog = false }) {
                    Text("Hủy", color = CharcoalTextSecondary)
                }
            }
        )
    }
}
