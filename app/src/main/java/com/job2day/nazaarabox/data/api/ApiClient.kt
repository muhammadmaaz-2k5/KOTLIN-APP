package com.job2day.nazaarabox.data.api

object ApiClient {
    val apiService: ApiService get() = RetrofitClient.api
}
