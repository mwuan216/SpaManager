package com.spacare.manager.feature.sale

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.core.license.OnlineLicenseService
import com.spacare.manager.core.sale.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalePortalScreen(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var isSalePartner by remember { mutableStateOf(SalePartnerManager.isSalePartner(context)) }
    var inputSaleCode by remember { mutableStateOf("") }
    var saleCodeError by remember { mutableStateOf<String?>(null) }

    // State khi mua key
    var selectedTierToBuy by remember { mutableStateOf<SaleTier?>(null) }
    var paymentData by remember { mutableStateOf<CreatePaymentResponse?>(null) }
    var isCreatingPayment by remember { mutableStateOf(false) }
    var isCheckingPayment by remember { mutableStateOf(false) }
    var newlyIssuedKey by remember { mutableStateOf<PurchasedKey?>(null) }

    // Kho key đã mua
    var purchasedKeys by remember { mutableStateOf(SalePartnerManager.getPurchasedKeys(context)) }

    // Tự động kiểm tra và đồng bộ tất cả Key đã mua lên Google Sheet (phòng ngừa trường hợp trước đó chưa đồng bộ)
    LaunchedEffect(purchasedKeys.size) {
        if (purchasedKeys.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                purchasedKeys.forEach { pKey ->
                    val plan = when (pKey.tierId.lowercase()) {
                        "spa_1m" -> "1_MONTH"
                        "spa_3m" -> "3_MONTHS"
                        "spa_6m" -> "6_MONTHS"
                        "spa_1y" -> "1_YEAR"
                        "spa_life" -> "LIFETIME"
                        else -> "1_MONTH"
                    }
                    OnlineLicenseService.registerPurchasedKeyToSheet(
                        context = context,
                        key = pKey.key,
                        plan = plan,
                        orderCode = pKey.orderCode
                    )
                }
            }
        }
    }

    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }

    // Dialog toàn màn hình
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Kênh Đối Tác Bán Key (Sale 70%)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextPrimary
                            )
                            Text(
                                text = "Mua sỉ 30% • Lợi nhuận Sale 70% • Tự động 100%",
                                style = MaterialTheme.typography.bodySmall,
                                color = SageGreenPrimary,
                                fontSize = 11.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = CharcoalTextPrimary)
                        }
                    },
                    actions = {
                        if (isSalePartner) {
                            IconButton(onClick = {
                                SalePartnerManager.lockSalePartner(context)
                                isSalePartner = false
                                Toast.makeText(context, "Đã đăng xuất chế độ Sale", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Logout, contentDescription = "Đăng xuất Sale", tint = AllergyAlertRed)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmBeigeBackground)
                )
            },
            containerColor = WarmBeigeBackground
        ) { paddingValues ->
            if (!isSalePartner) {
                // MÀN HÌNH NHẬP MÃ KÍCH HOẠT QUYỀN SALE
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SageGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Kích Hoạt Kênh Đối Tác Sale",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Nhập Mã Đối Tác được cung cấp bởi tác giả để mở khóa quyền mua key giá sỉ (chiết khấu 70%) và nhận key tự động qua payOS.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = inputSaleCode,
                        onValueChange = {
                            inputSaleCode = it.uppercase()
                            saleCodeError = null
                        },
                        label = { Text("Nhập Mã Đối Tác Sale") },
                        placeholder = { Text("VD: SALE_VANDINH_2026") },
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = SageGreenPrimary) },
                        isError = saleCodeError != null,
                        supportingText = {
                            if (saleCodeError != null) {
                                Text(saleCodeError!!, color = AllergyAlertRed)
                            } else {
                                Text("Mã kích hoạt do tác giả cấp (Gợi ý: SALE_VANDINH_2026)")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (inputSaleCode.isBlank()) {
                                saleCodeError = "Vui lòng nhập mã đối tác."
                                return@Button
                            }
                            val success = SalePartnerManager.unlockSalePartner(context, inputSaleCode)
                            if (success) {
                                isSalePartner = true
                                purchasedKeys = SalePartnerManager.getPurchasedKeys(context)
                                Toast.makeText(context, "Kích hoạt quyền Sale Partner thành công!", Toast.LENGTH_SHORT).show()
                            } else {
                                saleCodeError = "Mã đối tác không đúng. Vui lòng liên hệ tác giả."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Xác Nhận & Mở Khóa", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // GIAO DIỆN MUA KEY & KHO KEY ĐỐI TÁC
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Thẻ thông tin quyền lợi Sale
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SageGreenContainer.copy(alpha = 0.6f)),
                            border = BorderStroke(1.dp, SageGreenPrimary.copy(alpha = 0.3f))
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
                                        .background(SageGreenPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Percent, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Chính Sách Đối Tác: Sale Nhận 70%",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SageGreenOnContainer
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Bạn chỉ cần thanh toán 30% giá bán lẻ qua payOS. Hệ thống tự động sinh và bàn giao Key ngay lập tức trong 3 giây!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Tiêu đề danh sách gói
                    item {
                        Text(
                            text = "DANH SÁCH GÓI KEY CẦN MUA",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextMuted
                        )
                    }

                    // Danh sách 5 gói key
                    items(PayOSPaymentService.SALE_TIERS, key = { it.id }) { tier ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = tier.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalTextPrimary
                                        )
                                        Text(
                                            text = tier.durationLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CharcoalTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = SpaGoldContainer
                                    ) {
                                        Text(
                                            text = "+${numberFormatter.format(tier.profit)} ₫ (70%)",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SpaGoldPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = WarmBeigeBorder.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Giá bán khách: ",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CharcoalTextSecondary,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "${numberFormatter.format(tier.retailPrice)} ₫",
                                                style = MaterialTheme.typography.bodySmall,
                                                textDecoration = TextDecoration.LineThrough,
                                                color = CharcoalTextMuted,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Bạn nạp (30%): ",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = CharcoalTextPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${numberFormatter.format(tier.wholesalePrice)} ₫",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = SageGreenPrimary
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            selectedTierToBuy = tier
                                            coroutineScope.launch {
                                                isCreatingPayment = true
                                                val res = PayOSPaymentService.createPayment(context, tier)
                                                isCreatingPayment = false
                                                if (res.success) {
                                                    paymentData = res
                                                } else {
                                                    Toast.makeText(context, res.error ?: "Lỗi tạo đơn thanh toán", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Mua Key", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // KHO KEY ĐÃ MUA CỦA SALE
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "KHO KEY CỦA BẠN (${purchasedKeys.size} KEY)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextMuted
                            )

                            if (purchasedKeys.isNotEmpty()) {
                                var isSyncingAll by remember { mutableStateOf(false) }
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            isSyncingAll = true
                                            var count = 0
                                            withContext(Dispatchers.IO) {
                                                purchasedKeys.forEach { pKey ->
                                                    val plan = when (pKey.tierId.lowercase()) {
                                                        "spa_1m" -> "1_MONTH"
                                                        "spa_3m" -> "3_MONTHS"
                                                        "spa_6m" -> "6_MONTHS"
                                                        "spa_1y" -> "1_YEAR"
                                                        "spa_life" -> "LIFETIME"
                                                        else -> "1_MONTH"
                                                    }
                                                    val ok = OnlineLicenseService.registerPurchasedKeyToSheet(
                                                        context = context,
                                                        key = pKey.key,
                                                        plan = plan,
                                                        orderCode = pKey.orderCode
                                                    )
                                                    if (ok) count++
                                                }
                                            }
                                            isSyncingAll = false
                                            Toast.makeText(context, "Đã đồng bộ $count/${purchasedKeys.size} Key lên Google Sheet!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = !isSyncingAll
                                ) {
                                    if (isSyncingAll) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = SageGreenPrimary)
                                    } else {
                                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(14.dp), tint = SageGreenPrimary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Đồng bộ Sheet", fontSize = 11.sp, color = SageGreenPrimary, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    if (purchasedKeys.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = CharcoalTextMuted, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Chưa có Key nào trong kho", color = CharcoalTextSecondary, style = MaterialTheme.typography.bodyMedium)
                                    Text("Sau khi thanh toán qua payOS, Key sẽ xuất hiện ngay tại đây.", color = CharcoalTextMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        items(purchasedKeys, key = { it.key + it.orderCode }) { pKey ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = SageGreenContainer
                                        ) {
                                            Text(
                                                text = pKey.planTitle,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SageGreenPrimary
                                            )
                                        }

                                        Text(
                                            text = pKey.purchasedAt,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = CharcoalTextMuted
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Hiển thị Key
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = WarmBeigeBackground,
                                        border = BorderStroke(1.dp, WarmBeigeBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = pKey.key,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = SageGreenPrimary
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                // Nút đồng bộ lên Google Sheet
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            val plan = when (pKey.tierId.lowercase()) {
                                                                "spa_1m" -> "1_MONTH"
                                                                "spa_3m" -> "3_MONTHS"
                                                                "spa_6m" -> "6_MONTHS"
                                                                "spa_1y" -> "1_YEAR"
                                                                "spa_life" -> "LIFETIME"
                                                                else -> "1_MONTH"
                                                            }
                                                            val ok = withContext(Dispatchers.IO) {
                                                                OnlineLicenseService.registerPurchasedKeyToSheet(
                                                                    context = context,
                                                                    key = pKey.key,
                                                                    plan = plan,
                                                                    orderCode = pKey.orderCode
                                                                )
                                                            }
                                                            if (ok) {
                                                                Toast.makeText(context, "Đã đồng bộ Key lên máy chủ Google Sheet!", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                Toast.makeText(context, "Chưa thể kết nối Google Sheet. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.CloudUpload, contentDescription = "Đồng bộ", tint = SageGreenPrimary, modifier = Modifier.size(16.dp))
                                                }

                                                // Nút sao chép
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(pKey.key))
                                                        SalePartnerManager.markKeyAsCopied(context, pKey.key)
                                                        purchasedKeys = SalePartnerManager.getPurchasedKeys(context)
                                                        Toast.makeText(context, "Đã sao chép: ${pKey.key}", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Sao chép", tint = SageGreenPrimary, modifier = Modifier.size(16.dp))
                                                }

                                                // Nút gửi cho khách
                                                IconButton(
                                                    onClick = {
                                                        shareKeyToCustomer(context, pKey.key, pKey.planTitle)
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Share, contentDescription = "Chia sẻ", tint = SageGreenPrimary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG THANH TOÁN QR PAYOS TỰ ĐỘNG
    // =========================================================================
    if (paymentData != null && selectedTierToBuy != null) {
        val pay = paymentData!!
        val tier = selectedTierToBuy!!

        // Polling kiểm tra trạng thái thanh toán mỗi 3s
        LaunchedEffect(pay.orderCode) {
            while (true) {
                delay(3000)
                val checkRes = PayOSPaymentService.checkPayment(context, tier.id, pay.orderCode)
                if (checkRes.paid && checkRes.key.isNotBlank()) {
                    // Tự động đồng bộ Key lên Google Sheet ngay lập tức
                    OnlineLicenseService.registerPurchasedKeyToSheet(
                        context = context,
                        key = checkRes.key,
                        plan = tier.plan,
                        orderCode = pay.orderCode
                    )

                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val newPurchased = PurchasedKey(
                        key = checkRes.key,
                        tierId = tier.id,
                        planTitle = tier.title,
                        amountPaid = tier.wholesalePrice,
                        retailPrice = tier.retailPrice,
                        orderCode = pay.orderCode,
                        purchasedAt = sdf.format(Date()),
                        isCopied = false
                    )
                    SalePartnerManager.savePurchasedKey(context, newPurchased)
                    purchasedKeys = SalePartnerManager.getPurchasedKeys(context)

                    // Đóng payment dialog và mở congratulation dialog
                    paymentData = null
                    newlyIssuedKey = newPurchased
                    break
                }
            }
        }

        AlertDialog(
            onDismissRequest = { paymentData = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thanh Toán Mua Key",
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                    IconButton(onClick = { paymentData = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = CharcoalTextMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Gói: ${tier.title} • Bạn nạp 30%: ${numberFormatter.format(tier.wholesalePrice)} ₫",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SageGreenPrimary
                    )

                    // Hiển thị mã QR VietQR từ payOS
                    if (pay.qrCode.isNotBlank()) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(2.dp, SageGreenPrimary),
                            modifier = Modifier.size(220.dp)
                        ) {
                            // Tạo link QR VietQR chuẩn ngân hàng
                            val qrUrl = "https://img.vietqr.io/image/${pay.bin}-${pay.accountNumber}-compact2.png?amount=${pay.amount}&addInfo=${Uri.encode(pay.description)}&accountName=${Uri.encode(pay.accountName)}"
                            AsyncImage(
                                model = qrUrl,
                                contentDescription = "Mã QR Thanh Toán payOS",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Thông tin chuyển khoản chi tiết
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = WarmBeigeBackground,
                        border = BorderStroke(1.dp, WarmBeigeBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ngân hàng:", fontSize = 12.sp, color = CharcoalTextSecondary)
                                Text("MBBank (Quân Đội)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalTextPrimary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Số tài khoản:", fontSize = 12.sp, color = CharcoalTextSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(pay.accountNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SageGreenPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = SageGreenPrimary,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                clipboardManager.setText(AnnotatedString(pay.accountNumber))
                                                Toast.makeText(context, "Đã sao chép STK", Toast.LENGTH_SHORT).show()
                                            }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Chủ tài khoản:", fontSize = 12.sp, color = CharcoalTextSecondary)
                                Text(pay.accountName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalTextPrimary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Nội dung CK:", fontSize = 12.sp, color = CharcoalTextSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(pay.description, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AllergyAlertRed)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = AllergyAlertRed,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                clipboardManager.setText(AnnotatedString(pay.description))
                                                Toast.makeText(context, "Đã sao chép nội dung chuyển khoản", Toast.LENGTH_SHORT).show()
                                            }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = SageGreenPrimary)
                        Text(
                            text = "Hệ thống đang tự động kiểm tra chuyển khoản...",
                            fontSize = 11.sp,
                            color = CharcoalTextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isCheckingPayment = true
                            val checkRes = PayOSPaymentService.checkPayment(context, tier.id, pay.orderCode)
                            isCheckingPayment = false
                            if (checkRes.paid && checkRes.key.isNotBlank()) {
                                // Tự động đồng bộ Key lên Google Sheet ngay lập tức
                                OnlineLicenseService.registerPurchasedKeyToSheet(
                                    context = context,
                                    key = checkRes.key,
                                    plan = tier.plan,
                                    orderCode = pay.orderCode
                                )

                                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                val newPurchased = PurchasedKey(
                                    key = checkRes.key,
                                    tierId = tier.id,
                                    planTitle = tier.title,
                                    amountPaid = tier.wholesalePrice,
                                    retailPrice = tier.retailPrice,
                                    orderCode = pay.orderCode,
                                    purchasedAt = sdf.format(Date()),
                                    isCopied = false
                                )
                                SalePartnerManager.savePurchasedKey(context, newPurchased)
                                purchasedKeys = SalePartnerManager.getPurchasedKeys(context)
                                paymentData = null
                                newlyIssuedKey = newPurchased
                            } else {
                                Toast.makeText(context, "Hệ thống chưa ghi nhận chuyển khoản. Vui lòng thử lại sau vài giây.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                ) {
                    if (isCheckingPayment) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Kiểm Tra Ngay", color = Color.White)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { paymentData = null }) {
                    Text("Đóng")
                }
            },
            containerColor = WarmBeigeSurface
        )
    }

    // =========================================================================
    // DIALOG CHÚC MỪNG PHÁT HÀNH KEY THÀNH CÔNG
    // =========================================================================
    if (newlyIssuedKey != null) {
        val issued = newlyIssuedKey!!

        AlertDialog(
            onDismissRequest = { newlyIssuedKey = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SuccessGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(36.dp))
                }
            },
            title = {
                Text(
                    text = "Phát Hành Key Thành Công!",
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Đã nhận thanh toán từ payOS. Key bản quyền ${issued.planTitle} của bạn đã sẵn sàng bàn giao cho khách hàng:",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = SageGreenContainer,
                        border = BorderStroke(1.dp, SageGreenPrimary)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = issued.key,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = SageGreenOnContainer,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Key đã được lưu vào Kho Key của bạn và đồng bộ với hệ thống kích hoạt.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = CharcoalTextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        shareKeyToCustomer(context, issued.key, issued.planTitle)
                        newlyIssuedKey = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gửi Cho Khách", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    clipboardManager.setText(AnnotatedString(issued.key))
                    Toast.makeText(context, "Đã sao chép Key!", Toast.LENGTH_SHORT).show()
                    newlyIssuedKey = null
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sao Chép")
                }
            },
            containerColor = WarmBeigeSurface
        )
    }
}

/**
 * Chia sẻ Key bản quyền sang Zalo, Messenger, SMS... với tin nhắn mẫu chuẩn
 */
private fun shareKeyToCustomer(context: Context, key: String, planTitle: String) {
    val message = """
        💆 Kính gửi Quý Khách Hàng Spa!
        Văn Định Skinlab xin gửi Key kích hoạt bản quyền ứng dụng Quản Lý Spa:
        
        🔑 Mã Key: $key
        📦 Gói dịch vụ: $planTitle
        
        * Hướng dẫn kích hoạt: Mở ứng dụng -> Vào mục Cài đặt -> Bản quyền -> Dán mã Key này và bấm Kích Hoạt.
        * Lưu ý: Mỗi key chỉ kích hoạt trên 1 thiết bị duy nhất.
        
        Chúc Spa của bạn kinh doanh phát đạt và luôn đông khách!
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
        putExtra(Intent.EXTRA_SUBJECT, "Key Bản Quyền Phần Mềm Quản Lý Spa Văn Định Skinlab")
    }
    context.startActivity(Intent.createChooser(intent, "Gửi Key Cho Khách Hàng"))
}
