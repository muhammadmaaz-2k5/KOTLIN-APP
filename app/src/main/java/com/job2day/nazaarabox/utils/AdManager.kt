package com.job2day.nazaarabox.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
// AdMob imports - commented out since IS_ADMOB_ENABLED = false
// import com.google.android.gms.ads.AdError
// import com.google.android.gms.ads.AdRequest
// import com.google.android.gms.ads.FullScreenContentCallback
// import com.google.android.gms.ads.LoadAdError
// import com.google.android.gms.ads.interstitial.InterstitialAd
// import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
// import com.google.android.gms.ads.rewarded.RewardedAd
// import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
// import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
// import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
// import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date
import com.job2day.nazaarabox.BuildConfig
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.job2day.nazaarabox.widgets.DynamicWebView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope

object AdManager {
    private const val TAG = "AdManager"

    // Google Official Test Ad Unit IDs Constants
    private const val GOOGLE_TEST_BANNER_ID = "ca-app-pub-3940256099942544/2014213617"
    private const val GOOGLE_TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val GOOGLE_TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val GOOGLE_TEST_REWARDED_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/5354046379"
    private const val GOOGLE_TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
    private const val GOOGLE_TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"

    // Production Ad Unit IDs Constants
    private const val PROD_BANNER_ID = "ca-app-pub-2809929499941883/2780677786"
    private const val PROD_INTERSTITIAL_ID = "ca-app-pub-2809929499941883/7431540097"
    private const val PROD_REWARDED_ID = "ca-app-pub-2809929499941883/4805376758"
    private const val PROD_REWARDED_INTERSTITIAL_ID = "ca-app-pub-2809929499941883/6528351104"
    private const val PROD_NATIVE_ID = "ca-app-pub-2809929499941883/1715008121"
    private const val PROD_APP_OPEN_ID = "ca-app-pub-2809929499941883/6137764018"

    // Toggle this to false to run the app with production AdMob ads.
    // Set to true to run the app with Google test ad unit IDs.
    const val USE_TEST_ADS = true

    // Current Ad Unit IDs (Default based on USE_TEST_ADS)
    var INTERSTITIAL_AD_ID = if (USE_TEST_ADS) GOOGLE_TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID
        private set
    var REWARDED_AD_ID = if (USE_TEST_ADS) GOOGLE_TEST_REWARDED_ID else PROD_REWARDED_ID
        private set
    var REWARDED_INTERSTITIAL_AD_ID = if (USE_TEST_ADS) GOOGLE_TEST_REWARDED_INTERSTITIAL_ID else PROD_REWARDED_INTERSTITIAL_ID
        private set
    var BANNER_AD_ID = if (USE_TEST_ADS) GOOGLE_TEST_BANNER_ID else PROD_BANNER_ID
        private set
    var NATIVE_AD_ID = if (USE_TEST_ADS) GOOGLE_TEST_NATIVE_ID else PROD_NATIVE_ID
        private set
    var APP_OPEN_AD_ID = if (USE_TEST_ADS) GOOGLE_TEST_APP_OPEN_ID else PROD_APP_OPEN_ID
        private set
 
    // Master static toggle to enable/disable all ads in the app.
    // If set to false, all ads are completely disabled on all pages.
    // If set to true, ads are enabled and respect dynamic settings from the admin panel (or default constants).
    const val STATIC_ADS_ENABLED = true
    const val IS_ADMOB_ENABLED = false

    var isAdsEnabled: Boolean = STATIC_ADS_ENABLED
        private set

    var isWebviewAdsEnabled: Boolean by mutableStateOf(true)
        private set

    var isSafeMode: Boolean = false
        private set

    var dynamicWebviewUrl: String = "https://nazaarabox.com"
        private set
    var popupWebviewUrl: String = "https://nazaarabox.com"
        private set

    var webviewAutoClickDelayMs: Long = 5000
        private set

    /**
     * Global Y-click position as a fraction (0.0–1.0) for inline WebView ads that
     * have no per-drama override.  Maps directly to DynamicWebView's clickYFraction.
     * Set via the backend settings key "webview_click_fraction" (value 1–100).
     * Default 95 → 0.95f (near the bottom of the WebView).
     */
    var webviewClickFraction: Float = 0.95f
        private set

