package com.job2day.nazaarabox.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * ENGORA Standardized Curved Shapes & Geometry
 */
@Immutable
object EngoraShapes {
    val Badge = RoundedCornerShape(6.dp)
    val Chip = RoundedCornerShape(10.dp)
    val Poster = RoundedCornerShape(12.dp)
    val Card = RoundedCornerShape(14.dp)
    val Sheet = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val Dialog = RoundedCornerShape(20.dp)
    val Button = RoundedCornerShape(24.dp)
    val FullPill = RoundedCornerShape(999.dp)
}

val LocalEngoraShapes = staticCompositionLocalOf { EngoraShapes }
