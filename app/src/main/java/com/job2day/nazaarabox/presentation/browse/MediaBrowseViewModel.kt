package com.job2day.nazaarabox.presentation.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.job2day.nazaarabox.core.AnimeFilters
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.SearchFilters
import com.job2day.nazaarabox.services.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BrowseTab(
    val label: String,
    val sortBy: String,
    val genreId: Int? = null,
    val trending: Boolean = false,
    val animeMediaType: String? = null,
)

enum class BrowseMode { MOVIES, TV, ANIME }

data class BrowseUiState(
    val mode: BrowseMode = BrowseMode.MOVIES,
    val tabs: List<BrowseTab> = emptyList(),
    val selectedTab: Int = 0,
    val itemsByTab: Map<Int, List<MediaItem>> = emptyMap(),
    val pageByTab: Map<Int, Int> = emptyMap(),
    val totalPagesByTab: Map<Int, Int> = emptyMap(),
    val loadingByTab: Set<Int> = emptySet(),
    val fetchingMoreByTab: Set<Int> = emptySet(),
    val searchFilters: SearchFilters = SearchFilters(),
    val animeFilters: AnimeFilters = AnimeFilters(),
)

class MediaBrowseViewModel(
    private val mode: BrowseMode,
    private val repository: MediaRepository = MediaRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(BrowseUiState(mode = mode, tabs = tabsFor(mode)))
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    init {
        fetchPage(0, 1)
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        if (_uiState.value.itemsByTab[index].isNullOrEmpty()) fetchPage(index, 1)
    }

    fun updateSearchFilters(filters: SearchFilters) {
        _uiState.update { it.copy(searchFilters = filters) }
        reloadCurrentTab()
    }

    fun updateAnimeFilters(filters: AnimeFilters) {
        _uiState.update { it.copy(animeFilters = filters) }
        reloadCurrentTab()
    }

    fun loadMoreIfNeeded(lastVisibleIndex: Int) {
        val state = _uiState.value
        val tab = state.selectedTab
        val items = state.itemsByTab[tab].orEmpty()
        if (lastVisibleIndex < items.size - 4) return
        if (state.fetchingMoreByTab.contains(tab)) return
        val page = state.pageByTab[tab] ?: 1
        val total = state.totalPagesByTab[tab] ?: 1
        if (page < total) fetchPage(tab, page + 1)
    }

    private fun reloadCurrentTab() {
        val tab = _uiState.value.selectedTab
        _uiState.update {
            it.copy(
                itemsByTab = it.itemsByTab - tab,
                pageByTab = it.pageByTab - tab,
                totalPagesByTab = it.totalPagesByTab - tab,
            )
        }
        fetchPage(tab, 1)
    }

    private fun fetchPage(tabIndex: Int, page: Int) {
        val state = _uiState.value
        val tab = state.tabs[tabIndex]
        _uiState.update {
            it.copy(
                loadingByTab = if (page == 1) it.loadingByTab + tabIndex else it.loadingByTab,
                fetchingMoreByTab = if (page > 1) it.fetchingMoreByTab + tabIndex else it.fetchingMoreByTab,
            )
        }

        viewModelScope.launch {
            val mediaType = when (mode) {
                BrowseMode.MOVIES -> "movie"
                BrowseMode.TV -> "tv"
                BrowseMode.ANIME -> tab.animeMediaType ?: "movie"
            }

            val items: List<MediaItem>
            val totalPages: Int

            if (tab.trending && mode != BrowseMode.ANIME) {
                items = repository.trending(mediaType)
                totalPages = 1
            } else {
                val params = when (mode) {
                    BrowseMode.ANIME -> repository.buildAnimeParams(mediaType, state.animeFilters)
                    BrowseMode.MOVIES -> repository.buildBrowseParams("movie", tab.sortBy, tab.genreId, state.searchFilters)
                    BrowseMode.TV -> repository.buildBrowseParams("tv", tab.sortBy, tab.genreId, state.searchFilters, isTv = true)
                }
                val result = repository.discover(mediaType, params, page)
                var merged = result.first
                if (page == 1) {
                    val customParams = mutableMapOf("type" to mediaType)
                    if (mode == BrowseMode.ANIME) {
                        customParams["genre"] = "16"
                    } else {
                        tab.genreId?.let { customParams["genre"] = it.toString() }
                    }
                    val custom = repository.getCustomContent(customParams)
                    if (custom.isNotEmpty()) {
                        val ids = custom.map { it.id }.toSet()
                        merged = custom + merged.filterNot { ids.contains(it.id) }
                    }
                }
                items = merged
                totalPages = result.second
            }

            _uiState.update { current ->
                val existing = if (page == 1) emptyList() else current.itemsByTab[tabIndex].orEmpty()
                current.copy(
                    itemsByTab = current.itemsByTab + (tabIndex to (existing + items)),
                    pageByTab = current.pageByTab + (tabIndex to page),
                    totalPagesByTab = current.totalPagesByTab + (tabIndex to totalPages),
                    loadingByTab = current.loadingByTab - tabIndex,
                    fetchingMoreByTab = current.fetchingMoreByTab - tabIndex,
                )
            }
        }
    }

    companion object {
        fun tabsFor(mode: BrowseMode): List<BrowseTab> = when (mode) {
            BrowseMode.MOVIES -> listOf(
                BrowseTab("All", "popularity.desc"),
                BrowseTab("🔥 Trending", "popularity.desc", trending = true),
                BrowseTab("⭐ Top Rated", "vote_average.desc"),
                BrowseTab("🆕 New", "release_date.desc"),
                BrowseTab("👊 Action", "popularity.desc", genreId = 28),
                BrowseTab("😂 Comedy", "popularity.desc", genreId = 35),
                BrowseTab("😱 Horror", "popularity.desc", genreId = 27),
                BrowseTab("🌌 Sci-Fi", "popularity.desc", genreId = 878),
                BrowseTab("🎭 Drama", "popularity.desc", genreId = 18),
                BrowseTab("🪄 Fantasy", "popularity.desc", genreId = 14),
                BrowseTab("🔪 Thriller", "popularity.desc", genreId = 53),
                BrowseTab("💗 Romance", "popularity.desc", genreId = 10749),
                BrowseTab("🗺️ Adventure", "popularity.desc", genreId = 12),
            )
            BrowseMode.TV -> listOf(
                BrowseTab("All", "popularity.desc"),
                BrowseTab("🔥 Trending", "popularity.desc", trending = true),
                BrowseTab("⭐ Top Rated", "vote_average.desc"),
                BrowseTab("🎭 Drama", "popularity.desc", genreId = 18),
                BrowseTab("😂 Comedy", "popularity.desc", genreId = 35),
                BrowseTab("🔪 Crime", "popularity.desc", genreId = 80),
                BrowseTab("🌌 Sci-Fi", "popularity.desc", genreId = 10765),
                BrowseTab("👨‍👩‍👧 Family", "popularity.desc", genreId = 10751),
                BrowseTab("🔮 Mystery", "popularity.desc", genreId = 9648),
                BrowseTab("💗 Romance", "popularity.desc", genreId = 10749),
                BrowseTab("📺 Reality", "popularity.desc", genreId = 10764),
                BrowseTab("🗺️ Documentary", "popularity.desc", genreId = 99),
            )
            BrowseMode.ANIME -> listOf(
                BrowseTab("🎬 Movies", "popularity.desc", animeMediaType = "movie"),
                BrowseTab("📺 Shows", "popularity.desc", animeMediaType = "tv"),
            )
        }
    }
}
