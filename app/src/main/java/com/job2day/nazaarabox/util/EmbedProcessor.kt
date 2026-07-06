package com.job2day.nazaarabox.util

import java.net.URLDecoder

// Data class for EmbedProcessor compatibility
data class EmbedData(
    val url: String,
    val hasCustomStyling: Boolean = false,
    val divStyle: String? = null,
    val iframeStyle: String? = null,
    val iframeAttributes: Map<String, String> = emptyMap()
)

object EmbedProcessor {
    fun processEmbedUrl(embedUrl: String?): EmbedData {
        if (embedUrl.isNullOrEmpty()) {
            return EmbedData(url = "")
        }

        val trimmedUrl = embedUrl.trim()

        // YouTube Shorts handling (handles both plain URLs and iframe embeds)
        if (trimmedUrl.contains("youtube.com/shorts/") || trimmedUrl.contains("youtube.com/embed/") || trimmedUrl.contains("youtu.be/")) {
            try {
                // If it's an iframe, extract the src first
                var actualUrl = trimmedUrl
                if (trimmedUrl.contains("<iframe")) {
                    val srcMatch = Regex("src\\s*=\\s*\"([^\"]+)\"", RegexOption.IGNORE_CASE).find(trimmedUrl)
                        ?: Regex("src\\s*=\\s*'([^']+)'", RegexOption.IGNORE_CASE).find(trimmedUrl)
                    actualUrl = srcMatch?.groupValues?.get(1) ?: trimmedUrl
                }

                // Extract video ID from shorts URL, embed URL, or youtu.be URL
                val videoId = when {
                    actualUrl.contains("/shorts/") -> actualUrl.substringAfter("/shorts/").substringBefore("?").substringBefore("/")
                    actualUrl.contains("/embed/") -> actualUrl.substringAfter("/embed/").substringBefore("?").substringBefore("/")
                    actualUrl.contains("youtu.be/") -> actualUrl.substringAfter("youtu.be/").substringBefore("?").substringBefore("/")
                    actualUrl.contains("v=") -> actualUrl.substringAfter("v=").substringBefore("&").substringBefore("/")
                    else -> ""
                }

                if (videoId.isNotEmpty()) {
                    // For Shorts, use embed URL with parameters for better experience
                    val youtubeEmbedUrl = "https://www.youtube.com/embed/$videoId?autoplay=1&mute=1&loop=1&playlist=$videoId&controls=0&rel=0&modestbranding=1&iv_load_policy=3"
                    return EmbedData(
                        url = youtubeEmbedUrl,
                        iframeAttributes = mapOf(
                            "allow" to "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share",
                            "allowfullscreen" to "true"
                        )
                    )
                }
            } catch (e: Exception) {
                // Fallback
            }
        }

        // TikTok URL handling
        if (trimmedUrl.contains("tiktok.com") && !trimmedUrl.contains("iframe")) {
            try {
                // Extract video ID from various TikTok URL formats
                val videoId = when {
                    trimmedUrl.contains("/video/") -> {
                        trimmedUrl.substringAfter("/video/").substringBefore("?").substringBefore("/")
                    }
                    trimmedUrl.contains("/v/") -> {
                        trimmedUrl.substringAfter("/v/").substringBefore("?").substringBefore("/")
                    }
                    else -> null
                }

                if (videoId != null && videoId.all { it.isDigit() }) {
                    // Construct TikTok embed URL with customization parameters
                    val tiktokEmbedUrl = "https://www.tiktok.com/embed/v2/$videoId?music_info=1&description=1"
                    return EmbedData(
                        url = tiktokEmbedUrl,
                        iframeAttributes = mapOf(
                            "allow" to "encrypted-media;",
                            "referrerpolicy" to "unsafe-url"
                        )
                    )
                }
            } catch (e: Exception) {
                // Fallback
            }
        }

        // Check if embedUrl contains full HTML structure with div wrapper
        val hasDivWrapper = Regex("<div[^>]*>", RegexOption.IGNORE_CASE).containsMatchIn(trimmedUrl)

        var divStyle: String? = null
        var iframeStyle: String? = null
        val iframeAttributes = mutableMapOf<String, String>()
        var processedUrl = trimmedUrl

        if (hasDivWrapper) {
            // Extract div style attribute
            val divStyleMatch = Regex(
                "<div[^>]*style\\s*=\\s*\"([^\"]+)\"",
                setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
            ).find(trimmedUrl) ?: Regex(
                "<div[^>]*style\\s*=\\s*'([^']+)'",
                setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
            ).find(trimmedUrl)

            divStyleMatch?.groupValues?.get(1)?.let {
                divStyle = it.trim()
            }

            // Extract iframe and its attributes
            val iframeMatch = Regex(
                "<iframe([\\s\\S]*?)>",
                RegexOption.IGNORE_CASE
            ).find(trimmedUrl)

            iframeMatch?.groupValues?.get(1)?.let { iframeAttrs ->
                // Extract iframe src
                val srcMatch = Regex(
                    "src\\s*=\\s*\"([^\"]+)\"",
                    setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
                ).find(iframeAttrs) ?: Regex(
                    "src\\s*=\\s*'([^']+)'",
                    setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
                ).find(iframeAttrs) ?: Regex(
                    "src\\s*=\\s*([^\\s>]+)",
                    setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
                ).find(iframeAttrs)

                srcMatch?.groupValues?.get(1)?.let {
                    processedUrl = try {
                        URLDecoder.decode(it.trim(), "UTF-8")
                    } catch (e: Exception) {
                        it.trim()
                    }
                }

                // Extract iframe style
                val iframeStyleMatch = Regex(
                    "style\\s*=\\s*\"([^\"]+)\"",
                    setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
                ).find(iframeAttrs) ?: Regex(
                    "style\\s*=\\s*'([^']+)'",
                    setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
                ).find(iframeAttrs)

                iframeStyleMatch?.groupValues?.get(1)?.let {
                    iframeStyle = it.trim()
                }

                // Extract scrolling attribute
                if (Regex("scrolling", RegexOption.IGNORE_CASE).containsMatchIn(iframeAttrs)) {
                    val scrollingMatch = Regex(
                        "scrolling\\s*=\\s*\"([^\"]+)\"",
                        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
                    ).find(iframeAttrs) ?: Regex(
                        "scrolling\\s*=\\s*'([^']+)'",
                        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
                    ).find(iframeAttrs) ?: Regex(
                        "scrolling\\s*=\\s*([^\\s>]+)",
                        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
                    ).find(iframeAttrs)

                    scrollingMatch?.groupValues?.get(1)?.let {
                        iframeAttributes["scrolling"] = it.trim()
                    }
                }

                // Extract allow attribute
                val allowMatch = Regex(
                    "allow\\s*=\\s*\"([^\"]+)\"",
                    setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
                ).find(iframeAttrs) ?: Regex(
                    "allow\\s*=\\s*'([^']+)'",
                    setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
                ).find(iframeAttrs)

                allowMatch?.groupValues?.get(1)?.let {
                    iframeAttributes["allow"] = it.trim()
                }

                // Check for allowfullscreen
                if (Regex("allowfullscreen", RegexOption.IGNORE_CASE).containsMatchIn(iframeAttrs)) {
                    iframeAttributes["allowfullscreen"] = "true"
                }
            }
        } else if (Regex("<iframe", RegexOption.IGNORE_CASE).containsMatchIn(trimmedUrl)) {
            // Check if embedUrl contains HTML iframe tags - extract the src attribute
            val srcMatch = Regex(
                "src\\s*=\\s*\"([^\"]+)\"",
                RegexOption.IGNORE_CASE
            ).find(trimmedUrl) ?: Regex(
                "src\\s*=\\s*'([^']+)'",
                RegexOption.IGNORE_CASE
            ).find(trimmedUrl) ?: Regex(
                "src\\s*=\\s*([^\\s>]+)",
                RegexOption.IGNORE_CASE
            ).find(trimmedUrl)

            srcMatch?.groupValues?.get(1)?.let {
                processedUrl = try {
                    URLDecoder.decode(it.trim(), "UTF-8")
                } catch (e: Exception) {
                    it.trim()
                }
            }
        }

        // Handle Google Drive URLs
        val lowerUrl = processedUrl.lowercase()
        if (lowerUrl.contains("drive.google.com") || lowerUrl.contains("google.com/file/d") || lowerUrl.contains("sharing")) {
            var fileId: String? = null
            
            if (processedUrl.contains("/file/d/")) {
                fileId = processedUrl.substringAfter("/file/d/").substringBefore("/").substringBefore("?").trim()
            } else if (processedUrl.contains("/open?id=")) {
                fileId = processedUrl.substringAfter("/open?id=").substringBefore("&").trim()
            } else if (processedUrl.contains("/uc?id=")) {
                fileId = processedUrl.substringAfter("/uc?id=").substringBefore("&").trim()
            } else if (processedUrl.contains("id=")) {
                fileId = processedUrl.substringAfter("id=").substringBefore("&").trim()
            }
            
            if (!fileId.isNullOrEmpty() && fileId.length >= 15) {
                processedUrl = "https://drive.google.com/file/d/$fileId/preview"
            }
            
            // Auto-apply Google Drive custom styling to crop the desktop header bar
            if (!hasDivWrapper) {
                divStyle = "width: 100%; height: 100%; overflow: hidden; position: relative; background: #000;"
                iframeStyle = "width: 100%; height: calc(100% + 56px); border: none; display: block; position: absolute; top: -56px; left: 0;"
                iframeAttributes["allow"] = "autoplay; fullscreen; accelerometer; gyroscope; picture-in-picture"
                iframeAttributes["allowfullscreen"] = "true"
            }
        }

        // Handle Mixdrop, Dailymotion, Bilibili, and Doodstream - ensure proper embed URLs
        if (lowerUrl.contains("mixdrop") || lowerUrl.contains("dailymotion") || 
            lowerUrl.contains("bilibili") || lowerUrl.contains("doodstream") || 
            lowerUrl.contains("dood.to") || lowerUrl.contains("dood.") ||
            lowerUrl.contains("vidsrc") || lowerUrl.contains("vidlink") || 
            lowerUrl.contains("vidfast")) {

            // For VidSrc, VidLink, VidFast: ensure embed format
            if (lowerUrl.contains("vidsrc") || lowerUrl.contains("vidlink") || lowerUrl.contains("vidfast")) {
                if (!processedUrl.contains("/embed/")) {
                    if (processedUrl.contains("/movie/")) {
                        processedUrl = processedUrl.replace("/movie/", "/embed/movie/")
                    } else if (processedUrl.contains("/tv/")) {
                        processedUrl = processedUrl.replace("/tv/", "/embed/tv/")
                    }
                }
                
                if (!hasDivWrapper) {
                    divStyle = "width: 100%; height: 100%; overflow: hidden; position: relative; background: #000;"
                    iframeStyle = "width: 100%; height: 100%; border: none; display: block; position: absolute; top: 0; left: 0;"
                    iframeAttributes["allow"] = "autoplay; fullscreen; picture-in-picture; encrypted-media"
                    iframeAttributes["allowfullscreen"] = "true"
                }
            }

            // For Mixdrop
            if (lowerUrl.contains("mixdrop")) {
                if (!processedUrl.contains("/e/") && !processedUrl.contains("/f/")) {
                    if (processedUrl.contains("/v/") || processedUrl.contains("/watch/")) {
                        val fileIdMatch = Regex("[/]([a-zA-Z0-9]+)$").find(processedUrl) ?:
                            Regex("[/]([a-zA-Z0-9]+)\\?").find(processedUrl)
                        fileIdMatch?.groupValues?.get(1)?.let { fileId ->
                            val domainMatch = Regex("https?://([^/]+)").find(processedUrl)
                            val domain = domainMatch?.groupValues?.get(1) ?: "mixdrop.co"
                            processedUrl = "https://$domain/e/$fileId"
                        }
                    }
                }
            }

            // For Dailymotion
            if (lowerUrl.contains("dailymotion.com")) {
                if (!processedUrl.contains("/embed/")) {
                    if (processedUrl.contains("/video/")) {
                        val videoIdMatch = Regex("/video/([a-zA-Z0-9]+)").find(processedUrl)
                        videoIdMatch?.groupValues?.get(1)?.let { videoId ->
                            processedUrl = "https://www.dailymotion.com/embed/video/$videoId"
                        }
                    }
                }
            }

            // For Bilibili
            if (lowerUrl.contains("bilibili.tv") || lowerUrl.contains("bilibili.com")) {
                try {
                    val uri = java.net.URI(processedUrl)
                    val queryParams = uri.query?.split("&")?.associate {
                        val parts = it.split("=")
                        if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
                    }?.toMutableMap() ?: mutableMapOf()

                    queryParams.remove("platform")
                    queryParams.remove("from")
                    queryParams.remove("share_source")
                    queryParams.remove("share_medium")

                    queryParams["platform"] = "web"

                    var hostname = uri.host ?: ""
                    var path = uri.path

                    if (hostname.contains("bilibili.com")) {
                        hostname = "www.bilibili.tv"
                        if (!path.startsWith("/en/") && !path.startsWith("/en")) {
                            path = "/en$path"
                        }
                    } else if (!hostname.contains("bilibili.tv")) {
                        hostname = "www.bilibili.tv"
                        val videoIdMatch = Regex("/video/([a-zA-Z0-9]+)").find(path)
                        if (videoIdMatch != null) {
                            path = "/en/video/${videoIdMatch.groupValues[1]}"
                        } else if (!path.startsWith("/en/") && !path.startsWith("/en")) {
                            path = "/en$path"
                        }
                    }

                    val queryString = queryParams.entries.joinToString("&") { (k, v) ->
                        "$k=$v"
                    }
                    processedUrl = "${uri.scheme}://$hostname$path${if (queryString.isNotEmpty()) "?$queryString" else ""}"
                } catch (e: Exception) {
                    // Fallback
                }

                if (!hasDivWrapper) {
                    divStyle = "width: 100%; height: 280px; overflow: hidden; position: relative;"
                    iframeStyle = "width: 100%; height: 330px; position: absolute; top: -60px; left: 0; border: none;"
                    iframeAttributes["scrolling"] = "no"
                }
            }

            // For Doodstream
            if (lowerUrl.contains("doodstream") || lowerUrl.contains("dood.to") || lowerUrl.contains("dood.")) {
                if (!processedUrl.contains("/e/")) {
                    if (processedUrl.contains("/d/") || processedUrl.contains("/v/") || processedUrl.contains("/watch/")) {
                        val fileIdMatch = Regex("[/]([a-zA-Z0-9]+)$").find(processedUrl) ?:
                            Regex("[/]([a-zA-Z0-9]+)\\?").find(processedUrl)
                        fileIdMatch?.groupValues?.get(1)?.let { fileId ->
                            val domainMatch = Regex("https?://([^/]+)").find(processedUrl)
                            var domain = domainMatch?.groupValues?.get(1) ?: "doodstream.com"

                            if (domain.contains("dood.to")) {
                                domain = "dood.to"
                            } else if (domain.contains("dood.")) {
                                domain = "doodstream.com"
                            }

                            processedUrl = "https://$domain/e/$fileId"
                        }
                    }
                }

                if (!hasDivWrapper) {
                    divStyle = "width: 100%; height: 280px; overflow: hidden; position: relative; display: flex; align-items: center; justify-content: center;"
                    iframeStyle = "width: 100%; height: 330px; position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); border: none;"
                    iframeAttributes["scrolling"] = "no"
                }
            }
        }

        // Validate URL
        try {
            val uri = java.net.URI(processedUrl)
            if (!uri.scheme.startsWith("http")) {
                return EmbedData(url = "")
            }
        } catch (e: Exception) {
            return EmbedData(url = "")
        }

        return EmbedData(
            url = processedUrl,
            hasCustomStyling = (hasDivWrapper || processedUrl.contains("drive.google.com")) && (divStyle != null || iframeStyle != null),
            divStyle = divStyle,
            iframeStyle = iframeStyle,
            iframeAttributes = iframeAttributes
        )
    }
}
