package com.job2day.nazaarabox.components

import com.job2day.nazaarabox.utils.Logger
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.os.Message
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.job2day.nazaarabox.util.EmbedProcessor
import com.job2day.nazaarabox.util.EmbedData
import com.job2day.nazaarabox.utils.PlaybackPositionService
import com.job2day.nazaarabox.ui.theme.PrimaryRed

@Composable
fun VideoPlayer(
    embedUrl: String?,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 16f / 9f,
    showFullscreenButton: Boolean = true,
    isFullscreen: Boolean = false,
    onFullscreenClick: (() -> Unit)? = null,
    onServerError: (() -> Unit)? = null,
    thumbnailUrl: String? = null
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorTriggered by remember { mutableStateOf(false) }
    
    // Process embed URL using EmbedProcessor
    val embedData = remember(embedUrl) {
        if (embedUrl.isNullOrEmpty()) {
            EmbedData(url = "")
        } else {
            EmbedProcessor.processEmbedUrl(embedUrl)
        }
    }
    
    val processedUrl = embedData.url
    
    // Check if this is a vidsrc embed
    val isVidsrc = remember(processedUrl) {
        processedUrl.lowercase().let { url ->
            url.contains("vidsrc.icu") || 
            url.contains("vidsrc.to") || 
            url.contains("vidsrc.me") ||
            url.contains("vidsrc.net") ||
            url.contains("vidsrc.xyz") ||
            url.contains("vidsrc.cc") ||
            url.contains("vidfast.pro") ||
            url.contains("vidlink.pro") ||
            url.contains("vidsrc")
        }
    }
    
    // Fallback timeout: stop showing spinner if load takes too long
    // Increased timeout for src embeds to give them more time to load
    // For vidsrc embeds, don't trigger error on timeout - just stop loading indicator
    LaunchedEffect(embedUrl, isVidsrc) {
        isLoading = true
        hasError = false
        errorTriggered = false
        kotlinx.coroutines.delay(20_000) // Increased timeout to 20 seconds for src embeds
        if (isLoading && !errorTriggered) {
            isLoading = false
            // For vidsrc embeds, don't show error or trigger server switch
            if (!isVidsrc) {
                hasError = true
                errorTriggered = true
                // Trigger server error callback after timeout to try next server
                onServerError?.invoke()
            } else {
                // For vidsrc, just stop loading but don't show error
                hasError = false
            }
        }
    }
    
    // Get saved playback position for this URL
    val savedPosition = remember(processedUrl) {
        if (processedUrl.isNotEmpty()) {
            PlaybackPositionService.getPosition(context, processedUrl)
        } else {
            0L
        }
    }
    
    // Check if this is a Doodstream URL that needs position fix
    val isDoodstream = remember(processedUrl) {
        processedUrl.lowercase().let { url ->
            url.contains("doodstream.com") || 
            url.contains("dsvplay.com") || 
            url.contains("dood.to") ||
            url.contains("ds2play.com") ||
            url.contains("ds2video.com")
        }
    }
    
    // Calculate height based on screen width to maintain aspect ratio
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val videoHeight = remember(screenWidth, aspectRatio) {
        with(density) {
            (screenWidth.value / aspectRatio).dp
        }
    }
    
    if (processedUrl.isEmpty()) {
        Box(
            modifier = if (isFullscreen) {
                modifier.fillMaxSize()
            } else {
                modifier
                    .fillMaxWidth()
                    .height(videoHeight)
            },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    color = PrimaryRed,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        return
    }
    
    // Build HTML with proper iframe styling
    val htmlContent = remember(embedData, processedUrl) {
        buildString {
            append("<!DOCTYPE html>")
            append("<html><head>")
            append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\">")
            append("<style>")
            append("html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background-color: #000; display: flex; align-items: center; justify-content: center; }")
            
            // YouTube-specific styling for mobile embedding
            if (processedUrl.contains("youtube.com")) {
                append("iframe { width: 100%; height: 100%; border: none; display: block; }")
                // Prevent YouTube from showing related videos and branding as much as possible
                append(".ytp-pause-overlay { display: none !important; }")
            } else if (isDoodstream) {
                append("iframe { width: 100%; height: 100%; border: none; display: block; margin: 0 auto; }")
            } else {
                append("iframe { width: 100%; height: 100%; border: none; display: block; }")
            }
            embedData.divStyle?.let {
                append("div { $it }")
            }
            embedData.iframeStyle?.let {
                append("iframe { $it }")
            }
            append("</style>")
            
            // Add JavaScript for Doodstream playback position tracking
            if (isDoodstream && savedPosition > 0) {
                append("<script>")
                append("""
                    (function() {
                        var savedPosition = ${savedPosition};
                        var positionRestored = false;
                        var iframe = null;
                        
                        function tryRestorePosition() {
                            if (positionRestored || savedPosition <= 0) return;
                            
                            try {
                                iframe = document.querySelector('iframe');
                                if (!iframe) {
                                    setTimeout(tryRestorePosition, 500);
                                    return;
                                }
                                
                                // Try to access iframe content (may fail due to CORS)
                                try {
                                    var iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
                                    var video = iframeDoc.querySelector('video');
                                    
                                    if (video) {
                                        video.currentTime = savedPosition;
                                        positionRestored = true;
                                        console.log('Position restored to: ' + savedPosition);
                                    } else {
                                        // Try alternative selectors
                                        var players = iframeDoc.querySelectorAll('[class*="player"], [id*="player"], video, [class*="video"], [id*="video"]');
                                        for (var i = 0; i < players.length; i++) {
                                            if (players[i].tagName === 'VIDEO' || players[i].currentTime !== undefined) {
                                                players[i].currentTime = savedPosition;
                                                positionRestored = true;
                                                console.log('Position restored to: ' + savedPosition);
                                                break;
                                            }
                                        }
                                    }
                                } catch (e) {
                                    // CORS error - try postMessage approach
                                    iframe.contentWindow.postMessage({
                                        type: 'seek',
                                        time: savedPosition
                                    }, '*');
                                    
                                    // Also try to inject script into iframe
                                    setTimeout(function() {
                                        try {
                                            var script = iframe.contentDocument.createElement('script');
                                            script.textContent = '''
                                                if (document.querySelector('video')) {
                                                    document.querySelector('video').currentTime = ${savedPosition};
                                                }
                                            ''';
                                            iframe.contentDocument.head.appendChild(script);
                                        } catch (err) {
                                            console.log('Cannot inject script due to CORS');
                                        }
                                    }, 2000);
                                }
                            } catch (e) {
                                console.log('Error restoring position: ' + e.message);
                            }
                        }
                        
                        // Try to restore position when page loads
                        window.addEventListener('load', function() {
                            setTimeout(tryRestorePosition, 1000);
                            setTimeout(tryRestorePosition, 3000);
                            setTimeout(tryRestorePosition, 5000);
                        });
                        
                        // Also try on iframe load
                        document.addEventListener('DOMContentLoaded', function() {
                            setTimeout(tryRestorePosition, 1000);
                        });
                    })();
                """.trimIndent())
                append("</script>")
            }
            
            val bodyStyle = if (isDoodstream) {
                "margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; display: flex; align-items: center; justify-content: center;"
            } else {
                "margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden;"
            }
            append("</head><body style=\"$bodyStyle\">")
            
            if (embedData.hasCustomStyling && embedData.divStyle != null) {
                append("<div style=\"${embedData.divStyle}\">")
            }
            
            val iframeBaseStyle = if (isDoodstream && embedData.iframeStyle == null) {
                "width: 100%; height: 100%; border: none; display: block; margin: 0 auto;"
            } else {
                "width: 100%; height: 100%; border: none; display: block;"
            }
            
            append("<iframe")
            append(" id=\"video-iframe\"")
            append(" src=\"$processedUrl\"")
            append(" allowfullscreen")
            // Add additional attributes for vidsrc for better compatibility
            if (isVidsrc) {
                append(" allow=\"autoplay; fullscreen; picture-in-picture; encrypted-media\"")
            }
            // Add YouTube specific allow attributes
            if (processedUrl.contains("youtube.com")) {
                append(" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\"")
            }
            if (embedData.iframeStyle != null && !embedData.hasCustomStyling) {
                append(" style=\"${embedData.iframeStyle}\"")
            } else {
                append(" style=\"$iframeBaseStyle\"")
            }
            embedData.iframeAttributes.forEach { entry ->
                append(" ${entry.key}=\"${entry.value}\"")
            }
            append("></iframe>")
            
            if (embedData.hasCustomStyling && embedData.divStyle != null) {
                append("</div>")
            }
            
            // Add JavaScript for tracking playback position (for Doodstream)
            if (isDoodstream) {
                append("<script>")
                append("""
                    (function() {
                        var videoUrl = '$processedUrl';
                        var positionInterval = null;
                        var lastSavedPosition = 0;
                        
                        function savePosition(position) {
                            if (position > 0 && Math.abs(position - lastSavedPosition) >= 5) {
                                lastSavedPosition = position;
                                currentTime = position;
                                // Save to localStorage
                                try {
                                    localStorage.setItem('video_position_' + videoUrl.hashCode(), position.toString());
                                } catch (e) {
                                    console.log('Cannot save to localStorage: ' + e.message);
                                }
                                
                                // Also try to communicate with Android
                                try {
                                    if (window.Android && window.Android.savePlaybackPosition) {
                                        window.Android.savePlaybackPosition(videoUrl, Math.floor(position));
                                    }
                                } catch (e) {
                                    // Android interface not available
                                }
                            }
                        }
                        
                        function trackPosition() {
                            try {
                                var iframe = document.getElementById('video-iframe');
                                if (!iframe) return;
                                
                                try {
                                    var iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
                                    var video = iframeDoc.querySelector('video');
                                    
                                    if (video) {
                                        if (!video.paused) {
                                            isPlaying = true;
                                            savePosition(video.currentTime);
                                        } else {
                                            isPlaying = false;
                                        }
                                    }
                                } catch (e) {
                                    // CORS error - try postMessage
                                    iframe.contentWindow.postMessage({type: 'getCurrentTime'}, '*');
                                }
                            } catch (e) {
                                console.log('Error tracking position: ' + e.message);
                            }
                        }
                        
                        // Listen for messages from iframe
                        window.addEventListener('message', function(event) {
                            if (event.data && event.data.type === 'currentTime') {
                                savePosition(event.data.time);
                            }
                        });
                        
                        // Start tracking position every 5 seconds
                        if (positionInterval) clearInterval(positionInterval);
                        positionInterval = setInterval(trackPosition, 5000);
                        
                        // Also track on page unload
                        window.addEventListener('beforeunload', function() {
                            trackPosition();
                        });
                    })();
                """.trimIndent())
                append("</script>")
            }
            
            append("</body></html>")
        }
    }

    // Check if this is a YouTube URL that should load directly in WebView
    val isYoutube = remember(processedUrl) {
        processedUrl.contains("youtube.com") || processedUrl.contains("youtu.be")
    }
    
    var previousUrl by remember { mutableStateOf("") }
    
    // Helper function to get video hosting service
    fun getVideoHostingService(url: String): String? {
        val lowerUrl = url.lowercase()
        
        // OneDrive service
        if (lowerUrl.contains("1drv.ms") || 
            lowerUrl.contains("onedrive.live.com") || 
            lowerUrl.contains("sharepoint.com")) {
            return "onedrive"
        }
        
        // Doodstream service
        if (lowerUrl.contains("doodstream.com") || 
            lowerUrl.contains("dsvplay.com") || 
            lowerUrl.contains("dood.to") ||
            lowerUrl.contains("ds2play.com") ||
            lowerUrl.contains("ds2video.com")) {
            return "doodstream"
        }
        
        // Vidsrc service
        if (lowerUrl.contains("vidsrc.icu") || 
            lowerUrl.contains("vidsrc.to") || 
            lowerUrl.contains("vidsrc.me") ||
            lowerUrl.contains("vidsrc.net") ||
            lowerUrl.contains("vidsrc.xyz") ||
            lowerUrl.contains("vidsrc.cc") ||
            lowerUrl.contains("vidfast.pro") ||
            lowerUrl.contains("vidlink.pro")) {
            return "vidsrc"
        }
        
        // VidZee service
        if (lowerUrl.contains("vidzee.wtf") || 
            lowerUrl.contains("player.vidzee.wtf")) {
            return "vidzee"
        }
        
        // VideoEasy service
        if (lowerUrl.contains("videasy.net") || 
            lowerUrl.contains("player.videasy.net")) {
            return "videasy"
        }
        
        // VidNest service
        if (lowerUrl.contains("vidnest.fun")) {
            return "vidnest"
        }
        
        // Mixdrop service
        if (lowerUrl.contains("mixdrop.co") || 
            lowerUrl.contains("mixdrop.to") ||
            lowerUrl.contains("mixdrop.sx") ||
            lowerUrl.contains("mixdrop.bz")) {
            return "mixdrop"
        }
        
        // Streamtape service
        if (lowerUrl.contains("streamtape.com") || 
            lowerUrl.contains("streamtape.net") ||
            lowerUrl.contains("streamtape.to")) {
            return "streamtape"
        }
        
        // Other services
        if (lowerUrl.contains("tiktok.com")) return "tiktok"
        if (lowerUrl.contains("embedsito.com")) return "embedsito"
        if (lowerUrl.contains("embed.su")) return "embedsu"
        if (lowerUrl.contains("upstream.to")) return "upstream"
        if (lowerUrl.contains("youtube.com") || lowerUrl.contains("youtu.be")) return "youtube"
        if (lowerUrl.contains("vimeo.com")) return "vimeo"
        if (lowerUrl.contains("dailymotion.com")) return "dailymotion"
        if (lowerUrl.contains("streamable.com")) return "streamable"
        if (lowerUrl.contains("mdy48tn97.com")) return "mdy48tn97"
        if (lowerUrl.contains("vidstream.pro")) return "vidstream"
        if (lowerUrl.contains("gogo-stream.com")) return "gogostream"
        if (lowerUrl.contains("mp4upload.com")) return "mp4upload"
        if (lowerUrl.contains("streamlare.com")) return "streamlare"
        if (lowerUrl.contains("filemoon.sx")) return "filemoon"
        if (lowerUrl.contains("bilibili.tv") || lowerUrl.contains("bilibili.com")) return "bilibili"
        
        // CDN services
        if (lowerUrl.contains("cloudflare.com") || 
            lowerUrl.contains("cloudfront.net") ||
            lowerUrl.contains("googleapis.com") ||
            lowerUrl.contains("gstatic.com") ||
            lowerUrl.contains("jwpcdn.com") ||
            lowerUrl.contains("jwplatform.com")) {
            return "cdn"
        }
        
        return null
    }
    
    // Check if URL is allowed video hosting
    fun isAllowedVideoHosting(url: String): Boolean {
        val lowerUrl = url.lowercase()
        if (lowerUrl.contains("drive.google.com") || lowerUrl.contains("docs.google.com") || lowerUrl.contains("google.com/file/d")) {
            return true
        }
        return getVideoHostingService(url) != null
    }
    
    // Check if navigation should be blocked
    fun shouldBlockNavigation(url: String, currentUrl: String): Boolean {
        // Always allow the current video URL
        if (url == currentUrl || url == processedUrl) {
            Logger.d("VideoPlayer", "✅ Allowing video URL: $url")
            return false
        }
        
        val lowerUrl = url.lowercase()

        // For Google Drive embeds, do not block any google-related requests
        val isGoogleDrive = processedUrl.lowercase().contains("drive.google.com") || 
                           processedUrl.lowercase().contains("google.com/file/d") ||
                           currentUrl.lowercase().contains("drive.google.com")
        if (isGoogleDrive) {
            if (lowerUrl.contains("google.com") || 
                lowerUrl.contains("gstatic.com") || 
                lowerUrl.contains("googleapis.com") || 
                lowerUrl.contains("googleusercontent.com")) {
                Logger.d("VideoPlayer", "✅ Allowing Google Drive resource: $url")
                return false
            }
        }
        
        // For vidsrc, be very lenient - allow most domains that vidsrc uses
        if (isVidsrc) {
            // Block only obvious ad/spam domains, allow everything else
            val strictBlockedPatterns = listOf(
                "doubleclick.net",
                "googlesyndication.com",
                "google-analytics.com",
                "adservice.google",
                "facebook.com",
                "twitter.com",
                "instagram.com",
                "pinterest.com",
                "linkedin.com",
                "reddit.com",
                "tiktok.com",
                "snapchat.com",
                "play.google.com",
                "apps.apple.com",
                "itunes.apple.com"
            )
            val shouldBlock = strictBlockedPatterns.any { pattern ->
                lowerUrl.contains(pattern)
            }
            if (!shouldBlock) {
                Logger.d("VideoPlayer", "✅ Allowing vidsrc resource: $url")
                return false
            }
        }
        
        // Allow video hosting domains
        if (isAllowedVideoHosting(url)) {
            return false
        }
        
        // Block known ad/spam domains
        val blockedPatterns = listOf(
            "doubleclick.net",
            "googlesyndication.com",
            "google-analytics.com",
            "adservice.google",
            "advertising.com",
            "adnxs.com",
            "adsystem.com",
            "adsrvr.org",
            "adroll.com",
            "serving-sys.com",
            "adcolony.com",
            "applovin.com",
            "chartboost.com",
            "unity3d.com",
            "ironsrc.com",
            "facebook.com",
            "twitter.com",
            "instagram.com",
            "pinterest.com",
            "linkedin.com",
            "reddit.com",
            "tiktok.com",
            "snapchat.com",
            "play.google.com",
            "apps.apple.com",
            "itunes.apple.com"
        )
        
        for (pattern in blockedPatterns) {
            if (lowerUrl.contains(pattern)) {
                Logger.d("VideoPlayer", "🚫 Blocked ad/spam: $url")
                return true
            }
        }
        
        // Block app store links
        if (lowerUrl.contains("/app/") || lowerUrl.contains("/apps/")) {
            Logger.d("VideoPlayer", "🚫 Blocked app store link: $url")
            return true
        }
        
        return false
    }
    
    Box(
        modifier = if (isFullscreen) {
            modifier.fillMaxSize()
        } else {
            modifier
                .fillMaxWidth()
                .height(videoHeight)
        }
    ) {
        AndroidView(
            factory = { ctx ->
                // Enable WebView debugging for easier diagnostics on device
                WebView.setWebContentsDebuggingEnabled(true)

                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        allowFileAccess = true
                        allowContentAccess = true
                        setSupportZoom(false)
                        builtInZoomControls = false
                        displayZoomControls = false
                        loadWithOverviewMode = false
                        useWideViewPort = true
                        setSupportMultipleWindows(false) // Disable to prevent crash: Parent WebView cannot host its own popup
                        javaScriptCanOpenWindowsAutomatically = false
                        
                        // Additional settings for better vidsrc compatibility
                        if (isVidsrc) {
                            allowUniversalAccessFromFileURLs = true
                            allowFileAccessFromFileURLs = true
                            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                        }
                    }
                    
                    // Use hardware acceleration for better performance
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                    
                    // Ensure WebView fills parent and is not clipped
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    // Prevent clipping
                    clipToPadding = false
                    clipChildren = false
                    
                    // Block popups and new windows
                    webChromeClient = object : WebChromeClient() {
                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: Message?
                        ): Boolean {
                            // Block all new windows (popups, ads)
                            Logger.d("VideoPlayer", "ðŸš« Blocked popup/new window")
                            return false // false means we didn't handle it, but since we don't send msg, it's blocked
                        }
                    }
                    
                    // Add JavaScript interface for saving playback position
                    addJavascriptInterface(object {
                        @android.webkit.JavascriptInterface
                        fun savePlaybackPosition(videoUrl: String, position: Long) {
                            if (videoUrl.isNotEmpty() && position > 0) {
                                PlaybackPositionService.savePosition(ctx, videoUrl, position)
                                Logger.d("VideoPlayer", "Saved position: $position for URL: $videoUrl")
                            }
                        }
                    }, "Android")
                    
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString() ?: return false
                            val isMainFrame = request.isForMainFrame
                            
                            // 1. CRITICAL: Block ALL Main Frame navigations (Frame busting)
                            // Since we load our own HTML wrapper, any main frame navigation is an attempt
                            // by an ad or script to break out of our player or redirect the user.
                            if (isMainFrame) {
                                // Exception: If it's somehow the exact same URL we loaded (unlikely via navigation)
                                if (url == processedUrl) return false
                                
                                Log.d("VideoPlayer", "ðŸš« Blocked main frame navigation (frame busting): $url")
                                return true
                            }
                            
                            // 2. Sub-frame (iframe) logic
                            // We must allow the iframe to load its video content and internal redirects
                            
                            // Block known ad/spam domains immediately
                            if (shouldBlockNavigation(url, processedUrl)) {
                                Log.d("VideoPlayer", "ðŸš« Blocked navigation: $url")
                                return true
                            }
                            
                            // Allow vidsrc domains and their resources
                            if (isVidsrc) {
                                Logger.d("VideoPlayer", "âœ… Allowing vidsrc resource: $url")
                                return false
                            }
                            
                            // Allow Doodstream internal navigation
                            if (isDoodstream) {
                                val sameDomain = try {
                                    val processedDomain = java.net.URL(processedUrl).host
                                    val requestDomain = java.net.URL(url).host
                                    requestDomain == processedDomain || 
                                    requestDomain.endsWith(".$processedDomain") ||
                                    processedDomain.endsWith(".$requestDomain")
                                } catch (e: Exception) {
                                    false
                                }
                                
                                if (sameDomain) {
                                    Logger.d("VideoPlayer", "âœ… Allowing Doodstream server navigation: $url")
                                    return false
                                }
                            }
                            
                            // Default: Allow other subframe navigations (video chunks, scripts, etc.)
                            // as long as they didn't trigger the ad blocker above.
                            return false
                        }
                        
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            
                            // Check if page loaded successfully by checking for video elements
                            // Skip video detection for vidsrc - they load differently
                            if (view != null && url != null && !isVidsrc) {
                                // Delay to allow page to fully render
                                view.postDelayed({
                                    view.evaluateJavascript("""
                                        (function() {
                                            try {
                                                var iframe = document.getElementById('video-iframe');
                                                if (!iframe) {
                                                    return JSON.stringify({hasVideo: false, reason: 'no_iframe'});
                                                }
                                                
                                                try {
                                                    var iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
                                                    var video = iframeDoc.querySelector('video');
                                                    var hasVideo = video !== null;
                                                    
                                                    // Check for common error indicators
                                                    var bodyText = iframeDoc.body ? iframeDoc.body.innerText.toLowerCase() : '';
                                                    var hasError = bodyText.includes('error') || 
                                                                   bodyText.includes('not found') || 
                                                                   bodyText.includes('404') ||
                                                                   bodyText.includes('unavailable') ||
                                                                   bodyText.includes('not available');
                                                    
                                                    return JSON.stringify({
                                                        hasVideo: hasVideo && !hasError,
                                                        reason: hasError ? 'error_page' : (hasVideo ? 'video_found' : 'no_video')
                                                    });
                                                } catch (e) {
                                                    // CORS error - assume video might be there
                                                    return JSON.stringify({hasVideo: true, reason: 'cors_blocked'});
                                                }
                                            } catch (e) {
                                                return JSON.stringify({hasVideo: false, reason: 'check_failed'});
                                            }
                                        })();
                                    """.trimIndent()) { result ->
                                        try {
                                            val resultJson = result.removeSurrounding("\"").replace("\\\"", "\"")
                                            val hasVideo = resultJson.contains("\"hasVideo\":true") || 
                                                          resultJson.contains("hasVideo:true") ||
                                                          resultJson.contains("\"reason\":\"cors_blocked\"")
                                            
                                            isLoading = false
                                            
                                            // Only trigger error if we're certain there's no video
                                            // Be more lenient with src embeds - they might load differently
                                            // For vidsrc embeds, don't trigger errors - they might load slowly
                                            if (!hasVideo && !errorTriggered && !isVidsrc) {
                                                // Check if it's a CORS issue or just no video detected
                                                val isCorsBlocked = resultJson.contains("cors_blocked") || 
                                                                   resultJson.contains("check_failed")
                                                // Only trigger error if we're sure it's not a CORS issue
                                                if (!isCorsBlocked) {
                                                    hasError = true
                                                    errorTriggered = true
                                                    // Trigger server error callback to try next server
                                                    onServerError?.invoke()
                                                } else {
                                                    // CORS blocked - assume video might be there, just wait longer
                                                    hasError = false
                                                }
                                            } else {
                                                // For vidsrc or if video found, don't show error
                                                hasError = false
                                            }
                                        } catch (e: Exception) {
                                            // If check fails, assume video is loading
                                            isLoading = false
                                            hasError = false
                                        }
                                    }
                                }, 3000) // Wait 3 seconds after page load to check
                            } else {
                                // For vidsrc, just stop loading indicator after page loads
                                // Don't check for video - let it load naturally
                                if (isVidsrc) {
                                    // Give vidsrc more time to load, then stop loading indicator
                                    view?.postDelayed({
                                        isLoading = false
                                        hasError = false
                                    }, 2000) // Wait 2 seconds for vidsrc to start loading
                                } else {
                                    isLoading = false
                                }
                            }
                            
                            // For Doodstream, try to restore playback position only if URL actually changed
                            if (isDoodstream && savedPosition > 0 && view != null && url != null) {
                                // Only restore if this is a new page load, not an internal navigation
                                val isNewLoad = url == processedUrl || url == previousUrl
                                
                                if (isNewLoad) {
                                    // Inject JavaScript to restore position after a delay
                                    view.postDelayed({
                                        val restoreScript = """
                                            (function() {
                                                var savedPosition = ${savedPosition};
                                                var attempts = 0;
                                                var maxAttempts = 10;
                                                
                                                function tryRestore() {
                                                    attempts++;
                                                    try {
                                                        var iframe = document.getElementById('video-iframe');
                                                        if (!iframe) {
                                                            if (attempts < maxAttempts) {
                                                                setTimeout(tryRestore, 1000);
                                                            }
                                                            return;
                                                        }
                                                        
                                                        try {
                                                            var iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
                                                            var video = iframeDoc.querySelector('video');
                                                            
                                                            // Only restore if video is not playing or just started
                                                            if (video && video.readyState >= 2) {
                                                                // Check if video is already playing at a different position
                                                                if (video.currentTime > 1 && Math.abs(video.currentTime - savedPosition) > 30) {
                                                                    // Video is already playing, don't interrupt
                                                                    console.log('Video already playing, skipping position restore');
                                                                    return;
                                                                }
                                                                
                                                                // Only restore if video is at beginning or close to saved position
                                                                if (video.currentTime < 5 || Math.abs(video.currentTime - savedPosition) < 30) {
                                                                    video.currentTime = savedPosition;
                                                                    console.log('Position restored to: ' + savedPosition);
                                                                    return;
                                                                }
                                                            }
                                                        } catch (e) {
                                                            // CORS error - try postMessage
                                                            iframe.contentWindow.postMessage({
                                                                type: 'seek',
                                                                time: savedPosition
                                                            }, '*');
                                                        }
                                                        
                                                        if (attempts < maxAttempts) {
                                                            setTimeout(tryRestore, 1000);
                                                        }
                                                    } catch (e) {
                                                        console.log('Error restoring position: ' + e.message);
                                                    }
                                                }
                                                
                                                setTimeout(tryRestore, 2000);
                                            })();
                                        """.trimIndent()
                                        
                                        view.evaluateJavascript(restoreScript, null)
                                    }, 2000)
                                }
                            }
                        }

                        override fun onPageCommitVisible(view: WebView?, url: String?) {
                            super.onPageCommitVisible(view, url)
                            // For vidsrc, give it more time before stopping loading indicator
                            if (isVidsrc) {
                                view?.postDelayed({
                                    isLoading = false
                                    hasError = false
                                }, 1000) // Wait 1 second for vidsrc to start rendering
                            } else {
                                isLoading = false
                                hasError = false
                            }
                        }
                        
                        @Deprecated("Deprecated in Java")
                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            // Don't trigger errors for vidsrc embeds - they might have different error patterns
                            if (isVidsrc) {
                                // For vidsrc, just stop loading indicator but don't show error
                                isLoading = false
                                hasError = false
                                return
                            }
                            
                            // Log the error for debugging
                            Logger.e("VideoPlayer", "WebView Error ($errorCode): $description for URL: $failingUrl")
                            
                            // Trigger server switch for most errors that indicate the specific server is unreachable
                            // We include HOST_LOOKUP, CONNECT, and TIMEOUT because if one mirror is down, we want to try the next one.
                            if (!errorTriggered) {
                                isLoading = false
                                hasError = true
                                errorTriggered = true
                                onServerError?.invoke()
                            }
                        }
                        
                        override fun onReceivedHttpError(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?,
                            errorResponse: android.webkit.WebResourceResponse?
                        ) {
                            super.onReceivedHttpError(view, request, errorResponse)
                            // Don't trigger errors for vidsrc embeds - they might have different error patterns
                            if (isVidsrc) {
                                isLoading = false
                                hasError = false
                                return
                            }
                            val statusCode = errorResponse?.statusCode ?: 0
                            // Only treat critical errors (404, 500+) as server failures
                            // 403, 401 might be temporary or CORS issues
                            if ((statusCode == 404 || statusCode >= 500) && !errorTriggered) {
                                isLoading = false
                                hasError = true
                                errorTriggered = true
                                // Trigger server error callback for critical HTTP errors
                                onServerError?.invoke()
                            }
                        }
                    }
                    
                    // Load content
                    if (isYoutube) {
                        loadUrl(processedUrl)
                    } else {
                        loadDataWithBaseURL(
                            processedUrl,
                            htmlContent,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                    previousUrl = processedUrl
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { webView ->
                // Only reload if URL actually changes
                if (processedUrl.isNotEmpty() && processedUrl != previousUrl) {
                    // For Doodstream, check if it's just a parameter change
                    val shouldReload = if (isDoodstream) {
                        try {
                            val prevUrlObj = java.net.URL(previousUrl)
                            val currentUrlObj = java.net.URL(processedUrl)
                            // Only reload if domain or path changed, not just query parameters
                            prevUrlObj.host != currentUrlObj.host || 
                            prevUrlObj.path != currentUrlObj.path
                        } catch (e: Exception) {
                            // If URL parsing fails, reload to be safe
                            true
                        }
                    } else {
                        true
                    }
                    
                    if (shouldReload) {
                        isLoading = true
                        hasError = false
                        if (isYoutube) {
                            webView.loadUrl(processedUrl)
                        } else {
                            webView.loadDataWithBaseURL(
                                processedUrl,
                                htmlContent,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                        previousUrl = processedUrl
                    } else {
                        // Just update the previous URL without reloading
                        previousUrl = processedUrl
                    }
                }
            }
        )
        
        // Loading indicator
        if (isLoading) {
            // Show thumbnail if available
            if (thumbnailUrl != null) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Semi-transparent overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                )
            }
            
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryRed,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        // Fullscreen Button Overlay (bottom-right)
        if (showFullscreenButton && onFullscreenClick != null && processedUrl.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onFullscreenClick() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "â›¶",
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}


