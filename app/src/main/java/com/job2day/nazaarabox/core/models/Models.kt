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
)

@Serializable
data class ThemedSection(
    val emoji: String = "",
    val title: String = "",
    val tmdbParams: Map<String, String> = emptyMap(),
    val mediaType: String = "movie",
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
data class TrailerItem(
    val key: String = "",
    val name: String = "Trailer",
    val type: String = "Trailer",
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
