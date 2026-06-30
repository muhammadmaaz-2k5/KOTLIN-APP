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
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            builtInZoomControls = false
                            displayZoomControls = false
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        }
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                Log.d("CustomNativeAd", "Page loaded: $url")
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                Log.e("CustomNativeAd", "Error: ${error?.description} for ${request?.url}")
                            }

                            override fun onReceivedSslError(
                                view: WebView?,
                                handler: android.webkit.SslErrorHandler?,
                                error: android.net.http.SslError?
                            ) {
                                Log.e("CustomNativeAd", "SSL Error: $error")
                                handler?.cancel()
                            }
                        }
                        loadUrl(adUrl)
                    }
                },
                modifier = Modifier.fillMaxSize(),
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
