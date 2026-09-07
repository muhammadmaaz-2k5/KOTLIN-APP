package com.job2day.nazaarabox.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.SearchFilters
import com.job2day.nazaarabox.services.MediaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val selectedType: String = "all",
    val isLoading: Boolean = false,
    val results: List<MediaItem> = emptyList(),
    val filters: SearchFilters = SearchFilters(),
    val isGridView: Boolean = true,
    val recentSearches: List<String> = emptyList(),
    val trendingItems: List<MediaItem> = emptyList(),
    val isLoadingTrending: Boolean = false,
)

class SearchViewModel(
    private val repository: MediaRepository = MediaRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var debounceJob: Job? = null

    val suggestions = listOf(
        "🔥 Trending", "🦸 Avengers", "🧙 Harry Potter", "🚀 Interstellar",
        "🕷️ Spider-Man", "🦁 The Lion King", "🎭 Breaking Bad", "🤖 Transformers",
        "🧟 The Walking Dead", "🧊 Game of Thrones",
    )

    init {
        loadTrending()
    }

    fun loadTrending() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTrending = true) }
            val movies = repository.trending("movie").take(10)
            val tv = repository.trending("tv").take(10)
            val combined = (movies + tv).shuffled()
            _uiState.update {
                it.copy(
                    trendingItems = combined,
                    isLoadingTrending = false,
                )
            }
        }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

    fun clearQuery() {
        _uiState.update { it.copy(query = "", results = emptyList(), isLoading = false) }
        debounceJob?.cancel()
    }

    fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        _uiState.update { current ->
            val updated = (listOf(trimmed) + current.recentSearches.filterNot { it.equals(trimmed, ignoreCase = true) }).take(8)
            current.copy(recentSearches = updated)
        }
    }

    fun removeRecentSearch(query: String) {
        _uiState.update { current ->
            current.copy(recentSearches = current.recentSearches.filterNot { it.equals(query, ignoreCase = true) })
        }
    }

    fun clearRecentSearches() {
        _uiState.update { it.copy(recentSearches = emptyList()) }
    }

    fun onQueryChanged(value: String) {
        _uiState.update { it.copy(query = value) }
        debounceJob?.cancel()
        if (value.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isLoading = false) }
            return
        }
        debounceJob = viewModelScope.launch {
            delay(400)
            search(value.trim())
        }
    }

    fun setType(type: String) {
        _uiState.update { it.copy(selectedType = type) }
        val query = _uiState.value.query
        if (query.isNotBlank()) search(query)
    }

    fun applyFilters(filters: SearchFilters) {
        _uiState.update { it.copy(filters = filters) }
        val query = _uiState.value.query
        if (query.isNotBlank()) search(query)
    }

    fun selectGenre(genre: String) {
        val updatedFilters = _uiState.value.filters.copy(genre = genre)
        _uiState.update { it.copy(filters = updatedFilters) }
        val query = _uiState.value.query.ifBlank { genre }
        _uiState.update { it.copy(query = query) }
        search(query)
    }

    fun onSuggestionTap(suggestion: String) {
        val clean = suggestion.replace(Regex("^[^\\p{L}\\p{N}]+"), "").trim().ifBlank { suggestion }
        _uiState.update { it.copy(query = clean) }
        search(clean)
    }

    private fun search(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value
            val results = repository.search(query, state.selectedType, state.filters)
            val sorted = sortResults(results, state.filters.sortBy)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    results = sorted,
                )
            }
            if (sorted.isNotEmpty()) {
                addRecentSearch(query)
            }
        }
    }

    private fun sortResults(items: List<MediaItem>, sortBy: String): List<MediaItem> = when (sortBy) {
        "Rating" -> items.sortedByDescending { it.rating }
        "Latest" -> items.sortedByDescending { it.year }
        else -> items.sortedByDescending { it.popularity }
    }
}
