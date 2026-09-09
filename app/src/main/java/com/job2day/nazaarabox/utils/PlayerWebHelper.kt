package com.job2day.nazaarabox.utils

object PlayerWebHelper {

    // Comprehensive list of video embed ad networks, pop-under scripts, trackers, and gambling sponsors
    private val blockedAdDomains = setOf(
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
        "vungle.com",
        "inmobi.com",
        "fyber.com",
        "mintegral.com",
        "smaato.net",
        "mopub.com",
        "popads.net",
        "popcash.net",
        "propellerads.com",
        "propellerclick.com",
        "adsterra.com",
        "exoclick.com",
        "monetag.com",
        "hilltopads.net",
        "richpush.co",
        "clickadu.com",
        "trafficjunky.net",
        "juicyads.com",
        "trafficstars.com",
        "bidgear.com",
        "adxad.com",
        "yandex.ru",
        "aniview.com",
        "spotxchange.com",
        "springserve.com",
        "streamrail.com",
        "teads.tv",
        "outbrain.com",
        "taboola.com",
        "mgid.com",
        "revcontent.com",
        "zergnet.com",
        "bet365.com",
        "1xbet.com",
        "parimatch.com",
        "mostbet.com",
        "melbet.com",
        "stake.com",
        "dafabet.com",
        "888casino.com",
        "vulkan.bet",
        "pin-up.bet",
        "linebet.com",
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

    private val blockedPathKeywords = listOf(
        "/popunder",
        "/pop-under",
        "/popup",
        "/ad.js",
        "/ads.js",
        "/advert.js",
        "/advertisement.js",
        "/ad-banner",
        "/adbanner",
        "/bannerad",
        "/banner_ad",
        "/prebid",
        "/adserver",
        "/pixel.gif",
        "/ad/banner",
        "/ads/banner"
    )

    fun isMediaStream(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains(".m3u8") ||
            lower.contains(".ts") ||
            lower.contains(".mp4") ||
            lower.contains(".webm") ||
            lower.contains(".mkv") ||
            lower.contains(".key") ||
            lower.contains("chunk") ||
            lower.contains("segment") ||
            lower.contains("manifest") ||
            lower.contains("playlist") ||
            lower.startsWith("blob:")
    }

    fun shouldBlockAdRequest(url: String): Boolean {
        if (url.isBlank()) return false
        val lower = url.lowercase()

        // Critical: Never block actual video playback data or legitimate video hosting
        if (isMediaStream(lower)) return false
        if (isAllowedVideoHosting(lower)) return false

        // Check if domain matches any known ad/tracking network
        val host = try { java.net.URI(url).host?.lowercase().orEmpty() } catch (_: Exception) { "" }
        if (host.isNotBlank()) {
            if (blockedAdDomains.any { host == it || host.endsWith(".$it") }) {
                return true
            }
        }

        // Check if URL path matches explicit ad patterns
        if (blockedPathKeywords.any { lower.contains(it) }) {
            return true
        }

        return false
    }

    fun detectEmbedPlayer(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("embed") || lower.contains("player") ||
            lower.contains("vidfast") || lower.contains("vidsrc") ||
            lower.contains("vidlink") || lower.contains("stream") ||
            lower.contains("movie") || lower.contains("tv")
    }

    fun isAllowedVideoHosting(url: String): Boolean = getVideoHostingService(url) != null

    fun getVideoHostingService(url: String): String? {
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

    fun shouldBlockNavigation(url: String, currentUrl: String): Boolean {
        if (url.isBlank() || url == currentUrl) return false
        val lower = url.lowercase()

        // 1. Never block media stream segments or allowed video hosting
        if (isMediaStream(lower) || isAllowedVideoHosting(lower)) {
            return false
        }

        // 2. Allow navigation within the same host/subdomain of the current video provider
        val targetHost = try { java.net.URI(url).host?.lowercase() } catch (_: Exception) { null }
        val currentHost = try { java.net.URI(currentUrl).host?.lowercase() } catch (_: Exception) { null }
        if (targetHost != null && currentHost != null) {
            if (targetHost == currentHost || targetHost.endsWith(".$currentHost") || currentHost.endsWith(".$targetHost")) {
                return false
            }
        }

        // 3. Block explicit ad domains or ad keywords
        if (shouldBlockAdRequest(url)) {
            return true
        }

        // 4. Block non-http/https schemes (intent://, market://, tel://, etc.)
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return true
        }

        // 5. Block external redirects/navigation away from video hosting (popups / new pages)
        return true
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
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            if (lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".webm")) {
                return true
            }
            return false
        }
        return false
    }
}

