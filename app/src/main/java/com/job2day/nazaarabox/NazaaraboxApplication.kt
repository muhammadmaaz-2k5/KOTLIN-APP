package com.job2day.nazaarabox

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.job2day.nazaarabox.data.api.RetrofitClient
import com.job2day.nazaarabox.utils.AdSettingsLoader

class NazaaraboxApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
        AdSettingsLoader.load(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }
}
