package com.job2day.nazaarabox.model

import kotlinx.serialization.Serializable

@Serializable
data class EmbedServer(
    val name: String = "",
    val link: String = ""
)
