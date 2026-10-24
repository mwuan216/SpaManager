package com.spacare.manager.feature.checkout

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.spacare.manager.R
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.*
import java.text.NumberFormat
import java.util.Locale

enum class CheckoutTab(val title: String) {
    STORE_QR("QR Cửa Hàng (Mặc định)"),
    INVOICE_BILL("Hóa Đơn Dịch Vụ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutQrScreen(
    invoice: CheckoutInvoice?,
    storeProfile: StoreProfile = StoreProfile(),
    isStoreDefaultMode: Boolean = true,
    unpaidAppointments: List<Appointment> = emptyList(),
    onSelectAppointmentInvoice: (Appointment) -> Unit = {},
    onConfirmPayment: () -> Unit,
    onConfirmStorePayment: (amount: Long, memo: String) -> Unit = { _, _ -> },
    onResetInvoice: () -> Unit,
    onUpdateBank: (bank: BankOption, accountNo: String, accountHolder: String, defaultMemo: String) -> Unit = { _, _, _, _ -> },
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }

    var activeTab by remember(isStoreDefaultMode) {
        mutableStateOf(if (isStoreDefaultMode) CheckoutTab.STORE_QR else CheckoutTab.INVOICE_BILL)
    }

    // Tự động nạp lịch hẹn đầu tiên nếu tab Hóa Đơn đang mở mà chưa có hóa đơn nào được chọn
    LaunchedEffect(unpaidAppointments, invoice, activeTab) {
        if (activeTab == CheckoutTab.INVOICE_BILL && invoice == null && unpaidAppointments.isNotEmpty()) {
            onSelectAppointmentInvoice(unpaidAppointments.first())
        }
    }

    // State cho QR Cửa Hàng
    var storeCustomAmount by remember { mutableStateOf("") }
    var storePaymentReceived by remember { mutableStateOf(false) }
    var storeTransactionRef by remember { mutableStateOf("") }

    // Dialog state
    var showEditAccountDialog by remember { mutableStateOf(false) }
    var showBankPickerDialog by remember { mutableStateOf(false) }
    var bankSearchQuery by remember { mutableStateOf("") }

    val parsedStoreAmount = storeCustomAmount.replace(".", "").replace(",", "").toLongOrNull() ?: 0L
    val storeMemo = storeProfile.defaultMemo.ifBlank { "VANDINH" }

