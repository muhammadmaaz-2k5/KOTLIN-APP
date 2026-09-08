package com.job2day.nazaarabox.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * ENGORA Official Cinema Brand Colors
 */
@Immutable
object EngoraColors {
    // Signature Cinema Red (Netflix/Engora Ribbon Identity)
    val Red = Color(0xFFE50914)
    val RedLight = Color(0xFFFF2E3D)
    val RedDark = Color(0xFF991218)
    val RedDeep = Color(0xFF6B0B10)
    val RedGlow = Color(0xFFFF384C)

    // Deep Obsidian / Cinematic Blacks (True OLED streaming dark mode)
    val Background = Color(0xFF09090C)
    val BackgroundElevated = Color(0xFF0F0F14)
    val Surface = Color(0xFF14141D)
    val SurfaceVariant = Color(0xFF1B1B26)
    val Card = Color(0xFF181822)
    val CardElevated = Color(0xFF222230)
    val CardBorder = Color(0xFF2B2B3D)
    val GlassSurface = Color(0xFF14141D).copy(alpha = 0.82f)
    val GlassBorder = Color.White.copy(alpha = 0.12f)
    val GlassHighlight = Color.White.copy(alpha = 0.06f)

    // Vibrant Brand Accents
    val Gold = Color(0xFFFFB800)       // Ratings, 4K UHD, Top 10 badges
    val GoldDark = Color(0xFFD49200)
    val Cyan = Color(0xFF00E5FF)       // Series / TV badge, tech specs
    val Purple = Color(0xFF7C4DFF)     // Anime, special collections
    val Pink = Color(0xFFFF3366)       // Trending rank, featured live
    val Emerald = Color(0xFF00E676)    // Free streaming, fresh release
    val Error = Color(0xFFFF5252)

    // Text & Neutrals
    val TextPrimary = Color(0xFFF5F5FA)
    val TextSecondary = Color(0xFFA5A5BC)
    val TextMuted = Color(0xFF6E6E88)
    val TextDisabled = Color(0xFF454558)
    val OverlayScrim = Color.Black.copy(alpha = 0.70f)

    fun ratingColor(rating: Double): Color = when {
        rating >= 7.5 -> Gold
        rating >= 6.0 -> Emerald
        rating >= 4.5 -> Color(0xFFFFB74D)
        else -> Error
    }
}

/**
 * ENGORA Signature Gradients
 */
object EngoraGradients {
    val BrandLinear = Brush.horizontalGradient(
        listOf(EngoraColors.RedLight, EngoraColors.Red, EngoraColors.RedDark)
    )

    val BrandVertical = Brush.verticalGradient(
        listOf(EngoraColors.RedLight, EngoraColors.Red, EngoraColors.RedDark)
    )

    val CardBorderGradient = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.18f),
            Color.White.copy(alpha = 0.04f),
        )
    )

    val HeroScrim = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color.Black.copy(alpha = 0.75f),
            0.20f to Color.Black.copy(alpha = 0.20f),
            0.45f to Color.Transparent,
            0.65f to EngoraColors.Background.copy(alpha = 0.70f),
            0.88f to EngoraColors.Background.copy(alpha = 0.95f),
            1.0f to EngoraColors.Background,
        )
    )

    val GoldBadge = Brush.horizontalGradient(
        listOf(Color(0xFFFFC72C), Color(0xFFFF9900))
    )

    val TrendingRank = Brush.horizontalGradient(
        listOf(Color(0xFFFF2442), Color(0xFFFF5E3A))
    )

    val GlassShine = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.14f),
            Color.White.copy(alpha = 0.03f),
        )
    )
}

val LocalEngoraColors = staticCompositionLocalOf { EngoraColors }
