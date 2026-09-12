package com.job2day.nazaarabox.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.PersonItem

object AppActions {
    fun shareItem(context: Context, item: MediaItem) {
        val appUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
        val text = buildString {
            append("🎬 I am watching \"${item.title}\"!\n\n")
            append("🍿 Watch your favorite Movies, TV Shows & Anime for FREE on Nazaarabox.\n\n")
            append("🚀 Download the Nazaarabox App now and start streaming!\n")
            append("👉 $appUrl")
        }
        shareText(context, text)
    }

    fun sharePerson(context: Context, person: PersonItem) {
        val appUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
        val text = buildString {
            append("🎭 ${person.name}\n\n")
            append("🍿 Discover your favorite Movies, TV Shows & Anime for FREE on Nazaarabox.\n\n")
            append("🚀 Download the Nazaarabox App now and start streaming!\n")
            append("👉 $appUrl")
        }
        shareText(context, text)
    }

    fun openTmdbPage(context: Context, item: MediaItem) {
        val type = if (item.type == "tv") "tv" else "movie"
        openInBrowser(context, "https://www.themoviedb.org/$type/${item.id}")
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
