package com.job2day.nazaarabox.utils

import android.content.Context

object PlaybackPositionService {
    private const val PREFS_NAME = "nazaara_playback_positions"

    fun getPosition(context: Context, url: String): Long {
        if (url.isBlank()) return 0L
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(url, 0L)
    }

    fun savePosition(context: Context, url: String, position: Long) {
        if (url.isBlank() || position < 0) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(url, position).apply()
    }
}
