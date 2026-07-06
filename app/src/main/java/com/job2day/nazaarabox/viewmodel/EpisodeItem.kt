package com.job2day.nazaarabox.viewmodel

import com.job2day.nazaarabox.model.EmbedServer
import kotlinx.serialization.Serializable

@Serializable
data class EpisodeItem(
    val number: Int,
    val title: String?,
    val url: String,
    val urls: List<EmbedServer>? = null
)
