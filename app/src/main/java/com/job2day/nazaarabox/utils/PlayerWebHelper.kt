package com.job2day.nazaarabox.utils

object PlayerWebHelper {
    private val strictBlockedPatterns = listOf(
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
        "itunes.apple.com",
    )

    private val blockedPatterns = listOf(
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
        "itunes.apple.com",
    )

    fun detectVidsrc(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("vidsrc") ||
            lower.contains("vidfast.pro") ||
            lower.contains("vidlink.pro")
    }

    fun detectDoodstream(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("doodstream.com") ||
            lower.contains("dsvplay.com") ||
            lower.contains("dood.to") ||
            lower.contains("ds2play.com") ||
            lower.contains("ds2video.com")
    }

    fun isAllowedVideoHosting(url: String): Boolean = getVideoHostingService(url) != null

    private fun getVideoHostingService(url: String): String? {
        val lower = url.lowercase()
        return when {
            lower.contains("1drv.ms") || lower.contains("onedrive.live.com") || lower.contains("sharepoint.com") -> "onedrive"
            detectDoodstream(lower) -> "doodstream"
            detectVidsrc(lower) -> "vidsrc"
            lower.contains("vidzee.wtf") || lower.contains("player.vidzee.wtf") -> "vidzee"
            lower.contains("videasy.net") || lower.contains("player.videasy.net") -> "videasy"
            lower.contains("vidnest.fun") -> "vidnest"
            lower.contains("mixdrop.co") || lower.contains("mixdrop.to") || lower.contains("mixdrop.sx") || lower.contains("mixdrop.bz") -> "mixdrop"
            lower.contains("streamtape.com") || lower.contains("streamtape.net") || lower.contains("streamtape.to") -> "streamtape"
            lower.contains("youtube.com") || lower.contains("youtu.be") -> "youtube"
            lower.contains("vimeo.com") -> "vimeo"
            lower.contains("dailymotion.com") -> "dailymotion"
            lower.contains("streamable.com") -> "streamable"
            lower.contains("cloudflare.com") || lower.contains("cloudfront.net") ||
                lower.contains("googleapis.com") || lower.contains("gstatic.com") ||
                lower.contains("jwpcdn.com") || lower.contains("jwplatform.com") -> "cdn"
            else -> null
        }
    }

    fun shouldBlockNavigation(url: String, currentUrl: String, isVidsrc: Boolean): Boolean {
        if (url == currentUrl) return false
        val lower = url.lowercase()
        if (isVidsrc) {
            val shouldBlock = strictBlockedPatterns.any { lower.contains(it) }
            if (!shouldBlock) return false
        }
        if (isAllowedVideoHosting(url)) return false
        if (blockedPatterns.any { lower.contains(it) }) return true
        if (lower.contains("/app/") || lower.contains("/apps/")) return true
        return false
    }

    fun buildHtmlContent(url: String): String {
        val isVidsrc = detectVidsrc(url)
        val isDoodstream = detectDoodstream(url)
        val isYoutube = url.contains("youtube.com", ignoreCase = true)
        val iframeStyle = if (isDoodstream) {
            "width:100%;height:100%;border:none;display:block;margin:0 auto;"
        } else {
            "width:100%;height:100%;border:none;display:block;"
        }
        val allowAttr = buildString {
            append(" allowfullscreen")
            if (isVidsrc) append(" allow=\"autoplay; fullscreen; picture-in-picture; encrypted-media\"")
            if (isYoutube) append(" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\"")
        }
        return """
            <!DOCTYPE html>
            <html><head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
            <style>
            html, body { margin:0; padding:0; width:100%; height:100%; overflow:hidden; background:#000;
                display:flex; align-items:center; justify-content:center; }
            iframe { $iframeStyle }
            </style>
            </head><body>
            <iframe id="video-iframe" src="$url"$allowAttr style="$iframeStyle"></iframe>
            </body></html>
        """.trimIndent()
    }

    fun shouldUseHtmlWrapper(url: String): Boolean {
        if (url.isBlank()) return false
        if (url.contains("youtube.com", ignoreCase = true) && url.contains("/embed/", ignoreCase = true)) {
            return false
        }
        return true
    }
}
