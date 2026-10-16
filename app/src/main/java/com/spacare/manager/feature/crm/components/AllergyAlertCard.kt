package com.spacare.manager.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spacare.manager.core.designsystem.theme.AllergyAlertContainer
import com.spacare.manager.core.designsystem.theme.AllergyAlertRed
import com.spacare.manager.core.designsystem.theme.CharcoalTextPrimary

@Composable
fun AllergyAlertCard(
    allergies: List<String>,
    contraindications: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // 16.dp
        colors = CardDefaults.cardColors(containerColor = AllergyAlertContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AllergyAlertRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Cảnh báo dị ứng",
                        tint = AllergyAlertRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "LƯU Ý DỊ ỨNG & CHỐNG CHỈ ĐỊNH",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = AllergyAlertRed
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Danh sách dị ứng
            allergies.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        color = AllergyAlertRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalTextPrimary
                    )
                }
            }

            if (contraindications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                contraindications.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "⚠ ",
                            color = AllergyAlertRed
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = CharcoalTextPrimary
                        )
                    }
                }
            }
        }
    }
}

