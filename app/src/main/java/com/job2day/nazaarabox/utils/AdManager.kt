package com.job2day.nazaarabox.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdManager {
    private const val TAG = "AdManager"
    const val DEFAULT_WEBVIEW_AD_URL = "https://nazaarabox.com"

    var isAdsEnabled: Boolean = false
        private set

    var isWebviewAdsEnabled: Boolean = false
        private set

    var webviewAdUrl: String = DEFAULT_WEBVIEW_AD_URL
        private set

    var isSafeMode: Boolean = true
        private set

    var isShowingAd = false
        private set

    private val _showInterstitial = MutableStateFlow(false)
    val showInterstitial: StateFlow<Boolean> = _showInterstitial.asStateFlow()

    private var pendingDismissCallback: (() -> Unit)? = null

    fun applySettings(settings: Map<String, String>) {
        isAdsEnabled = parseBoolean(settings["ads_enabled"])
        isWebviewAdsEnabled = parseBoolean(settings["enable_webview_ads"])
        webviewAdUrl = settings["webview_ad_url"]?.trim()?.takeIf { it.isNotBlank() }
            ?: DEFAULT_WEBVIEW_AD_URL
        isSafeMode = !isAdsEnabled

        Log.d(
            TAG,
            "Settings applied: ads=$isAdsEnabled, webview=$isWebviewAdsEnabled, url=$webviewAdUrl",
        )
    }

    /** @deprecated Use [applySettings] instead */
    fun setAdUnitIds(settings: Map<String, String>) {
        applySettings(settings)
    }

    private fun parseBoolean(value: String?): Boolean {
        if (value == null) return false
        return when (value.lowercase()) {
            "true", "1", "yes", "on" -> true
            else -> false
        }
    }

    fun initialize(context: Context) {
        if (!isAdsEnabled) return
    }

    fun loadInterstitial(context: Context) {
    }

    fun isInterstitialAdReady(): Boolean =
        isAdsEnabled && isWebviewAdsEnabled && webviewAdUrl.isNotBlank()

    fun isRewardedAdReady(): Boolean = false
    fun isRewardedInterstitialAdReady(): Boolean = false

    fun showInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        showWebviewAd(activity, onAdDismissed)
    }

    fun showWebviewAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (!isInterstitialAdReady()) {
            onAdDismissed()
            return
        }
        pendingDismissCallback = onAdDismissed
        isShowingAd = true
        _showInterstitial.value = true
    }

    fun dismissInterstitial() {
        _showInterstitial.value = false
        isShowingAd = false
        pendingDismissCallback?.invoke()
        pendingDismissCallback = null
    }

    fun showAdMobInterstitialOnly(activity: Activity, onAdDismissed: () -> Unit) {
        onAdDismissed()
    }

    fun showTmdbAd(activity: Activity, onAdDismissed: () -> Unit) {
        showWebviewAd(activity, onAdDismissed)
    }

    fun showOwnDramaAd(activity: Activity, requiredAdType: String, onAdDismissed: () -> Unit) {
        showWebviewAd(activity, onAdDismissed)
    }

    fun loadRewarded(context: Context) {
    }

    fun showRewarded(activity: Activity, onUserEarnedReward: () -> Unit, onAdDismissed: () -> Unit) {
        onUserEarnedReward()
        onAdDismissed()
    }

    fun loadAppOpenAd(context: Context) {
    }

    fun showAppOpenAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        showWebviewAd(activity, onAdDismissed)
    }

    fun loadRewardedInterstitial(context: Context) {
    }

    fun showRewardedInterstitial(activity: Activity, onUserEarnedReward: () -> Unit, onAdDismissed: () -> Unit) {
        onUserEarnedReward()
        onAdDismissed()
    }
}
