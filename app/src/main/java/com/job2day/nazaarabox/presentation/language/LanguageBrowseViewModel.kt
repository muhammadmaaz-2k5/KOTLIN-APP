package com.job2day.nazaarabox.presentation.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.job2day.nazaarabox.core.LanguageOption
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.services.MediaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LanguageBrowseUiState(
    val selectedLangIndex: Int = 0,
    val selectedType: String = "all",
    val moviesByLang: Map<Int, List<MediaItem>> = emptyMap(),
    val tvByLang: Map<Int, List<MediaItem>> = emptyMap(),
    val loadingMovies: Set<Int> = emptySet(),
    val loadingTv: Set<Int> = emptySet(),
)

class LanguageBrowseViewModel(
    private val repository: MediaRepository = MediaRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(LanguageBrowseUiState())
    val uiState: StateFlow<LanguageBrowseUiState> = _uiState.asStateFlow()

    val languages: List<LanguageOption> = defaultLanguages()

    init {
        fetchForLang(0)
    }

    fun selectLanguage(index: Int) {
        _uiState.update { it.copy(selectedLangIndex = index) }
        fetchForLang(index)
    }

    fun selectType(type: String) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun currentItems(): List<MediaItem> {
        val state = _uiState.value
        val movies = state.moviesByLang[state.selectedLangIndex].orEmpty()
        val tv = state.tvByLang[state.selectedLangIndex].orEmpty()
        return when (state.selectedType) {
            "movie" -> movies
            "tv" -> tv
            else -> {
                val all = mutableListOf<MediaItem>()
                val len = maxOf(movies.size, tv.size)
                for (i in 0 until len) {
                    if (i < movies.size) all.add(movies[i])
                    if (i < tv.size) all.add(tv[i])
                }
                all
            }
        }
    }

    fun isLoading(): Boolean {
        val state = _uiState.value
        val idx = state.selectedLangIndex
        return state.loadingMovies.contains(idx) || state.loadingTv.contains(idx)
    }

    private fun fetchForLang(index: Int) {
        val state = _uiState.value
        if (state.moviesByLang.containsKey(index) && state.tvByLang.containsKey(index)) return
        val code = languages[index].code
        viewModelScope.launch {
            _uiState.update {
                it.copy(loadingMovies = it.loadingMovies + index, loadingTv = it.loadingTv + index)
            }
            val moviesDeferred = async { repository.discoverByLanguage(code, "movie") }
            val tvDeferred = async { repository.discoverByLanguage(code, "tv") }
            val movies = moviesDeferred.await()
            val tv = tvDeferred.await()
            _uiState.update {
                it.copy(
                    moviesByLang = it.moviesByLang + (index to movies),
                    tvByLang = it.tvByLang + (index to tv),
                    loadingMovies = it.loadingMovies - index,
                    loadingTv = it.loadingTv - index,
                )
            }
        }
    }

    companion object {
        fun defaultLanguages(): List<LanguageOption> = listOf(
            LanguageOption("en", "English", "🇺🇸", "English", "both", 0xFF0984E3),
            LanguageOption("hi", "Hindi", "🇮🇳", "हिन्दी", "both", 0xFFFF6B35),
            LanguageOption("pa", "Punjabi", "🎵", "ਪੰਜਾਬੀ", "movie", 0xFFFFBC00),
            LanguageOption("ko", "Korean", "🇰🇷", "한국어", "tv", 0xFFFF4B9E),
            LanguageOption("ja", "Japanese", "🇯🇵", "日本語", "both", 0xFFE84393),
            LanguageOption("zh", "Chinese", "🇨🇳", "中文", "both", 0xFFEE2A2A),
            LanguageOption("es", "Spanish", "🇪🇸", "Español", "both", 0xFFF39C12),
            LanguageOption("fr", "French", "🇫🇷", "Français", "both", 0xFF4834D4),
            LanguageOption("tr", "Turkish", "🇹🇷", "Türkçe", "tv", 0xFFE74C3C),
            LanguageOption("ar", "Arabic", "🇸🇦", "العربية", "both", 0xFF00B894),
            LanguageOption("ta", "Tamil", "🎞️", "தமிழ்", "movie", 0xFF6C5CE7),
            LanguageOption("te", "Telugu", "🎥", "తెలుగు", "movie", 0xFFBD5AF4),
            LanguageOption("ml", "Malayalam", "🌴", "മലയാളം", "movie", 0xFF00CEC9),
            LanguageOption("de", "German", "🇩🇪", "Deutsch", "both", 0xFF636E72),
            LanguageOption("pt", "Portuguese", "🇵🇹", "Português", "both", 0xFF55A630),
            LanguageOption("ru", "Russian", "🇷🇺", "Русский", "both", 0xFF1E88E5),
            LanguageOption("th", "Thai", "🇹🇭", "ภาษาไทย", "both", 0xFF6AB04C),
            LanguageOption("id", "Indonesian", "🇮🇩", "Bahasa", "both", 0xFFEB4D4B),
            LanguageOption("ur", "Urdu", "🇵🇰", "اردو", "movie", 0xFF01CBC6),
            LanguageOption("bn", "Bengali", "🇧🇩", "বাংলা", "movie", 0xFFF9CA24),
        )
    }
}
