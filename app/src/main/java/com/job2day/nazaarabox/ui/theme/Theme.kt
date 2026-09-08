package com.job2day.nazaarabox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val EngoraDarkColorScheme = darkColorScheme(
    primary = EngoraColors.Red,
    onPrimary = EngoraColors.TextPrimary,
    primaryContainer = EngoraColors.RedDark,
    onPrimaryContainer = EngoraColors.TextPrimary,
    secondary = EngoraColors.Cyan,
    onSecondary = EngoraColors.Background,
    tertiary = EngoraColors.Gold,
    background = EngoraColors.Background,
    onBackground = EngoraColors.TextPrimary,
    surface = EngoraColors.Surface,
    onSurface = EngoraColors.TextPrimary,
    surfaceVariant = EngoraColors.SurfaceVariant,
    onSurfaceVariant = EngoraColors.TextSecondary,
    error = EngoraColors.Error,
    outline = EngoraColors.CardBorder,
    outlineVariant = EngoraColors.SurfaceVariant,
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

/**
 * ENGORA Official Branded Platform Theme
 */
@Composable
fun EngoraTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalEngoraColors provides EngoraColors,
        LocalEngoraTypography provides EngoraTypography,
        LocalEngoraShapes provides EngoraShapes,
    ) {
        MaterialTheme(
            colorScheme = EngoraDarkColorScheme,
            typography = androidx.compose.material3.Typography(
                titleLarge = EngoraTypography.TitleLarge,
                titleMedium = EngoraTypography.TitleMedium,
                titleSmall = EngoraTypography.TitleSmall,
                bodyLarge = EngoraTypography.BodyMedium,
                bodyMedium = EngoraTypography.BodyMedium,
                bodySmall = EngoraTypography.BodySmall,
                labelLarge = EngoraTypography.ButtonText,
                labelMedium = EngoraTypography.Caption,
                labelSmall = EngoraTypography.BadgeLabel,
            ),
            content = content,
        )
    }
}

/**
 * Backwards-compatible alias for existing invocations
 */
@Composable
fun NazaaraboxTheme(content: @Composable () -> Unit) {
    EngoraTheme(content = content)
}

/**
 * Quick access extensions
 */
val MaterialTheme.engoraColors: EngoraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalEngoraColors.current

val MaterialTheme.engoraTypography: EngoraTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalEngoraTypography.current

val MaterialTheme.engoraShapes: EngoraShapes
    @Composable
    @ReadOnlyComposable
    get() = LocalEngoraShapes.current
