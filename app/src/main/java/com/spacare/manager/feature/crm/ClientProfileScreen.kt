package com.spacare.manager.feature.crm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.*
import com.spacare.manager.feature.crm.components.AllergyAlertCard
import com.spacare.manager.feature.crm.components.BeforeAfterGalleryCard
import com.spacare.manager.feature.crm.components.ClientFormDialog
import com.spacare.manager.feature.crm.components.TreatmentPackageCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(
    client: Client,
    onBackClick: () -> Unit,
    onBookAppointmentClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onUpdateClient: (Client) -> Unit,
    onDeleteClient: (String) -> Unit,
    onCheckInSession: (packageId: String) -> Unit,
    onAddTreatmentPackage: (TreatmentPackage) -> Unit,
    onAddBeforeAfter: (BeforeAfterComparison) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAddPackageDialog by remember { mutableStateOf(false) }
    var showAddBeforeAfterDialog by remember { mutableStateOf(false) }

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
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Thanh toán", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // 2. Thẻ Cảnh báo dị ứng & Chống chỉ định
            if (client.allergyAlerts.isNotEmpty() || client.contraindications.isNotEmpty()) {
                item {
                    AllergyAlertCard(
                        allergies = client.allergyAlerts,
                        contraindications = client.contraindications
                    )
                }
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

                        if (client.skinConditionNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = client.skinConditionNotes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CharcoalTextSecondary
                            )
                        }
                    }
                }
            }

            // 4. Gói liệu trình đang sử dụng (Package Tracking)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gói Liệu Trình Đang Sử Dụng",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                    TextButton(onClick = { showAddPackageDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm gói", color = SageGreenPrimary)
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

            // 5. Thư viện Before & After
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hồ Sơ Hình Ảnh Trước & Sau",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                    TextButton(onClick = { showAddBeforeAfterDialog = true }) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm ảnh", color = SageGreenPrimary)
                    }
                }
            }

            if (client.beforeAfterComparisons.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = WarmBeigeSurfaceVariant)
                    ) {
                        Text(
                            text = "Chưa có hình ảnh so sánh trước & sau cho khách hàng này.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CharcoalTextMuted
                        )
                    }
                }
            } else {
                items(client.beforeAfterComparisons.size) { index ->
                    BeforeAfterGalleryCard(comparison = client.beforeAfterComparisons[index])
                }
            }

            // 6. Lịch sử sử dụng dịch vụ
            if (client.serviceHistory.isNotEmpty()) {
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
        var therapist by remember { mutableStateOf("KTV. Thảo My") }

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
                        val newPackage = TreatmentPackage(
                            id = "PKG-" + (1000..9999).random(),
                            name = pkgName.ifBlank { "Liệu Trình Chuyên Sâu Spa" },
                            totalSessions = sessions,
                            usedSessions = 0,
                            remainingSessions = sessions,
                            startDate = "16/10/2026",
                            expiryDate = "16/04/2027",
                            assignedTherapist = therapist
                        )
                        onAddTreatmentPackage(newPackage)
                        showAddPackageDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                ) {
                    Text("Lưu gói")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddPackageDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog Thêm ảnh Before & After mới
    if (showAddBeforeAfterDialog) {
        var treatmentTitle by remember { mutableStateOf("Liệu Trình Chăm Sóc Da") }
        var notes by remember { mutableStateOf("Cải thiện sắc tố da, giảm viêm đỏ rõ rệt.") }

        AlertDialog(
            onDismissRequest = { showAddBeforeAfterDialog = false },
            title = { Text("Thêm Ảnh So Sánh Trước & Sau") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = treatmentTitle,
                        onValueChange = { treatmentTitle = it },
                        label = { Text("Tên liệu trình") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Đánh giá lâm sàng") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val record = BeforeAfterComparison(
                            id = "BA-" + (1000..9999).random(),
                            treatmentName = treatmentTitle,
                            beforeDate = "16/10/2026 (Buổi 1)",
                            afterDate = "16/11/2026 (Buổi 5)",
                            beforeImageUrl = "https://images.unsplash.com/photo-1512290900672-1f55b9e59d4c?auto=format&fit=crop&w=600&q=80",
                            afterImageUrl = "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?auto=format&fit=crop&w=600&q=80",
                            sessionMilestone = "Tiến độ: Buổi 1 ➔ Buổi 5",
                            technicianName = "Chuyên viên Thảo My",
                            clinicalNotes = notes
                        )
                        onAddBeforeAfter(record)
                        showAddBeforeAfterDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                ) {
                    Text("Thêm so sánh")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddBeforeAfterDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}
