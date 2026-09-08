package com.job2day.nazaarabox.presentation.midnight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.job2day.nazaarabox.core.MidnightFeed
import com.job2day.nazaarabox.services.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MidnightUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val feed: MidnightFeed = MidnightFeed(),
    val selectedCategoryId: Int = 0,
    val errorMessage: String? = null,
)

class MidnightViewModel(
    private val repository: MediaRepository = MediaRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(MidnightUiState())
    val uiState: StateFlow<MidnightUiState> = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh) {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            } else {
                _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            }

            val feed = repository.getMidnightFeed(forceRefresh)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                feed = feed,
                errorMessage = if (feed.sections.isEmpty() && feed.featured.isEmpty()) "Failed to load Midnight streams" else null,
            )
        }
    }

    fun selectCategory(id: Int) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = id)
    }
}
