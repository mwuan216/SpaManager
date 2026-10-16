package com.spacare.manager.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SpaLightColorScheme = lightColorScheme(
    primary = SageGreenPrimary,
    onPrimary = WarmBeigeSurface,
    primaryContainer = SageGreenContainer,
    onPrimaryContainer = SageGreenOnContainer,
    secondary = SageGreenSecondary,
    onSecondary = WarmBeigeSurface,
    secondaryContainer = SageGreenContainer,
    onSecondaryContainer = SageGreenOnContainer,
    tertiary = SpaGoldPrimary,
    onTertiary = WarmBeigeSurface,
    tertiaryContainer = SpaGoldContainer,
    onTertiaryContainer = SpaGoldPrimary,
    background = WarmBeigeBackground,
    onBackground = CharcoalTextPrimary,
    surface = WarmBeigeSurface,
    onSurface = CharcoalTextPrimary,
    surfaceVariant = WarmBeigeSurfaceVariant,
    onSurfaceVariant = CharcoalTextSecondary,
    outline = WarmBeigeBorder,
    error = AllergyAlertRed,
    onError = WarmBeigeSurface,
    errorContainer = AllergyAlertContainer,
    onErrorContainer = AllergyAlertRed
)

@Composable
fun SpaCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Spa salon interface ưu tiên tông sáng, tao nhã và thanh lịch
    MaterialTheme(
        colorScheme = SpaLightColorScheme,
        typography = SpaTypography,
        shapes = SpaShapes,
        content = content
    )
}

