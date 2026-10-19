package com.spacare.manager.feature.license

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.core.license.DeviceIdManager
import com.spacare.manager.core.license.LicenseManager
import kotlinx.coroutines.launch

@Composable
fun TrialExpiredLockDialog(
    licenseManager: LicenseManager,
    onActivatedSuccess: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var inputKey by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isActivating by remember { mutableStateOf(false) }

    val deviceId = remember { DeviceIdManager.getDeviceId(context) }
    val deviceName = remember { DeviceIdManager.getDeviceName() }

    Dialog(
        onDismissRequest = { /* Không cho phép tắt dialog khi đã hết hạn dùng thử / bản quyền */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Biểu tượng ổ khóa màu vàng gold
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SpaGoldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = SpaGoldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Bản Quyền Đã Hết Hạn",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Thời gian dùng thử hoặc gói bản quyền của Văn Định Skinlab đã kết thúc. Vui lòng nhập Key kích hoạt để tiếp tục sử dụng phần mềm trên thiết bị này.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Thẻ hiển thị Device ID để gửi cho người bán key
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = WarmBeigeBackground,
                    border = BorderStroke(1.dp, WarmBeigeBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mã thiết bị của bạn ($deviceName):",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = CharcoalTextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = deviceId,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SageGreenPrimary,
                                fontSize = 13.sp
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(deviceId))
                                Toast.makeText(context, "Đã sao chép mã thiết bị: $deviceId", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Sao chép mã máy",
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ô nhập Key
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = {
                        inputKey = it.uppercase()
                        errorMessage = null
                    },
                    label = { Text("Nhập Key kích hoạt bản quyền") },
                    placeholder = { Text("VD: VD-ABCD-1234-EFGH") },
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null, tint = SageGreenPrimary)
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            val clipText = clipboardManager.getText()?.text
                            if (!clipText.isNullOrBlank()) {
                                inputKey = clipText.trim().uppercase()
                                errorMessage = null
                            }
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Dán mã", tint = SageGreenPrimary)
                        }
                    },
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = AllergyAlertRed)
                        } else {
                            Text("Mỗi Key kích hoạt dùng được cho 1 thiết bị duy nhất")
                        }
                    },
                    singleLine = true,
                    enabled = !isActivating,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Nút kích hoạt
                Button(
                    onClick = {
                        if (inputKey.isBlank()) {
                            errorMessage = "Vui lòng nhập Key kích hoạt."
                            return@Button
                        }

                        coroutineScope.launch {
                            isActivating = true
                            errorMessage = null
                            val result = licenseManager.activateLicenseOnline(inputKey)
                            isActivating = false

                            if (result.success) {
                                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                onActivatedSuccess()
                            } else {
                                errorMessage = result.message
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageGreenPrimary,
                        contentColor = Color.White
                    ),
                    enabled = !isActivating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isActivating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đang Kiểm Tra & Khóa Key...", color = Color.White, fontSize = 14.sp)
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kích Hoạt Ngay", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                var isRestoring by remember { mutableStateOf(false) }

                // Nút khôi phục bản quyền đã mua cho thiết bị này
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            isRestoring = true
                            errorMessage = null
                            val res = licenseManager.restoreLicenseOnline()
                            isRestoring = false
                            if (res.success) {
                                Toast.makeText(context, "Khôi phục thành công gói ${res.plan}!", Toast.LENGTH_LONG).show()
                                onActivatedSuccess()
                            } else {
                                errorMessage = res.message
                            }
                        }
                    },
                    enabled = !isActivating && !isRestoring,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(
                            color = SageGreenPrimary,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Đang Kiểm Tra Bản Quyền Cũ...", color = SageGreenPrimary, fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp), tint = SageGreenPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Khôi Phục Bản Quyền Đã Mua", color = SageGreenPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Liên hệ mua bản quyền / Hỗ trợ: Văn Định Skinlab",
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
