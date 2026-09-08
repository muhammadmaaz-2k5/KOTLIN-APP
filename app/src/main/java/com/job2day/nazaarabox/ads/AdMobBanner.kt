package com.job2day.nazaarabox.ads

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.job2day.nazaarabox.utils.AdManager

private const val TAG = "AdMobBanner"

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
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    var isLoaded by remember { mutableStateOf(false) }
    var hasFailed by remember { mutableStateOf(false) }

    // Adaptive banner size calculation to ensure exact non-zero dimensions
    val effectiveAdSize = remember(adSize, screenWidthDp) {
        if (adSize != null) {
            adSize
        } else {
            val width = if (screenWidthDp > 0) screenWidthDp else 360
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width)
        }
    }

    val bannerHeightDp = remember(effectiveAdSize) {
        val h = effectiveAdSize.getHeightInPixels(context)
        val density = context.resources.displayMetrics.density
        if (density > 0 && h > 0) {
            (h / density).dp
        } else {
            50.dp
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeightDp)
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeightDp),
            factory = { ctx ->
                AdView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    setAdSize(effectiveAdSize)
                    setAdUnitId(adUnitId.ifBlank { AdManager.TEST_BANNER_ID })
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            isLoaded = true
                            hasFailed = false
                            onAdLoaded?.invoke()
                            Log.d(TAG, "AdMob banner successfully loaded: $adUnitId")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            super.onAdFailedToLoad(error)
                            hasFailed = true
                            onAdFailed?.invoke(error)
                            Log.w(TAG, "AdMob banner failed to load: ${error.message} (code ${error.code})")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            },
        )

        // If AdMob failed to fill and Webview ads are enabled, show Webview banner fallback
        if (hasFailed && AdManager.isWebviewAdsEnabled) {
            CustomBannerAd(
                adUrl = AdManager.webviewAdUrl,
                modifier = Modifier.fillMaxWidth(),
                alwaysExpanded = true,
            )
        }
    }
}
