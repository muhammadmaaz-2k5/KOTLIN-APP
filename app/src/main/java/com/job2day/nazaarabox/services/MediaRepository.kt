package com.job2day.nazaarabox.services

import com.google.gson.JsonObject
import com.job2day.nazaarabox.core.AppConfig
import com.job2day.nazaarabox.core.AnimeFilters
import com.job2day.nazaarabox.core.CastMember
import com.job2day.nazaarabox.core.DownloadLink
import com.job2day.nazaarabox.core.EpisodeItem
import com.job2day.nazaarabox.core.HomeCategory
import com.job2day.nazaarabox.core.HomeFeed
import com.job2day.nazaarabox.core.MidnightFeed
import com.job2day.nazaarabox.core.ThemedSection
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.PersonItem
import com.job2day.nazaarabox.core.PromotedApp
import com.job2day.nazaarabox.core.ReviewItem
import com.job2day.nazaarabox.core.SearchFilters
import com.job2day.nazaarabox.core.SeasonItem
import com.job2day.nazaarabox.core.VideoServer
import com.job2day.nazaarabox.data.api.RetrofitClient
import com.job2day.nazaarabox.utils.MediaParser
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.ConcurrentHashMap

class MediaRepository {
    private val api get() = RetrofitClient.api

    companion object {
        private data class CacheEntry<T>(val data: T, val expiresAt: Long)
        private var categoriesCache: CacheEntry<List<HomeCategory>>? = null
        private var settingsCache: CacheEntry<Map<String, String>>? = null
        private val detailsCache = ConcurrentHashMap<String, CacheEntry<MediaItem>>()
        private val homeFeedCache = ConcurrentHashMap<Int, CacheEntry<HomeFeed>>()
    }

    suspend fun getHomeFeed(categoryId: Int = 0, forceRefresh: Boolean = false): HomeFeed {
        val now = System.currentTimeMillis()
        if (!forceRefresh) {
            homeFeedCache[categoryId]?.let {
                if (now < it.expiresAt) return it.data
            }
        }

        val result = runCatching {
            val response = api.getHomeFeed(categoryId)
            MediaParser.parseHomeFeed(response)
        }.getOrElse {
            homeFeedCache[categoryId]?.data ?: HomeFeed()
        }

        if (result.categories.isNotEmpty() || result.trending.isNotEmpty()) {
            homeFeedCache[categoryId] = CacheEntry(result, now + 1800_000L) // 30 minutes
        }
        return result
    }

    private var midnightFeedCache: CacheEntry<MidnightFeed>? = null

    suspend fun getMidnightFeed(forceRefresh: Boolean = false): MidnightFeed {
        val now = System.currentTimeMillis()
        if (!forceRefresh) {
            midnightFeedCache?.let {
                if (now < it.expiresAt) return it.data
            }
        }

        val result = runCatching {
            val response = api.getMidnightFeed()
            MediaParser.parseMidnightFeed(response)
        }.getOrElse {
            midnightFeedCache?.data ?: MidnightFeed()
        }

        if (result.sections.isNotEmpty() || result.featured.isNotEmpty()) {
            midnightFeedCache = CacheEntry(result, now + 1800_000L) // 30 minutes
        }
        return result
    }

    suspend fun getCategories(): List<HomeCategory> {
        val now = System.currentTimeMillis()
        categoriesCache?.let {
            if (now < it.expiresAt) return it.data
        }

        val result = runCatching {
            MediaParser.parseCategories(api.getCategories().asList())
        }.getOrElse { MediaParser.defaultCategories() }

        categoriesCache = CacheEntry(result, now + 3600_000L) // 1 Hour
        return result
    }

    suspend fun getGlobalSettings(): Map<String, String> {
        val now = System.currentTimeMillis()
        settingsCache?.let {
            if (now < it.expiresAt) return it.data
        }

        val result = runCatching {
            api.getGlobalSettings().asMap().mapValues { (_, value) ->
                when {
                    value.isJsonNull -> ""
                    value.isJsonPrimitive && value.asJsonPrimitive.isBoolean -> value.asBoolean.toString()
                    value.isJsonPrimitive && value.asJsonPrimitive.isNumber -> value.asNumber.toString()
                    else -> value.asString
                }
            }
        }.getOrDefault(emptyMap())

        settingsCache = CacheEntry(result, now + 900_000L) // 15 Min
        return result
    }

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

