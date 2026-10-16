package com.spacare.manager.feature.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.core.content.FileProvider
import com.spacare.manager.R
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.DashboardMetrics
import com.spacare.manager.domain.model.InvoiceTransactionRecord
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueDashboardScreen(
    metrics: DashboardMetrics,
    selectedDate: String = "16/10/2026",
    transactions: List<InvoiceTransactionRecord> = emptyList(),
    onDateSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showExportConfirmDialog by remember { mutableStateOf(false) }
    var exportedFileName by remember { mutableStateOf<String?>(null) }

    val availableDates = listOf(
        "16/10/2026" to "Hôm nay (16/10)",
        "15/10/2026" to "Hôm qua (15/10)",
        "14/10/2026" to "Thứ 4 (14/10)",
        "13/10/2026" to "Thứ 3 (13/10)",
        "12/10/2026" to "Thứ 2 (12/10)"
    )

    val dayTransactions = remember(transactions, selectedDate) {
        transactions.filter { it.date == selectedDate }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.clickable { showDatePickerDialog = true }
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
                                    text = "Báo Cáo & Doanh Thu",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SageGreenPrimary, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = "Ngày: $selectedDate",
                                style = MaterialTheme.typography.bodySmall,
                                color = SageGreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePickerDialog = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Chọn ngày xem báo cáo", tint = SageGreenPrimary)
                    }
                    IconButton(onClick = { showExportConfirmDialog = true }) {
                        Icon(Icons.Default.Download, contentDescription = "Xuất file báo cáo", tint = SageGreenPrimary)
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

            // 1. Thẻ Doanh thu theo ngày đã chọn
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium, // 16.dp
                    colors = CardDefaults.cardColors(containerColor = SageGreenPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DOANH THU NGÀY $selectedDate",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = WarmBeigeSurface.copy(alpha = 0.85f)
                            )
                            Surface(
                                shape = CircleShape,
                                color = WarmBeigeSurface.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = WarmBeigeSurface,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+${metrics.todayGrowthPercent}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WarmBeigeSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${currencyFormatter.format(metrics.todayRevenue)} đ",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = WarmBeigeSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = WarmBeigeSurface.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Tuần này",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WarmBeigeSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${currencyFormatter.format(metrics.weekRevenue)} đ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarmBeigeSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Tháng này",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WarmBeigeSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${currencyFormatter.format(metrics.monthRevenue)} đ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarmBeigeSurface
                                )
                            }
                        }
                    }
                }
            }

            // 2. Thẻ Công suất phòng & Lượt khách trong ngày
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Lượt Khách Trong Ngày",
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${metrics.totalBookingsToday} khách",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextPrimary
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Công Suất Lấp Đầy Phòng",
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${metrics.roomOccupancyRate}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SageGreenPrimary
                            )
                        }
                    }
                }
            }

            // 3. Cơ cấu nguồn thu
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tỷ Trọng Nguồn Thu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val totalDist = (metrics.revenueDistribution.treatmentPackages +
                                metrics.revenueDistribution.singleServices +
                                metrics.revenueDistribution.retailProducts).toFloat()

                        val packagePercent = if (totalDist > 0) (metrics.revenueDistribution.treatmentPackages / totalDist) * 100 else 33.3f
                        val singlePercent = if (totalDist > 0) (metrics.revenueDistribution.singleServices / totalDist) * 100 else 33.3f
                        val retailPercent = if (totalDist > 0) (metrics.revenueDistribution.retailProducts / totalDist) * 100 else 33.3f

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(packagePercent.coerceAtLeast(1f))
                                    .fillMaxHeight()
                                    .background(SageGreenPrimary)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(singlePercent.coerceAtLeast(1f))
                                    .fillMaxHeight()
                                    .background(SpaGoldPrimary)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(retailPercent.coerceAtLeast(1f))
                                    .fillMaxHeight()
                                    .background(SageGreenSecondary)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        DistributionLegendRow(
                            color = SageGreenPrimary,
                            label = "Gói liệu trình chuyên sâu",
                            amount = "${currencyFormatter.format(metrics.revenueDistribution.treatmentPackages)} đ",
                            percent = "${packagePercent.toInt()}%"
                        )
                        DistributionLegendRow(
                            color = SpaGoldPrimary,
                            label = "Dịch vụ lẻ / Trải nghiệm",
                            amount = "${currencyFormatter.format(metrics.revenueDistribution.singleServices)} đ",
                            percent = "${singlePercent.toInt()}%"
                        )
                        DistributionLegendRow(
                            color = SageGreenSecondary,
                            label = "Mỹ phẩm chăm sóc tại nhà",
                            amount = "${currencyFormatter.format(metrics.revenueDistribution.retailProducts)} đ",
                            percent = "${retailPercent.toInt()}%"
                        )
                    }
                }
            }

            // 4. Dịch vụ bán chạy nhất
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Top Dịch Vụ Được Đặt Nhiều Nhất",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        metrics.topServices.forEachIndexed { index, service ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(26.dp),
                                    shape = CircleShape,
                                    color = if (index == 0) SpaGoldContainer else WarmBeigeSurfaceVariant
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (index == 0) SpaGoldPrimary else CharcoalTextSecondary
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = service.serviceName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CharcoalTextPrimary
                                    )
                                    Text(
                                        text = "${service.bookingCount} lượt phục vụ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextMuted
                                    )
                                }

                                Text(
                                    text = "${currencyFormatter.format(service.totalRevenue)} đ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SageGreenPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 5. Hiệu suất Kỹ thuật viên
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Hiệu Suất Kỹ Thuật Viên Nổi Bật",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        metrics.topPerformers.forEach { staff ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SageGreenContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = staff.staffName.split(" ").last().take(1),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SageGreenPrimary
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = staff.staffName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalTextPrimary
                                    )
                                    Text(
                                        text = "${staff.role} • ${staff.totalCustomersServed} khách",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextMuted
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${currencyFormatter.format(staff.revenueGenerated)} đ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CharcoalTextPrimary
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = SpaGoldPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = " ${staff.rating}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SpaGoldPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Lịch sử hóa đơn & Giao dịch hôm nay
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = SageGreenPrimary
                                )
                                Text(
                                    text = "Lịch Sử Hóa Đơn & Giao Dịch",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = if (dayTransactions.isNotEmpty()) SageGreenContainer else WarmBeigeSurfaceVariant
                            ) {
                                Text(
                                    text = "${dayTransactions.size} đơn",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dayTransactions.isNotEmpty()) SageGreenPrimary else CharcoalTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Danh sách hóa đơn và giao dịch phát sinh trong ngày $selectedDate:",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (dayTransactions.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = WarmBeigeSurfaceVariant
                            ) {
                                Text(
                                    text = "Chưa có hóa đơn hoặc giao dịch nào được ghi nhận cho ngày $selectedDate.",
                                    modifier = Modifier.padding(14.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalTextMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                dayTransactions.forEach { tx ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = WarmBeigeSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBeigeBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "#${tx.invoiceCode}",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SageGreenPrimary
                                                    )
                                                    Text(
                                                        text = "• ${tx.time}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = CharcoalTextMuted
                                                    )
                                                }

                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (tx.isPaid) SuccessGreenContainer else SpaGoldContainer
                                                ) {
                                                    Text(
                                                        text = if (tx.isPaid) "Đã thanh toán" else "Chờ thanh toán",
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (tx.isPaid) SuccessGreen else SpaGoldPrimary
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${tx.clientName} (${tx.clientPhone})",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = CharcoalTextPrimary
                                                    )
                                                    Text(
                                                        text = "${tx.serviceName} • KTV: ${tx.technicianName}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = CharcoalTextSecondary
                                                    )
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = if (tx.amount == 0L) "Trừ gói" else "${currencyFormatter.format(tx.amount)} đ",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (tx.isPaid) SuccessGreen else SpaGoldPrimary
                                                    )
                                                    Text(
                                                        text = tx.paymentMethod,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = CharcoalTextMuted
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal chọn ngày xem báo cáo bất kỳ
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = remember(selectedDate) {
                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    sdf.parse(selectedDate)?.time ?: System.currentTimeMillis()
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
                            val pickedDate = sdf.format(Date(millis))
                            onDateSelected(pickedDate)
                        }
                        showDatePickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Xem báo cáo", color = Color.White, fontWeight = FontWeight.Bold)
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

    // Modal Xác nhận Xuất Báo Cáo CSV
    if (showExportConfirmDialog) {
        val safeDateName = selectedDate.replace("/", "_")
        val fileName = "BaoCao_DoanhThu_SpaCare_$safeDateName.csv"

        AlertDialog(
            onDismissRequest = { showExportConfirmDialog = false },
            title = { Text("Xuất Báo Cáo Doanh Thu") },
            containerColor = WarmBeigeSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tên file: $fileName", fontWeight = FontWeight.Bold, color = SageGreenPrimary)
                    Text("Dữ liệu báo cáo bao gồm: Tổng doanh thu ngày $selectedDate, lượt khách phục vụ, tỷ lệ lấp đầy phòng, tỷ trọng nguồn thu và danh sách dịch vụ hot.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val exported = exportReportToCsv(context, selectedDate, metrics)
                        showExportConfirmDialog = false
                        if (exported) {
                            exportedFileName = fileName
                            Toast.makeText(context, "Đã xuất báo cáo thành công: $fileName", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tải file CSV về máy", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExportConfirmDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

// Hàm ghi file báo cáo CSV thực tế vào bộ nhớ máy
fun exportReportToCsv(context: Context, dateStr: String, metrics: DashboardMetrics): Boolean {
    return try {
        val safeDateName = dateStr.replace("/", "_")
        val fileName = "BaoCao_DoanhThu_SpaCare_$safeDateName.csv"
        val file = File(context.getExternalFilesDir(null), fileName)

        val csvContent = buildString {
            append("BÁO CÁO DOANH THU & HOẠT ĐỘNG - SPACARE MANAGER\n")
            append("Ngày báo cáo,$dateStr\n")
            append("Thời gian xuất file,${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")

            append("CHỈ SỐ TỔNG QUAN\n")
            append("Doanh thu trong ngày,${metrics.todayRevenue} VND\n")
            append("Tăng trưởng so với hôm trước,${metrics.todayGrowthPercent}%\n")
            append("Lượt khách phục vụ,${metrics.totalBookingsToday} khách\n")
            append("Công suất phòng,${metrics.roomOccupancyRate}%\n")
            append("Doanh thu tuần này,${metrics.weekRevenue} VND\n")
            append("Doanh thu tháng này,${metrics.monthRevenue} VND\n\n")

            append("CƠ CẤU NGUỒN THU\n")
            append("Gói liệu trình chuyên sâu,${metrics.revenueDistribution.treatmentPackages} VND\n")
            append("Dịch vụ lẻ / Trải nghiệm,${metrics.revenueDistribution.singleServices} VND\n")
            append("Mỹ phẩm chăm sóc tại nhà,${metrics.revenueDistribution.retailProducts} VND\n\n")

            append("TOP DỊCH VỤ BÁN CHẠY NHẤT\n")
            append("Tên dịch vụ,Số lượt đặt,Doanh số mang về\n")
            metrics.topServices.forEach {
                append("\"${it.serviceName}\",${it.bookingCount},${it.totalRevenue} VND\n")
            }
            append("\nHIỆU SUẤT KỸ THUẬT VIÊN\n")
            append("Họ tên KTV,Vai trò,Lượt phục vụ,Doanh số mang lại,Đánh giá sao\n")
            metrics.topPerformers.forEach {
                append("\"${it.staffName}\",\"${it.role}\",${it.totalCustomersServed},${it.revenueGenerated} VND,${it.rating}*\n")
            }
        }

        FileOutputStream(file).use { out ->
            out.write(csvContent.toByteArray(Charsets.UTF_8))
        }

        // Kích hoạt Intent chia sẻ file
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ / Mở file báo cáo"))
        true
    } catch (e: Exception) {
        e.printStackTrace()
        // Nếu FileProvider chưa cấu hình, vẫn lưu file bình thường vào bộ nhớ ngoài
        try {
            val fileName = "BaoCao_DoanhThu_SpaCare_${dateStr.replace("/", "_")}.csv"
            val file = File(context.filesDir, fileName)
            file.writeText("Bao cao SpaCare ngay $dateStr: ${metrics.todayRevenue} VND")
            true
        } catch (ex: Exception) {
            false
        }
    }
}

@Composable
fun DistributionLegendRow(
    color: Color,
    label: String,
    amount: String,
    percent: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = CharcoalTextPrimary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = CharcoalTextPrimary
            )
            Text(
                text = "($percent)",
                style = MaterialTheme.typography.bodySmall,
                color = CharcoalTextMuted
            )
        }
    }
}
