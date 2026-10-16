package com.spacare.manager.feature.license

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.core.license.LicenseManager

@Composable
fun TrialExpiredLockDialog(
    licenseManager: LicenseManager,
    onActivatedSuccess: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var inputKey by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { /* Không cho phép tắt dialog khi đã hết hạn dùng thử */ },
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
                    text = "Hết Hạn Dùng Thử 3 Ngày",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Thời gian trải nghiệm miễn phí 3 ngày của SpaCare Manager đã kết thúc. Vui lòng nhập Key kích hoạt bản quyền để tiếp tục sử dụng phần mềm quản lý Spa.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Ô nhập Key
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = {
                        inputKey = it.uppercase()
                        errorMessage = null
                    },
                    label = { Text("Nhập Key kích hoạt bản quyền") },
                    placeholder = { Text("Ví dụ: SPACARE-PRO-2026-8888") },
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
                            Text("Định dạng: SPACARE-XXXX-XXXX-XXXX")
                        }
                    },
                    singleLine = true,
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

                        val success = licenseManager.activateLicense(inputKey)
                        if (success) {
                            Toast.makeText(context, "Kích hoạt bản quyền trọn đời thành công!", Toast.LENGTH_LONG).show()
                            onActivatedSuccess()
                        } else {
                            errorMessage = "Key kích hoạt không đúng hoặc không hợp lệ. Vui lòng kiểm tra lại."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageGreenPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kích Hoạt Ngay", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Gợi ý hỗ trợ / thử key mẫu
                TextButton(
                    onClick = {
                        // Tự động điền 1 key VIP mẫu
                        inputKey = "SPACARE-VIP-8888-9999"
                        errorMessage = null
                    }
                ) {
                    Text(
                        text = "Điền key mẫu: SPACARE-VIP-8888-9999",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpaGoldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "Hotline hỗ trợ: 1900 6868 • Email: support@spacare.vn",
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalTextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