    suspend fun fetchSectionPage(
        endpoint: String,
        params: Map<String, String> = emptyMap(),
        page: Int = 1,
        mediaType: String = "movie"
    ): Pair<List<MediaItem>, Int> = runCatching {
        val query = params.toMutableMap()
        query["page"] = page.toString()
        query["include_adult"] = "false"
        val cleanEndpoint = endpoint.trim().trimStart('/')
        val response = tmdb(cleanEndpoint, query)
        val items = MediaParser.parseItems(response.getAsJsonArray("results")?.asList(), mediaType)
        val totalPages = response.get("total_pages")?.asInt ?: 1
        items to totalPages
    }.getOrDefault(emptyList<MediaItem>() to 1)

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
            val customId = item.customId ?: if (item.id >= 1000000000) item.id - 1000000000 else item.id
            val response = api.getJson("api/custom-movie/$customId")
            
            val tmdbId = response.get("tmdb_id")?.takeIf { !it.isJsonNull }?.asInt ?: 0
            val title = response.get("title")?.takeIf { !it.isJsonNull }?.asString ?: item.title
            val mediaType = response.get("type")?.takeIf { !it.isJsonNull }?.asString ?: item.type
            val posterPath = response.get("poster_path")?.takeIf { !it.isJsonNull }?.asString
                ?: response.get("poster_url")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            val backdropPath = response.get("backdrop_path")?.takeIf { !it.isJsonNull }?.asString
                ?: response.get("backdrop_url")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            val rating = response.get("rating")?.takeIf { !it.isJsonNull }?.asDouble ?: item.rating
            val year = response.get("year")?.takeIf { !it.isJsonNull }?.asString
                ?: response.get("release_date")?.takeIf { !it.isJsonNull }?.asString?.take(4) ?: item.year
            val overview = response.get("overview")?.takeIf { !it.isJsonNull }?.asString
                ?: response.get("description")?.takeIf { !it.isJsonNull }?.asString ?: item.overview
            
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
                    val parsed = MediaParser.enrichDetails(tmdbResponse, enriched)
                    // Explicitly restore custom overrides so generic TMDB details don't override them
                    enriched = parsed.copy(
                        title = title,
                        posterUrl = if (posterPath.isNotEmpty()) MediaParser.imageUrl(posterPath) else parsed.posterUrl,
                        backdropUrl = if (backdropPath.isNotEmpty()) MediaParser.imageUrl(backdropPath, "w780") else parsed.backdropUrl,
                        rating = if (rating > 0) rating else parsed.rating,
                        year = if (year.isNotEmpty()) year else parsed.year,
                        overview = if (overview.isNotEmpty()) overview else parsed.overview,
                        id = if (item.id >= 1000000000) item.id else parsed.id
                    )
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
        }.getOrElse { emptyList() }

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

    private var moreAppsCache: CacheEntry<List<PromotedApp>>? = null

    suspend fun getMoreApps(forceRefresh: Boolean = false): List<PromotedApp> {
        val now = System.currentTimeMillis()
        if (!forceRefresh) {
            moreAppsCache?.let {
                if (now < it.expiresAt) return it.data
            }
        }

        val apps = runCatching {
            val jsonArray = api.getMoreApps()
            val list = mutableListOf<PromotedApp>()
            for (elem in jsonArray) {
                if (elem.isJsonObject) {
                    val obj = elem.asJsonObject
                    list.add(
                        PromotedApp(
                            id = obj.get("id")?.asInt ?: 0,
                            name = obj.get("name")?.asString ?: "",
                            tagline = if (obj.has("tagline") && !obj.get("tagline").isJsonNull) obj.get("tagline").asString else "",
                            description = if (obj.has("description") && !obj.get("description").isJsonNull) obj.get("description").asString else "",
                            category = if (obj.has("category") && !obj.get("category").isJsonNull) obj.get("category").asString else "Entertainment",
                            packageName = if (obj.has("package_name") && !obj.get("package_name").isJsonNull) obj.get("package_name").asString else "",
                            playStoreUrl = if (obj.has("play_store_url") && !obj.get("play_store_url").isJsonNull) obj.get("play_store_url").asString else "",
                            iconUrl = if (obj.has("icon_url") && !obj.get("icon_url").isJsonNull) obj.get("icon_url").asString else "",
                            bannerUrl = if (obj.has("banner_url") && !obj.get("banner_url").isJsonNull) obj.get("banner_url").asString else "",
                            rating = if (obj.has("rating") && !obj.get("rating").isJsonNull) obj.get("rating").asDouble else 4.8,
                            downloads = if (obj.has("downloads") && !obj.get("downloads").isJsonNull) obj.get("downloads").asString else "100K+",
                            badge = if (obj.has("badge") && !obj.get("badge").isJsonNull) obj.get("badge").asString else "",
                            sortOrder = if (obj.has("sort_order") && !obj.get("sort_order").isJsonNull) obj.get("sort_order").asInt else 0,
                            isFeatured = if (obj.has("is_featured") && !obj.get("is_featured").isJsonNull) obj.get("is_featured").asBoolean else false,
                            isActive = if (obj.has("is_active") && !obj.get("is_active").isJsonNull) obj.get("is_active").asBoolean else true,
                        )
                    )
                }
            }
            list
        }.getOrElse {
            moreAppsCache?.data ?: getFallbackMoreApps()
        }

        val finalList = if (apps.isNotEmpty()) apps else getFallbackMoreApps()
        moreAppsCache = CacheEntry(finalList, now + 1800_000L) // 30 minutes cache
        return finalList
    }

    private fun getFallbackMoreApps(): List<PromotedApp> {
        return listOf(
            PromotedApp(
                id = 1,
                name = "CinePlay Ultra 4K Player",
                tagline = "Hardware accelerated 4K HDR & subtitle player",
                description = "Ultra high-performance media player with HDR10+ support, multi-audio tracks, background playback, and automatic subtitle downloader.",
                category = "Utilities",
                packageName = "com.cineplay.ultraplayer",
                playStoreUrl = "https://play.google.com/store/apps/details?id=com.cineplay.ultraplayer",
                iconUrl = "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=256&h=256&fit=crop",
                bannerUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&h=400&fit=crop",
                rating = 4.9,
                downloads = "500K+",
                badge = "FEATURED",
                sortOrder = 1,
                isFeatured = true,
                isActive = true,
            ),
            PromotedApp(
                id = 2,
                name = "AnimeWorld Pro",
                tagline = "Seasonal anime schedule, tracking & reminders",
                description = "The ultimate anime companion. Track ongoing simulcasts, manga updates, voice cast notes, and episode notifications.",
                category = "Anime",
                packageName = "com.animeworld.hub",
                playStoreUrl = "https://play.google.com/store/apps/details?id=com.animeworld.hub",
                iconUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=256&h=256&fit=crop",
                bannerUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=800&h=400&fit=crop",
                rating = 4.8,
                downloads = "250K+",
                badge = "HOT",
                sortOrder = 2,
                isFeatured = false,
                isActive = true,
            ),
            PromotedApp(
                id = 3,
                name = "Cast2Screen Smart TV",
                tagline = "Cast videos & mirror screen to any Smart TV",
                description = "1-tap stream casting to Roku, Chromecast, Fire TV, Apple TV, and DLNA devices with zero latency.",
                category = "Utilities",
                packageName = "com.streamcast.smartmirror",
                playStoreUrl = "https://play.google.com/store/apps/details?id=com.streamcast.smartmirror",
                iconUrl = "https://images.unsplash.com/photo-1593784991095-a205069470b6?w=256&h=256&fit=crop",
                bannerUrl = "https://images.unsplash.com/photo-1526738549149-8e07eca6c147?w=800&h=400&fit=crop",
                rating = 4.7,
                downloads = "1M+",
                badge = "POPULAR",
                sortOrder = 3,
                isFeatured = false,
                isActive = true,
            ),
            PromotedApp(
                id = 4,
                name = "Midnight Cinema VIP",
                tagline = "Curated late-night cinema & noir lounge",
                description = "Exclusive portal for mature cinematic storytelling, psychological thrillers, and private indie collections.",
                category = "Entertainment",
                packageName = "com.engora.midnightclub",
                playStoreUrl = "https://play.google.com/store/apps/details?id=com.engora.midnightclub",
                iconUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=256&h=256&fit=crop",
                bannerUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=800&h=400&fit=crop",
                rating = 4.9,
                downloads = "100K+",
                badge = "18+ VIP",
                sortOrder = 4,
                isFeatured = false,
                isActive = true,
            ),
            PromotedApp(
                id = 5,
                name = "Subtitle Master & Audio Sync",
                tagline = "Instant subtitles in 50+ languages with auto sync",
                description = "Search and download SRT subtitles in seconds with time-shift fine tuning and offline playback support.",
                category = "Utilities",
                packageName = "com.subtitlesync.multilingual",
                playStoreUrl = "https://play.google.com/store/apps/details?id=com.subtitlesync.multilingual",
                iconUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=256&h=256&fit=crop",
                bannerUrl = "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?w=800&h=400&fit=crop",
                rating = 4.8,
                downloads = "300K+",
                badge = "NEW",
                sortOrder = 5,
                isFeatured = false,
                isActive = true,
            ),
        )
    }
}
