package com.job2day.nazaarabox.widgets

import android.annotation.SuppressLint
import android.content.Intent
import android.os.SystemClock
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.job2day.nazaarabox.ui.theme.AppColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Wraps arbitrary user JS in a smart `waitAndRun` helper.
 *
 * The helper polls (every 300 ms, up to 20 times) until
 * `readySelector` is present in the DOM, then calls the user's [userScript].
 * If [readySelector] is null / blank the user script is executed immediately.
 */
private fun buildInjectionScript(userScript: String, readySelector: String?): String {
    return if (!readySelector.isNullOrBlank()) {
        val safeSelector = readySelector.replace("\\", "\\\\").replace("'", "\\'")
        """
(function() {
  var _maxTries = 20;
  var _tries    = 0;
  function waitAndRun() {
    var el = document.querySelector('$safeSelector');
    if (el) {
      try { (function(){ ${userScript} })(); }
      catch(e){ console.error('NazaaraWebView script error: ' + e); }
    } else if (_tries < _maxTries) {
      _tries++;
      setTimeout(waitAndRun, 300);
    }
  }
  waitAndRun();
})();
"""
    } else {
        """
(function() {
  try { (function(){ ${userScript} })(); }
  catch(e){ console.error('NazaaraWebView script error: ' + e); }
})();
"""
    }
}

private fun simulateTouchOnWebView(webView: WebView, yFraction: Float = 0.95f) {
    val w = webView.width
    val h = webView.height
    if (w <= 0 || h <= 0) return

    val x = w / 2f
    val y = h * yFraction
    val downTime = SystemClock.uptimeMillis()
    val eventTime = SystemClock.uptimeMillis()

    val downEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0)
    webView.dispatchTouchEvent(downEvent)
    downEvent.recycle()

    val upEvent = MotionEvent.obtain(downTime, eventTime + 50, MotionEvent.ACTION_UP, x, y, 0)
    webView.dispatchTouchEvent(upEvent)
    upEvent.recycle()
}