    val storeVietQrUrl = remember(storeProfile, parsedStoreAmount, storeMemo) {
        val bankId = storeProfile.selectedBank.bin
        val accNo = storeProfile.accountNumber
        val holder = java.net.URLEncoder.encode(storeProfile.accountHolder, "UTF-8")
        val memoEncoded = java.net.URLEncoder.encode(storeMemo, "UTF-8")
        if (parsedStoreAmount > 0) {
            "https://img.vietqr.io/image/$bankId-$accNo-compact2.png?amount=$parsedStoreAmount&addInfo=$memoEncoded&accountName=$holder"
        } else {
            "https://img.vietqr.io/image/$bankId-$accNo-compact2.png?addInfo=$memoEncoded&accountName=$holder"
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
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column {
                            Text(
                                text = "Thu Ngân & VietQR",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (activeTab == CheckoutTab.STORE_QR) {
                                    storeProfile.name
                                } else {
                                    invoice?.let { "Hóa đơn #${it.invoiceCode} • ${it.clientName}" } ?: "Hóa Đơn Dịch Vụ"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalTextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onResetInvoice()
                        activeTab = CheckoutTab.INVOICE_BILL
                    }) {
                        Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = "Tạo đơn mới", tint = SageGreenPrimary)
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
            // Thanh chuyển Tab Thu Ngân
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeTab == CheckoutTab.STORE_QR,
                    onClick = { activeTab = CheckoutTab.STORE_QR },
                    label = { Text("QR Cửa Hàng (Mặc định)", fontWeight = if (activeTab == CheckoutTab.STORE_QR) FontWeight.Bold else FontWeight.Normal) },
                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SageGreenContainer,
                        selectedLabelColor = SageGreenOnContainer
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = activeTab == CheckoutTab.INVOICE_BILL,
                    onClick = { activeTab = CheckoutTab.INVOICE_BILL },
                    label = { Text("Theo Hóa Đơn", fontWeight = if (activeTab == CheckoutTab.INVOICE_BILL) FontWeight.Bold else FontWeight.Normal) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SageGreenContainer,
                        selectedLabelColor = SageGreenOnContainer
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {

                // ==========================================
                // CHẾ ĐỘ 1: MÃ QR CỦA CỬA HÀNG (MẶC ĐỊNH)
                // ==========================================
                if (activeTab == CheckoutTab.STORE_QR) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode2,
                                            contentDescription = null,
                                            tint = SageGreenPrimary
                                        )
                                        Text(
                                            text = "VIETQR CỬA HÀNG",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SageGreenPrimary
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = if (storePaymentReceived) SuccessGreenContainer else WarmBeigeSurfaceVariant
                                    ) {
                                        Text(
                                            text = if (storePaymentReceived) "Đã nhận tiền" else "Sẵn sàng nhận tiền",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (storePaymentReceived) SuccessGreen else CharcoalTextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Khung ảnh QR cửa hàng
                                Box(
                                    modifier = Modifier
                                        .size(240.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.5.dp, if (storePaymentReceived) SuccessGreen else SageGreenContainer, RoundedCornerShape(16.dp))
                                        .background(Color.White)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (storePaymentReceived) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(68.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Đã nhận tiền thành công!",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = SuccessGreen
                                            )
                                            Text(
                                                text = "Mã GD: $storeTransactionRef",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CharcoalTextSecondary
                                            )
                                        }
                                    } else {
                                        AsyncImage(
                                            model = storeVietQrUrl,
                                            contentDescription = "VietQR Code",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Ô nhập số tiền tùy chọn
                                OutlinedTextField(
                                    value = storeCustomAmount,
                                    onValueChange = { storeCustomAmount = it },
                                    label = { Text("Số tiền cần thu (để trống nếu khách tự nhập)") },
                                    placeholder = { Text("0 đ") },
                                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = SageGreenPrimary) },
                                    trailingIcon = {
                                        if (storeCustomAmount.isNotEmpty()) {
                                            IconButton(onClick = { storeCustomAmount = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "Xóa")
                                            }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.small
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Dải nút bấm nhanh số tiền
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val quickAmounts = listOf(100000L, 200000L, 500000L, 1000000L, 2000000L)
                                    items(quickAmounts) { amt ->
                                        SuggestionChip(
                                            onClick = { storeCustomAmount = amt.toString() },
                                            label = { Text("${amt / 1000}k", fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = WarmBeigeBorder)
                                Spacer(modifier = Modifier.height(10.dp))

                                // Chi tiết tài khoản cửa hàng
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    BankInfoItemRow(
                                        label = "Ngân hàng thụ hưởng",
                                        value = "${storeProfile.selectedBank.shortName} (${storeProfile.selectedBank.fullName})",
                                        onAction = { showBankPickerDialog = true },
                                        actionIcon = Icons.Default.SwapHoriz
                                    )
                                    BankInfoItemRow(
                                        label = "Số tài khoản",
                                        value = storeProfile.accountNumber,
                                        onAction = {
                                            clipboardManager.setText(AnnotatedString(storeProfile.accountNumber))
                                            Toast.makeText(context, "Đã copy số tài khoản", Toast.LENGTH_SHORT).show()
                                        },
                                        actionIcon = Icons.Default.ContentCopy
                                    )
                                    BankInfoItemRow(
                                        label = "Chủ tài khoản",
                                        value = storeProfile.accountHolder
                                    )
                                    BankInfoItemRow(
                                        label = "Nội dung chuyển khoản",
                                        value = storeMemo,
                                        isBold = true,
                                        onAction = {
                                            clipboardManager.setText(AnnotatedString(storeMemo))
                                            Toast.makeText(context, "Đã copy nội dung chuyển khoản", Toast.LENGTH_SHORT).show()
                                        },
                                        actionIcon = Icons.Default.ContentCopy
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Nút xác nhận nhận tiền / reset
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (storePaymentReceived) {
                                        OutlinedButton(
                                            onClick = {
                                                storePaymentReceived = false
                                                storeCustomAmount = ""
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Thu tiền mới")
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                storePaymentReceived = true
                                                storeTransactionRef = "CK-${System.currentTimeMillis().toString().takeLast(6)}"
                                                val collectedAmount = if (parsedStoreAmount > 0) parsedStoreAmount else 0L
                                                onConfirmStorePayment(collectedAmount, storeMemo)
                                                Toast.makeText(context, "Đã ghi nhận thanh toán thành công và cập nhật doanh thu!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = SageGreenPrimary,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "Xác nhận đã nhận tiền",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // CHẾ ĐỘ 2: THANH TOÁN THEO HÓA ĐƠN CHI TIẾT
                // ==========================================
                if (activeTab == CheckoutTab.INVOICE_BILL) {
                    // Thẻ Chọn Hóa Đơn / Lịch Hẹn Chưa Thanh Toán Hôm Nay
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
                                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                            contentDescription = null,
                                            tint = SageGreenPrimary
                                        )
                                        Text(
                                            text = "Chọn Hóa Đơn Hôm Nay",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalTextPrimary
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = if (unpaidAppointments.isNotEmpty()) SageGreenContainer else WarmBeigeSurfaceVariant
                                    ) {
                                        Text(
                                            text = "${unpaidAppointments.size} lịch hẹn",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (unpaidAppointments.isNotEmpty()) SageGreenPrimary else CharcoalTextMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Bấm vào lịch hẹn bên dưới để tự động tải thông tin bill & tạo mã VietQR:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalTextSecondary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                if (unpaidAppointments.isEmpty()) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = WarmBeigeSurfaceVariant
                                    ) {
                                        Text(
                                            text = "Không có lịch hẹn nào chờ thanh toán.",
                                            modifier = Modifier.padding(12.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CharcoalTextMuted
                                        )
                                    }
                                } else {
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(unpaidAppointments, key = { it.id }) { appt ->
                                            val isSelectedAppt = invoice?.appointmentId == appt.id ||
                                                (invoice != null && invoice.clientName == appt.clientName && invoice.items.any { it.name == appt.serviceName })
                                            Surface(
                                                modifier = Modifier
                                                    .width(230.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .border(
                                                        width = if (isSelectedAppt) 1.5.dp else 1.dp,
                                                        color = if (isSelectedAppt) SageGreenPrimary else WarmBeigeBorder,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable { onSelectAppointmentInvoice(appt) },
                                                color = if (isSelectedAppt) SageGreenContainer.copy(alpha = 0.45f) else WarmBeigeSurfaceVariant
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = appt.startTime,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = SageGreenPrimary
                                                        )
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = when (appt.status) {
                                                                AppointmentStatus.IN_SERVICE -> SpaGoldContainer
                                                                else -> SageGreenContainer
                                                            }
                                                        ) {
                                                            Text(
                                                                text = when (appt.status) {
                                                                    AppointmentStatus.IN_SERVICE -> "Đang làm"
                                                                    else -> "Đã hẹn"
                                                                },
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = when (appt.status) {
                                                                    AppointmentStatus.IN_SERVICE -> SpaGoldPrimary
                                                                    else -> SageGreenPrimary
                                                                }
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = appt.clientName,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = CharcoalTextPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = appt.serviceName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = CharcoalTextSecondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        fontSize = 11.sp
                                                    )

                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = if (appt.price == 0L) "Trừ gói" else "${currencyFormatter.format(appt.price)} đ",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (appt.price == 0L) SageGreenPrimary else SpaGoldPrimary
                                                        )
                                                        Text(
                                                            text = if (isSelectedAppt) "Đang chọn" else "Chọn thanh toán",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelectedAppt) SageGreenPrimary else CharcoalTextMuted,
                                                            fontSize = 10.sp
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

                    if (invoice != null) {
                        item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode2,
                                            contentDescription = null,
                                            tint = SageGreenPrimary
                                        )
                                        Text(
                                            text = "VIETQR HÓA ĐƠN",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SageGreenPrimary
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = if (invoice.paymentStatus == PaymentStatus.PAYMENT_SUCCESS) SuccessGreenContainer else WarmBeigeSurfaceVariant
                                    ) {
                                        Text(
                                            text = if (invoice.paymentStatus == PaymentStatus.PAYMENT_SUCCESS) "Đã thanh toán" else "Chờ thanh toán",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (invoice.paymentStatus == PaymentStatus.PAYMENT_SUCCESS) SuccessGreen else CharcoalTextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Ảnh QR hóa đơn
                                Box(
                                    modifier = Modifier
                                        .size(240.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.5.dp, if (invoice.paymentStatus == PaymentStatus.PAYMENT_SUCCESS) SuccessGreen else SageGreenContainer, RoundedCornerShape(16.dp))
                                        .background(Color.White)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (invoice.paymentStatus == PaymentStatus.PAYMENT_SUCCESS) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(68.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Thanh toán thành công!",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = SuccessGreen
                                            )
                                            Text(
                                                text = "Mã GD: ${invoice.transactionRef ?: "CK-SPA"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CharcoalTextSecondary
                                            )
                                        }
                                    } else {
                                        AsyncImage(
                                            model = invoice.vietQrInfo.qrImageUrl,
                                            contentDescription = "VietQR Code",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Số tiền thanh toán",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalTextSecondary
                                )
                                Text(
                                    text = "${currencyFormatter.format(invoice.finalTotal)} đ",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SageGreenPrimary
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = WarmBeigeBorder)
                                Spacer(modifier = Modifier.height(10.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    BankInfoItemRow(
                                        label = "Ngân hàng thụ hưởng",
                                        value = "${invoice.selectedBank.shortName} (${invoice.selectedBank.fullName})",
                                        onAction = { showBankPickerDialog = true },
                                        actionIcon = Icons.Default.SwapHoriz
                                    )
                                    BankInfoItemRow(
                                        label = "Số tài khoản",
                                        value = invoice.accountNumber,
                                        onAction = {
                                            clipboardManager.setText(AnnotatedString(invoice.accountNumber))
                                            Toast.makeText(context, "Đã copy số tài khoản", Toast.LENGTH_SHORT).show()
                                        },
                                        actionIcon = Icons.Default.ContentCopy
                                    )
                                    BankInfoItemRow(
                                        label = "Chủ tài khoản",
                                        value = invoice.accountHolder
                                    )
                                    BankInfoItemRow(
                                        label = "Nội dung chuyển khoản",
                                        value = "${invoice.defaultMemo} ${invoice.invoiceCode}".trim(),
                                        isBold = true,
                                        onAction = {
                                            clipboardManager.setText(AnnotatedString("${invoice.defaultMemo} ${invoice.invoiceCode}".trim()))
                                            Toast.makeText(context, "Đã copy nội dung chuyển khoản", Toast.LENGTH_SHORT).show()
                                        },
                                        actionIcon = Icons.Default.ContentCopy
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                if (invoice.paymentStatus != PaymentStatus.PAYMENT_SUCCESS) {
                                    Button(
                                        onClick = {
                                            onConfirmPayment()
                                            Toast.makeText(context, "Đã xác nhận thanh toán hóa đơn #${invoice.invoiceCode} và cập nhật báo cáo doanh thu!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SageGreenPrimary,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Xác nhận đã thanh toán",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = SuccessGreenContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                                Text(
                                                    "Đã Thu Tiền & Cập Nhật Báo Cáo Doanh Thu",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SuccessGreen
                                                )
                                            }
                                            Text(
                                                "Mã giao dịch: ${invoice.transactionRef ?: "Thành công"} • ${invoice.paidAt ?: ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CharcoalTextSecondary,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Button(
                                                onClick = onResetInvoice,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = SageGreenPrimary,
                                                    contentColor = Color.White
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Thu ngân đơn tiếp theo", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Danh sách món trong hóa đơn
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Chi Tiết Dịch Vụ & Mỹ Phẩm",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                invoice.items.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                            Text(
                                                text = "${item.quantity} x ${currencyFormatter.format(item.unitPrice)} đ",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CharcoalTextSecondary
                                            )
                                        }
                                        Text(
                                            text = "${currencyFormatter.format(item.totalPrice)} đ",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalTextPrimary
                                        )
                                    }
                                    HorizontalDivider(color = WarmBeigeBorder.copy(alpha = 0.5f))
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                CheckoutSummaryRow(label = "Tạm tính", value = "${currencyFormatter.format(invoice.subtotal)} đ")
                                if (invoice.discountAmount > 0) {
                                    CheckoutSummaryRow(
                                        label = "Giảm giá / Khuyến mãi",
                                        value = "-${currencyFormatter.format(invoice.discountAmount)} đ",
                                        highlightColor = AllergyAlertRed
                                    )
                                }
                                CheckoutSummaryRow(
                                    label = "Tổng thanh toán",
                                    value = "${currencyFormatter.format(invoice.finalTotal)} đ",
                                    isTotal = true
                                )
                            }
                        }
                    }
                } else {
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
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (unpaidAppointments.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = SageGreenPrimary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Chọn lịch hẹn cần thanh toán",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Bấm vào một trong các thẻ lịch hẹn ở phía trên để nạp chi tiết hóa đơn và tạo mã VietQR thanh toán tự động.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CharcoalTextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Không có hóa đơn chờ thanh toán",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tất cả lịch hẹn dịch vụ hôm nay đã được thanh toán hoặc chưa có lịch hẹn mới. Bạn có thể chuyển sang tab 'QR Cửa Hàng' để nhận tiền thanh toán trực tiếp tại quầy.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CharcoalTextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { activeTab = CheckoutTab.STORE_QR },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SageGreenPrimary,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Chuyển sang QR Cửa Hàng", fontWeight = FontWeight.Bold)
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

    // Modal chọn ngân hàng từ danh sách 44 ngân hàng VietQR
    if (showBankPickerDialog) {
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
                showBankPickerDialog = false
                bankSearchQuery = ""
            },
            title = {
                Text("Đổi Ngân Hàng Thụ Hưởng (${SupportedBanks.size})", fontWeight = FontWeight.Bold, color = SageGreenPrimary)
            },
            containerColor = WarmBeigeSurface,
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = bankSearchQuery,
                        onValueChange = { bankSearchQuery = it },
                        placeholder = { Text("Tìm theo tên hoặc BIN...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SageGreenPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        singleLine = true,
                        shape = MaterialTheme.shapes.small
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredBanks, key = { it.bin + it.shortName }) { bank ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onUpdateBank(bank, storeProfile.accountNumber, storeProfile.accountHolder, storeMemo)
                                        showBankPickerDialog = false
                                        bankSearchQuery = ""
                                        Toast.makeText(context, "Đã chuyển sang ngân hàng ${bank.shortName}", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(bank.shortName, fontWeight = FontWeight.Bold, color = SageGreenPrimary)
                                    Text(
                                        "${bank.fullName} • ${bank.bin}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextSecondary
                                    )
                                }
                                if (storeProfile.selectedBank.bin == bank.bin) {
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
                    showBankPickerDialog = false
                    bankSearchQuery = ""
                }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
fun BankInfoItemRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    onAction: (() -> Unit)? = null,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = CharcoalTextSecondary
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                color = CharcoalTextPrimary
            )
            if (onAction != null && actionIcon != null) {
                IconButton(
                    onClick = onAction,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CheckoutSummaryRow(
    label: String,
    value: String,
    highlightColor: Color? = null,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = CharcoalTextPrimary
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.SemiBold,
            color = highlightColor ?: if (isTotal) SageGreenPrimary else CharcoalTextPrimary
        )
    }
}
