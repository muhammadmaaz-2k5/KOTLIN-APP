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

    fun detectEmbedPlayer(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("embed") || lower.contains("player") ||
            lower.contains("vidfast") || lower.contains("vidsrc") ||
            lower.contains("vidlink") || lower.contains("stream") ||
            lower.contains("movie") || lower.contains("tv")
    }

    fun isAllowedVideoHosting(url: String): Boolean = getVideoHostingService(url) != null

    private fun getVideoHostingService(url: String): String? {
        val lower = url.lowercase()
        return when {
            lower.contains("vidfast") -> "vidfast"
            lower.contains("vidsrc") -> "vidsrc"
            lower.contains("vidlink") -> "vidlink"
            lower.contains("superembed") || lower.contains("2embed") || lower.contains("autoembed") || lower.contains("multiembed") -> "embed"
            lower.contains("videasy") || lower.contains("vidzee") || lower.contains("vidnest") -> "videasy"
            lower.contains("dood") || lower.contains("doodstream") || lower.contains("ds2play") || lower.contains("dsvplay") -> "doodstream"
            lower.contains("mixdrop") -> "mixdrop"
            lower.contains("streamtape") -> "streamtape"
            lower.contains("1drv.ms") || lower.contains("onedrive.live.com") || lower.contains("sharepoint.com") -> "onedrive"
            lower.contains("youtube.com") || lower.contains("youtu.be") -> "youtube"
            lower.contains("vimeo.com") -> "vimeo"
            lower.contains("dailymotion.com") -> "dailymotion"
            lower.contains("streamable.com") -> "streamable"
            lower.contains("cloudflare.com") || lower.contains("cloudfront.net") ||
                lower.contains("googleapis.com") || lower.contains("gstatic.com") ||
                lower.contains("jwpcdn.com") || lower.contains("jwplatform.com") ||
                lower.contains(".m3u8") || lower.contains(".mp4") -> "cdn"
            else -> null
        }
    }

    fun shouldBlockNavigation(url: String, currentUrl: String, isEmbedPlayer: Boolean): Boolean {
        if (url == currentUrl) return false
        val lower = url.lowercase()
        if (isEmbedPlayer) {
            val shouldBlock = strictBlockedPatterns.any { lower.contains(it) }
            if (!shouldBlock) return false
        }
        if (isAllowedVideoHosting(url)) return false

        // If target shares same host as the currently loaded stream/server, allow it
        val targetHost = try { java.net.URI(url).host } catch (_: Exception) { null }
        val currentHost = try { java.net.URI(currentUrl).host } catch (_: Exception) { null }
        if (targetHost != null && currentHost != null && (targetHost.endsWith(currentHost) || currentHost.endsWith(targetHost))) {
            return false
        }

        if (blockedPatterns.any { lower.contains(it) }) return true
        if (lower.contains("/app/") || lower.contains("/apps/")) return true
        return false
    }

    fun buildHtmlContent(url: String): String {
        val isEmbed = detectEmbedPlayer(url)
        val isYoutube = url.contains("youtube.com", ignoreCase = true)
        val iframeStyle = "width:100%;height:100%;border:none;display:block;"
        val allowAttr = buildString {
            append(" allowfullscreen")
            if (isEmbed) append(" allow=\"autoplay; fullscreen; picture-in-picture; encrypted-media\"")
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
        val lower = url.lowercase()
        // Web streaming servers (VidFast, VidSrc, VidLink, etc.) must load directly with loadUrl
        // to prevent X-Frame-Options: SAMEORIGIN from blocking the iframe and displaying a black screen.
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            if (lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".webm")) {
                return true
            }
            return false
        }
        return false
    }
}
