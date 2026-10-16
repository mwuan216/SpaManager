package com.spacare.manager.feature.crm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.Client
import com.spacare.manager.domain.model.VipTier
import com.spacare.manager.feature.crm.components.AllergyAlertCard
import com.spacare.manager.feature.crm.components.BeforeAfterGalleryCard
import com.spacare.manager.feature.crm.components.TreatmentPackageCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(
    client: Client,
    onBackClick: () -> Unit = {},
    onBookAppointmentClick: () -> Unit = {},
    onCheckoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Chỉnh sửa")
                    }
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Tùy chọn")
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
                            // Avatar tròn thanh lịch
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
                                Spacer(modifier = Modifier.height(6.dp))
                                // VIP Chip
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

                        // Nút liên lạc nhanh (Call / Zalo / Đặt lịch)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onBookAppointmentClick,
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Đặt lịch mới", style = MaterialTheme.typography.labelMedium)
                            }

                            OutlinedButton(
                                onClick = onCheckoutClick,
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreenPrimary)
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Thanh toán", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // 2. Thẻ Cảnh báo dị ứng & Chống chỉ định (Allergy Alert)
            item {
                AllergyAlertCard(
                    allergies = client.allergyAlerts,
                    contraindications = client.contraindications
                )
            }

            // 3. Tình trạng da & Ghi chú da liễu
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Tình Trạng Da & Hồ Sơ Y Khoa",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = WarmBeigeSurfaceVariant
                        ) {
                            Text(
                                text = "Phân loại: ${client.skinType}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = client.skinConditionNotes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CharcoalTextSecondary
                        )
                    }
                }
            }

            // 4. Theo dõi gói dịch vụ (Package Tracking - Remaining Sessions Counter)
            item {
                Text(
                    text = "Gói Liệu Trình Đang Sử Dụng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
            }

            items(client.treatmentPackages.size) { index ->
                TreatmentPackageCard(pkg = client.treatmentPackages[index])
            }

            // 5. Thư viện Before & After (Side-by-Side Photo Comparison)
            item {
                Text(
                    text = "Hồ Sơ Hình Ảnh Trước & Sau",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
            }

            items(client.beforeAfterComparisons.size) { index ->
                BeforeAfterGalleryCard(comparison = client.beforeAfterComparisons[index])
            }

            // 6. Lịch sử sử dụng dịch vụ (Service History)
            item {
                Text(
                    text = "Lịch Sử Sử Dụng Dịch Vụ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
            }

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
    }
}

