package com.job2day.nazaarabox.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.ThemedSection
import com.job2day.nazaarabox.services.MediaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeSectionsUiState(
    val customExclusives: List<MediaItem> = emptyList(),
    val isLoadingCustom: Boolean = true,
    val sectionPreviews: Map<Int, List<MediaItem>> = emptyMap(),
    val sectionLoading: Map<Int, Boolean> = emptyMap(),
)

class HomeSectionsViewModel(
    private val repository: MediaRepository = MediaRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeSectionsUiState())
    val uiState: StateFlow<HomeSectionsUiState> = _uiState.asStateFlow()

    val sections: List<ThemedSection> = buildSections()

    init {
        loadCustomExclusives()
        sections.forEachIndexed { index, _ ->
            viewModelScope.launch {
                delay((index + 1) * 120L)
                loadSectionPreview(index)
            }
        }
    }

    private fun loadCustomExclusives() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCustom = true) }
            val items = repository.getCustomContent(mapOf("limit" to "15"))
            _uiState.update { it.copy(customExclusives = items, isLoadingCustom = false) }
        }
    }

    private fun loadSectionPreview(index: Int) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(sectionLoading = state.sectionLoading + (index to true))
            }
            val section = sections[index]
            val params = section.tmdbParams.toMutableMap().apply {
                put("page", "1")
                put("include_adult", "false")
            }
            val (items, _) = if (section.mediaType == "tv") {
                repository.discover("tv", params)
            } else {
                repository.discoverMovies(params)
            }
            _uiState.update { state ->
                state.copy(
                    sectionPreviews = state.sectionPreviews + (index to items.take(10)),
                    sectionLoading = state.sectionLoading + (index to false),
                )
            }
        }
    }

    companion object {
        fun buildSections(): List<ThemedSection> {
            val today = LocalDate.now().toString()
            return listOf(
                ThemedSection("🔥", "Trending in Cinema", mapOf("sort_by" to "popularity.desc")),
                ThemedSection(
                    emoji = "📅",
                    title = "Upcoming",
                    tmdbParams = mapOf(
                        "release_date.gte" to today,
                        "sort_by" to "release_date.asc",
                    ),
                ),
                ThemedSection("🏆", "Top 20 Movies", mapOf("sort_by" to "vote_average.desc", "vote_count.gte" to "500")),
                ThemedSection("🎬", "Movies New Release", mapOf("sort_by" to "release_date.desc", "release_date.lte" to today)),
                ThemedSection("👊", "One-Person Army Action", mapOf("with_genres" to "28", "sort_by" to "popularity.desc")),
                ThemedSection("🪄", "Fantastic Adventure Journey", mapOf("with_genres" to "12,14", "sort_by" to "popularity.desc")),
                ThemedSection("🧗", "Adventure Unfolded", mapOf("with_genres" to "12", "sort_by" to "vote_average.desc", "vote_count.gte" to "200")),
                ThemedSection("🪦", "Tomb Tales", mapOf("with_genres" to "27,9648", "sort_by" to "popularity.desc")),
                ThemedSection("🦸", "Super Hero", mapOf("with_keywords" to "9715", "sort_by" to "popularity.desc")),
                ThemedSection("🔭", "Sci-Fi Future", mapOf("with_genres" to "878", "sort_by" to "popularity.desc")),
                ThemedSection(
                    emoji = "🇰🇷",
                    title = "KDrama TV Shows",
                    tmdbParams = mapOf(
                        "with_original_language" to "ko",
                        "sort_by" to "popularity.desc",
                    ),
                    mediaType = "tv",
                ),
                ThemedSection("😀", "Laugh Out Loud", mapOf("with_genres" to "35", "sort_by" to "popularity.desc")),
                ThemedSection("📈", "Most Trending", mapOf("sort_by" to "popularity.desc", "vote_count.gte" to "100")),
            )
        }
    }
}
