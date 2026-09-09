package com.job2day.nazaarabox.ads

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.job2day.nazaarabox.utils.AdManager

private const val TAG = "AdMobBanner"

/**
 * Calculates current anchored adaptive banner ad size based on screen width.
 * Fills the full width of the screen.
 */
fun getAdaptiveBannerAdSize(context: Context): AdSize {
    val displayMetrics = context.resources.displayMetrics
    val widthPixels = displayMetrics.widthPixels.toFloat()
    val density = displayMetrics.density
    val adWidth = if (density > 0f) (widthPixels / density).toInt() else 360
    val effectiveWidth = if (adWidth > 0) adWidth else 360
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, effectiveWidth)
}

/**
 * Sticky Google AdMob Collapsible Adaptive Banner Ad.
 * Uses Google AdMob's anchored adaptive banner size with "collapsible" = "bottom".
 * Renders on all pages anchored to the bottom.
 */
@Composable
fun StickyCollapsibleBannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdManager.admobBannerId,
    onAdLoaded: (() -> Unit)? = null,
    onAdFailed: ((LoadAdError) -> Unit)? = null,
) {
    if (!AdManager.isAdsEnabled || !AdManager.isAdMobEnabled) return

    val context = LocalContext.current
    var isLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(if (isLoaded) Modifier.background(Color(0xFF0D0D11)) else Modifier),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { ctx ->
                val adaptiveSize = getAdaptiveBannerAdSize(ctx)
                AdView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.BOTTOM,
                    )
                    setAdSize(adaptiveSize)
                    setAdUnitId(adUnitId.ifBlank { AdManager.TEST_BANNER_ID })

                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            isLoaded = true
                            onAdLoaded?.invoke()
                            Log.d(TAG, "Sticky collapsible adaptive banner loaded: $adUnitId")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            super.onAdFailedToLoad(error)
                            isLoaded = false
                            onAdFailed?.invoke(error)
                            Log.w(TAG, "Sticky collapsible banner failed to load: ${error.message} (code ${error.code})")
                        }
                    }

                    val extras = Bundle().apply {
                        putString("collapsible", "bottom")
                    }
                    val adRequest = AdRequest.Builder()
                        .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                        .build()

                    loadAd(adRequest)
                }
            },
            onRelease = { adView ->
                try {
                    adView.destroy()
                } catch (e: Exception) {
                    Log.w(TAG, "Error destroying collapsible AdView: ${e.message}")
                }
            },
        )
    }
}

/**
 * Alias for StickyCollapsibleBannerAd.
 */
@Composable
fun StickyAdaptiveBannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdManager.admobBannerId,
    onAdLoaded: (() -> Unit)? = null,
    onAdFailed: ((LoadAdError) -> Unit)? = null,
) {
    StickyCollapsibleBannerAd(
        modifier = modifier,
        adUnitId = adUnitId,
        onAdLoaded = onAdLoaded,
        onAdFailed = onAdFailed,
    )
}

/**
 * Natural Google AdMob Banner Composable.
 * Renders the official Google AdView naturally without custom mock badges or policy-violating text.
 */
@Composable
fun AdMobBanner(
    adUnitId: String = AdManager.admobBannerId,
    modifier: Modifier = Modifier,
    adSize: AdSize? = null,
    onAdLoaded: (() -> Unit)? = null,
    onAdFailed: ((LoadAdError) -> Unit)? = null,
) {
    if (!AdManager.isAdsEnabled || !AdManager.isAdMobEnabled) return

    val context = LocalContext.current
    var isLoaded by remember { mutableStateOf(false) }

    val effectiveAdSize = remember(adSize) {
        adSize ?: AdSize.BANNER
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            modifier = Modifier.wrapContentSize(),
            factory = { ctx ->
                AdView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.Gravity.CENTER,
                    )
                    setAdSize(effectiveAdSize)
                    setAdUnitId(adUnitId.ifBlank { AdManager.TEST_BANNER_ID })
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            isLoaded = true
                            onAdLoaded?.invoke()
                            Log.d(TAG, "AdMob banner successfully loaded: $adUnitId")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            super.onAdFailedToLoad(error)
                            isLoaded = false
                            onAdFailed?.invoke(error)
                            Log.w(TAG, "AdMob banner failed to load: ${error.message} (code ${error.code})")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            },
        )
    }
}
