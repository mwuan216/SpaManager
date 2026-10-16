package com.spacare.manager.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val SpaShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp), // Chuẩn thẻ card 16dp mềm mại
    large = RoundedCornerShape(24.dp),  // Bottom sheets & Dialogs
    extraLarge = RoundedCornerShape(32.dp)
)

