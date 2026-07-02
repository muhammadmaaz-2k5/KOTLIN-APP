package com.job2day.nazaarabox.data.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface ApiService {
    @GET
    suspend fun getJson(@Url url: String, @QueryMap params: Map<String, String> = emptyMap()): JsonObject

    @GET
    suspend fun getJsonArray(@Url url: String, @QueryMap params: Map<String, String> = emptyMap()): JsonArray

    @GET("api/config/categories")
    suspend fun getCategories(): JsonArray

    @GET("api/config/settings")
    suspend fun getGlobalSettings(): JsonObject

    @GET("api/config/native-ads")
    suspend fun getNativeAds(@Query("screen") screen: String): JsonArray

    @GET("api/config/button-ads")
    suspend fun getButtonAds(@Query("screen") screen: String): JsonArray

    @GET("api/config/servers")
    suspend fun getServers(
        @Query("id") id: Int,
        @Query("type") type: String,
        @Query("season") season: String = "",
        @Query("episode") episode: String = "",
    ): JsonArray

    @GET("api/custom-content")
    suspend fun getCustomContent(@QueryMap params: Map<String, String>): JsonArray

    @GET("api/download-links/{type}/{id}")
    suspend fun getDownloadLinks(
        @Path("type") type: String,
        @Path("id") id: Int,
        @Query("season") season: Int? = null,
        @Query("episode") episode: Int? = null,
    ): JsonArray

    @GET
    suspend fun tmdb(@Url url: String, @QueryMap params: Map<String, String>): JsonObject
}
