package com.job2day.nazaarabox

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.job2day.nazaarabox.data.api.RetrofitClient
import com.job2day.nazaarabox.utils.AdSettingsLoader
import com.onesignal.OneSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NazaaraboxApplication : Application(), SingletonImageLoader.Factory {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
        AdSettingsLoader.load(this)
        OneSignal.initWithContext(this, "9afbbec9-7155-4766-a78b-4e22e6f926d4")
        applicationScope.launch {
            OneSignal.Notifications.requestPermission(true)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }
}
