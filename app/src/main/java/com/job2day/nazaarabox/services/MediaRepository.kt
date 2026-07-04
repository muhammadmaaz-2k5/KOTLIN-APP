package com.job2day.nazaarabox.services

import com.google.gson.JsonObject
import com.job2day.nazaarabox.core.AppConfig
import com.job2day.nazaarabox.core.AnimeFilters
import com.job2day.nazaarabox.core.CastMember
import com.job2day.nazaarabox.core.DownloadLink
import com.job2day.nazaarabox.core.EpisodeItem
import com.job2day.nazaarabox.core.HomeCategory
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.PersonItem
import com.job2day.nazaarabox.core.ReviewItem
import com.job2day.nazaarabox.core.SearchFilters
import com.job2day.nazaarabox.core.SeasonItem
import com.job2day.nazaarabox.core.TrailerItem
import com.job2day.nazaarabox.core.VideoServer
import com.job2day.nazaarabox.data.api.RetrofitClient
import com.job2day.nazaarabox.utils.MediaParser
import java.time.LocalDate
import java.time.YearMonth

class MediaRepository {
    private val api get() = RetrofitClient.api

    suspend fun getCategories(): List<HomeCategory> = runCatching {
        MediaParser.parseCategories(api.getCategories().asList())
    }.getOrElse { MediaParser.defaultCategories() }

    suspend fun getGlobalSettings(): Map<String, String> = runCatching {
        api.getGlobalSettings().asMap().mapValues { (_, value) ->
            when {
                value.isJsonNull -> ""
                value.isJsonPrimitive && value.asJsonPrimitive.isBoolean -> value.asBoolean.toString()
                value.isJsonPrimitive && value.asJsonPrimitive.isNumber -> value.asNumber.toString()
                else -> value.asString
            }
        }
    }.getOrDefault(emptyMap())

    suspend fun getCustomContent(params: Map<String, String>): List<MediaItem> = runCatching {
        MediaParser.parseCustomContent(api.getCustomContent(params).asList())
    }.getOrDefault(emptyList())

    suspend fun getTrending(category: HomeCategory): List<MediaItem> = runCatching {
        if (category.mediaType == "all") {
            val response = tmdb("trending/all/week", mapOf("page" to "1"))
            MediaParser.parseItems(response.getAsJsonArray("results")?.asList(), "movie")
        } else {
            val endpoint = if (category.mediaType == "tv") "tv" else "movie"
            val params = category.trendingParams.toMutableMap()
            params["page"] = "1"
            val response = tmdb("discover/$endpoint", params)
            MediaParser.parseItems(response.getAsJsonArray("results")?.asList(), category.mediaType)
        }
    }.getOrDefault(emptyList())

    suspend fun getPopular(category: HomeCategory): List<MediaItem> = runCatching {
        if (category.mediaType == "all") {
            val now = YearMonth.now()
            val monthStart = LocalDate.of(now.year, now.month, 1).toString()
            val monthEnd = LocalDate.of(now.year, now.month, now.lengthOfMonth()).toString()
            val params = category.popularParams.toMutableMap()
            params["release_date.gte"] = monthStart
            params["release_date.lte"] = monthEnd
            params["page"] = "1"
            var response = tmdb("discover/movie", params)
            var items = MediaParser.parseItems(response.getAsJsonArray("results")?.asList(), "movie")
            if (items.size < 4) {
                response = tmdb("discover/movie", category.popularParams + mapOf("page" to "1"))
                items = MediaParser.parseItems(response.getAsJsonArray("results")?.asList(), "movie")
            }
            items
        } else {
            val endpoint = if (category.mediaType == "tv") "tv" else "movie"
            val params = category.popularParams.toMutableMap()
            params["page"] = "1"
            val response = tmdb("discover/$endpoint", params)
            MediaParser.parseItems(response.getAsJsonArray("results")?.asList(), category.mediaType)
        }
    }.getOrDefault(emptyList())

    suspend fun discoverMovies(
        params: Map<String, String>,
        page: Int = 1,
    ): Pair<List<MediaItem>, Int> = discover("movie", params, page)

    suspend fun discoverByLanguage(
        languageCode: String,
        mediaType: String,
        page: Int = 1,
    ): List<MediaItem> = runCatching {
        val endpoint = if (mediaType == "tv") "tv" else "movie"
        val response = tmdb(
            "discover/$endpoint",
            mapOf(
                "with_original_language" to languageCode,
                "sort_by" to "popularity.desc",
                "include_adult" to "false",
                "page" to page.toString(),
            ),
        )
        MediaParser.parseItems(response.getAsJsonArray("results")?.asList(), mediaType)
    }.getOrDefault(emptyList())

    suspend fun discover(
        mediaType: String,
        params: Map<String, String>,
        page: Int = 1,
    ): Pair<List<MediaItem>, Int> = runCatching {
        val endpoint = if (mediaType == "tv") "tv" else "movie"
        val query = params.toMutableMap()
        query["page"] = page.toString()
        val response = tmdb("discover/$endpoint", query)
        val items = MediaParser.parseItems(response.getAsJsonArray("results")?.asList(), mediaType)
        val totalPages = response.get("total_pages")?.asInt ?: 1
        items to totalPages
    }.getOrDefault(emptyList<MediaItem>() to 1)

