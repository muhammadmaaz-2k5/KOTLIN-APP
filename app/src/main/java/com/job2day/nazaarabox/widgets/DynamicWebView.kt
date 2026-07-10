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
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun buildInjectionScript(userScript: String, readySelector: String?): String {
    if (!readySelector.isNullOrBlank()) {
        val safeSelector = readySelector.replace("\\", "\\\\").replace("'", "\\'")
        return """
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
        """.trimIndent()
    }
    return """
        (function() {
          try { (function(){ ${userScript} })(); }
          catch(e){ console.error('NazaaraWebView script error: ' + e); }
        })();
    """.trimIndent()
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
    scriptToInject: String? = null,
    readySelector: String? = null,
    onPageLoaded: (() -> Unit)? = null,
    autoClickDelayMs: Long? = 3000L,
    autoClickIntervalMs: Long = 3000L,
    clickYFraction: Float = 0.95f,
    wrapInCard: Boolean = true,
    enableVideoNavigationGuard: Boolean = false,
    onTouch: (() -> Unit)? = null,
    enableMultiTabs: Boolean = false,
    onNewTabCreated: ((WebView) -> Unit)? = null,
    onTabClosed: ((Int) -> Unit)? = null,
    onTabSwitched: ((Int) -> Unit)? = null,
    onError: ((String) -> Unit)? = null,
    onReady: (() -> Unit)? = null,
    enableDebug: Boolean = false
) {
    if (url.isBlank()) return

    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var isPageLoaded by remember { mutableStateOf(false) }
    var scriptInjected by remember { mutableStateOf(false) }
    var webViewError by remember { mutableStateOf<String?>(null) }
    
    // Single WebView reference - no existingWebView parameter to avoid conflicts
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    var timeoutJob by remember { mutableStateOf<Job?>(null) }
    var scriptInjectionJob by remember { mutableStateOf<Job?>(null) }
    var autoClickJob by remember { mutableStateOf<Job?>(null) }
    
    // Track current URL to prevent reload loops
    val currentUrl = remember(url) { url }
    
    // Use unique key only for initial creation
    val webViewKey = remember(url) { url.hashCode() }

    val currentOnPageLoaded by rememberUpdatedState(onPageLoaded)
    val currentOnReady by rememberUpdatedState(onReady)
    val currentOnError by rememberUpdatedState(onError)
    val currentInjectionScript by rememberUpdatedState(
        if (!scriptToInject.isNullOrBlank()) buildInjectionScript(scriptToInject, readySelector)
        else null
    )

    // Clean up WebView on dispose
    DisposableEffect(Unit) {
        onDispose {
            timeoutJob?.cancel()
            scriptInjectionJob?.cancel()
            autoClickJob?.cancel()
            
            webViewRef.value?.let { wv ->
                wv.stopLoading()
                wv.loadUrl("about:blank")
                wv.clearHistory()
                wv.clearCache(true)
                wv.destroy()
                webViewRef.value = null
            }
        }
    }

    // Reset state when URL changes
    LaunchedEffect(url) {
        isPageLoaded = false
        scriptInjected = false
        isLoading = true
        webViewError = null
        
        timeoutJob?.cancel()
        timeoutJob = coroutineScope.launch {
            delay(15_000)
            if (isLoading) {
                isLoading = false
                webViewError = "Loading timeout"
                currentOnError?.invoke("Loading timeout")
            }
        }
    }

    // Handle script injection after page loads
    LaunchedEffect(isPageLoaded, currentInjectionScript) {
        scriptInjectionJob?.cancel()
        
        val script = currentInjectionScript
        if (isPageLoaded && !script.isNullOrBlank() && !scriptInjected) {
            scriptInjectionJob = coroutineScope.launch {
                scriptInjected = true
                if (enableDebug) {
                    android.util.Log.d("DynamicWebView", "Injecting script")
                }
                webViewRef.value?.evaluateJavascript(script) { result ->
                    if (enableDebug) {
                        android.util.Log.d("DynamicWebView", "Script result: $result")
                    }
                }
            }
        }
    }

    // Handle auto-click
    LaunchedEffect(isPageLoaded, autoClickDelayMs, autoClickIntervalMs) {
        autoClickJob?.cancel()
        
        if (isPageLoaded && autoClickDelayMs != null && autoClickDelayMs > 0) {
            autoClickJob = coroutineScope.launch {
                delay(autoClickDelayMs)
                while (true) {
                    val wv = webViewRef.value
                    if (wv != null && wv.width > 0 && wv.height > 0) {
                        wv.post { simulateTouchOnWebView(wv, clickYFraction) }
                    }
                    delay(autoClickIntervalMs)
                }
            }
        }
    }

    val webViewContent = @Composable {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.runtime.key(webViewKey) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)

                            isVerticalScrollBarEnabled = isScrollEnabled
                            isHorizontalScrollBarEnabled = isScrollEnabled

                            settings.apply {
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
                                setSupportMultipleWindows(enableMultiTabs)
                                javaScriptCanOpenWindowsAutomatically = enableMultiTabs
                                mediaPlaybackRequiresUserGesture = false
                                allowUniversalAccessFromFileURLs = true
                                allowFileAccessFromFileURLs = true
                                pluginState = WebSettings.PluginState.ON
                                setRenderPriority(WebSettings.RenderPriority.HIGH)
                                // Improved video playback
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    setSafeBrowsingEnabled(false)
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    if (enableDebug) {
                                        android.util.Log.d("DynamicWebView", "Page started: $url")
                                    }
                                    isLoading = true
                                }

                                override fun onPageFinished(view: WebView?, pageUrl: String?) {
                                    super.onPageFinished(view, pageUrl)
                                    if (enableDebug) {
                                        android.util.Log.d("DynamicWebView", "Page finished: $pageUrl")
                                    }
                                    view?.post {
                                        isLoading = false
                                        isPageLoaded = true
                                        webViewError = null
                                        currentOnPageLoaded?.invoke()
                                        currentOnReady?.invoke()
                                    }
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val originalUrl = request?.url?.toString() ?: return false
                                    
                                    if (enableDebug) {
                                        android.util.Log.d("DynamicWebView", "shouldOverrideUrlLoading: $originalUrl")
                                    }
                                    
                                    // CRITICAL: Allow the main URL to load
                                    if (originalUrl == currentUrl || originalUrl.startsWith(currentUrl)) {
                                        if (enableDebug) {
                                            android.util.Log.d("DynamicWebView", "✅ Allowing main URL: $originalUrl")
                                        }
                                        return false
                                    }

                                    if (enableVideoNavigationGuard) {
                                        // Block known ad/spam domains
                                        if (VideoNavigationGuard.shouldBlockNavigation(originalUrl)) {
                                            if (enableDebug) {
                                                android.util.Log.d("DynamicWebView", "🚫 Blocked ad/spam: $originalUrl")
                                            }
                                            return true
                                        }

                                        // Allow video hosting services
                                        if (VideoNavigationGuard.isAllowedVideoHosting(originalUrl)) {
                                            if (enableDebug) {
                                                android.util.Log.d("DynamicWebView", "✅ Allowing video hosting: $originalUrl")
                                            }
                                            return false
                                        }

                                        // Block non-http schemas
                                        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
                                            if (enableDebug) {
                                                android.util.Log.d("DynamicWebView", "🚫 Blocked non-http: $originalUrl")
                                            }
                                            return true
                                        }

                                        // For iframes, always allow
                                        if (request != null && !request.isForMainFrame) {
                                            return false
                                        }

                                        // For main frame navigation, check if same service
                                        if (request != null && request.isForMainFrame) {
                                            val initialService = VideoNavigationGuard.getVideoHostingService(currentUrl)
                                            val requestService = VideoNavigationGuard.getVideoHostingService(originalUrl)

                                            if (initialService != null && 
                                                requestService != null && 
                                                initialService == requestService
                                            ) {
                                                if (enableDebug) {
                                                    android.util.Log.d("DynamicWebView", "✅ Same service: $originalUrl")
                                                }
                                                return false
                                            }

                                            // Block other main frame navigation
                                            if (enableDebug) {
                                                android.util.Log.d("DynamicWebView", "🚫 Blocked navigation: $originalUrl")
                                            }
                                            return true
                                        }
                                    }

                                    // Handle special URLs
                                    if (!originalUrl.startsWith("http")) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(originalUrl))
                                            ctx.startActivity(intent)
                                            return true
                                        } catch (e: Exception) {
                                            return false
                                        }
                                    }

                                    // Default: allow loading
                                    return false
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true) {
                                        val errorMsg = "${error?.description} (${error?.errorCode})"
                                        if (enableDebug) {
                                            android.util.Log.e("DynamicWebView", "Error: $errorMsg")
                                        }
                                        view?.post {
                                            // Don't show error for frame load interruptions (common on video sites)
                                            if (error?.errorCode != WebViewClient.ERROR_FAILED_SSL_HANDSHAKE &&
                                                error?.errorCode != WebViewClient.ERROR_HOST_LOOKUP) {
                                                isLoading = false
                                                webViewError = errorMsg
                                                currentOnError?.invoke(errorMsg)
                                            }
                                        }
                                    }
                                }

                                override fun onReceivedHttpError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    errorResponse: WebResourceResponse?
                                ) {
                                    super.onReceivedHttpError(view, request, errorResponse)
                                    if (enableDebug && request?.isForMainFrame == true) {
                                        android.util.Log.e("DynamicWebView", "HTTP Error: ${errorResponse?.statusCode}")
                                    }
                                }

                                override fun onReceivedSslError(
                                    view: WebView?,
                                    handler: SslErrorHandler?,
                                    error: android.net.http.SslError?
                                ) {
                                    if (enableDebug) {
                                        android.util.Log.e("DynamicWebView", "SSL Error: ${error?.toString()}")
                                    }
                                    // For video sites, proceed despite SSL errors
                                    handler?.proceed()
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    if (enableDebug) {
                                        android.util.Log.d("DynamicWebView", "Progress: $newProgress%")
                                    }
                                    if (newProgress >= 90) {
                                        view?.post {
                                            isLoading = false
                                        }
                                    }
                                }

                                override fun onShowCustomView(view: android.view.View?, callback: CustomViewCallback?) {
                                    super.onShowCustomView(view, callback)
                                    if (enableDebug) {
                                        android.util.Log.d("DynamicWebView", "Custom view shown (fullscreen video)")
                                    }
                                }

                                override fun onHideCustomView() {
                                    super.onHideCustomView()
                                    if (enableDebug) {
                                        android.util.Log.d("DynamicWebView", "Custom view hidden")
                                    }
                                }
                            }

                            // Prepare URL with headers
                            val adHeaders = mapOf(
                                "Referer" to "https://nazaarabox.com",
                                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                            )
                            
                            // REMOVED: Debug URL replacements that were breaking video loading
                            var finalUrl = currentUrl
                            
                            // Only apply debug replacements if explicitly enabled
                            if (enableDebug) {
                                finalUrl = finalUrl
                                    .replace(Regex("https?://(www\\.)?nazaaracircle\\.com"), "https://nazaarabox.com")
                            }

                            if (enableDebug) {
                                android.util.Log.d("DynamicWebView", "Loading URL: $finalUrl")
                            }
                            
                            // Store reference before loading
                            webViewRef.value = this
                            
                            // Load the URL
                            loadUrl(finalUrl, adHeaders)

                            if (onTouch != null) {
                                setOnTouchListener { _, event ->
                                    if (event.action == MotionEvent.ACTION_UP) {
                                        onTouch()
                                    }
                                    false
                                }
                            }
                        }
                    },
                    update = { wv ->
                        // Only update if needed - prevent reload loops
                        if (webViewRef.value != wv) {
                            webViewRef.value = wv
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Loading indicator
            if (isLoading && webViewError == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.Primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Loading video...",
                            color = Color.White,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                        )
                    }
                }
            }

            // Error state
            if (webViewError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️ Failed to load video",
                            color = Color.White,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = webViewError ?: "Unknown error",
                            color = Color.Gray,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                webViewError = null
                                isLoading = true
                                webViewRef.value?.reload()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }

    // Wrap in card if needed
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

