package com.job2day.nazaarabox.utils

import com.job2day.nazaarabox.viewmodel.EpisodeItem

object PlaylistManager {
    private var playlist: List<EpisodeItem> = emptyList()
    private var currentSeason: Int = 1

    fun setPlaylist(newPlaylist: List<EpisodeItem>) {
        playlist = newPlaylist
    }

    fun getPlaylist(): List<EpisodeItem> {
        return playlist
    }

    fun updateSeason(season: Int) {
        currentSeason = season
    }

    fun getSeason(): Int {
        return currentSeason
    }
}
