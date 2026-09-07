package com.job2day.nazaarabox.data.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.job2day.nazaarabox.core.AppConfig
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val TAG = "RetrofitCache"
    private lateinit var apiService: ApiService

    fun init(context: Context) {
        if (::apiService.isInitialized) return

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        // 1. Setup 50MB Disk Cache
        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = Cache(cacheDir, 50L * 1024L * 1024L)

        // 2. Offline Interceptor: Read from cache up to 7 days if offline
        val offlineInterceptor = Interceptor { chain ->
            var request = chain.request()
            if (!isNetworkAvailable(context)) {
                val cacheControl = CacheControl.Builder()
                    .maxStale(7, TimeUnit.DAYS)
                    .build()
                request = request.newBuilder()
                    .cacheControl(cacheControl)
                    .build()
                Log.d(TAG, "[OFFLINE CACHE LOOKUP] ${request.url}")
            }
            chain.proceed(request)
        }

        // 3. Network Cache Interceptor: Inspect Cache Hit vs Miss and preserve TTL
        val networkInterceptor = Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)

            val serverCacheHeader = response.header("X-Cache") ?: "N/A"
            val isClientCacheHit = response.cacheResponse != null

            if (isClientCacheHit) {
                Log.d(TAG, "[CLIENT CACHE HIT] Served from OkHttp disk: ${request.url}")
            } else {
                Log.d(TAG, "[NETWORK FETCH] ${request.url} (Server X-Cache: $serverCacheHeader)")
            }

            // Ensure response has valid Cache-Control for OkHttp cache storage
            val cacheControlHeader = response.header("Cache-Control")
            if (cacheControlHeader.isNullOrBlank() || cacheControlHeader.contains("no-store")) {
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=300")
                    .build()
            } else {
                response
            }
        }

        val client = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(offlineInterceptor)
            .addNetworkInterceptor(networkInterceptor)
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(ensureTrailingSlash(AppConfig.backendBaseUrl))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val api: ApiService
        get() = apiService

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun ensureTrailingSlash(url: String): String =
        if (url.endsWith("/")) url else "$url/"
}

