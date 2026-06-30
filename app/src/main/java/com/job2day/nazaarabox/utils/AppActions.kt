package com.job2day.nazaarabox.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.PersonItem

object AppActions {
    fun shareItem(context: Context, item: MediaItem) {
        val typeStr = if (item.type == "tv") "tv" else "movie"
        val url = "https://www.themoviedb.org/$typeStr/${item.id}"
        val text = buildString {
            append("🎬 I am watching \"${item.title}\"!\n\n")
            append("🍿 Watch your favorite Movies, TV Shows & Anime for FREE on CineTrack.\n\n")
            append("ℹ️ Info: $url\n\n")
            append("🚀 Download the CineTrack App now and start streaming!\n")
            append("👉 https://play.google.com/store/apps/details?id=com.job2day.nazaarabox")
        }
        shareText(context, text)
    }

    fun sharePerson(context: Context, person: PersonItem) {
        val url = "https://www.themoviedb.org/person/${person.id}"
        shareText(context, "🎭 ${person.name}\n\nView on CineTrack!\n$url")
    }

    fun openTmdbPage(context: Context, item: MediaItem) {
        val type = if (item.type == "tv") "tv" else "movie"
        openInBrowser(context, "https://www.themoviedb.org/$type/${item.id}")
    }

    fun openTrailer(context: Context, youtubeKey: String) {
        val uri = Uri.parse("https://www.youtube.com/watch?v=$youtubeKey")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    fun openInBrowser(context: Context, url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }
}
