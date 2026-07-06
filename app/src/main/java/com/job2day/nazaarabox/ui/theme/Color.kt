package com.job2day.nazaarabox.ui.theme

import androidx.compose.ui.graphics.Color

val NazaaraBlackBackground = Color(0xFF000000)
val NazaaraBoxHeaderBackground = Color(0xFF000000)
val NazaaraBoxPrimary = Color(0xFFE50914)
val NazaaraBoxCardBackground = Color(0xFF1E1E1E)
val PrimaryRed = Color(0xFFE50914)

object AppColors {
    val Primary = Color(0xFF6C5CE7)
    val PrimaryContainer = Color(0xFF3D2F9E)
    val Secondary = Color(0xFF00CEC9)
    val Accent = Color(0xFFFDCB6E)
    val Success = Color(0xFF00B894)
    val Warning = Color(0xFFFDCB6E)
    val Error = Color(0xFFE17055)
    val BackgroundDark = Color(0xFF12121A)
    val SurfaceDark = Color(0xFF1E1E2E)
    val SurfaceVariantDark = Color(0xFF2A2A3E)
    val CardDark = Color(0xFF252538)
    val TextPrimary = Color(0xFFE6E6F0)
    val TextMuted = Color(0xFF888899)
    val Outline = Color(0xFF444466)

    val TabHome = Color(0xFF6C5CE7)
    val TabMovies = Color(0xFF0984E3)
    val TabTv = Color(0xFF00B894)
    val TabAnime = Color(0xFFFF6B9D)
    val TabSearch = Color(0xFFFDAA07)

    fun ratingColor(rating: Double): Color = when {
        rating >= 7.5 -> Success
        rating >= 6.0 -> Warning
        else -> Error
    }
}
