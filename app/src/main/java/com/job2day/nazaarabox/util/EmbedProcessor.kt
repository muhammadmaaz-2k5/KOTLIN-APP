package com.job2day.nazaarabox.util

data class EmbedData(
    val url: String,
    val iframeStyle: String? = null,
    val hasCustomStyling: Boolean = false,
    val iframeAttributes: Map<String, String> = emptyMap(),
    val divStyle: String? = null
)

object EmbedProcessor {
    fun processEmbedUrl(url: String): EmbedData {
        // Standard parser: returns default URL and empty overrides
        return EmbedData(
            url = url,
            iframeStyle = null,
            hasCustomStyling = false,
            iframeAttributes = emptyMap(),
            divStyle = null
        )
    }
}