    suspend fun trending(mediaType: String): List<MediaItem> = runCatching {
        val path = if (mediaType == "tv") "trending/tv/week" else "trending/movie/week"
        val response = tmdb(path, mapOf("page" to "1"))
        MediaParser.parseItems(response.getAsJsonArray("results")?.asList(), mediaType)
    }.getOrDefault(emptyList())

    suspend fun search(
        query: String,
        type: String,
        filters: SearchFilters,
        page: Int = 1,
    ): List<MediaItem> = runCatching {
        val endpoint = when (type) {
            "movie", "tv", "person" -> "search/$type"
            else -> "search/multi"
        }
        val params = buildFilterParams(filters).toMutableMap()
        params["query"] = query
        params["include_adult"] = "false"
        params["page"] = page.toString()
        val response = tmdb(endpoint, params)
        MediaParser.parseItems(response.getAsJsonArray("results")?.asList(), type)
    }.getOrDefault(emptyList())

    suspend fun loadDetail(item: MediaItem): MediaItem = runCatching {
        if (item.isCustom || item.id >= 1000000000) {
            val customId = if (item.id >= 1000000000) item.id - 1000000000 else item.id
            val response = api.getJson("api/custom-movie/$customId")
            
            val tmdbId = response.get("tmdb_id")?.takeIf { !it.isJsonNull }?.asInt ?: 0
            val title = response.get("title")?.takeIf { !it.isJsonNull }?.asString ?: item.title
            val mediaType = response.get("type")?.takeIf { !it.isJsonNull }?.asString ?: item.type
            val posterPath = response.get("poster_path")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            val backdropPath = response.get("backdrop_path")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            val rating = response.get("rating")?.takeIf { !it.isJsonNull }?.asDouble ?: item.rating
            val year = response.get("year")?.takeIf { !it.isJsonNull }?.asString ?: item.year
            val overview = response.get("overview")?.takeIf { !it.isJsonNull }?.asString ?: item.overview
            
            var enriched = item.copy(
                id = if (tmdbId > 0) tmdbId else item.id,
                title = title,
                type = mediaType,
                posterUrl = MediaParser.imageUrl(posterPath),
                backdropUrl = MediaParser.imageUrl(backdropPath, "w780"),
                rating = rating,
                year = year,
                overview = overview,
                isCustom = true,
                customId = customId
            )
            
            if (tmdbId > 0) {
                runCatching {
                    val tmdbResponse = tmdb("$mediaType/$tmdbId", emptyMap())
                    enriched = MediaParser.enrichDetails(tmdbResponse, enriched)
                }
            }
            enriched
        } else {
            val endpoint = if (item.type == "tv") "tv" else "movie"
            val response = tmdb("$endpoint/${item.id}", emptyMap())
            MediaParser.enrichDetails(response, item)
        }
    }.getOrDefault(item)

    suspend fun getCast(item: MediaItem): List<CastMember> = runCatching {
        val endpoint = if (item.type == "tv") "tv" else "movie"
        val response = tmdb("$endpoint/${item.id}/credits", emptyMap())
        MediaParser.parseCast(response.getAsJsonArray("cast")?.asList())
    }.getOrDefault(emptyList())

    suspend fun getTrailers(item: MediaItem): List<TrailerItem> = runCatching {
        val endpoint = if (item.type == "tv") "tv" else "movie"
        val response = tmdb("$endpoint/${item.id}/videos", emptyMap())
        MediaParser.parseTrailers(response.getAsJsonArray("results")?.asList())
    }.getOrDefault(emptyList())

    suspend fun getSimilar(item: MediaItem): List<MediaItem> = runCatching {
        val endpoint = if (item.type == "tv") "tv" else "movie"
        val response = tmdb("$endpoint/${item.id}/similar", emptyMap())
        MediaParser.parseItems(response.getAsJsonArray("results")?.asList(), item.type)
    }.getOrDefault(emptyList())

    suspend fun getReviews(item: MediaItem): List<ReviewItem> = runCatching {
        val endpoint = if (item.type == "tv") "tv" else "movie"
        val response = tmdb("$endpoint/${item.id}/reviews", emptyMap())
        MediaParser.parseReviews(response.getAsJsonArray("results")?.asList())
    }.getOrDefault(emptyList())

    suspend fun getSeasons(item: MediaItem): List<SeasonItem> = runCatching {
        if (item.type != "tv") return emptyList()
        val response = tmdb("tv/${item.id}", emptyMap())
        MediaParser.parseSeasons(response.getAsJsonArray("seasons")?.asList())
    }.getOrDefault(emptyList())

    suspend fun getEpisodes(showId: Int, seasonNumber: Int): List<EpisodeItem> = runCatching {
        val response = tmdb("tv/$showId/season/$seasonNumber", emptyMap())
        MediaParser.parseEpisodes(response.getAsJsonArray("episodes")?.asList(), seasonNumber)
    }.getOrDefault(emptyList())

