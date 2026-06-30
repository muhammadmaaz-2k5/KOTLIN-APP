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
            _uiState.update {
                it.copy(
                    isLoading = false,
                    results = sortResults(results, state.filters.sortBy),
                )
            }
        }
    }

    private fun sortResults(items: List<MediaItem>, sortBy: String): List<MediaItem> = when (sortBy) {
        "Rating" -> items.sortedByDescending { it.rating }
        "Latest" -> items.sortedByDescending { it.year }
        else -> items.sortedByDescending { it.popularity }
    }
}
