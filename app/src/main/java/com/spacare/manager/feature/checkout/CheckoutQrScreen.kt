package com.spacare.manager.feature.checkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.CheckoutInvoice
import com.spacare.manager.domain.model.ItemType
import com.spacare.manager.domain.model.PaymentStatus
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutQrScreen(
    invoice: CheckoutInvoice,
    onSimulatePaymentSuccess: () -> Unit = {},
    onResetPayment: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onPrintReceipt: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedMessage by remember { mutableStateOf<String?>(null) }
    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }

    // Pulsing animation cho trạng thái đang chờ thanh toán
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Thu Ngân & Thanh Toán",
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
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = onPrintReceipt) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = "In hóa đơn")
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

            // 1. Banner trạng thái thanh toán (Listener thời gian thực)
            item {
                if (invoice.paymentStatus == PaymentStatus.PAYMENT_SUCCESS) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = SuccessGreenContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Thành công",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ĐÃ NHẬN TIỀN THÀNH CÔNG!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "Mã GD: ${invoice.transactionRef} • ${invoice.paidAt}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalTextSecondary
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(SageGreenPrimary)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Đang chờ khách quét mã VietQR...",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CharcoalTextPrimary
                                )
                                Text(
                                    text = "Hệ thống tự động đồng bộ khi tài khoản báo có tiền",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Thẻ hiển thị mã VietQR động
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
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = SageGreenPrimary
                            )
                            Text(
                                text = "MÃ THANH TOÁN VIETQR",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SageGreenPrimary,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            text = "Hỗ trợ 40+ ứng dụng ngân hàng & ví điện tử Việt Nam",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextMuted,
                            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                        )

                        // Khung hiển thị QR Image
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.5.dp, SageGreenContainer, RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(10.dp),
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
                                        modifier = Modifier.size(72.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Giao dịch hoàn tất",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Số tiền thanh toán làm nổi bật
                        Text(
                            text = "Số tiền cần thanh toán",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary
                        )
                        Text(
                            text = "${currencyFormatter.format(invoice.finalTotal)} đ",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = WarmBeigeBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Thông tin chuyển khoản chi tiết & Copy action
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BankDetailRow(
                                label = "Ngân hàng",
                                value = "${invoice.vietQrInfo.bankShortName} (Mã BIN: ${invoice.vietQrInfo.bankId})"
                            )
                            BankDetailRow(
                                label = "Số tài khoản",
                                value = invoice.vietQrInfo.accountNumber,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(invoice.vietQrInfo.accountNumber))
                                    copiedMessage = "Đã copy số tài khoản"
                                }
                            )
                            BankDetailRow(
                                label = "Chủ tài khoản",
                                value = invoice.vietQrInfo.accountHolder
                            )
                            BankDetailRow(
                                label = "Nội dung CK",
                                value = invoice.vietQrInfo.referenceCode,
                                isBold = true,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(invoice.vietQrInfo.referenceCode))
                                    copiedMessage = "Đã copy nội dung chuyển khoản"
                                }
                            )
                        }
                    }
                }
            }

            // 3. Chi tiết dịch vụ & sản phẩm (Itemized Breakdown)
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
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Tổng phụ & Giảm giá
                        SummaryRow("Tạm tính", "${currencyFormatter.format(invoice.subtotal)} đ")
                        if (invoice.discountAmount > 0) {
                            SummaryRow(
                                "Ưu đãi VIP Gold (10%)",
                                "-${currencyFormatter.format(invoice.discountAmount)} đ",
                                highlightColor = AllergyAlertRed
                            )
                        }
                        SummaryRow(
                            "Tổng thanh toán",
                            "${currencyFormatter.format(invoice.finalTotal)} đ",
                            isTotal = true
                        )
                    }
                }
            }

            // 4. Nút Giả lập Webhook / Kiểm tra kết quả (Dành cho Lễ tân / Demo Test)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (invoice.paymentStatus == PaymentStatus.WAITING_FOR_PAYMENT) {
                        Button(
                            onClick = onSimulatePaymentSuccess,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Giả Lập Webhook Báo Có Tiền (Test Nhận Tiền)")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onResetPayment,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset lại trạng thái thanh toán")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BankDetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    onCopy: (() -> Unit)? = null
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
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                color = CharcoalTextPrimary
            )
            if (onCopy != null) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Sao chép",
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryRow(
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