    /**
     * Optional JavaScript to auto-execute inside every DynamicWebView after page load.
     * Set via the backend settings key "webview_interaction_script".
     *
     * Backed by Compose mutableStateOf so any Composable reading this field will
     * automatically recompose when the value arrives from the API, triggering late
     * script injection even if the WebView page finished loading earlier.
     *
     * Example value stored in Laravel settings:
     *   document.querySelector('.close-btn')?.click();
     */
    var webviewInteractionScript by mutableStateOf("")
        private set

    /**
     * Optional CSS selector. If set, the [webviewInteractionScript] waits (up to 6 s)
     * until this element appears in the DOM before executing.
     * Set via the backend settings key "webview_ready_selector".
     *
     * Also Compose-observable for the same timing-safety reason.
     *
     * Example value: ".ad-loaded" or "#submit-button"
     */
    var webviewReadySelector by mutableStateOf("")
        private set

    var isShowingAd = false
    private var isInitialized = false

    var currentEnvironment = "test"
        private set

    fun setAdUnitIds(settings: Map<String, String>) {
        val rawEnable = settings["enable_webview_ads"]?.trim()?.lowercase()
        isWebviewAdsEnabled = rawEnable == "true" || rawEnable == "1" || rawEnable == "yes"

        // Force enable ads statically, but check if webview ads are enabled on the backend
        isAdsEnabled = STATIC_ADS_ENABLED && isWebviewAdsEnabled

        val reviewMode = settings["app_review_mode"]?.trim()?.lowercase()
        isSafeMode = (reviewMode == "off")
        
        if (isWebviewAdsEnabled) {
            dynamicWebviewUrl = settings["dynamic_webview_url"]?.takeIf { it.isNotBlank() }?.let { url ->
                if (BuildConfig.DEBUG && (url.contains("nazaaracircle.com") || url.contains("nazaarabox.com"))) {
                    url.replace("https://nazaaracircle.com", "https://nazaarabox.com")
                       .replace("https://blog.nazaarabox.com", "https://nazaarabox.com")
                } else url
            } ?: "https://nazaarabox.com"
            
            popupWebviewUrl = settings["popup_webview_url"]?.takeIf { it.isNotBlank() }?.let { url ->
                if (BuildConfig.DEBUG && (url.contains("nazaaracircle.com") || url.contains("nazaarabox.com"))) {
                    url.replace("https://nazaaracircle.com", "https://nazaarabox.com")
                       .replace("https://blog.nazaarabox.com", "https://nazaarabox.com")
                } else url
            } ?: "https://nazaarabox.com"
        } else {
            // Webview ads disabled globally — keep the fallback URL so dramas
            // with no per-drama URL still load https://nazaarabox.com instead of blank.
            dynamicWebviewUrl = "https://nazaarabox.com"
            popupWebviewUrl   = "https://nazaarabox.com"
        }

        // Interaction script & ready selector – both optional
        webviewInteractionScript = settings["webview_interaction_script"] ?: ""
        webviewReadySelector     = settings["webview_ready_selector"]     ?: ""

        webviewAutoClickDelayMs  = settings["webview_auto_click_delay_ms"]?.toLongOrNull() ?: 5000L

        // Global click fraction (1–100 from backend, stored as 0.0–1.0 float)
        webviewClickFraction = settings["webview_click_fraction"]
            ?.toIntOrNull()?.coerceIn(1, 100)?.div(100f) ?: 0.95f

        // Dynamic AdMob IDs for Interstitial, Rewarded, and Rewarded Interstitial
        INTERSTITIAL_AD_ID = if (USE_TEST_ADS) {
            settings["test_interstitial_id"]?.takeIf { it.isNotBlank() } ?: GOOGLE_TEST_INTERSTITIAL_ID
        } else {
            settings["interstitial_id"]?.takeIf { it.isNotBlank() } ?: PROD_INTERSTITIAL_ID
        }

        REWARDED_AD_ID = if (USE_TEST_ADS) {
            settings["test_rewarded_id"]?.takeIf { it.isNotBlank() } ?: GOOGLE_TEST_REWARDED_ID
        } else {
            settings["rewarded_id"]?.takeIf { it.isNotBlank() } ?: PROD_REWARDED_ID
        }

        REWARDED_INTERSTITIAL_AD_ID = if (USE_TEST_ADS) {
            settings["test_rewarded_interstitial_id"]?.takeIf { it.isNotBlank() } ?: GOOGLE_TEST_REWARDED_INTERSTITIAL_ID
        } else {
            settings["rewarded_interstitial_id"]?.takeIf { it.isNotBlank() } ?: PROD_REWARDED_INTERSTITIAL_ID
        }

        // Banner, Native, App Open remain static
        BANNER_AD_ID = if (USE_TEST_ADS) GOOGLE_TEST_BANNER_ID else PROD_BANNER_ID
        NATIVE_AD_ID = if (USE_TEST_ADS) GOOGLE_TEST_NATIVE_ID else PROD_NATIVE_ID
        APP_OPEN_AD_ID = if (USE_TEST_ADS) GOOGLE_TEST_APP_OPEN_ID else PROD_APP_OPEN_ID
        
        Log.d(TAG, "Ad Environment: static ${if (USE_TEST_ADS) "test" else "production"}. IDs: Int=$INTERSTITIAL_AD_ID, Rew=$REWARDED_AD_ID, RewInt=$REWARDED_INTERSTITIAL_AD_ID, Ban=$BANNER_AD_ID, Nat=$NATIVE_AD_ID, AppOpen=$APP_OPEN_AD_ID")
    }

