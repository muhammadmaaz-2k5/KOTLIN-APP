package com.job2day.nazaarabox.utils

import android.app.Application
import android.util.Log
import com.job2day.nazaarabox.data.api.RetrofitClient
import com.job2day.nazaarabox.services.MediaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AdSettingsLoader {
    private const val TAG = "AdSettingsLoader"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun load(application: Application) {
        scope.launch {
            try {
                RetrofitClient.init(application)
                val settings = MediaRepository().getGlobalSettings()
                withContext(Dispatchers.Main) {
                    AdManager.applySettings(settings)
                }
                Log.d(TAG, "Remote ad settings loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load remote ad settings, using defaults", e)
            }
        }
    }
}
