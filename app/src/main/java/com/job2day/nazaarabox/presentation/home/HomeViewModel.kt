package com.job2day.nazaarabox.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.job2day.nazaarabox.core.HomeCategory
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.services.MediaRepository
import com.job2day.nazaarabox.utils.AdManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoadingCategories: Boolean = true,
    val categories: List<HomeCategory> = emptyList(),
    val selectedCategoryIndex: Int = 0,
    val trendingByCategory: Map<Int, List<MediaItem>> = emptyMap(),
    val popularByCategory: Map<Int, List<MediaItem>> = emptyMap(),
    val loadingTrending: Set<Int> = emptySet(),
    val loadingPopular: Set<Int> = emptySet(),
    val nativeAds: List<Map<String, String>> = emptyList(),
    val buttonAds: List<Map<String, String>> = emptyList(),
)

class HomeViewModel(
    private val repository: MediaRepository = MediaRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadGlobalSettings()
        loadAds()
    }

    private fun loadGlobalSettings() {
        viewModelScope.launch {
            val settings = repository.getGlobalSettings()
            AdManager.setAdUnitIds(settings)
        }
    }

    private fun loadAds() {
        viewModelScope.launch {
            val nativeAds = repository.getNativeAds("home")
            val buttonAds = repository.getButtonAds("home")
            _uiState.update {
                it.copy(nativeAds = nativeAds, buttonAds = buttonAds)
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categories = repository.getCategories()
            _uiState.update {
                it.copy(categories = categories, isLoadingCategories = false)
            }
            if (categories.isNotEmpty()) fetchCategory(0)
        }
    }

    fun selectCategory(index: Int) {
        _uiState.update { it.copy(selectedCategoryIndex = index) }
        val state = _uiState.value
        if (state.trendingByCategory[index] == null || state.popularByCategory[index] == null) {
            fetchCategory(index)
        }
    }

    fun refresh() {
        val index = _uiState.value.selectedCategoryIndex
        _uiState.update {
            it.copy(
                trendingByCategory = it.trendingByCategory - index,
                popularByCategory = it.popularByCategory - index,
            )
        }
        fetchCategory(index)
        if (index != 0) {
            _uiState.update {
                it.copy(trendingByCategory = it.trendingByCategory - 0)
            }
            fetchCategory(0)
        }
    }

    private fun fetchCategory(index: Int) {
        val categories = _uiState.value.categories
        if (index !in categories.indices) return
        val category = categories[index]
        if (_uiState.value.loadingTrending.contains(index)) return

        _uiState.update {
            it.copy(
                loadingTrending = it.loadingTrending + index,
                loadingPopular = it.loadingPopular + index,
            )
        }

        viewModelScope.launch {
            val trendingDeferred = async { repository.getTrending(category) }
            val popularDeferred = async { repository.getPopular(category) }
            val customDeferred = async { fetchCustomForCategory(category) }

            var trending = trendingDeferred.await()
            var popular = popularDeferred.await()
            val custom = customDeferred.await()

            if (custom.isNotEmpty()) {
                val customIds = custom.map { it.id }.toSet()
                trending = custom + trending.filterNot { customIds.contains(it.id) }
                popular = (custom + popular.filterNot { customIds.contains(it.id) }).take(8)
            }

            _uiState.update {
                it.copy(
                    trendingByCategory = it.trendingByCategory + (index to trending),
                    popularByCategory = it.popularByCategory + (index to popular),
                    loadingTrending = it.loadingTrending - index,
                    loadingPopular = it.loadingPopular - index,
                )
            }
        }
    }

    private suspend fun fetchCustomForCategory(category: HomeCategory): List<MediaItem> {
        val params = mutableMapOf<String, String>()
        if (category.mediaType != "all") params["type"] = category.mediaType
        category.trendingParams["with_genres"]?.let { params["genre"] = it }
        return repository.getCustomContent(params)
    }
}
