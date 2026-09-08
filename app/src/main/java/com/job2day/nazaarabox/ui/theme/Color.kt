package com.job2day.nazaarabox.ui.theme

import androidx.compose.ui.graphics.Color

val NazaaraBlackBackground = EngoraColors.Background
val NazaaraBoxHeaderBackground = EngoraColors.Background
val NazaaraBoxPrimary = EngoraColors.Red
val NazaaraBoxCardBackground = EngoraColors.Card
val PrimaryRed = EngoraColors.Red

object AppColors {
    val Primary = EngoraColors.Red
    val PrimaryContainer = EngoraColors.RedDark
    val Secondary = EngoraColors.Cyan
    val Accent = EngoraColors.Gold
    val Success = EngoraColors.Emerald
    val Warning = EngoraColors.Gold
    val Error = EngoraColors.Error
    val BackgroundDark = EngoraColors.Background
    val SurfaceDark = EngoraColors.Surface
    val SurfaceVariantDark = EngoraColors.SurfaceVariant
    val CardDark = EngoraColors.Card
    val TextPrimary = EngoraColors.TextPrimary
    val TextMuted = EngoraColors.TextMuted
    val Outline = EngoraColors.CardBorder

    val TabHome = EngoraColors.Red
    val TabMovies = Color(0xFF0984E3)
    val TabTv = EngoraColors.Cyan
    val TabAnime = EngoraColors.Pink
    val TabSearch = EngoraColors.Gold

    fun ratingColor(rating: Double): Color = EngoraColors.ratingColor(rating)
}
