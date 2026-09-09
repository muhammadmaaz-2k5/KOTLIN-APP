package com.job2day.nazaarabox.ads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.widgets.DynamicWebView

@Composable
fun CustomSmallCardAd(
    adUrl: String = AdManager.webviewAdUrl,
    backgroundColor: Color = AppColors.SurfaceVariantDark,
    modifier: Modifier = Modifier,
    showClose: Boolean = true,
) {
    if (!AdManager.isAdsEnabled) {
        return
    }

    var isVisible by remember { mutableStateOf(true) }

    val effectiveUrl = remember(adUrl) {
        AdManager.sanitizeAdUrl(adUrl.ifBlank { AdManager.webviewAdUrl })
    }

    if (isVisible) {
        Surface(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF161726),
            border = BorderStroke(1.dp, Color(0xFF333550)),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                DynamicWebView(
                    url = effectiveUrl,
                    modifier = Modifier.fillMaxSize(),
                    height = null,
                    autoClickDelayMs = null,
                    wrapInCard = false,
                )

                if (showClose) {
                    IconButton(
                        onClick = { isVisible = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .padding(2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}
