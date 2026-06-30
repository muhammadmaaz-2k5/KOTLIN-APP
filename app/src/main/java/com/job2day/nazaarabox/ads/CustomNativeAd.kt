package com.job2day.nazaarabox.ads

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.viewinterop.AndroidView
import com.job2day.nazaarabox.R
import com.job2day.nazaarabox.ui.theme.AppColors

@Composable
fun CustomNativeAd(
    adUrl: String = "https://nazaarabox.com",
    backgroundColor: Color = AppColors.SurfaceVariantDark,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(true) }
    
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(backgroundColor, RoundedCornerShape(12.dp)),
        ) {
            AndroidView(
                factory = {
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = WebViewClient()
                        loadUrl(adUrl)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            
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
