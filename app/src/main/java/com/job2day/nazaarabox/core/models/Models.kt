package com.job2day.nazaarabox.core

import kotlinx.serialization.Serializable

@Serializable
data class MediaItem(
    val id: Int = 0,
    val title: String = "Unknown",
    val type: String = "movie",
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val rating: Double = 0.0,
    val year: String = "",
    val genres: List<String> = emptyList(),
    val runtime: String = "",
    val overview: String = "",
    val voteCount: Int = 0,
    val status: String = "",
    val isCustom: Boolean = false,
    val customId: Int? = null,
    val season: Int? = null,
    val episode: Int? = null,
    val popularity: Double = 0.0,
    val tmdbId: Int = 0,
    val isMidnight: Boolean = false,
)

@Serializable
data class ThemedSection(
    val emoji: String = "",
    val title: String = "",
    val tmdbParams: Map<String, String> = emptyMap(),
    val mediaType: String = "movie",
    val id: Int = 0,
    val endpoint: String = "discover/movie",
    val items: List<MediaItem> = emptyList(),
)

@Serializable
data class HomeFeed(
    val categories: List<HomeCategory> = emptyList(),
    val featured: List<MediaItem> = emptyList(),
    val trending: List<MediaItem> = emptyList(),
    val popular: List<MediaItem> = emptyList(),
    val customExclusives: List<MediaItem> = emptyList(),
    val sections: List<ThemedSection> = emptyList(),
)

@Serializable
data class MidnightFeed(
    val title: String = "ENGORA MIDNIGHT",
    val tagline: String = "18+ Adult Nightlife & Late Night Cinema",
    val is18Plus: Boolean = true,
    val categories: List<HomeCategory> = emptyList(),
    val featured: List<MediaItem> = emptyList(),
    val sections: List<ThemedSection> = emptyList(),
)

@Serializable
data class LanguageOption(
    val code: String = "",
    val label: String = "",
    val flag: String = "",
    val nativeName: String = "",
    val mediaType: String = "both",
    val accentArgb: Long = 0xFF6C5CE7,
)

@Serializable
data class HomeCategory(
    val id: Int = 0,
    val label: String = "All",
    val emoji: String = "🌐",
    val mediaType: String = "all",
    val trendingParams: Map<String, String> = emptyMap(),
    val popularParams: Map<String, String> = emptyMap(),
)

@Serializable
data class CastMember(
    val id: Int = 0,
    val name: String = "",
    val character: String = "",
    val photoUrl: String = "",
)

@Serializable
data class ReviewItem(
    val author: String = "Anonymous",
    val content: String = "",
    val rating: Double? = null,
    val avatarPath: String = "",
)

@Serializable
data class SeasonItem(
    val name: String = "Season",
    val seasonNumber: Int = 0,
    val episodeCount: Int = 0,
    val airDate: String = "",
    val posterPath: String = "",
    val overview: String = "",
)

@Serializable
data class EpisodeItem(
    val id: Int = 0,
    val name: String = "",
    val episodeNumber: Int = 0,
    val seasonNumber: Int = 0,
    val overview: String = "",
    val stillPath: String = "",
    val airDate: String = "",
    val runtime: Int = 0,
)

@Serializable
data class VideoServer(
    val name: String = "",
    val label: String = "",
    val icon: String = "▶",
    val movieUrlTemplate: String = "",
    val tvUrlTemplate: String = "",
) {
    fun buildUrl(item: MediaItem, season: Int? = null, episode: Int? = null): String {
        val id = item.id
        val type = item.type
        if (type == "tv" && season != null && episode != null) {
            return tvUrlTemplate
                .replace("{id}", id.toString())
                .replace("{season}", season.toString())
                .replace("{episode}", episode.toString())
        }
        return movieUrlTemplate.replace("{id}", id.toString())
    }
}

@Serializable
data class DownloadLink(
    val label: String = "",
    val url: String = "",
    val quality: String = "",
    val serverName: String = "",
    val serverIcon: String = "",
    val language: String = "",
    val fileSize: String = "",
    val notes: String = "",
)

@Serializable
data class PersonItem(
    val id: Int = 0,
    val name: String = "",
    val photoUrl: String = "",
    val biography: String = "",
    val birthday: String = "",
    val placeOfBirth: String = "",
    val knownForDepartment: String = "",
)

@Serializable
data class PromotedApp(
    val id: Int = 0,
    val name: String = "",
    val tagline: String = "",
    val description: String = "",
    val category: String = "Entertainment",
    val packageName: String = "",
    val playStoreUrl: String = "",
    val iconUrl: String = "",
    val bannerUrl: String = "",
    val rating: Double = 4.8,
    val downloads: String = "100K+",
    val badge: String = "",
    val sortOrder: Int = 0,
    val isFeatured: Boolean = false,
    val isActive: Boolean = true,
)
