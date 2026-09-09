package com.job2day.nazaarabox

import android.app.Activity
import android.app.Application
import android.os.Bundle
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.job2day.nazaarabox.data.api.RetrofitClient
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.utils.AdSettingsLoader
import com.onesignal.OneSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NazaaraboxApplication : Application(), SingletonImageLoader.Factory, Application.ActivityLifecycleCallbacks {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentActivity: Activity? = null
    private var startedActivityCount = 0

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        RetrofitClient.init(this)
        AdSettingsLoader.load(this)

        // Initialize Google Mobile Ads SDK synchronously on Main Thread
        try {
            val testDeviceIds = listOf(
                com.google.android.gms.ads.AdRequest.DEVICE_ID_EMULATOR,
            )
            val config = com.google.android.gms.ads.RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            com.google.android.gms.ads.MobileAds.setRequestConfiguration(config)
            com.google.android.gms.ads.MobileAds.initialize(this) { status ->
                android.util.Log.d("AdMob", "Google Mobile Ads initialized on Main thread: ${status.adapterStatusMap.keys}")
                // Preload App Open ad immediately on initialization
                AdManager.loadAppOpenAd(this@NazaaraboxApplication)
            }
        } catch (e: Exception) {
            android.util.Log.e("AdMob", "Failed to initialize Google Mobile Ads", e)
        }

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

    // --- ActivityLifecycleCallbacks for App Open Ads & Foreground Transitions ---

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
        startedActivityCount++
        if (startedActivityCount == 1 && !activity.isChangingConfigurations) {
            // App transitioned from background to foreground
            AdManager.onAppForegrounded(activity)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivityCount--
        if (startedActivityCount < 0) {
            startedActivityCount = 0
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) {
            currentActivity = null
        }
    }
}
