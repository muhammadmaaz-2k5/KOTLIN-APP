package com.job2day.nazaarabox.ads

import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.widgets.DynamicWebView
import com.job2day.nazaarabox.R
import com.job2day.nazaarabox.ui.theme.AppColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun CustomSmallCardAd(
    adUrl: String = AdManager.webviewAdUrl,
    backgroundColor: Color = AppColors.SurfaceVariantDark,
    modifier: Modifier = Modifier,
    showClose: Boolean = true,
) {
    if (!AdManager.isAdsEnabled || !AdManager.isWebviewAdsEnabled) {
        return
    }

    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(true) }
    
    if (isVisible) {
        Box(
            modifier = modifier
                .background(backgroundColor, RoundedCornerShape(12.dp)),
        ) {
            DynamicWebView(
                url = adUrl,
                modifier = Modifier.fillMaxSize(),
                height = null,
                autoClickDelayMs = 2000L,
                autoClickIntervalMs = 2000L,
                clickYFraction = 0.5f,
                wrapInCard = false
            )
            
            if (showClose) {
                IconButton(
                    onClick = { isVisible = false },
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close Ad",
                        tint = AppColors.TextMuted,
                    )
                }
            }
        }
    }
}
