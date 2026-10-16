package com.spacare.manager.feature.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutQrScreen(
    invoice: CheckoutInvoice,
    onConfirmPayment: () -> Unit,
    onResetInvoice: () -> Unit,
    onUpdateBank: (bank: BankOption, accountNo: String, accountHolder: String) -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }

    var expandedBankDropdown by remember { mutableStateOf(false) }
    var selectedBank by remember(invoice) { mutableStateOf(invoice.selectedBank) }
    var accountNumber by remember(invoice) { mutableStateOf(invoice.accountNumber) }
    var accountHolder by remember(invoice) { mutableStateOf(invoice.accountHolder) }

    var showEditAccountDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Thu Ngân & Sinh Mã VietQR",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hóa đơn #${invoice.invoiceCode} • ${invoice.clientName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = onResetInvoice) {
                        Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = "Tạo hóa đơn mới", tint = SageGreenPrimary)
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

            // 1. Thẻ mã VietQR động
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium, // 16.dp
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
                                    text = "MÃ THANH TOÁN VIETQR",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SageGreenPrimary
                                )
                            }

                            // Trạng thái thanh toán badge
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

                        // Khung hiển thị ảnh QR Napas 247
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

                        // Hiển thị số tiền to rõ
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

                        // Chi tiết tài khoản nhận tiền
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            BankInfoItemRow(
                                label = "Ngân hàng thụ hưởng",
                                value = "${invoice.selectedBank.shortName} (${invoice.selectedBank.fullName})",
                                onAction = { expandedBankDropdown = true },
                                actionIcon = Icons.Default.SwapHoriz
                            )
                            BankInfoItemRow(
                                label = "Số tài khoản",
                                value = invoice.accountNumber,
                                onAction = {
                                    clipboardManager.setText(AnnotatedString(invoice.accountNumber))
                                    snackbarMessage = "Đã copy số tài khoản"
                                },
                                actionIcon = Icons.Default.ContentCopy
                            )
                            BankInfoItemRow(
                                label = "Chủ tài khoản",
                                value = invoice.accountHolder
                            )
                            BankInfoItemRow(
                                label = "Nội dung chuyển khoản",
                                value = invoice.invoiceCode,
                                isBold = true,
                                onAction = {
                                    clipboardManager.setText(AnnotatedString(invoice.invoiceCode))
                                    snackbarMessage = "Đã copy nội dung chuyển khoản"
                                },
                                actionIcon = Icons.Default.ContentCopy
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = { showEditAccountDialog = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Đổi số tài khoản Spa", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // 2. Chi tiết dịch vụ & sản phẩm trong hóa đơn
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
                            Text(
                                text = "Chi Tiết Dịch Vụ & Mỹ Phẩm",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextPrimary
                            )
                            Text(
                                text = "${invoice.items.size} mục",
                                style = MaterialTheme.typography.labelSmall,
                                color = CharcoalTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        invoice.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = CharcoalTextPrimary
                                    )
                                    Text(
                                        text = when (item.type) {
                                            ItemType.PACKAGE -> "Trừ liệu trình (1 buổi)"
                                            ItemType.SERVICE -> "Dịch vụ spa (x${item.quantity})"
                                            ItemType.PRODUCT -> "Mỹ phẩm mang về (x${item.quantity})"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextMuted
                                    )
                                }
                                Text(
                                    text = if (item.totalPrice == 0L) "0 đ" else "${currencyFormatter.format(item.totalPrice)} đ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (item.totalPrice == 0L) SageGreenPrimary else CharcoalTextPrimary
                                )
                            }
                            HorizontalDivider(
                                color = WarmBeigeSurfaceVariant,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        CheckoutSummaryRow("Tạm tính", "${currencyFormatter.format(invoice.subtotal)} đ")
                        if (invoice.discountAmount > 0) {
                            CheckoutSummaryRow(
                                "Ưu đãi thành viên",
                                "-${currencyFormatter.format(invoice.discountAmount)} đ",
                                highlightColor = AllergyAlertRed
                            )
                        }
                        CheckoutSummaryRow(
                            "Tổng thanh toán",
                            "${currencyFormatter.format(invoice.finalTotal)} đ",
                            isTotal = true
                        )
                    }
                }
            }

            // 3. Thao tác chốt thanh toán & Tạo hóa đơn mới
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (invoice.paymentStatus == PaymentStatus.WAITING_FOR_PAYMENT) {
                        Button(
                            onClick = onConfirmPayment,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Xác Nhận Đã Nhận Tiền / Hoàn Tất",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        Button(
                            onClick = onResetInvoice,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tạo Hóa Đơn Cho Khách Mới")
                        }
                    }

                    OutlinedButton(
                        onClick = onResetInvoice,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Làm mới / Reset hóa đơn")
                    }
                }
            }
        }
    }

    // Dialog chọn nhanh Ngân hàng
    if (expandedBankDropdown) {
        AlertDialog(
            onDismissRequest = { expandedBankDropdown = false },
            title = { Text("Chọn Ngân Hàng Thụ Hưởng") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(SupportedBanks.size) { index ->
                        val bank = SupportedBanks[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedBank = bank
                                    onUpdateBank(bank, accountNumber, accountHolder)
                                    expandedBankDropdown = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(bank.shortName, fontWeight = FontWeight.Bold, color = SageGreenPrimary)
                                Text(bank.fullName, style = MaterialTheme.typography.bodySmall, color = CharcoalTextSecondary)
                            }
                            if (invoice.selectedBank.bin == bank.bin) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SageGreenPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { expandedBankDropdown = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    // Dialog chỉnh sửa thông tin tài khoản nhận tiền Spa
    if (showEditAccountDialog) {
        var tempAccountNo by remember { mutableStateOf(invoice.accountNumber) }
        var tempHolder by remember { mutableStateOf(invoice.accountHolder) }

        AlertDialog(
            onDismissRequest = { showEditAccountDialog = false },
            title = { Text("Cấu Hình Tài Khoản Nhận Tiền") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tempAccountNo,
                        onValueChange = { tempAccountNo = it },
                        label = { Text("Số tài khoản") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempHolder,
                        onValueChange = { tempHolder = it },
                        label = { Text("Tên chủ tài khoản") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        accountNumber = tempAccountNo.trim()
                        accountHolder = tempHolder.trim()
                        onUpdateBank(invoice.selectedBank, accountNumber, accountHolder)
                        showEditAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                ) {
                    Text("Lưu tài khoản")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditAccountDialog = false }) {
                    Text("Hủy")
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
