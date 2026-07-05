package com.job2day.nazaarabox.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.job2day.nazaarabox.core.AppConfig
import com.job2day.nazaarabox.core.HomeCategory
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.CastMember
import com.job2day.nazaarabox.core.TrailerItem
import com.job2day.nazaarabox.core.ReviewItem
import com.job2day.nazaarabox.core.SeasonItem
import com.job2day.nazaarabox.core.EpisodeItem
import com.job2day.nazaarabox.core.VideoServer
import com.job2day.nazaarabox.core.DownloadLink
import com.job2day.nazaarabox.core.PersonItem

object MediaParser {
    fun imageUrl(path: String?, size: String = "w342"): String {
        if (path.isNullOrBlank()) return ""
        if (path.startsWith("http")) return path
        return "${AppConfig.IMAGE_BASE}/$size$path"
    }

    fun parseItems(raw: List<JsonElement>?, defaultType: String = "movie"): List<MediaItem> {
        if (raw == null) return emptyList()
        return raw.mapNotNull { element ->
            val obj = element.asJsonObject
            parseItem(obj, defaultType)
        }
    }

    fun parseItem(obj: JsonObject, defaultType: String = "movie"): MediaItem {
        val mediaType = obj.stringOr("media_type", defaultType)
        val type = if (mediaType == "tv") "tv" else if (mediaType == "person") "person" else "movie"
        val title = obj.stringOr("title", obj.stringOr("name", "Unknown"))
        val dateRaw = obj.stringOr("release_date", obj.stringOr("first_air_date", ""))
        val year = if (dateRaw.length >= 4) dateRaw.substring(0, 4) else ""
        val posterPath = obj.get("poster_path")?.takeIf { !it.isJsonNull }?.asString
            ?: obj.get("profile_path")?.takeIf { !it.isJsonNull }?.asString
        val backdropPath = obj.get("backdrop_path")?.takeIf { !it.isJsonNull }?.asString
        return MediaItem(
            id = obj.intOr("id"),
            title = title,
            type = type,
            posterUrl = imageUrl(posterPath),
            backdropUrl = imageUrl(backdropPath, "w780"),
            rating = obj.doubleOr("vote_average"),
            year = year,
            overview = obj.stringOr("overview"),
            voteCount = obj.intOr("vote_count"),
            popularity = obj.doubleOr("popularity"),
            isCustom = obj.get("is_custom")?.asBoolean == true,
            tmdbId = obj.intOr("tmdb_id", obj.intOr("id")),
        )
    }

    fun parseCustomContent(raw: List<JsonElement>?): List<MediaItem> {
        if (raw == null) return emptyList()
        return raw.map { element ->
            val obj = element.asJsonObject
            MediaItem(
                id = obj.intOr("id"),
                customId = obj.get("custom_id")?.takeIf { !it.isJsonNull }?.asInt,
                title = obj.stringOr("title", "Unknown"),
                type = obj.stringOr("type", "movie"),
                posterUrl = obj.stringOr("posterUrl"),
                backdropUrl = obj.stringOr("backdropUrl"),
                rating = obj.doubleOr("rating"),
                year = obj.stringOr("year"),
                isCustom = true,
                tmdbId = obj.intOr("tmdb_id"),
            )
        }
    }

    fun parseCategories(raw: List<JsonElement>?): List<HomeCategory> {
        if (raw == null) return defaultCategories()
        return raw.map { element ->
            val obj = element.asJsonObject
            HomeCategory(
                id = obj.intOr("id"),
                label = obj.stringOr("label"),
                emoji = obj.stringOr("emoji"),
                mediaType = obj.stringOr("media_type", obj.stringOr("mediaType", "movie")),
                trendingParams = parseParamMap(obj.get("trending_params") ?: obj.get("trendingParams")),
                popularParams = parseParamMap(obj.get("popular_params") ?: obj.get("popularParams")),
            )
        }
    }

    fun defaultCategories(): List<HomeCategory> = listOf(
        HomeCategory(label = "All", emoji = "🌐", mediaType = "all"),
        HomeCategory(
            label = "Hollywood", emoji = "🇺🇸", mediaType = "movie",
            trendingParams = mapOf("with_original_language" to "en"),
            popularParams = mapOf("with_original_language" to "en"),
        ),
        HomeCategory(
            label = "Bollywood", emoji = "🇮🇳", mediaType = "movie",
            trendingParams = mapOf("with_original_language" to "hi"),
            popularParams = mapOf("with_original_language" to "hi"),
        ),
    )

