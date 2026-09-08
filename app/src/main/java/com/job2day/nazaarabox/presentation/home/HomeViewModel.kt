package com.job2day.nazaarabox.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.job2day.nazaarabox.core.HomeCategory
import com.job2day.nazaarabox.core.HomeFeed
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.ThemedSection
import com.job2day.nazaarabox.services.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val categories: List<HomeCategory> = emptyList(),
    val selectedCategoryIndex: Int = 0,
    val featured: List<MediaItem> = emptyList(),
    val trending: List<MediaItem> = emptyList(),
    val popular: List<MediaItem> = emptyList(),
    val customExclusives: List<MediaItem> = emptyList(),
    val sections: List<ThemedSection> = emptyList(),
    val isCategoryLoading: Boolean = false,
)

class HomeViewModel(
    private val repository: MediaRepository = MediaRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val categoryFeeds = mutableMapOf<Int, HomeFeed>()

    init {
        loadFeed(categoryId = 0, forceRefresh = false)
    }

    fun selectCategory(index: Int) {
        val categories = _uiState.value.categories
        if (index !in categories.indices || index == _uiState.value.selectedCategoryIndex) return

        _uiState.update { it.copy(selectedCategoryIndex = index) }
        val category = categories[index]
        val catId = category.id

        // If cached in memory, display immediately
        categoryFeeds[catId]?.let { cached ->
            applyFeed(cached)
            return
        }

        // Fetch category-specific feed
        viewModelScope.launch {
            _uiState.update { it.copy(isCategoryLoading = true) }
            val feed = repository.getHomeFeed(catId, forceRefresh = false)
            if (feed.trending.isNotEmpty() || feed.popular.isNotEmpty()) {
                categoryFeeds[catId] = feed
                applyFeed(feed)
            } else {
                fallbackCategoryFetch(category, index)
            }
            _uiState.update { it.copy(isCategoryLoading = false) }
        }
    }

    fun refresh() {
        val categories = _uiState.value.categories
        val selected = _uiState.value.selectedCategoryIndex
        val catId = categories.getOrNull(selected)?.id ?: 0
        loadFeed(categoryId = catId, forceRefresh = true)
    }

    private fun loadFeed(categoryId: Int, forceRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = forceRefresh) }

            val feed = repository.getHomeFeed(categoryId, forceRefresh)
            if (feed.categories.isNotEmpty() || feed.trending.isNotEmpty() || feed.sections.isNotEmpty()) {
                categoryFeeds[categoryId] = feed
                applyFeed(feed)
            } else {
                loadFallbackCategories()
            }

            _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    private fun applyFeed(feed: HomeFeed) {
        _uiState.update { current ->
            val categories = if (feed.categories.isNotEmpty()) feed.categories else current.categories
            val trending = if (feed.trending.isNotEmpty()) feed.trending else current.trending
            val featured = if (feed.featured.isNotEmpty()) feed.featured else trending.take(5)
            val popular = if (feed.popular.isNotEmpty()) feed.popular else current.popular
            val sections = if (feed.sections.isNotEmpty()) feed.sections else current.sections
            val custom = if (feed.customExclusives.isNotEmpty()) feed.customExclusives else current.customExclusives

            current.copy(
                categories = categories,
                featured = featured,
                trending = trending,
                popular = popular,
                sections = sections,
                customExclusives = custom,
                isLoading = false,
            )
        }
    }

    private suspend fun loadFallbackCategories() {
        val cats = repository.getCategories()
        _uiState.update { it.copy(categories = cats, isLoading = false) }
        if (cats.isNotEmpty()) {
            val trending = repository.getTrending(cats[0])
            val popular = repository.getPopular(cats[0])
            _uiState.update { it.copy(trending = trending, popular = popular, featured = trending.take(5)) }
        }
    }

    private suspend fun fallbackCategoryFetch(category: HomeCategory, index: Int) {
        val trending = repository.getTrending(category)
        val popular = repository.getPopular(category)
        _uiState.update { current ->
            current.copy(
                trending = trending,
                popular = popular,
                featured = if (trending.isNotEmpty()) trending.take(5) else current.featured,
            )
        }
    }
}
