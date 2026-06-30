package com.job2day.nazaarabox.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.job2day.nazaarabox.core.CastMember
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.ReviewItem
import com.job2day.nazaarabox.core.SeasonItem
import com.job2day.nazaarabox.core.TrailerItem
import com.job2day.nazaarabox.services.MediaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val item: MediaItem? = null,
    val cast: List<CastMember> = emptyList(),
    val similar: List<MediaItem> = emptyList(),
    val trailers: List<TrailerItem> = emptyList(),
    val reviews: List<ReviewItem> = emptyList(),
    val seasons: List<SeasonItem> = emptyList(),
    val isOverviewExpanded: Boolean = false,
)

class DetailViewModel(
    private val repository: MediaRepository = MediaRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun load(initial: MediaItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, item = initial) }
            val detail = repository.loadDetail(initial)
            val castDeferred = async { repository.getCast(detail) }
            val similarDeferred = async { repository.getSimilar(detail) }
            val trailersDeferred = async { repository.getTrailers(detail) }
            val reviewsDeferred = async { repository.getReviews(detail) }
            val seasonsDeferred = async { repository.getSeasons(detail) }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    item = detail,
                    cast = castDeferred.await(),
                    similar = similarDeferred.await(),
                    trailers = trailersDeferred.await(),
                    reviews = reviewsDeferred.await(),
                    seasons = seasonsDeferred.await(),
                )
            }
        }
    }

    fun toggleOverview() {
        _uiState.update { it.copy(isOverviewExpanded = !it.isOverviewExpanded) }
    }
}
