package com.spacare.manager.feature.settings

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.spacare.manager.R
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.*
import java.text.NumberFormat
import java.util.Locale

enum class SettingsTab(val title: String) {
    BANK("Ngân Hàng & VietQR"),
    PACKAGES("Gói Liệu Trình"),
    TECHNICIANS("Đội Ngũ KTV"),
    PROFILE("Thông Tin Spa")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreSettingsScreen(
    storeProfile: StoreProfile,
    catalogPackages: List<SpaServicePackage>,
    technicians: List<TechnicianStaff>,
    onUpdateStoreProfile: (StoreProfile) -> Unit,
    onAddCatalogPackage: (SpaServicePackage) -> Unit,
    onUpdateCatalogPackage: (SpaServicePackage) -> Unit,
    onDeleteCatalogPackage: (String) -> Unit,
    onAddTechnician: (TechnicianStaff) -> Unit,
    onUpdateTechnician: (TechnicianStaff) -> Unit,
    onDeleteTechnician: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(SettingsTab.BANK) }
    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }

    // State chỉnh sửa tài khoản ngân hàng
    var selectedBank by remember(storeProfile) { mutableStateOf(storeProfile.selectedBank) }
    var accountNumber by remember(storeProfile) { mutableStateOf(storeProfile.accountNumber) }
    var accountHolder by remember(storeProfile) { mutableStateOf(storeProfile.accountHolder) }
    var showBankPicker by remember { mutableStateOf(false) }

    // State chỉnh sửa thông tin Spa
    var spaName by remember(storeProfile) { mutableStateOf(storeProfile.name) }
    var hotline by remember(storeProfile) { mutableStateOf(storeProfile.hotline) }
    var address by remember(storeProfile) { mutableStateOf(storeProfile.address) }

    // Dialog state
    var showAddPackageDialog by remember { mutableStateOf(false) }
    var packageToEdit by remember { mutableStateOf<SpaServicePackage?>(null) }
    var showAddTechDialog by remember { mutableStateOf(false) }
    var techToEdit by remember { mutableStateOf<TechnicianStaff?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column {
                            Text(
                                text = "Thiết Lập Cửa Hàng",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = storeProfile.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalTextSecondary
                            )
                        }
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Thanh chuyển Tab cài đặt (LazyRow cuộn ngang không bao giờ bị méo chiều cao)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(SettingsTab.entries) { tab ->
                    FilterChip(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        label = { Text(tab.title, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenContainer,
                            selectedLabelColor = SageGreenOnContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Nội dung theo Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {

                // --- TAB 1: CẤU HÌNH NGÂN HÀNG & VIETQR ---
                if (activeTab == SettingsTab.BANK) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Tài Khoản Ngân Hàng Nhận Tiền",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SageGreenPrimary
                                )
                                Text(
                                    text = "Toàn bộ mã VietQR tại màn Thu Ngân sẽ tự động tạo theo thông tin này.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalTextSecondary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Chọn ngân hàng
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, WarmBeigeBorder, RoundedCornerShape(8.dp))
                                        .clickable { showBankPicker = true }
                                        .padding(12.dp),
                                    color = WarmBeigeSurfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Ngân hàng thụ hưởng",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CharcoalTextSecondary
                                            )
                                            Text(
                                                text = "${selectedBank.shortName} - ${selectedBank.fullName}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = CharcoalTextPrimary
                                            )
                                        }
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SageGreenPrimary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Số tài khoản
                                OutlinedTextField(
                                    value = accountNumber,
                                    onValueChange = { accountNumber = it },
                                    label = { Text("Số tài khoản ngân hàng *") },
                                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = SageGreenPrimary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Chủ tài khoản
                                OutlinedTextField(
                                    value = accountHolder,
                                    onValueChange = { accountHolder = it },
                                    label = { Text("Tên chủ tài khoản (Viết hoa không dấu) *") },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = SageGreenPrimary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Nút Lưu
                                Button(
                                    onClick = {
                                        if (accountNumber.isNotBlank() && accountHolder.isNotBlank()) {
                                            onUpdateStoreProfile(
                                                storeProfile.copy(
                                                    selectedBank = selectedBank,
                                                    accountNumber = accountNumber.trim(),
                                                    accountHolder = accountHolder.trim().uppercase()
                                                )
                                            )
                                            snackbarMessage = "Đã cập nhật tài khoản ngân hàng thành công!"
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SageGreenPrimary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Lưu Cấu Hình Ngân Hàng", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Preview VietQR
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Mã VietQR Mẫu (Xem Trước)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val testUrl = "https://img.vietqr.io/image/${selectedBank.bin}-$accountNumber-compact2.png?amount=500000&addInfo=TEST&accountName=${java.net.URLEncoder.encode(accountHolder, "UTF-8")}"
                                Box(
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, SageGreenContainer, RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = testUrl,
                                        contentDescription = "Preview QR",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                // --- TAB 2: QUẢN LÝ GÓI LIỆU TRÌNH ---
                if (activeTab == SettingsTab.PACKAGES) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Danh Mục Gói Liệu Trình (${catalogPackages.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextPrimary
                            )
                            Button(
                                onClick = { showAddPackageDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary, contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm gói", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    items(catalogPackages, key = { it.id }) { pkg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = pkg.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalTextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row {
                                        IconButton(onClick = { packageToEdit = pkg }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = CharcoalTextSecondary, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = { onDeleteCatalogPackage(pkg.id) }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = AllergyAlertRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = pkg.description, style = MaterialTheme.typography.bodySmall, color = CharcoalTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(color = WarmBeigeSurfaceVariant, shape = MaterialTheme.shapes.small) {
                                        Text(
                                            text = "${pkg.totalSessions} buổi • ${pkg.durationMinutes} phút/buổi",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CharcoalTextPrimary
                                        )
                                    }
                                    Text(
                                        text = "${currencyFormatter.format(pkg.price)} đ",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SageGreenPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // --- TAB 3: QUẢN LÝ ĐỘI NGŨ KỸ THUẬT VIÊN ---
                if (activeTab == SettingsTab.TECHNICIANS) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Đội Ngũ Kỹ Thuật Viên (${technicians.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextPrimary
                            )
                            Button(
                                onClick = { showAddTechDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary, contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm KTV", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    items(technicians, key = { it.id }) { tech ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(SageGreenContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tech.name.split(" ").last().take(1),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SageGreenPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = tech.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.Star, contentDescription = null, tint = SpaGoldPrimary, modifier = Modifier.size(13.dp))
                                        Text(
                                            text = " ${tech.rating}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SpaGoldPrimary
                                        )
                                    }
                                    Text(
                                        text = tech.specialty,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SageGreenPrimary
                                    )
                                    Text(
                                        text = "SĐT: ${tech.phone}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextMuted
                                    )
                                }

                                IconButton(onClick = { techToEdit = tech }, modifier = Modifier.size(30.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = CharcoalTextSecondary, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { onDeleteTechnician(tech.id) }, modifier = Modifier.size(30.dp)) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = AllergyAlertRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // --- TAB 4: THÔNG TIN CƠ SỞ SPA ---
                if (activeTab == SettingsTab.PROFILE) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Thông Tin Cơ Sở Spa",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SageGreenPrimary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = spaName,
                                    onValueChange = { spaName = it },
                                    label = { Text("Tên cơ sở Spa / Thẩm mỹ viện") },
                                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = SageGreenPrimary) },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = hotline,
                                    onValueChange = { hotline = it },
                                    label = { Text("Số điện thoại Hotline") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SageGreenPrimary) },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = address,
                                    onValueChange = { address = it },
                                    label = { Text("Địa chỉ cơ sở") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SageGreenPrimary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        onUpdateStoreProfile(
                                            storeProfile.copy(
                                                name = spaName.trim(),
                                                hotline = hotline.trim(),
                                                address = address.trim()
                                            )
                                        )
                                        snackbarMessage = "Đã lưu thông tin cơ sở Spa!"
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SageGreenPrimary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Lưu Thông Tin Cơ Sở", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal chọn ngân hàng
    if (showBankPicker) {
        AlertDialog(
            onDismissRequest = { showBankPicker = false },
            title = { Text("Chọn Ngân Hàng Của Bạn") },
            containerColor = WarmBeigeSurface,
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(SupportedBanks) { bank ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedBank = bank
                                    showBankPicker = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(bank.shortName, fontWeight = FontWeight.Bold, color = SageGreenPrimary)
                                Text(bank.fullName, style = MaterialTheme.typography.bodySmall, color = CharcoalTextSecondary)
                            }
                            if (selectedBank.bin == bank.bin) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SageGreenPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showBankPicker = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    // Modal Thêm / Sửa Gói Liệu Trình
    if (showAddPackageDialog || packageToEdit != null) {
        val editing = packageToEdit
        var name by remember { mutableStateOf(editing?.name ?: "") }
        var sessions by remember { mutableStateOf(editing?.totalSessions?.toString() ?: "10") }
        var price by remember { mutableStateOf(editing?.price?.toString() ?: "5000000") }
        var duration by remember { mutableStateOf(editing?.durationMinutes?.toString() ?: "75") }
        var desc by remember { mutableStateOf(editing?.description ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddPackageDialog = false
                packageToEdit = null
            },
            title = { Text(if (editing != null) "Sửa Gói Liệu Trình" else "Thêm Gói Liệu Trình Mới") },
            containerColor = WarmBeigeSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên gói *") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = sessions, onValueChange = { sessions = it }, label = { Text("Số buổi") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Thời lượng (p)") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Giá tiền trọn gói (đ)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Mô tả liệu trình") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val pkg = SpaServicePackage(
                                id = editing?.id ?: ("PKG-CAT-" + (1000..9999).random()),
                                name = name.trim(),
                                totalSessions = sessions.toIntOrNull() ?: 10,
                                price = price.toLongOrNull() ?: 0L,
                                durationMinutes = duration.toIntOrNull() ?: 60,
                                description = desc.trim()
                            )
                            if (editing != null) onUpdateCatalogPackage(pkg) else onAddCatalogPackage(pkg)
                            showAddPackageDialog = false
                            packageToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary, contentColor = Color.White)
                ) {
                    Text(if (editing != null) "Cập nhật" else "Lưu gói", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showAddPackageDialog = false
                    packageToEdit = null
                }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Modal Thêm / Sửa Kỹ Thuật Viên
    if (showAddTechDialog || techToEdit != null) {
        val editing = techToEdit
        var name by remember { mutableStateOf(editing?.name ?: "") }
        var phone by remember { mutableStateOf(editing?.phone ?: "") }
        var specialty by remember { mutableStateOf(editing?.specialty ?: "Chuyên viên Chăm sóc da") }

        AlertDialog(
            onDismissRequest = {
                showAddTechDialog = false
                techToEdit = null
            },
            title = { Text(if (editing != null) "Sửa Thông Tin KTV" else "Thêm Kỹ Thuật Viên Mới") },
            containerColor = WarmBeigeSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên KTV *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Số điện thoại *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = specialty, onValueChange = { specialty = it }, label = { Text("Chuyên môn trị liệu") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val tech = TechnicianStaff(
                                id = editing?.id ?: ("TECH-" + (1000..9999).random()),
                                name = name.trim(),
                                phone = phone.trim(),
                                specialty = specialty.trim(),
                                rating = editing?.rating ?: 5.0f,
                                isActive = true
                            )
                            if (editing != null) onUpdateTechnician(tech) else onAddTechnician(tech)
                            showAddTechDialog = false
                            techToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary, contentColor = Color.White)
                ) {
                    Text(if (editing != null) "Cập nhật" else "Lưu KTV", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showAddTechDialog = false
                    techToEdit = null
                }) {
                    Text("Hủy")
                }
            }
        )
    }
}

