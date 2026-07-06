package com.job2day.nazaarabox.data

import kotlinx.serialization.Serializable

@Serializable
data class DownloadingDramaDetailResponse(
    val success: Boolean,
    val message: String? = null,
    val data: DownloadingDramaDetailData? = null
)

@Serializable
data class DownloadingDramaDetailData(
    val id: Int,
    val title: String,
    val slug: String
)
