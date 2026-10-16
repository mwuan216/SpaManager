package com.spacare.manager.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.DashboardMetrics
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueDashboardScreen(
    metrics: DashboardMetrics,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Báo Cáo & Doanh Thu",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cập nhật thời gian thực hôm nay",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.DateRange, contentDescription = "Chọn khoảng ngày")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Download, contentDescription = "Xuất file Excel")
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

            // 1. Thẻ Doanh thu hôm nay (Hero KPI Card)
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
                                text = "DOANH THU HÔM NAY",
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
                                        imageVector = Icons.Default.TrendingUp,
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

            // 2. Thẻ Công suất phòng & Lượt khách
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
                                text = "Lịch Hẹn Hôm Nay",
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
                                text = "Công Suất Phòng",
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

            // 3. Cơ cấu nguồn thu (Gói liệu trình vs Dịch vụ lẻ vs Bán lẻ mỹ phẩm)
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

                        // Multi-segment progress bar
                        val totalDist = (metrics.revenueDistribution.treatmentPackages +
                                metrics.revenueDistribution.singleServices +
                                metrics.revenueDistribution.retailProducts).toFloat()

                        val packagePercent = (metrics.revenueDistribution.treatmentPackages / totalDist) * 100
                        val singlePercent = (metrics.revenueDistribution.singleServices / totalDist) * 100
                        val retailPercent = (metrics.revenueDistribution.retailProducts / totalDist) * 100

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(packagePercent)
                                    .fillMaxHeight()
                                    .background(SageGreenPrimary)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(singlePercent)
                                    .fillMaxHeight()
                                    .background(SpaGoldPrimary)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(retailPercent)
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

            // 4. Dịch vụ bán chạy nhất (Top-selling services)
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

            // 5. Hiệu suất Kỹ thuật viên (Staff Performance)
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

