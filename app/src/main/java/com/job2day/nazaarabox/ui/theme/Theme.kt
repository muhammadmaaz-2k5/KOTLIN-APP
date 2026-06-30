package com.job2day.nazaarabox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.TextPrimary,
    primaryContainer = AppColors.PrimaryContainer,
    secondary = AppColors.Secondary,
    surface = AppColors.SurfaceDark,
    onSurface = AppColors.TextPrimary,
    error = AppColors.Error,
    outline = AppColors.Outline,
    outlineVariant = AppColors.SurfaceVariantDark,
)

object AppTypography {
    val titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
    )
    val titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    )
    val bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    )
    val labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
    )
}

@Composable
fun NazaaraboxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = androidx.compose.material3.Typography(
            titleLarge = AppTypography.titleLarge,
            titleMedium = AppTypography.titleMedium,
            bodyMedium = AppTypography.bodyMedium,
            labelSmall = AppTypography.labelSmall,
        ),
        content = content,
    )
}
