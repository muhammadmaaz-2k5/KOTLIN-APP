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
                // Preload App Open, Interstitial, and Rewarded ads immediately on initialization
                AdManager.loadAppOpenAd(this@NazaaraboxApplication)
                AdManager.loadInterstitial(this@NazaaraboxApplication)
                AdManager.loadRewarded(this@NazaaraboxApplication)
            }
        } catch (e: Exception) {
            android.util.Log.e("AdMob", "Failed to initialize Google Mobile Ads", e)
        }

        createNotificationChannel()
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        android.util.Log.d("FCM", "Successfully subscribed to topic 'all'")
                    }
                }
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        android.util.Log.i("ENGORA_FCM", "=================================================")
                        android.util.Log.i("ENGORA_FCM", ">>> YOUR FCM DEVICE TOKEN: $token <<<")
                        android.util.Log.i("ENGORA_FCM", "=================================================")
                        RetrofitClient.registerFcmToken(token)
                    }
                }
        } catch (e: Exception) {
            android.util.Log.e("FCM", "FCM init error in Application: ${e.message}")
        }

        OneSignal.initWithContext(this, "9afbbec9-7155-4766-a78b-4e22e6f926d4")
        applicationScope.launch {
            OneSignal.Notifications.requestPermission(true)
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            val channel = android.app.NotificationChannel(channelId, channelName, android.app.NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Default channel for app notifications"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager?.createNotificationChannel(channel)
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
