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
            lower.contains(".m4s") ||
            lower.contains(".mp4") ||
            lower.contains(".webm") ||
            lower.contains(".mkv") ||
            lower.contains(".key") ||
            lower.contains(".mpd") ||
            lower.contains("chunk") ||
            lower.contains("segment") ||
            lower.contains("manifest") ||
            lower.contains("playlist") ||
            lower.contains("range=") ||
            lower.contains("bytes=") ||
            lower.contains("/hls/") ||
            lower.contains("/stream/") ||
            lower.contains("/video/") ||
            lower.contains("/seg-") ||
            lower.startsWith("blob:")
    }

    fun shouldBlockAdRequest(url: String): Boolean {
        if (url.isBlank()) return false
        val lower = url.lowercase()

        // Critical: Fast-path return false for media streams to avoid string overhead
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
            lower.contains("playmogo") || lower.contains("/e/") ||
            lower.contains("movie") || lower.contains("tv")
    }

    fun isAllowedVideoHosting(url: String): Boolean = getVideoHostingService(url) != null

    fun getVideoHostingService(url: String): String? {
        val lower = url.lowercase()
        return when {
            lower.contains("playmogo") -> "playmogo"
            lower.contains("vidfast") -> "vidfast"
            lower.contains("vidsrc") || lower.contains("cloudstream") -> "vidsrc"
            lower.contains("vidlink") -> "vidlink"
            lower.contains("megacloud") || lower.contains("rabbitstream") || lower.contains("dokicloud") -> "megacloud"
            lower.contains("streamwish") || lower.contains("filelions") || lower.contains("vidplay") || lower.contains("multimovies") -> "streamwish"
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
                lower.contains("akamaized") || lower.contains("fastly") ||
                lower.contains("googleapis.com") || lower.contains("gstatic.com") ||
                lower.contains("jwpcdn.com") || lower.contains("jwplatform.com") ||
                lower.contains(".m3u8") || lower.contains(".mp4") || lower.contains(".m4s") -> "cdn"
            else -> null
        }
    }

    /**
     * Anti-freeze & Seek Watchdog script.
     * Prevents black screens when forwarding or seeking in third-party video embeds (VidSrc, SuperEmbed, etc.).
     * Neutralizes popunder ad traps on seek, prevents video hiding, and automatically unpauses/nudges playback.
     */
    /**
     * Player control class names for known embed players - protected from ad overlay blocking.
     * Covers VidFast, VidSrc, Plyr, JWPlayer, Video.js, Artplayer, and generic players.
     */
    private val playerControlSelectors = setOf(
        "jw-controls", "jw-overlay", "jw-settings", "jw-settings-menu",
        "vjs-control-bar", "vjs-menu", "vjs-modal", "vjs-text-track-settings",
        "plyr__controls", "plyr__menu", "plyr__control",
        "art-controls", "art-setting", "art-panel", "art-selector", "art-layer",
        "shaka-controls-container", "shaka-settings-menu",
        "player-controls", "player-overlay", "player-menu", "player-settings",
        "settings-panel", "settings-menu", "quality-menu", "subtitle-menu",
        "control-bar", "controls", "control-panel", "seekbar",
        "speed-menu", "speed-panel", "audio-menu", "audio-panel",
        "vidfast", "vidsrc"
    )

    fun getPlayerWatchdogScript(): String {
        val safeClasses = playerControlSelectors.joinToString("|") { it }
        return """
            (function() {
                // 1. Neutralize popunder ad traps ONLY - do NOT override window.open
                //    because player settings/quality panels use it for their menus.
                try {
                    window.alert = function() {};
                    window.prompt = function() { return null; };
                    // Only block window.open if it navigates to a non-video external page
                    var _origOpen = window.open;
                    window.open = function(url, target, features) {
                        if (!url || url === '' || url === 'about:blank') return null;
                        var u = (url || '').toLowerCase();
                        // Allow blob: URLs (used by some players for quality selection)
                        if (u.startsWith('blob:')) return _origOpen.call(this, url, target, features);
                        // Block external ad/redirect URLs
                        return null;
                    };
                } catch(e) {}

                // 2. Neutralize ONLY truly invisible transparent popunder overlays.
                //    NEVER touch elements that have visible content, text, or are
                //    known player control containers.
                var safePatterns = '${safeClasses}'.split('|');
                function isPlayerControl(el) {
                    if (!el || !el.classList) return false;
                    for (var i = 0; i < safePatterns.length; i++) {
                        if (el.classList.contains(safePatterns[i])) return true;
                        // Check ancestors up to 5 levels
                        var p = el.parentElement;
                        for (var j = 0; j < 5; j++) {
                            if (!p) break;
                            if (p.classList && p.classList.contains(safePatterns[i])) return true;
                            p = p.parentElement;
                        }
                    }
                    // If element has visible text content, protect it
                    if (el.innerText && el.innerText.trim().length > 0) return true;
                    // If element has visible children (buttons, inputs, etc.), protect it
                    if (el.querySelector('button, input, select, label, span, p, h1, h2, h3, h4, a')) return true;
                    return false;
                }

                function neutralizeAdOverlays(doc) {
                    try {
                        var els = doc.querySelectorAll('div, a');
                        els.forEach(function(el) {
                            if (el.tagName === 'VIDEO' || el.tagName === 'IFRAME') return;
                            var s = el.style;
                            var cs = window.getComputedStyle(el);
                            if (cs.position === 'absolute' || cs.position === 'fixed') {
                                var z = parseInt(cs.zIndex) || 0;
                                // Only target very high z-index invisible overlays
                                if (z > 999 && !el.querySelector('video') && !isPlayerControl(el)) {
                                    var w = el.offsetWidth || 0;
                                    var h = el.offsetHeight || 0;
                                    var isInvisible = cs.opacity === '0';
                                    var isTransparent = (cs.backgroundColor === 'transparent' || cs.backgroundColor === 'rgba(0, 0, 0, 0)');
                                    var hasNoContent = !(el.innerText && el.innerText.trim().length > 0);
                                    // Only disable pointer events on large completely invisible/transparent overlays with no content
                                    if (w > 250 && h > 200 && isInvisible && hasNoContent) {
                                        el.style.pointerEvents = 'none';
                                    }
                                }
                            }
                        });
                    } catch(e) {}
                }

                // 3. Video element watchdog - fixes black screen on seek/forward
                function attachVideoWatchdog(doc) {
                    try {
                        var vids = doc.querySelectorAll('video');
                        vids.forEach(function(v) {
                            if (v._nzWatchdog) return;
                            v._nzWatchdog = true;

                            // Ensure video is always visible
                            v.style.opacity = '1';
                            v.style.visibility = 'visible';
                            v.style.display = 'block';

                            v.addEventListener('seeking', function() {
                                v._isSeeking = true;
                                v._seekTime = Date.now();
                            });

                            v.addEventListener('seeked', function() {
                                v._isSeeking = false;
                                setTimeout(function() {
                                    if (v.paused) {
                                        v.play().catch(function(){});
                                    }
                                }, 200);
                            });

                            v.addEventListener('waiting', function() {
                                setTimeout(function() {
                                    if (v.paused && v.readyState >= 2) {
                                        v.play().catch(function(){});
                                    }
                                }, 700);
                            });

                            v.addEventListener('stalled', function() {
                                setTimeout(function() {
                                    if (v.paused) {
                                        try { v.currentTime += 0.01; } catch(e) {}
                                        v.play().catch(function(){});
                                    }
                                }, 1200);
                            });

                            // Only auto-resume pause if it happened during/right after a seek
                            // Do NOT auto-resume user-intentional pauses (user clicking pause button)
                            v.addEventListener('pause', function() {
                                if (v._isSeeking || (v._seekTime && (Date.now() - v._seekTime < 2000))) {
                                    setTimeout(function() {
                                        if (v.paused) {
                                            v.play().catch(function(){});
                                        }
                                    }, 400);
                                }
                            });
                        });
                    } catch(e) {}
                }

                function scanAndApply() {
                    attachVideoWatchdog(document);
                    neutralizeAdOverlays(document);
                    // Also scan inside iframes (nested player iframes)
                    var iframes = document.querySelectorAll('iframe');
                    iframes.forEach(function(f) {
                        try {
                            if (f.contentDocument) {
                                attachVideoWatchdog(f.contentDocument);
                                neutralizeAdOverlays(f.contentDocument);
                            }
                        } catch(e) {}
                    });
                }

                scanAndApply();
                if (!window._nzIntervalSet) {
                    window._nzIntervalSet = true;
                    setInterval(scanAndApply, 1500);
                }
            })();
        """.trimIndent()
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

