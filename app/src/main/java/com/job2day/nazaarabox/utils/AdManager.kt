package com.job2day.nazaarabox.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdManager {
    private const val TAG = "AdManager"
    private const val INTERSTITIAL_COOLDOWN_MS = 30_000L
    private const val MAX_INTERSTITIALS_PER_SESSION = 10
    private const val APP_OPEN_COOLDOWN_MS = 180_000L // 3 minutes cooldown between App Open ads
    private const val FOUR_HOURS_MS = 4 * 3600_000L // App open ads expire after 4 hours per AdMob policy

    // Google Official Sample Test Ad Unit IDs (Safe for testing on productions & release APKs)
    const val FORCE_TEST_ADS = true
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_REWARDED_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/5354046379"
    const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"
    const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"

    const val DEFAULT_WEBVIEW_AD_URL = "https://thereviewepisode.com"

    var isAdsEnabled: Boolean = true
        private set

    var isAdMobEnabled: Boolean = true
        private set

    var isWebviewAdsEnabled: Boolean = true
        private set

    var admobBannerId: String = TEST_BANNER_ID
        private set

    var admobInterstitialId: String = TEST_INTERSTITIAL_ID
        private set

    var admobRewardedId: String = TEST_REWARDED_ID
        private set

    var admobRewardedInterstitialId: String = TEST_REWARDED_INTERSTITIAL_ID
        private set

    var admobAppOpenId: String = TEST_APP_OPEN_ID
        private set

    var admobNativeId: String = TEST_NATIVE_ID
        private set

    var webviewAdUrl: String = DEFAULT_WEBVIEW_AD_URL
        private set

    val popupWebviewUrl: String
        get() = webviewAdUrl

    var isSafeMode: Boolean = false
        private set

    var appMode: String = "live"
        private set

    val isLiveMode: Boolean
        get() = appMode == "live"

    val isSafeReviewMode: Boolean
        get() = appMode == "safe_review"

    var isShowingAd = false
        private set

    var isPlayerActive: Boolean = false

    var isSplashFinished: Boolean = false

    @Volatile
    private var lastAppOpenShownAt: Long = 0L

    @Volatile
    private var appOpenLoadTime: Long = 0L

    @Volatile
    private var lastInterstitialAt: Long = 0L

    @Volatile
    private var interstitialCount: Int = 0

    private val _showInterstitial = MutableStateFlow(false)
    val showInterstitial: StateFlow<Boolean> = _showInterstitial.asStateFlow()

    private var pendingDismissCallback: (() -> Unit)? = null
    private var rawSettings: Map<String, String> = emptyMap()

    // AdMob Instances
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenAdLoading = false

    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var isRewardedInterstitialLoading = false

    fun applySettings(settings: Map<String, String>) {
        rawSettings = settings
        isAdsEnabled = if (settings.containsKey("ads_enabled")) parseBoolean(settings["ads_enabled"]) else true
        isAdMobEnabled = if (settings.containsKey("admob_enabled")) parseBoolean(settings["admob_enabled"]) else true
        isWebviewAdsEnabled = if (settings.containsKey("enable_webview_ads")) parseBoolean(settings["enable_webview_ads"]) else true

        if (FORCE_TEST_ADS) {
            admobBannerId = TEST_BANNER_ID
            admobInterstitialId = TEST_INTERSTITIAL_ID
            admobRewardedId = TEST_REWARDED_ID
            admobRewardedInterstitialId = TEST_REWARDED_INTERSTITIAL_ID
            admobAppOpenId = TEST_APP_OPEN_ID
            admobNativeId = TEST_NATIVE_ID
        } else {
            admobBannerId = settings["admob_banner_id"]?.takeIf { it.isNotBlank() } ?: TEST_BANNER_ID
            admobInterstitialId = settings["admob_interstitial_id"]?.takeIf { it.isNotBlank() } ?: TEST_INTERSTITIAL_ID
            admobRewardedId = settings["admob_rewarded_id"]?.takeIf { it.isNotBlank() } ?: TEST_REWARDED_ID
            admobRewardedInterstitialId = settings["admob_rewarded_interstitial_id"]?.takeIf { it.isNotBlank() } ?: TEST_REWARDED_INTERSTITIAL_ID
            admobAppOpenId = settings["admob_app_open_id"]?.takeIf { it.isNotBlank() } ?: TEST_APP_OPEN_ID
            admobNativeId = settings["admob_native_id"]?.takeIf { it.isNotBlank() } ?: TEST_NATIVE_ID
        }

        val remoteUrl = settings["webview_ad_url"]?.trim()?.takeIf { it.isNotBlank() }
        webviewAdUrl = sanitizeAdUrl(remoteUrl)
        val modeValue = settings["app_mode"]?.trim()?.lowercase()
        appMode = if (modeValue == "live") "live" else if (modeValue == "safe_review") "safe_review" else "live"
        isSafeMode = appMode == "safe_review"

        Log.d(
            TAG,
            "Settings applied: ads=$isAdsEnabled, admob=$isAdMobEnabled, webview=$isWebviewAdsEnabled, appMode=$appMode, forceTestAds=$FORCE_TEST_ADS",
        )
    }

    fun sanitizeAdUrl(rawUrl: String?): String {
        val trimmed = rawUrl?.trim() ?: ""
        if (trimmed.isBlank()) return DEFAULT_WEBVIEW_AD_URL
        if (trimmed.contains("nazaarabox.com", ignoreCase = true) ||
            trimmed.contains("onlineviewer.net", ignoreCase = true)
        ) {
            return DEFAULT_WEBVIEW_AD_URL
        }
        return trimmed
    }

    fun isAdPlacementEnabled(placement: String): Boolean {
        if (FORCE_TEST_ADS) return true
        if (!isAdsEnabled) return false
        val specificToggle = rawSettings["enable_ad_$placement"]
        if (specificToggle != null) {
            return parseBoolean(specificToggle)
        }
        val basePlacement = when {
            placement == "app_open" -> "app_open"
            placement.endsWith("_native") || placement.startsWith("native_") -> "native_ads"
            placement.startsWith("home_") -> "home_banner"
            placement.startsWith("detail_") -> "detail_banner"
            placement.startsWith("actor_") -> "actor_banner"
            placement.startsWith("browse_") -> "browse_banner"
            placement.startsWith("search_") -> "search_banner"
            placement.startsWith("season_") -> "season_banner"
            placement.startsWith("category_") -> "category_banner"
            placement.startsWith("seeall_") -> "seeall_banner"
            placement.startsWith("midnight_") -> "midnight_banner"
            placement.startsWith("player_") -> "player_banner"
            placement.startsWith("moreapps_") -> "moreapps_banner"
            placement.startsWith("language_") -> "language_banner"
            placement.startsWith("privacy_") -> "privacy_banner"
            else -> null
        }
        if (basePlacement != null) {
            val baseToggle = rawSettings["enable_ad_$basePlacement"]
            if (baseToggle != null) {
                return parseBoolean(baseToggle)
            }
        }
        return true
    }

    fun getAdPlacementUrl(placement: String): String {
        val specificUrl = rawSettings["ad_url_$placement"]?.trim()
        val effective = if (!specificUrl.isNullOrBlank()) specificUrl else webviewAdUrl
        return sanitizeAdUrl(effective)
    }

    /** @deprecated Use [applySettings] instead */
    fun setAdUnitIds(settings: Map<String, String>) {
        applySettings(settings)
    }

    private fun parseBoolean(value: String?): Boolean {
        if (value == null) return true
        return when (value.trim().lowercase()) {
            "false", "0", "no", "off" -> false
            else -> true
        }
    }

    fun initialize(context: Context) {
        if (!isAdsEnabled) return
        if (isAdMobEnabled) {
            loadInterstitial(context)
            loadRewarded(context)
            loadAppOpenAd(context)
            loadRewardedInterstitial(context)
        }
    }

    @Synchronized
    fun canShowInterstitial(): Boolean {
        if (!isAdsEnabled) return false
        if (interstitialCount >= MAX_INTERSTITIALS_PER_SESSION) return false
        val now = System.currentTimeMillis()
        return (now - lastInterstitialAt > INTERSTITIAL_COOLDOWN_MS)
    }

    @Synchronized
    fun recordInterstitial() {
        lastInterstitialAt = System.currentTimeMillis()
        interstitialCount++
    }

    fun isInterstitialAdReady(): Boolean =
        (isAdMobEnabled && interstitialAd != null) || (isWebviewAdsEnabled && webviewAdUrl.isNotBlank())

    fun isAdMobInterstitialReady(): Boolean = isAdMobEnabled && interstitialAd != null

    // --- AdMob Interstitial ---

    fun loadInterstitial(context: Context) {
        if (!isAdsEnabled || !isAdMobEnabled) return
        if (interstitialAd != null || isInterstitialLoading) return

        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            admobInterstitialId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "AdMob Test Interstitial loaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.d(TAG, "AdMob Test Interstitial failed: ${loadAdError.message}")
                }
            }
        )
    }

    fun showAdMobInterstitialOnly(activity: Activity, onAdDismissed: () -> Unit) {
        if (!isAdsEnabled || !isAdMobEnabled) {
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            isShowingAd = true
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    isShowingAd = false
                    loadInterstitial(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    isShowingAd = false
                    loadInterstitial(activity)
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    interstitialAd = null
                    recordInterstitial()
                }
            }
            ad.show(activity)
        } else {
            loadInterstitial(activity)
            onAdDismissed()
        }
    }

    fun showInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        showInterstitial(activity, force = false, onAdDismissed)
    }

    fun showInterstitial(activity: Activity, force: Boolean, onAdDismissed: () -> Unit) {
        if (!isAdsEnabled) {
            onAdDismissed()
            return
        }
        if (isShowingAd) {
            onAdDismissed()
            return
        }
        if (!force && !canShowInterstitial()) {
            onAdDismissed()
            return
        }

        if (isAdMobEnabled) {
            val ad = interstitialAd
            if (ad != null) {
                showAdMobInterstitialOnly(activity, onAdDismissed)
                return
            } else if (force) {
                // If forced (e.g. user selected even episode or watched movie ad), load and show on demand
                loadAndShowInterstitial(activity, onAdDismissed)
                return
            }
        }

        // Preload next AdMob interstitial
        loadInterstitial(activity)

        if (isWebviewAdsEnabled && webviewAdUrl.isNotBlank()) {
            showWebviewAd(activity, onAdDismissed)
        } else {
            onAdDismissed()
        }
    }

    fun loadAndShowInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        if (!isAdsEnabled || !isAdMobEnabled) {
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            showAdMobInterstitialOnly(activity, onAdDismissed)
            return
        }

        var hasDismissed = false
        val dismissOnce: () -> Unit = {
            if (!hasDismissed) {
                hasDismissed = true
                onAdDismissed()
            }
        }

        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            Log.d(TAG, "AdMob Interstitial on-demand load timeout (3.5s), proceeding to stream")
            dismissOnce()
        }
        handler.postDelayed(timeoutRunnable, 3500L)

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            admobInterstitialId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(loadedAd: InterstitialAd) {
                    handler.removeCallbacks(timeoutRunnable)
                    interstitialAd = loadedAd
                    isInterstitialLoading = false
                    Log.d(TAG, "AdMob Interstitial loaded on demand, displaying now")
                    showAdMobInterstitialOnly(activity, dismissOnce)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    handler.removeCallbacks(timeoutRunnable)
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.d(TAG, "AdMob Interstitial failed on demand: ${loadAdError.message}")
                    dismissOnce()
                }
            }
        )
    }

    fun showWebviewAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (!isAdsEnabled || !isWebviewAdsEnabled || webviewAdUrl.isBlank()) {
            onAdDismissed()
            return
        }
        if (isShowingAd || !canShowInterstitial()) {
            onAdDismissed()
            return
        }

        pendingDismissCallback = onAdDismissed
        isShowingAd = true
        recordInterstitial()
        _showInterstitial.value = true
    }

    fun dismissInterstitial() {
        _showInterstitial.value = false
        isShowingAd = false
        pendingDismissCallback?.invoke()
        pendingDismissCallback = null
    }

    // --- AdMob Rewarded ---

    fun loadRewarded(context: Context) {
        if (!isAdsEnabled || !isAdMobEnabled) return
        if (rewardedAd != null || isRewardedLoading) return

        isRewardedLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            admobRewardedId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "AdMob Test Rewarded Ad loaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.d(TAG, "AdMob Test Rewarded Ad failed: ${loadAdError.message}")
                }
            }
        )
    }

    fun showRewarded(
        activity: Activity,
        force: Boolean = false,
        onUserEarnedReward: () -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        if (!isAdsEnabled) {
            onUserEarnedReward()
            onAdDismissed()
            return
        }

        if (isAdMobEnabled) {
            val ad = rewardedAd
            if (ad != null) {
                showAdMobRewardedOnly(activity, onUserEarnedReward, onAdDismissed)
                return
            } else if (force) {
                // If forced (e.g. Midnight 18+ VIP content), load on demand and display immediately
                loadAndShowRewarded(activity, onUserEarnedReward, onAdDismissed)
                return
            }
        }

        loadRewarded(activity)
        onUserEarnedReward()
        onAdDismissed()
    }

    fun showRewarded(activity: Activity, onUserEarnedReward: () -> Unit, onAdDismissed: () -> Unit) {
        showRewarded(activity, force = false, onUserEarnedReward = onUserEarnedReward, onAdDismissed = onAdDismissed)
    }

    private fun showAdMobRewardedOnly(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        val ad = rewardedAd ?: run {
            onUserEarnedReward()
            onAdDismissed()
            return
        }

        var earned = false
        var hasDismissed = false
        val dismissOnce: () -> Unit = {
            if (!hasDismissed) {
                hasDismissed = true
                isShowingAd = false
                rewardedAd = null
                loadRewarded(activity)
                if (earned) onUserEarnedReward()
                onAdDismissed()
            }
        }

        isShowingAd = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "AdMob Rewarded Ad dismissed")
                dismissOnce()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "AdMob Rewarded Ad failed to show: ${adError.message}")
                dismissOnce()
            }
        }

        ad.show(activity) { rewardItem ->
            earned = true
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
        }
    }

    fun loadAndShowRewarded(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        if (!isAdsEnabled || !isAdMobEnabled) {
            onUserEarnedReward()
            onAdDismissed()
            return
        }

        val ad = rewardedAd
        if (ad != null) {
            showAdMobRewardedOnly(activity, onUserEarnedReward, onAdDismissed)
            return
        }

        var hasFinished = false
        val finishOnce: (Boolean) -> Unit = { earnedReward ->
            if (!hasFinished) {
                hasFinished = true
                if (earnedReward) onUserEarnedReward()
                onAdDismissed()
            }
        }

        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            Log.d(TAG, "AdMob Rewarded on-demand load timeout (4.0s), proceeding to stream")
            finishOnce(true)
        }
        handler.postDelayed(timeoutRunnable, 4000L)

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            activity,
            admobRewardedId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(loadedAd: RewardedAd) {
                    handler.removeCallbacks(timeoutRunnable)
                    rewardedAd = loadedAd
                    isRewardedLoading = false
                    Log.d(TAG, "AdMob Rewarded Ad loaded on demand, displaying now")
                    showAdMobRewardedOnly(activity, onUserEarnedReward) {
                        finishOnce(false)
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    handler.removeCallbacks(timeoutRunnable)
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.d(TAG, "AdMob Rewarded Ad failed on demand: ${loadAdError.message}")
                    finishOnce(true)
                }
            }
        )
    }

    // --- AdMob App Open ---

    fun isAppOpenAdAvailable(): Boolean {
        return appOpenAd != null && (System.currentTimeMillis() - appOpenLoadTime) < FOUR_HOURS_MS
    }

    fun loadAppOpenAd(context: Context) {
        if (!isAdsEnabled || !isAdMobEnabled || !isAdPlacementEnabled("app_open")) return
        if (isAppOpenAdAvailable() || isAppOpenAdLoading) return

        isAppOpenAdLoading = true
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            admobAppOpenId,
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    appOpenLoadTime = System.currentTimeMillis()
                    isAppOpenAdLoading = false
                    Log.d(TAG, "AdMob Test App Open Ad loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    appOpenAd = null
                    isAppOpenAdLoading = false
                    Log.d(TAG, "AdMob Test App Open Ad failed: ${loadAdError.message}")
                }
            }
        )
    }

    fun showAppOpenAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (!isAdsEnabled || !isAdMobEnabled || !isAdPlacementEnabled("app_open")) {
            onAdDismissed()
            return
        }

        if (isShowingAd) {
            Log.d(TAG, "Cannot show App Open ad: another full-screen ad is active")
            onAdDismissed()
            return
        }

        if (isPlayerActive) {
            Log.d(TAG, "Cannot show App Open ad: player is actively playing")
            onAdDismissed()
            return
        }

        val now = System.currentTimeMillis()
        if (lastAppOpenShownAt > 0L && (now - lastAppOpenShownAt) < APP_OPEN_COOLDOWN_MS) {
            Log.d(TAG, "Cannot show App Open ad: cooldown active (${(now - lastAppOpenShownAt) / 1000}s elapsed)")
            onAdDismissed()
            return
        }

        if (!isAppOpenAdAvailable()) {
            Log.d(TAG, "App Open ad not available or expired, preloading now")
            loadAppOpenAd(activity)
            onAdDismissed()
            return
        }

        val ad = appOpenAd
        if (ad != null) {
            isShowingAd = true
            lastAppOpenShownAt = now
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    Log.d(TAG, "App Open ad dismissed by user")
                    loadAppOpenAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                    Log.d(TAG, "App Open ad failed to show: ${adError.message}")
                    loadAppOpenAd(activity)
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    appOpenAd = null
                    Log.d(TAG, "App Open ad showed full screen content")
                }
            }
            ad.show(activity)
        } else {
            loadAppOpenAd(activity)
            onAdDismissed()
        }
    }

    fun onAppForegrounded(activity: Activity) {
        if (!isSplashFinished) {
            // App is still in initial cold start / splash screen
            return
        }
        if (!isAdsEnabled || !isAdMobEnabled || !isAdPlacementEnabled("app_open")) {
            return
        }
        if (isShowingAd || isPlayerActive) {
            return
        }
        val now = System.currentTimeMillis()
        if (lastAppOpenShownAt > 0L && (now - lastAppOpenShownAt) < APP_OPEN_COOLDOWN_MS) {
            return
        }
        if (isAppOpenAdAvailable()) {
            Log.d(TAG, "Triggering App Open ad on app foreground resume")
            showAppOpenAd(activity)
        } else {
            loadAppOpenAd(activity)
        }
    }

    // --- AdMob Rewarded Interstitial ---

    fun loadRewardedInterstitial(context: Context) {
        if (!isAdsEnabled || !isAdMobEnabled) return
        if (rewardedInterstitialAd != null || isRewardedInterstitialLoading) return

        isRewardedInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(
            context,
            admobRewardedInterstitialId,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    isRewardedInterstitialLoading = false
                    Log.d(TAG, "AdMob Test Rewarded Interstitial loaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedInterstitialAd = null
                    isRewardedInterstitialLoading = false
                    Log.d(TAG, "AdMob Test Rewarded Interstitial failed: ${loadAdError.message}")
                }
            }
        )
    }

    fun showRewardedInterstitial(activity: Activity, onUserEarnedReward: () -> Unit, onAdDismissed: () -> Unit) {
        if (!isAdsEnabled || !isAdMobEnabled) {
            onUserEarnedReward()
            onAdDismissed()
            return
        }

        val ad = rewardedInterstitialAd
        if (ad != null) {
            var earned = false
            isShowingAd = true
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedInterstitialAd = null
                    isShowingAd = false
                    loadRewardedInterstitial(activity)
                    if (earned) onUserEarnedReward()
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedInterstitialAd = null
                    isShowingAd = false
                    loadRewardedInterstitial(activity)
                    if (earned) onUserEarnedReward()
                    onAdDismissed()
                }
            }
            ad.show(activity) { rewardItem ->
                earned = true
                Log.d(TAG, "User earned rewarded interstitial: ${rewardItem.amount} ${rewardItem.type}")
            }
        } else {
            loadRewardedInterstitial(activity)
            onUserEarnedReward()
            onAdDismissed()
        }
    }

    fun showTmdbAd(activity: Activity, onAdDismissed: () -> Unit) {
        showInterstitial(activity, onAdDismissed)
    }

    fun showOwnDramaAd(activity: Activity, requiredAdType: String, onAdDismissed: () -> Unit) {
        showInterstitial(activity, onAdDismissed)
    }
}