    fun parseCast(raw: List<JsonElement>?): List<CastMember> {
        if (raw == null) return emptyList()
        return raw.take(15).map { element ->
            val obj = element.asJsonObject
            CastMember(
                id = obj.intOr("id"),
                name = obj.stringOr("name"),
                character = obj.stringOr("character"),
                photoUrl = imageUrl(obj.get("profile_path")?.takeIf { !it.isJsonNull }?.asString, "w185"),
            )
        }
    }

    fun parseTrailers(raw: List<JsonElement>?): List<TrailerItem> {
        if (raw == null) return emptyList()
        return raw.mapNotNull { element ->
            val obj = element.asJsonObject
            if (obj.stringOr("site") != "YouTube") return@mapNotNull null
            val type = obj.stringOr("type")
            if (type != "Trailer" && type != "Teaser") return@mapNotNull null
            TrailerItem(key = obj.stringOr("key"), name = obj.stringOr("name", "Trailer"), type = type)
        }.take(5)
    }

    fun parseReviews(raw: List<JsonElement>?): List<ReviewItem> {
        if (raw == null) return emptyList()
        return raw.take(5).map { element ->
            val obj = element.asJsonObject
            val authorDetails = obj.getAsJsonObject("author_details")
            val avatarPath = authorDetails?.get("avatar_path")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            val cleanAvatar = if (avatarPath.startsWith("/https")) avatarPath.substring(1) else avatarPath
            ReviewItem(
                author = obj.stringOr("author", "Anonymous"),
                content = obj.stringOr("content"),
                rating = authorDetails?.get("rating")?.takeIf { !it.isJsonNull }?.asDouble,
                avatarPath = cleanAvatar,
            )
        }
    }

    fun parseSeasons(raw: List<JsonElement>?): List<SeasonItem> {
        if (raw == null) return emptyList()
        return raw.mapNotNull { element ->
            val obj = element.asJsonObject
            val seasonNumber = obj.intOr("season_number")
            if (seasonNumber <= 0) return@mapNotNull null
            SeasonItem(
                name = obj.stringOr("name", "Season"),
                seasonNumber = seasonNumber,
                episodeCount = obj.intOr("episode_count"),
                airDate = obj.stringOr("air_date"),
                posterPath = obj.get("poster_path")?.takeIf { !it.isJsonNull }?.asString.orEmpty(),
                overview = obj.stringOr("overview"),
            )
        }
    }

    fun parseEpisodes(raw: List<JsonElement>?, seasonNumber: Int): List<EpisodeItem> {
        if (raw == null) return emptyList()
        return raw.map { element ->
            val obj = element.asJsonObject
            EpisodeItem(
                id = obj.intOr("id"),
                name = obj.stringOr("name"),
                episodeNumber = obj.intOr("episode_number"),
                seasonNumber = seasonNumber,
                overview = obj.stringOr("overview"),
                stillPath = obj.get("still_path")?.takeIf { !it.isJsonNull }?.asString.orEmpty(),
                airDate = obj.stringOr("air_date"),
                runtime = obj.intOr("runtime"),
            )
        }
    }

    fun parseVideoServers(raw: List<JsonElement>?): List<VideoServer> {
        if (raw == null) return defaultVideoServers()
        return raw.map { element ->
            val obj = element.asJsonObject
            VideoServer(
                name = obj.stringOr("name"),
                label = obj.stringOr("label"),
                icon = obj.stringOr("icon", "▶"),
                movieUrlTemplate = obj.stringOr("movie_url_template"),
                tvUrlTemplate = obj.stringOr("tv_url_template"),
            )
        }
    }

    fun defaultVideoServers(): List<VideoServer> = listOf(
        VideoServer(
            name = "vidsrc", label = "VidSrc", icon = "▶",
            movieUrlTemplate = "https://vidsrc.to/embed/movie/{id}",
            tvUrlTemplate = "https://vidsrc.to/embed/tv/{id}/{season}/{episode}",
        ),
        VideoServer(
            name = "vidlink", label = "VidLink", icon = "⚡",
            movieUrlTemplate = "https://vidlink.pro/movie/{id}?primaryColor=B20710&secondaryColor=170000&icons=vid&iconColor=B20710&title=false&poster=true&autoplay=false&nextbutton=true",
            tvUrlTemplate = "https://vidlink.pro/tv/{id}/{season}/{episode}?primaryColor=B20710&secondaryColor=170000&icons=vid&iconColor=B20710&title=false&poster=true&autoplay=false&nextbutton=true",
        ),
        VideoServer(
            name = "vidfast", label = "VidFast", icon = "⚡",
            movieUrlTemplate = "https://vidfast.pro/movie/{id}?autoPlay=true&theme=6C5CE7",
            tvUrlTemplate = "https://vidfast.pro/tv/{id}/{season}/{episode}?autoPlay=true&theme=6C5CE7&nextButton=true&autoNext=true",
        ),
    )

