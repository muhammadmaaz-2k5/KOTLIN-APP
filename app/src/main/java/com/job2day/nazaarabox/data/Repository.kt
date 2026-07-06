package com.job2day.nazaarabox.data

import com.job2day.nazaarabox.data.api.ApiService
import retrofit2.Response

class Repository(private val apiService: ApiService) {
    suspend fun getDownloadingDramaDetail(dramaSlug: String): Response<DownloadingDramaDetailResponse> {
        return Response.success(DownloadingDramaDetailResponse(success = true, data = null))
    }
}
