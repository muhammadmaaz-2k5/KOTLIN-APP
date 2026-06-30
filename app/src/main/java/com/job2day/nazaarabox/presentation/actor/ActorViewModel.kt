package com.job2day.nazaarabox.presentation.actor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.PersonItem
import com.job2day.nazaarabox.services.MediaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActorUiState(
    val isLoading: Boolean = true,
    val person: PersonItem? = null,
    val credits: List<MediaItem> = emptyList(),
    val knownFor: List<MediaItem> = emptyList(),
    val isBioExpanded: Boolean = false,
    val showAllFilmography: Boolean = false,
)

class ActorViewModel(
    private val repository: MediaRepository = MediaRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActorUiState())
    val uiState: StateFlow<ActorUiState> = _uiState.asStateFlow()

    fun load(personId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val personDeferred = async { repository.getPerson(personId) }
            val creditsDeferred = async { repository.getPersonCredits(personId) }
            val credits = creditsDeferred.await()
            val knownFor = credits
                .sortedByDescending { it.voteCount }
                .take(6)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    person = personDeferred.await(),
                    credits = credits,
                    knownFor = knownFor,
                )
            }
        }
    }

    fun toggleBio() {
        _uiState.update { it.copy(isBioExpanded = !it.isBioExpanded) }
    }

    fun toggleFilmography() {
        _uiState.update { it.copy(showAllFilmography = !it.showAllFilmography) }
    }
}
