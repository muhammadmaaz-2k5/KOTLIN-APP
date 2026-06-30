package com.job2day.nazaarabox.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun adaptiveGridColumns(): Int {
    val width = LocalConfiguration.current.screenWidthDp
    return if (width >= 600) 3 else 2
}