@SuppressLint("SetJavaScriptEnabled", "SetSupportMultipleWindows")
@Composable
fun DynamicWebView(
    url: String,
    modifier: Modifier = Modifier,
    height: Dp? = 490.dp,
    isScrollEnabled: Boolean = true,
    useWideViewPort: Boolean = true,
    existingWebView: WebView? = null,
    scriptToInject: String? = null,
    readySelector: String? = null,
    onPageLoaded: (() -> Unit)? = null,
    autoClickDelayMs: Long? = 3000L,
    autoClickIntervalMs: Long = 3000L,
    clickYFraction: Float = 0.95f,
    wrapInCard: Boolean = true
) {
    if (url.isBlank()) return

    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var isPageLoaded by remember { mutableStateOf(false) }
    var scriptInjected by remember { mutableStateOf(false) }

    val currentOnPageLoaded by rememberUpdatedState(onPageLoaded)
    val currentInjectionScript by rememberUpdatedState(
        if (!scriptToInject.isNullOrBlank()) buildInjectionScript(scriptToInject, readySelector)
        else null
    )

    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    // Job references for cancellation
    var timeoutJob by remember { mutableStateOf<Job?>(null) }
    var scriptInjectionJob by remember { mutableStateOf<Job?>(null) }
    var autoClickJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(url) {
        isPageLoaded = false
        scriptInjected = false
        isLoading = true
        
        // Cancel previous timeout job
        timeoutJob?.cancel()
        
        // Timeout fallback: hide loader after 10s even if page hasn't fully finished
        timeoutJob = coroutineScope.launch {
            delay(10_000)
            isLoading = false
        }
    }

    LaunchedEffect(isPageLoaded, currentInjectionScript) {
        // Cancel previous script injection job
        scriptInjectionJob?.cancel()
        
        val script = currentInjectionScript
        if (isPageLoaded && !script.isNullOrBlank() && !scriptInjected) {
            scriptInjectionJob = coroutineScope.launch {
                scriptInjected = true
                android.util.Log.d("DynamicWebView", "Late-injecting interaction script")
                webViewRef.value?.evaluateJavascript(script) { result ->
                    android.util.Log.d("DynamicWebView", "Script result: $result")
                }
            }
        }
    }

    LaunchedEffect(isPageLoaded, autoClickDelayMs, autoClickIntervalMs) {
        // Cancel previous auto-click job
        autoClickJob?.cancel()
        
        if (isPageLoaded && autoClickDelayMs != null && autoClickDelayMs > 0) {
            autoClickJob = coroutineScope.launch {
                // Initial delay before the first click
                android.util.Log.d("DynamicWebView", "Scheduling auto-click in ${autoClickDelayMs}ms, then every ${autoClickIntervalMs}ms")
                kotlinx.coroutines.delay(autoClickDelayMs)
                while (true) {
                    val wv = webViewRef.value
                    if (wv != null) {
                        android.util.Log.d("DynamicWebView", "Dispatching real touch event on WebView at y=${clickYFraction*100}%")
                        wv.post { simulateTouchOnWebView(wv, clickYFraction) }
                    }
                    kotlinx.coroutines.delay(autoClickIntervalMs)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Cancel all jobs when composable is disposed
            timeoutJob?.cancel()
            scriptInjectionJob?.cancel()
            autoClickJob?.cancel()
        }
    }

    val webViewContent = @Composable {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    existingWebView ?: object : WebView(ctx) {
                        override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
                            if (isScrollEnabled) {
                                requestDisallowInterceptTouchEvent(true)
                            }
                            return super.onTouchEvent(event)
                        }
                    }.also { wv ->
                        wv.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        wv.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        wv.setBackgroundColor(android.graphics.Color.TRANSPARENT)

                        wv.isVerticalScrollBarEnabled = isScrollEnabled
                        wv.isHorizontalScrollBarEnabled = isScrollEnabled

                        wv.settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            builtInZoomControls = false
                            displayZoomControls = false
                            this.useWideViewPort = useWideViewPort
                            loadWithOverviewMode = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            setSupportZoom(false)
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            javaScriptCanOpenWindowsAutomatically = false
                            setSupportMultipleWindows(false)
                            mediaPlaybackRequiresUserGesture = false
                        }

                        wv.webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, pageUrl: String?) {
                                super.onPageFinished(view, pageUrl)
                                view?.post {
                                    isLoading = false
                                    isPageLoaded = true
                                    currentOnPageLoaded?.invoke()
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    view?.post { isLoading = false }
                                    android.util.Log.e(
                                        "DynamicWebView",
                                        "Error: ${error?.description} (${error?.errorCode}) for ${request?.url}"
                                    )
                                }
                            }

                            override fun onReceivedSslError(
                                view: WebView?,
                                handler: SslErrorHandler?,
                                error: android.net.http.SslError?
                            ) {
                                android.util.Log.e("DynamicWebView", "SSL Error: ${error?.toString()}")
                                handler?.cancel()
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val originalUrl = request?.url?.toString() ?: return false
                                var clickedUrl = originalUrl

                                if (com.job2day.nazaarabox.BuildConfig.DEBUG) {
                                    val isLocalOrBlogUrl =
                                        clickedUrl.contains("nazaaracircle.com", ignoreCase = true) ||
                                        clickedUrl.contains("nazaarabox.com", ignoreCase = true) ||
                                        clickedUrl.contains("127.0.0.1", ignoreCase = true) ||
                                        clickedUrl.contains("localhost", ignoreCase = true)

                                    if (isLocalOrBlogUrl) {
                                        clickedUrl = clickedUrl
                                            .replace(Regex("https?://(www\\.)?nazaaracircle\\.com"), "https://nazaarabox.com")
                                            .replace(Regex("https?://(www\\.)?nazaarabox\\.com"), "https://nazaarabox.com")
                                            .replace(Regex("https?://blog\\.nazaarabox\\.com"), "https://nazaarabox.com")
                                            .replace(Regex("https?://127\\.0\\.0\\.1"), "http://10.0.2.2")
                                            .replace(Regex("https?://localhost"), "http://10.0.2.2")
                                    }
                                }

                                if (clickedUrl != originalUrl || !clickedUrl.startsWith("http")) {
                                    if (!clickedUrl.startsWith("http")) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(clickedUrl))
                                            ctx.startActivity(intent)
                                            return true
                                        } catch (e: Exception) {
                                            return false
                                        }
                                    }
                                    view?.loadUrl(clickedUrl)
                                    return true
                                }

                                return false
                            }
                        }

                        wv.webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                if (newProgress == 100) {
                                    view?.post { isLoading = false }
                                }
                            }

                            override fun onCreateWindow(
                                view: WebView?,
                                isDialog: Boolean,
                                isUserGesture: Boolean,
                                resultMsg: android.os.Message?
                            ): Boolean {
                                return false
                            }
                        }

                        val adHeaders = mapOf("Referer" to "https://nazaarabox.com")
                        var finalUrl = url
                        if (com.job2day.nazaarabox.BuildConfig.DEBUG) {
                            finalUrl = finalUrl
                                .replace(Regex("https?://(www\\.)?nazaaracircle\\.com"), "https://nazaarabox.com")
                                .replace(Regex("https?://(www\\.)?nazaarabox\\.com"), "https://nazaarabox.com")
                                .replace(Regex("https?://blog\\.nazaarabox\\.com"), "https://nazaarabox.com")
                                .replace(Regex("https?://127\\.0\\.0\\.1"), "http://10.0.2.2")
                                .replace(Regex("https?://localhost"), "http://10.0.2.2")
                        }
                        wv.loadUrl(finalUrl, adHeaders)
                        webViewRef.value = wv
                    }
                },
                update = { wv ->
                    webViewRef.value = wv
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = AppColors.Primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    if (wrapInCard) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .let { if (height != null) it.height(height) else it.fillMaxHeight() }
                .padding(12.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = AppColors.CardDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            tonalElevation = 2.dp
        ) {
            webViewContent()
        }
    } else {
        Box(
            modifier = modifier
                .let { if (height != null) it.height(height) else it },
            contentAlignment = Alignment.Center
        ) {
            webViewContent()
        }
    }
}