    fun parseDownloadLinks(raw: List<JsonElement>?): List<DownloadLink> {
        if (raw == null) return emptyList()
        return raw.map { element ->
            val obj = element.asJsonObject
            val serverName = obj.stringOr("server_name", obj.stringOr("label", "Mirror"))
            DownloadLink(
                label = obj.stringOr("label", obj.stringOr("title", serverName)),
                url = obj.stringOr("download_url", obj.stringOr("url", obj.stringOr("link"))),
                quality = obj.stringOr("quality", "1080p"),
                serverName = serverName,
                serverIcon = obj.stringOr("server_icon", "🔗"),
                language = obj.stringOr("language", "English"),
                fileSize = obj.stringOr("file_size"),
                notes = obj.stringOr("notes"),
            )
        }.filter { it.url.isNotBlank() }
    }

    fun parsePerson(obj: JsonObject): PersonItem {
        return PersonItem(
            id = obj.intOr("id"),
            name = obj.stringOr("name"),
            photoUrl = imageUrl(obj.get("profile_path")?.takeIf { !it.isJsonNull }?.asString, "w342"),
            biography = obj.stringOr("biography"),
            birthday = obj.stringOr("birthday"),
            placeOfBirth = obj.stringOr("place_of_birth"),
            knownForDepartment = obj.stringOr("known_for_department"),
        )
    }

    fun enrichDetails(obj: JsonObject, fallback: MediaItem): MediaItem {
        val type = fallback.type
        val title = obj.stringOr("title", obj.stringOr("name", fallback.title))
        val releaseDate = obj.stringOr("release_date", obj.stringOr("first_air_date", ""))
        val year = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else fallback.year
        val genres = obj.getAsJsonArray("genres")?.map { it.asJsonObject.stringOr("name") }.orEmpty()
        val runtimeMinutes = when {
            type == "movie" -> obj.intOr("runtime")
            else -> obj.getAsJsonArray("episode_run_time")?.firstOrNull()?.asInt ?: 0
        }
        val runtime = if (runtimeMinutes > 0) "$runtimeMinutes min" else fallback.runtime
        return fallback.copy(
            id = obj.intOr("id", fallback.id),
            title = title,
            posterUrl = imageUrl(obj.get("poster_path")?.takeIf { !it.isJsonNull }?.asString ?: fallback.posterUrl.removePrefix(AppConfig.IMAGE_BASE), "w342")
                .ifBlank { fallback.posterUrl },
            backdropUrl = imageUrl(obj.get("backdrop_path")?.takeIf { !it.isJsonNull }?.asString, "w780")
                .ifBlank { fallback.backdropUrl },
            rating = obj.doubleOr("vote_average", fallback.rating),
            year = year,
            genres = genres,
            runtime = runtime,
            overview = obj.stringOr("overview", fallback.overview),
            voteCount = obj.intOr("vote_count", fallback.voteCount),
            status = obj.stringOr("status"),
            isCustom = obj.get("is_custom")?.asBoolean == true || fallback.isCustom,
        )
    }

    private fun parseParamMap(element: JsonElement?): Map<String, String> {
        if (element == null || element.isJsonNull || !element.isJsonObject) return emptyMap()
        return element.asJsonObject.entrySet().associate { (key, value) ->
            key to when {
                value.isJsonNull -> ""
                value.isJsonPrimitive -> value.asString
                else -> value.toString()
            }
        }
    }

    private fun JsonObject.stringOr(key: String, default: String = ""): String {
        val value = get(key) ?: return default
        return if (value.isJsonNull) default else value.asString
    }

    private fun JsonObject.intOr(key: String, default: Int = 0): Int {
        val value = get(key) ?: return default
        return if (value.isJsonNull) default else value.asInt
    }

    private fun JsonObject.doubleOr(key: String, default: Double = 0.0): Double {
        val value = get(key) ?: return default
        return if (value.isJsonNull) default else value.asDouble
    }
}