object VideoNavigationGuard {
    fun getVideoHostingService(url: String): String? {
        val lowerUrl = url.lowercase()
        val patterns = mapOf(
            "onedrive" to listOf("1drv.ms", "onedrive.live.com", "sharepoint.com"),
            "doodstream" to listOf("doodstream.com", "dsvplay.com", "dood.to", "ds2play.com", "ds2video.com"),
            "vidsrc" to listOf("vidsrc.icu", "vidsrc.to", "vidsrc.me", "vidsrc.net", "vidsrc.xyz", "vidsrc.cc"),
            "mixdrop" to listOf("mixdrop.co", "mixdrop.to", "mixdrop.sx", "mixdrop.bz"),
            "streamtape" to listOf("streamtape.com", "streamtape.net", "streamtape.to"),
            "embedsito" to listOf("embedsito.com"),
            "embedsu" to listOf("embed.su"),
            "upstream" to listOf("upstream.to"),
            "youtube" to listOf("youtube.com", "youtu.be"),
            "vimeo" to listOf("vimeo.com"),
            "dailymotion" to listOf("dailymotion.com"),
            "streamable" to listOf("streamable.com"),
            "mdy48tn97" to listOf("mdy48tn97.com"),
            "vidstream" to listOf("vidstream.pro"),
            "gogostream" to listOf("gogo-stream.com"),
            "mp4upload" to listOf("mp4upload.com"),
            "streamlare" to listOf("streamlare.com"),
            "filemoon" to listOf("filemoon.sx"),
            "cdn" to listOf("cloudflare.com", "cloudfront.net", "googleapis.com", "gstatic.com", "jwpcdn.com", "jwplatform.com")
        )

        for ((service, domains) in patterns) {
            for (domain in domains) {
                if (lowerUrl.contains(domain)) {
                    return service
                }
            }
        }
        return null
    }

    fun isAllowedVideoHosting(url: String): Boolean {
        return getVideoHostingService(url) != null
    }

    fun shouldBlockNavigation(url: String): Boolean {
        if (isAllowedVideoHosting(url)) {
            return false
        }

        val lowerUrl = url.lowercase()
        val blockedPatterns = listOf(
            "doubleclick.net", "googlesyndication.com", "google-analytics.com",
            "adservice.google", "advertising.com", "adnxs.com", "adsystem.com",
            "adsrvr.org", "adroll.com", "serving-sys.com", "adcolony.com",
            "applovin.com", "chartboost.com", "unity3d.com", "ironsrc.com",
            "facebook.com", "twitter.com", "instagram.com", "pinterest.com",
            "linkedin.com", "reddit.com", "tiktok.com", "snapchat.com",
            "play.google.com", "apps.apple.com", "itunes.apple.com"
        )

        for (pattern in blockedPatterns) {
            if (lowerUrl.contains(pattern)) {
                return true
            }
        }

        if (lowerUrl.contains("/app/") || lowerUrl.contains("/apps/")) {
            return true
        }

        return false
    }
}