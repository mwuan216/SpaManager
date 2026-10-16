package com.spacare.manager.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.TreatmentPackage

@Composable
fun TreatmentPackageCard(
    pkg: TreatmentPackage,
    onUseSessionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // 16.dp
        colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SageGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = pkg.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                        Text(
                            text = "KTV phụ trách: ${pkg.assignedTherapist}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bộ đếm buổi liệu trình (Remaining session counter)
            Surface(
                shape = MaterialTheme.shapes.small,
                color = WarmBeigeSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Số buổi còn lại",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalTextSecondary
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${pkg.remainingSessions}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = SageGreenPrimary
                            )
                            Text(
                                text = " / ${pkg.totalSessions} buổi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CharcoalTextSecondary,
                                modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (pkg.remainingSessions <= 2) AllergyAlertContainer else SageGreenContainer
                    ) {
                        Text(
                            text = if (pkg.remainingSessions <= 2) "Sắp hết liệu trình" else "Đang kích hoạt",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (pkg.remainingSessions <= 2) AllergyAlertRed else SageGreenPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Thanh tiến độ trực quan (Progress Indicator)
            val progress = pkg.usedSessions.toFloat() / pkg.totalSessions.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = SageGreenPrimary,
                trackColor = WarmBeigeSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hạn dùng: ${pkg.expiryDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalTextMuted
                )

                OutlinedButton(
                    onClick = onUseSessionClick,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SageGreenPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Điểm danh buổi",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

