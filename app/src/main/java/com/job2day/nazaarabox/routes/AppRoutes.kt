package com.job2day.nazaarabox.routes

import com.job2day.nazaarabox.core.MediaItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AppRoutes {
    const val HOME = "home"
    const val MOVIES = "movies"
    const val TV_SHOWS = "tv_shows"
    const val ANIME = "anime"
    const val SEARCH = "search"
    const val DETAIL = "detail"
    const val ACTOR = "actor/{personId}"
    const val SEASON = "season"
    const val PLAYER = "player"
    const val FULLSCREEN_PLAYER = "fullscreen_player"
    const val SEE_ALL = "see_all"
    const val CATEGORY = "category"
    const val LANGUAGE_BROWSE = "language_browse"
    const val PRIVACY_POLICY = "privacy_policy"

    fun actor(personId: Int) = "actor/$personId"

    fun encodeItem(item: MediaItem): String = Json.encodeToString(item)

    fun decodeItem(raw: String): MediaItem = Json.decodeFromString(raw)
}
