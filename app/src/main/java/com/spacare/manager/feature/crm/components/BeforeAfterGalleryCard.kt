package com.spacare.manager.feature.crm.components

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.BeforeAfterComparison
import com.spacare.manager.domain.model.SessionPhotoRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

// =========================================================================
// THẺ TIẾN TRÌNH ẢNH THEO TỪNG BUỔI - LƯỚT NGANG & CHO PHÉP CHỤP LẠI
// =========================================================================

fun saveBitmapToTempFile(context: Context, bitmap: Bitmap): String {
    val file = File(context.cacheDir, "spa_session_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return Uri.fromFile(file).toString()
}

@Composable
fun SessionPhotoProgressionCard(
    photos: List<SessionPhotoRecord>,
    onAddPhotoClick: () -> Unit,
    onRetakePhotoClick: (SessionPhotoRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
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
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ảnh Tiến Trình Từng Buổi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Lướt ngang theo dõi thay đổi",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onAddPhotoClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageGreenPrimary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Thêm ảnh",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (photos.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onAddPhotoClick),
                    color = WarmBeigeSurfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Chưa có ảnh tiến trình nào",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                        Text(
                            text = "Nhấn vào đây để chụp ảnh hoặc tải ảnh buổi đầu tiên",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary
                        )
                    }
                }
            } else {
                // Dải lướt ngang (Horizontal Carousel)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(photos, key = { it.id }) { photo ->
                        Card(
                            modifier = Modifier.width(200.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmBeigeSurfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                // Tiêu đề buổi & ngày
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = SageGreenContainer
                                    ) {
                                        Text(
                                            text = if (photo.sessionNumber == 0) "Trước điều trị" else "Buổi ${photo.sessionNumber}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SageGreenPrimary
                                        )
                                    }
                                    Text(
                                        text = photo.date,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Ảnh buổi trị liệu theo khung dọc 3:4 chuẩn
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(3f / 4f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray)
                                ) {
                                    AsyncImage(
                                        model = photo.imageUrl,
                                        contentDescription = photo.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = photo.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (photo.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = photo.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Nút Chụp lại / Đổi ảnh cho buổi này
                                OutlinedButton(
                                    onClick = { onRetakePhotoClick(photo) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreenPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = SageGreenPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Chụp lại / Đổi ảnh",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    // Card thêm buổi mới ở cuối hàng
                    item {
                        Surface(
                            modifier = Modifier
                                .width(120.dp)
                                .height(340.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, SageGreenPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable(onClick = onAddPhotoClick),
                            color = WarmBeigeSurface
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "+ Thêm ảnh\nbuổi mới",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SageGreenPrimary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// DIALOG CHỤP ẢNH / TẢI ẢNH CHO BUỔI LIỆU TRÌNH (HỖ TRỢ CAMERA & THƯ VIỆN)
// =========================================================================

@Composable
fun TakeOrUploadPhotoDialog(
    existingPhoto: SessionPhotoRecord? = null,
    onDismissRequest: () -> Unit,
    onSavePhoto: (SessionPhotoRecord) -> Unit
) {
    val context = LocalContext.current
    var sessionNumberStr by remember { mutableStateOf(existingPhoto?.sessionNumber?.toString() ?: "1") }
    var title by remember { mutableStateOf(existingPhoto?.title ?: "Buổi 1: Tiến trình điều trị") }
    var dateStr by remember {
        mutableStateOf(existingPhoto?.date ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }
    var notes by remember { mutableStateOf(existingPhoto?.notes ?: "") }
    var imageUrl by remember {
        mutableStateOf(
            existingPhoto?.imageUrl ?: "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?auto=format&fit=crop&w=600&q=80"
        )
    }

    // Launcher mở Thư viện ảnh
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUrl = uri.toString()
            Toast.makeText(context, "Đã chọn ảnh từ thư viện!", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher mở Camera chụp ảnh trực tiếp
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val fileUri = saveBitmapToTempFile(context, bitmap)
            imageUrl = fileUri
            Toast.makeText(context, "Đã chụp ảnh thành công từ Camera!", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = if (existingPhoto != null) "Chụp Lại / Đổi Ảnh Buổi" else "Chụp / Tải Ảnh Buổi Liệu Trình",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        containerColor = WarmBeigeSurface,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Khung ảnh xem trước theo khung dọc 3:4
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .aspectRatio(3f / 4f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(WarmBeigeSurfaceVariant)
                            .border(1.dp, WarmBeigeBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Xem trước ảnh",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("Chưa chọn ảnh", color = CharcoalTextMuted)
                        }
                    }
                }

                // 2 Nút chụp / tải ảnh
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chụp Camera", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tải từ máy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Nhập thông tin buổi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sessionNumberStr,
                        onValueChange = {
                            sessionNumberStr = it
                            title = "Buổi $it: Tiến trình điều trị"
                        },
                        label = { Text("Số buổi") },
                        modifier = Modifier.weight(0.35f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("Ngày chụp (dd/MM/yyyy)") },
                        modifier = Modifier.weight(0.65f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề buổi liệu trình") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Ghi chú / Nhận xét tình trạng da") },
                    placeholder = { Text("Ví dụ: Giảm đỏ 50%, da căng mịn hơn...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val num = sessionNumberStr.toIntOrNull() ?: 1
                    val record = SessionPhotoRecord(
                        id = existingPhoto?.id ?: "SP-${System.currentTimeMillis()}",
                        sessionNumber = num,
                        title = title.trim(),
                        date = dateStr.trim(),
                        imageUrl = imageUrl.trim(),
                        notes = notes.trim(),
                        technicianName = existingPhoto?.technicianName ?: "Chuyên viên"
                    )
                    onSavePhoto(record)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (existingPhoto != null) "Cập nhật ảnh" else "Lưu ảnh", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text("Hủy")
            }
        }
    )
}