    // AdMob ads are disabled (IS_ADMOB_ENABLED = false)
    // Only WebView ads are used
    private var mInterstitialAd: Any? = null
    private var mRewardedAd: Any? = null
    private var mRewardedInterstitialAd: Any? = null

    private var isRewardedLoading = false
    private var isRewardedInterstitialLoading = false

    fun initialize(context: Context) {
        if (!isAdsEnabled) return
        // AdMob is disabled, only WebView ads are used
    }

    // --- Interstitial Ads ---

    fun loadInterstitial(context: Context) {
        // AdMob is disabled
    }

    fun isInterstitialAdReady(): Boolean = false
    fun isRewardedAdReady(): Boolean = false
    fun isRewardedInterstitialAdReady(): Boolean = false

    private var webviewAdCounter = 0
    private var tmdbAdClickCount = 0
    private var ownDramaAdClickCount = 0

    private fun showWebviewAdDialog(activity: Activity, adUrl: String, onAdDismissed: () -> Unit) {
        if (!isWebviewAdsEnabled) {
            onAdDismissed()
            return
        }
        activity.runOnUiThread {
            try {
                val dialog = android.app.Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                dialog.setCancelable(false)

                val composeView = ComposeView(activity).apply {
                    setContent {
                        var timeLeft by remember { mutableStateOf(15) }
                        var countdownJob by remember { mutableStateOf<Job?>(null) }
                        val coroutineScope = rememberCoroutineScope()

                        LaunchedEffect(Unit) {
                            // Cancel previous countdown job
                            countdownJob?.cancel()
                            
                            countdownJob = coroutineScope.launch {
                                while (timeLeft > 0) {
                                    delay(1000)
                                    timeLeft--
                                }
                                if (dialog.isShowing) {
                                    dialog.dismiss()
                                    onAdDismissed()
                                }
                            }
                        }

                        DisposableEffect(Unit) {
                            onDispose {
                                // Cancel countdown job when composable is disposed
                                countdownJob?.cancel()
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                        ) {
                            DynamicWebView(
                                url = adUrl,
                                modifier = Modifier.fillMaxSize(),
                                height = null,
                                scriptToInject = webviewInteractionScript,
                                readySelector = webviewReadySelector,
                                autoClickDelayMs = webviewAutoClickDelayMs,
                                clickYFraction = webviewClickFraction
                            )

                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (timeLeft > 0) "Close in ${timeLeft}s" else "Close",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                if (timeLeft <= 10) {
                                    IconButton(
                                        onClick = {
                                            if (dialog.isShowing) {
                                                dialog.dismiss()
                                                onAdDismissed()
                                            }
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                dialog.setContentView(composeView)

                dialog.window?.decorView?.let { decorView ->
                    val lifecycleOwner = activity as? androidx.lifecycle.LifecycleOwner
                    val viewModelStoreOwner = activity as? androidx.lifecycle.ViewModelStoreOwner
                    val savedStateRegistryOwner = activity as? androidx.savedstate.SavedStateRegistryOwner
                    
                    if (lifecycleOwner != null) {
                        decorView.setViewTreeLifecycleOwner(lifecycleOwner)
                    }
                    if (viewModelStoreOwner != null) {
                        decorView.setViewTreeViewModelStoreOwner(viewModelStoreOwner)
                    }
                    if (savedStateRegistryOwner != null) {
                        decorView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
                    }
                }

                dialog.show()

            } catch (e: Exception) {
                Log.d(TAG, "Error showing WebView dialog: ${e.message}")
                onAdDismissed()
            }
        }
    }

    fun showInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        if (!isAdsEnabled) {
            onAdDismissed()
            return
        }
        if (isShowingAd) {
            Log.d(TAG, "Another ad is showing, skipping Interstitial.")
            onAdDismissed()
            return
        }

        val adUrl = popupWebviewUrl.ifBlank { dynamicWebviewUrl }
        val canShowWebview = isWebviewAdsEnabled && adUrl.isNotBlank()

        if (canShowWebview) {
            showWebviewAdDialog(activity, adUrl, onAdDismissed)
        } else {
            onAdDismissed()
        }
    }

    fun showWebviewAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (!isAdsEnabled) {
            onAdDismissed()
            return
        }
        if (isShowingAd) {
            Log.d(TAG, "Another ad is showing, skipping Webview Interstitial.")
            onAdDismissed()
            return
        }
        val adUrl = popupWebviewUrl.ifBlank { dynamicWebviewUrl }
        if (isWebviewAdsEnabled && adUrl.isNotBlank()) {
            showWebviewAdDialog(activity, adUrl, onAdDismissed)
        } else {
            onAdDismissed()
        }
    }

    fun showAdMobInterstitialOnly(activity: Activity, onAdDismissed: () -> Unit) {
        // AdMob interstitial is disabled; proceed directly
        onAdDismissed()
    }

    fun showTmdbAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (!isAdsEnabled) {
            onAdDismissed()
            return
        }
        // Only show webview ads
        showWebviewAd(activity, onAdDismissed)
    }

    fun showOwnDramaAd(activity: Activity, requiredAdType: String, onAdDismissed: () -> Unit) {
        if (!isAdsEnabled) {
            onAdDismissed()
            return
        }
        val type = requiredAdType.trim().uppercase()
        if (type == "NONE") {
            onAdDismissed()
            return
        }
        // Only show webview ads
        showWebviewAd(activity, onAdDismissed)
    }

    // --- Rewarded Ads ---

    fun loadRewarded(context: Context) {
        // AdMob is disabled
    }

    fun showRewarded(activity: Activity, onUserEarnedReward: () -> Unit, onAdDismissed: () -> Unit) {
        if (!isAdsEnabled) {
            onUserEarnedReward()
            onAdDismissed()
            return
        }
        // Redirect to Webview ad, granting reward on dismissal/completion
        val adUrl = popupWebviewUrl.ifBlank { dynamicWebviewUrl }
        if (isWebviewAdsEnabled && adUrl.isNotBlank()) {
            showWebviewAdDialog(activity, adUrl) {
                onUserEarnedReward()
                onAdDismissed()
            }
        } else {
            onUserEarnedReward()
            onAdDismissed()
        }
    }

    // --- App Open Ads ---

    private var mAppOpenAd: Any? = null
    private var isAppOpenAdLoading = false
    private var appOpenAdLoadTime: Long = 0
    private var pendingAppOpenShowActivity: Activity? = null
    private var pendingAppOpenShowCallback: (() -> Unit)? = null

    fun loadAppOpenAd(context: Context) {
        // AdMob is disabled
    }

    private fun isAppOpenAdAvailable(): Boolean = false

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean = false

    fun showAppOpenAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        onAdDismissed()
    }

    // --- Rewarded Interstitial Ads ---

    fun loadRewardedInterstitial(context: Context) {
        // AdMob is disabled
    }

    fun showRewardedInterstitial(activity: Activity, onUserEarnedReward: () -> Unit, onAdDismissed: () -> Unit) {
        if (!isAdsEnabled) {
            onUserEarnedReward()
            onAdDismissed()
            return
        }
        // Redirect to Webview ad, granting reward on dismissal/completion
        val adUrl = popupWebviewUrl.ifBlank { dynamicWebviewUrl }
        if (isWebviewAdsEnabled && adUrl.isNotBlank()) {
            showWebviewAdDialog(activity, adUrl) {
                onUserEarnedReward()
                onAdDismissed()
            }
        } else {
            onUserEarnedReward()
            onAdDismissed()
        }
    }
}
