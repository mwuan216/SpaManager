package com.spacare.manager.feature.crm.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.Client
import com.spacare.manager.domain.model.VipTier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormDialog(
    initialClient: Client? = null,
    onDismissRequest: () -> Unit,
    onSaveClient: (Client) -> Unit
) {
    val isEdit = initialClient != null

    var fullName by remember { mutableStateOf(initialClient?.fullName ?: "") }
    var phone by remember { mutableStateOf(initialClient?.phone ?: "") }
    var email by remember { mutableStateOf(initialClient?.email ?: "") }
    var selectedVipTier by remember { mutableStateOf(initialClient?.vipTier ?: VipTier.STANDARD) }
    var skinType by remember { mutableStateOf(initialClient?.skinType ?: "Da hỗn hợp thiên dầu") }
    var skinNotes by remember { mutableStateOf(initialClient?.skinConditionNotes ?: "") }

    var expandedVipDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isEdit) "Chỉnh Sửa Hồ Sơ Khách Hàng" else "Thêm Khách Hàng Mới",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SageGreenPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Họ và tên
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Họ và tên khách hàng *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SageGreenPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Số điện thoại
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SageGreenPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email liên hệ") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SageGreenPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Hạng thành viên VIP
                ExposedDropdownMenuBox(
                    expanded = expandedVipDropdown,
                    onExpandedChange = { expandedVipDropdown = !expandedVipDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedVipTier.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hạng thành viên") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVipDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = MaterialTheme.shapes.small
                    )
                    ExposedDropdownMenu(
                        expanded = expandedVipDropdown,
                        onDismissRequest = { expandedVipDropdown = false }
                    ) {
                        VipTier.entries.forEach { tier ->
                            DropdownMenuItem(
                                text = { Text(tier.title) },
                                onClick = {
                                    selectedVipTier = tier
                                    expandedVipDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Phân loại da
                OutlinedTextField(
                    value = skinType,
                    onValueChange = { skinType = it },
                    label = { Text("Phân loại da (vd: Da dầu, da khô, nhạy cảm)") },
                    leadingIcon = { Icon(Icons.Default.Face, contentDescription = null, tint = SageGreenPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Ghi chú da liễu
                OutlinedTextField(
                    value = skinNotes,
                    onValueChange = { skinNotes = it },
                    label = { Text("Ghi chú tình trạng da") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Nút thao tác
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("Hủy")
                    }

                    Button(
                        onClick = {
                            if (fullName.isNotBlank() && phone.isNotBlank()) {
                                val clientToSave = if (isEdit) {
                                    initialClient!!.copy(
                                        fullName = fullName.trim(),
                                        phone = phone.trim(),
                                        email = email.trim(),
                                        vipTier = selectedVipTier,
                                        skinType = skinType.trim(),
                                        skinConditionNotes = skinNotes.trim()
                                    )
                                } else {
                                    Client(
                                        id = "KH-" + (1000..9999).random(),
                                        fullName = fullName.trim(),
                                        phone = phone.trim(),
                                        email = email.trim(),
                                        vipTier = selectedVipTier,
                                        skinType = skinType.trim(),
                                        skinConditionNotes = skinNotes.trim(),
                                        allergyAlerts = emptyList(),
                                        contraindications = emptyList(),
                                        treatmentPackages = emptyList(),
                                        serviceHistory = emptyList(),
                                        beforeAfterComparisons = emptyList()
                                    )
                                }
                                onSaveClient(clientToSave)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SageGreenPrimary,
                            contentColor = Color.White
                        ),
                        enabled = fullName.isNotBlank() && phone.isNotBlank()
                    ) {
                        Text(
                            text = if (isEdit) "Cập nhật" else "Lưu hồ sơ",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

