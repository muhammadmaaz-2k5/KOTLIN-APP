package com.job2day.nazaarabox.core

import com.job2day.nazaarabox.BuildConfig

object AppConfig {
    const val IMAGE_BASE = "https://image.tmdb.org/t/p"

    val backendBaseUrl: String
        get() = if (BuildConfig.DEBUG) BuildConfig.BACKEND_DEBUG_URL else BuildConfig.BACKEND_BASE_URL

    val tmdbProxyUrl: String
        get() = "$backendBaseUrl/api/tmdb"
}