    suspend fun getPerson(id: Int): PersonItem = runCatching {
        val response = tmdb("person/$id", emptyMap())
        MediaParser.parsePerson(response)
    }.getOrDefault(PersonItem(id = id))

    suspend fun getPersonCredits(id: Int): List<MediaItem> = runCatching {
        val response = tmdb("person/$id/combined_credits", emptyMap())
        val cast = response.getAsJsonArray("cast")?.asList().orEmpty()
        MediaParser.parseItems(cast, "movie")
    }.getOrDefault(emptyList())

    suspend fun getVideoServers(item: MediaItem, season: Int?, episode: Int?): List<VideoServer> =
        runCatching {
            MediaParser.parseVideoServers(
                api.getServers(
                    id = item.id,
                    type = item.type,
                    season = season?.toString().orEmpty(),
                    episode = episode?.toString().orEmpty(),
                ).asList(),
            )
        }.getOrElse { MediaParser.defaultVideoServers() }

    suspend fun getDownloadLinks(
        mediaType: String,
        id: Int,
        season: Int? = null,
        episode: Int? = null,
    ): List<DownloadLink> = runCatching {
        MediaParser.parseDownloadLinks(
            api.getDownloadLinks(mediaType, id, season, episode).asList(),
        )
    }.getOrDefault(emptyList())

    fun buildBrowseParams(
        mediaType: String,
        tabSortBy: String,
        tabGenreId: Int?,
        filters: SearchFilters,
        isTv: Boolean = mediaType == "tv",
    ): Map<String, String> {
        val params = mutableMapOf("include_adult" to "false")
        params["sort_by"] = when (filters.sortBy) {
            "Rating" -> {
                params["vote_count.gte"] = "200"
                "vote_average.desc"
            }
            "Latest" -> if (isTv) "first_air_date.desc" else "release_date.desc"
            else -> tabSortBy.also {
                if (it.contains("vote_average")) params["vote_count.gte"] = "200"
            }
        }
        when {
            tabGenreId != null -> params["with_genres"] = tabGenreId.toString()
            filters.genre != "All" -> SearchFilters.genreIds[filters.genre]?.let { params["with_genres"] = it.toString() }
        }
        if (filters.country != "All" && filters.country != "Other") {
            SearchFilters.countryCodes[filters.country]?.let { params["with_origin_country"] = it }
        }
        if (filters.language != "All") {
            SearchFilters.languageCodes[filters.language]?.let { params["with_original_language"] = it }
        }
        applyYearFilter(params, filters.year, isTv)
        return params
    }

    fun buildAnimeParams(
        mediaType: String,
        filters: AnimeFilters,
    ): Map<String, String> {
        val isTv = mediaType == "tv"
        val params = mutableMapOf(
            "with_genres" to "16",
            "with_keywords" to "210024",
            "include_adult" to "false",
        )
        params["sort_by"] = when (filters.sortBy) {
            "Rating" -> {
                params["vote_count.gte"] = "100"
                "vote_average.desc"
            }
            "Latest" -> if (isTv) "first_air_date.desc" else "release_date.desc"
            "Hottest", "ForYou" -> "popularity.desc"
            else -> "popularity.desc"
        }
        if (filters.country != "All" && filters.country != "Other") {
            AnimeFilters.countryCodes[filters.country]?.let { params["with_origin_country"] = it }
        }
        applyYearFilter(params, filters.year, isTv = isTv)
        return params
    }

    fun buildFilterParams(filters: SearchFilters): Map<String, String> {
        val params = mutableMapOf<String, String>()
        if (filters.genre != "All") {
            SearchFilters.genreIds[filters.genre]?.let { params["with_genres"] = it.toString() }
        }
        if (filters.country != "All" && filters.country != "Other") {
            SearchFilters.countryCodes[filters.country]?.let { params["region"] = it }
        }
        if (filters.language != "All") {
            SearchFilters.languageCodes[filters.language]?.let { params["language"] = it }
        }
        if (filters.year != "All" && filters.year != "Other" && !filters.year.contains("s")) {
            params["year"] = filters.year
        }
        return params
    }

    private fun applyYearFilter(params: MutableMap<String, String>, year: String, isTv: Boolean) {
        if (year == "All" || year == "Other") return
        val gteKey = if (isTv) "first_air_date.gte" else "release_date.gte"
        val lteKey = if (isTv) "first_air_date.lte" else "release_date.lte"
        if (year.endsWith("s")) {
            val decade = year.removeSuffix("s").toIntOrNull() ?: return
            params[gteKey] = "$decade-01-01"
            params[lteKey] = "${decade + 9}-12-31"
        } else {
            params[gteKey] = "$year-01-01"
            params[lteKey] = "$year-12-31"
        }
    }

    private suspend fun tmdb(path: String, params: Map<String, String>): JsonObject {
        val url = "${AppConfig.tmdbProxyUrl}/$path"
        return api.getJson(url, params)
    }
}
