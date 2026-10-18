package com.spacare.manager.feature.settings

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ShortText
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
import com.spacare.manager.core.notification.NotificationHelper
import com.spacare.manager.domain.model.*
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import com.spacare.manager.core.license.LicenseManager
import java.text.NumberFormat
import java.util.Locale

enum class SettingsTab(val title: String) {
    BANK("Ngân Hàng & VietQR"),
    PACKAGES("Gói Liệu Trình"),
    TECHNICIANS("Đội Ngũ KTV"),
    PROFILE("Thông Tin Spa"),
    LICENSE("Bản Quyền"),
    BACKUP("Sao Lưu & Dữ Liệu")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreSettingsScreen(
    storeProfile: StoreProfile,
    catalogPackages: List<SpaServicePackage>,
    technicians: List<TechnicianStaff>,
    isRealUsageMode: Boolean = false,
    clientCount: Int = 0,
    appointmentCount: Int = 0,
    transactionCount: Int = 0,
    onStartRealUsage: () -> Unit = {},
    onRestoreDemoData: () -> Unit = {},
    onExportBackup: () -> Unit = {},
    onImportBackup: (String) -> Unit = {},
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
    var defaultMemo by remember(storeProfile) { mutableStateOf(storeProfile.defaultMemo) }
    var showBankPicker by remember { mutableStateOf(false) }
    var bankSearchQuery by remember { mutableStateOf("") }

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

    // State quản lý bản quyền & key kích hoạt
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val licenseManager = remember { LicenseManager.getInstance(context) }
    var licenseStatus by remember { mutableStateOf(licenseManager.getLicenseStatus()) }
    var inputLicenseKey by remember { mutableStateOf("") }
    var licenseError by remember { mutableStateOf<String?>(null) }
    var showConfirmRealUsageDialog by remember { mutableStateOf(false) }
    var showConfirmRestoreDemoDialog by remember { mutableStateOf(false) }
    var showConfirmImportDialog by remember { mutableStateOf(false) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).readText()
                }
                if (!content.isNullOrBlank()) {
                    pendingImportJson = content
                    showConfirmImportDialog = true
                } else {
                    Toast.makeText(context, "File sao lưu trống!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Không thể đọc file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

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

                                Spacer(modifier = Modifier.height(12.dp))

                                // Nội dung chuyển khoản mặc định
                                OutlinedTextField(
                                    value = defaultMemo,
                                    onValueChange = { defaultMemo = it },
                                    label = { Text("Nội dung chuyển khoản mặc định *") },
                                    placeholder = { Text("Vd: VANDINH") },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ShortText, contentDescription = null, tint = SageGreenPrimary) },
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
                                                    accountHolder = accountHolder.trim().uppercase(),
                                                    defaultMemo = defaultMemo.trim()
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

                    item {
                        var reminderLeadMinutes by remember { mutableStateOf(15) }
                        val hasNotificationPerm = remember { NotificationHelper.hasNotificationPermission(context) }

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
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = SageGreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Cấu Hình Thông Báo Lịch Hẹn",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SageGreenPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Hệ thống sẽ tự động gửi thông báo popup kèm âm báo và rung lên điện thoại/tablet quầy lễ tân khi lịch hẹn sắp đến giờ.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalTextSecondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (hasNotificationPerm) SageGreenContainer.copy(alpha = 0.5f) else SpaGoldContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (hasNotificationPerm) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (hasNotificationPerm) SageGreenPrimary else SpaGoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (hasNotificationPerm) "Quyền thông báo: Đang hoạt động bình thường" else "Quyền thông báo: Chưa bật trên thiết bị",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasNotificationPerm) SageGreenPrimary else SpaGoldPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Thời gian nhắc nhở trước giờ bắt đầu:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(15 to "Trước 15p", 30 to "Trước 30p", 60 to "Trước 1h").forEach { (mins, label) ->
                                        FilterChip(
                                            selected = reminderLeadMinutes == mins,
                                            onClick = {
                                                reminderLeadMinutes = mins
                                                snackbarMessage = "Đã chọn nhắc hẹn $label"
                                            },
                                            label = { Text(label, fontSize = 12.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedButton(
                                    onClick = {
                                        NotificationHelper.showTestNotification(context)
                                        snackbarMessage = "🔔 Đã gửi thông báo thử nghiệm lên thanh trạng thái!"
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreenPrimary)
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gửi thử thông báo mẫu lên điện thoại", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // TAB 5: BẢN QUYỀN & KÍCH HOẠT
                // ==========================================
                if (activeTab == SettingsTab.LICENSE) {
                    // 1. Thẻ trạng thái bản quyền
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
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
                                            imageVector = if (licenseStatus.isActivated) Icons.Default.Verified else Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = if (licenseStatus.isActivated) SuccessGreen else SpaGoldPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Trạng Thái Bản Quyền",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalTextPrimary
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = when {
                                            licenseStatus.isActivated -> SuccessGreenContainer
                                            licenseStatus.isExpired -> AllergyAlertContainer
                                            else -> SageGreenContainer
                                        }
                                    ) {
                                        Text(
                                            text = when {
                                                licenseStatus.isActivated -> "ĐÃ KÍCH HOẠT PRO"
                                                licenseStatus.isExpired -> "HẾT HẠN DÙNG THỬ"
                                                else -> "DÙNG THỬ 15 NGÀY"
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                licenseStatus.isActivated -> SuccessGreen
                                                licenseStatus.isExpired -> AllergyAlertRed
                                                else -> SageGreenPrimary
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = licenseStatus.statusTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (licenseStatus.isActivated) SuccessGreen else CharcoalTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = licenseStatus.statusDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalTextSecondary
                                )

                                if (licenseStatus.isActivated) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = WarmBeigeBorder.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Key đang dùng:", style = MaterialTheme.typography.bodySmall, color = CharcoalTextSecondary)
                                        Text(licenseStatus.activatedKey, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SageGreenPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Thời điểm kích hoạt:", style = MaterialTheme.typography.bodySmall, color = CharcoalTextSecondary)
                                        Text(licenseStatus.activatedDate, style = MaterialTheme.typography.bodySmall, color = CharcoalTextSecondary)
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    OutlinedButton(
                                        onClick = {
                                            licenseManager.deactivateLicense()
                                            licenseStatus = licenseManager.getLicenseStatus()
                                            Toast.makeText(context, "Đã hủy kích hoạt bản quyền", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Đổi / Hủy Kích Hoạt Key Này", color = AllergyAlertRed)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    OutlinedTextField(
                                        value = inputLicenseKey,
                                        onValueChange = {
                                            inputLicenseKey = it.uppercase()
                                            licenseError = null
                                        },
                                        label = { Text("Nhập Key kích hoạt bản quyền") },
                                        placeholder = { Text("Nhập mã kích hoạt bản quyền...") },
                                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = SageGreenPrimary) },
                                        trailingIcon = {
                                            IconButton(onClick = {
                                                val clip = clipboardManager.getText()?.text
                                                if (!clip.isNullOrBlank()) {
                                                    inputLicenseKey = clip.trim().uppercase()
                                                    licenseError = null
                                                }
                                            }) {
                                                Icon(Icons.Default.ContentPaste, contentDescription = "Dán", tint = SageGreenPrimary)
                                            }
                                        },
                                        isError = licenseError != null,
                                        supportingText = {
                                            if (licenseError != null) {
                                                Text(licenseError!!, color = AllergyAlertRed)
                                            } else {
                                                Text("Nhập mã kích hoạt do đơn vị phát triển cung cấp")
                                            }
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            if (inputLicenseKey.isBlank()) {
                                                licenseError = "Vui lòng nhập Key bản quyền."
                                                return@Button
                                            }
                                            val ok = licenseManager.activateLicense(inputLicenseKey)
                                            if (ok) {
                                                licenseStatus = licenseManager.getLicenseStatus()
                                                inputLicenseKey = ""
                                                Toast.makeText(context, "Kích hoạt bản quyền PRO trọn đời thành công!", Toast.LENGTH_LONG).show()
                                            } else {
                                                licenseError = "Key kích hoạt không hợp lệ. Vui lòng kiểm tra lại."
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SageGreenPrimary,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Kích Hoạt Bản Quyền", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 2. Thẻ Chế Độ Dữ Liệu Ứng Dụng (Bắt Đầu Sử Dụng Thực Tế)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
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
                                            imageVector = Icons.Default.Dataset,
                                            contentDescription = null,
                                            tint = if (isRealUsageMode) SuccessGreen else SpaGoldPrimary
                                        )
                                        Text(
                                            text = "Chế Độ Dữ Liệu Spa",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalTextPrimary
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = if (isRealUsageMode) SuccessGreenContainer else SpaGoldContainer
                                    ) {
                                        Text(
                                            text = if (isRealUsageMode) "Dữ Liệu Thật (PRO)" else "Dữ Liệu Mẫu (Demo)",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isRealUsageMode) SuccessGreen else SpaGoldPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = if (isRealUsageMode) {
                                        "Ứng dụng đang hoạt động với dữ liệu thực tế của cơ sở Spa của bạn. Toàn bộ dữ liệu khách hàng, lịch hẹn và doanh thu mẫu ban đầu đã được dọn sạch hoàn toàn."
                                    } else {
                                        "Mặc định khi cài đặt, ứng dụng hiển thị sẵn danh sách khách hàng mẫu, liệu trình, lịch hẹn và doanh thu demo để bạn trải nghiệm UI/UX. Khi bạn đã sẵn sàng sử dụng chính thức cho Spa của mình, hãy bấm 'Bắt Đầu Sử Dụng' để xóa toàn bộ dữ liệu mẫu."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalTextSecondary,
                                    lineHeight = 19.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                if (!isRealUsageMode) {
                                    Button(
                                        onClick = { showConfirmRealUsageDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SageGreenPrimary,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Bắt Đầu Sử Dụng (Xóa Dữ Liệu Mẫu)",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { showConfirmRestoreDemoDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SpaGoldPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RestartAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = SpaGoldPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Khôi phục dữ liệu mẫu thử nghiệm",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = SpaGoldPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. TAB SAO LƯU & DỮ LIỆU
                if (activeTab == SettingsTab.BACKUP) {
                    // 1. Thẻ Cơ Sở Dữ Liệu Cục Bộ (Room SQLite)
                    item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
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
                                                imageVector = Icons.Default.Storage,
                                                contentDescription = null,
                                                tint = SageGreenPrimary
                                            )
                                            Text(
                                                text = "Cơ Sở Dữ Liệu SQLite (Room)",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = CharcoalTextPrimary
                                            )
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = SuccessGreenContainer
                                        ) {
                                            Text(
                                                text = "Bảo Mật 100%",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SuccessGreen
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "Toàn bộ dữ liệu hồ sơ khách hàng, lịch hẹn, hình ảnh trước sau và doanh thu được lưu an toàn trực tiếp trong bộ nhớ máy bằng Room Database (SQLite). Dữ liệu hoạt động offline độc lập, không gửi lên đám mây, bảo mật tuyệt đối.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextSecondary,
                                        lineHeight = 19.sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = WarmBeigeBorder.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "THỐNG KÊ DỮ LIỆU HIỆN TẠI",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SageGreenPrimary
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        DataStatItem(count = clientCount, label = "Khách Hàng", icon = Icons.Default.People)
                                        DataStatItem(count = appointmentCount, label = "Lịch Hẹn", icon = Icons.Default.CalendarMonth)
                                        DataStatItem(count = transactionCount, label = "Giao Dịch", icon = Icons.AutoMirrored.Filled.ReceiptLong)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        DataStatItem(count = catalogPackages.size, label = "Gói Dịch Vụ", icon = Icons.Default.MedicalServices)
                                        DataStatItem(count = technicians.size, label = "Kỹ Thuật Viên", icon = Icons.Default.Badge)
                                    }
                                }
                            }
                        }

                        // 2. Thẻ Xuất File Sao Lưu (.json)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            tint = SageGreenPrimary
                                        )
                                        Text(
                                            text = "Xuất Bản Sao Lưu (.json)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalTextPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Đóng gói toàn bộ thông tin cơ sở Spa, danh sách khách hàng, gói liệu trình, lịch hẹn và lịch sử hóa đơn thành file dự phòng .json. Bạn có thể gửi file qua Zalo, sao lưu lên Google Drive hoặc chuyển sang máy khác.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextSecondary,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = onExportBackup,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SageGreenPrimary,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Xuất & Chia Sẻ Bản Sao Lưu (.json)",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Thẻ Nhập File Sao Lưu (.json)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = SpaGoldPrimary
                                        )
                                        Text(
                                            text = "Khôi Phục Dữ Liệu Từ File (.json)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalTextPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Chọn file sao lưu .json đã lưu trước đó để phục hồi lại dữ liệu khi đổi điện thoại mới hoặc cần phục hồi phiên làm việc.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextSecondary,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedButton(
                                        onClick = { filePickerLauncher.launch("*/*") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SpaGoldPrimary),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SpaGoldPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FolderOpen,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = SpaGoldPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Chọn File Sao Lưu Để Phục Hồi",
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

    // Modal chọn ngân hàng
    if (showBankPicker) {
        val filteredBanks = remember(bankSearchQuery) {
            if (bankSearchQuery.isBlank()) SupportedBanks
            else SupportedBanks.filter {
                it.shortName.contains(bankSearchQuery, ignoreCase = true) ||
                it.fullName.contains(bankSearchQuery, ignoreCase = true) ||
                it.bin.contains(bankSearchQuery)
            }
        }
        AlertDialog(
            onDismissRequest = {
                showBankPicker = false
                bankSearchQuery = ""
            },
            title = {
                Text("Chọn Ngân Hàng (${SupportedBanks.size})", fontWeight = FontWeight.Bold, color = SageGreenPrimary)
            },
            containerColor = WarmBeigeSurface,
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = bankSearchQuery,
                        onValueChange = { bankSearchQuery = it },
                        placeholder = { Text("Tìm theo tên, viết tắt hoặc BIN...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SageGreenPrimary) },
                        trailingIcon = {
                            if (bankSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { bankSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Xóa")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        singleLine = true,
                        shape = MaterialTheme.shapes.small
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredBanks, key = { it.bin + it.shortName }) { bank ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedBank = bank
                                        showBankPicker = false
                                        bankSearchQuery = ""
                                    }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(bank.shortName, fontWeight = FontWeight.Bold, color = SageGreenPrimary)
                                    Text(
                                        "${bank.fullName} • BIN: ${bank.bin}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextSecondary
                                    )
                                }
                                if (selectedBank.bin == bank.bin) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = SageGreenPrimary)
                                }
                            }
                            HorizontalDivider(color = WarmBeigeBorder.copy(alpha = 0.5f))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = {
                    showBankPicker = false
                    bankSearchQuery = ""
                }) {
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

    // Dialog xác nhận chuyển sang chế độ Bắt đầu sử dụng thực tế (Xóa dữ liệu mẫu)
    if (showConfirmRealUsageDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmRealUsageDialog = false },
            title = {
                Text(
                    text = "Bắt Đầu Sử Dụng Thực Tế?",
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
            },
            text = {
                Text(
                    text = "Hành động này sẽ xóa toàn bộ dữ liệu mẫu (khách hàng, lịch hẹn, lịch sử hóa đơn demo) để bạn bắt đầu nhập và quản lý khách hàng thật cho cơ sở Spa của mình.\n\nBạn có chắc chắn muốn bắt đầu?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalTextSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmRealUsageDialog = false
                        onStartRealUsage()
                        Toast.makeText(context, "Đã xóa sạch dữ liệu mẫu! Sẵn sàng sử dụng thực tế cho Spa.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary, contentColor = Color.White)
                ) {
                    Text("Xác Nhận & Bắt Đầu", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmRealUsageDialog = false }) {
                    Text("Để sau")
                }
            },
            containerColor = WarmBeigeSurface
        )
    }

    // Dialog xác nhận khôi phục lại dữ liệu mẫu
    if (showConfirmRestoreDemoDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmRestoreDemoDialog = false },
            title = {
                Text(
                    text = "Khôi Phục Dữ Liệu Mẫu?",
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
            },
            text = {
                Text(
                    text = "Nạp lại danh sách khách hàng, lịch hẹn và doanh thu mẫu để tiếp tục thử nghiệm giao diện?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmRestoreDemoDialog = false
                        onRestoreDemoData()
                        Toast.makeText(context, "Đã nạp lại dữ liệu mẫu thử nghiệm thành công!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaGoldPrimary, contentColor = Color.White)
                ) {
                    Text("Khôi phục", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmRestoreDemoDialog = false }) {
                    Text("Hủy")
                }
            },
            containerColor = WarmBeigeSurface
        )
    }

    // Dialog xác nhận khôi phục bản sao lưu từ file
    if (showConfirmImportDialog && pendingImportJson != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmImportDialog = false
                pendingImportJson = null
            },
            title = {
                Text(
                    text = "Xác Nhận Khôi Phục Dữ Liệu?",
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
            },
            text = {
                Text(
                    text = "Thao tác này sẽ ghi đè dữ liệu hiện tại bằng dữ liệu từ file sao lưu đã chọn.\n\nBạn có chắc chắn muốn tiến hành khôi phục?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalTextSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val json = pendingImportJson ?: ""
                        showConfirmImportDialog = false
                        pendingImportJson = null
                        onImportBackup(json)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary, contentColor = Color.White)
                ) {
                    Text("Khôi Phục Ngay", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showConfirmImportDialog = false
                    pendingImportJson = null
                }) {
                    Text("Hủy")
                }
            },
            containerColor = WarmBeigeSurface
        )
    }
}

@Composable
private fun DataStatItem(count: Int, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = SageGreenPrimary
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CharcoalTextPrimary
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = CharcoalTextSecondary,
            fontSize = 11.sp
        )
    }
}

