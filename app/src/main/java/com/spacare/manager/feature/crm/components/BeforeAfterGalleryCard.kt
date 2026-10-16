package com.spacare.manager.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.BeforeAfterComparison

@Composable
fun BeforeAfterGalleryCard(
    comparison: BeforeAfterComparison,
    onAddPhotoClick: () -> Unit = {},
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
            // Header
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
                            .background(SpaGoldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = null,
                            tint = SpaGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Ảnh So Sánh Trước & Sau",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Bảo mật",
                                tint = CharcoalTextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Lưu trữ nội bộ chuẩn bảo mật y khoa",
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalTextMuted
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onAddPhotoClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WarmBeigeSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Chụp/Thêm ảnh",
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "${comparison.treatmentName} • ${comparison.sessionMilestone}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = SageGreenPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // So sánh Side-by-Side 2 ảnh Trước & Sau
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Ảnh TRƯỚC (BEFORE)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarmBeigeSurfaceVariant)
                ) {
                    AsyncImage(
                        model = comparison.beforeImageUrl,
                        contentDescription = "Trước điều trị",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Badge ngày chụp & nhãn Trước
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(6.dp),
                        color = CharcoalTextPrimary.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TRƯỚC LIỆU TRÌNH",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WarmBeigeSurface
                            )
                            Text(
                                text = comparison.beforeDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmBeigeSurface.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // Ảnh SAU (AFTER)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarmBeigeSurfaceVariant)
                ) {
                    AsyncImage(
                        model = comparison.afterImageUrl,
                        contentDescription = "Sau điều trị",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Badge ngày chụp & nhãn Sau
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(6.dp),
                        color = SageGreenPrimary.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "SAU ĐIỀU TRỊ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WarmBeigeSurface
                            )
                            Text(
                                text = comparison.afterDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmBeigeSurface.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nhận xét lâm sàng của chuyên viên
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = WarmBeigeSurfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Đánh giá của ${comparison.technicianName}:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = comparison.clinicalNotes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalTextSecondary
                    )
                }
            }
        }
    }
}

