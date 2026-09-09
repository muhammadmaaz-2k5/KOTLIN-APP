package com.job2day.nazaarabox.ads

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.job2day.nazaarabox.utils.AdManager

private const val TAG = "AdMobNativeAd"

/**
 * Official Google AdMob Native Medium Template.
 * Fully supports all video and image ads from Google AdMob using MediaView.
 */
@Composable
fun AdMobNativeMediumAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdManager.admobNativeId,
    placement: String = "medium_native",
) {
    if (!AdManager.isAdsEnabled || !AdManager.isAdMobEnabled || !AdManager.isAdPlacementEnabled(placement)) {
        return
    }

    val context = LocalContext.current
    var loadedNativeAd by remember { mutableStateOf<NativeAd?>(null) }
    val effectiveAdUnitId = adUnitId.ifBlank { AdManager.TEST_NATIVE_ID }

    DisposableEffect(effectiveAdUnitId) {
        val adLoader = AdLoader.Builder(context, effectiveAdUnitId)
            .forNativeAd { ad ->
                Log.d(TAG, "[$placement] Google Medium Template Ad loaded successfully: ${ad.headline}")
                loadedNativeAd?.destroy()
                loadedNativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    Log.w(TAG, "[$placement] Google Medium Template Ad failed: ${error.message} (code ${error.code})")
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_ANY)
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            loadedNativeAd?.destroy()
            loadedNativeAd = null
        }
    }

    AnimatedVisibility(
        visible = loadedNativeAd != null,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val nativeAd = loadedNativeAd ?: return@AnimatedVisibility

        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    TemplateView(ctx).apply {
                        setNativeAd(nativeAd)
                    }
                },
                update = { view ->
                    view.setNativeAd(nativeAd)
                }
            )
        }
    }
}

/**
 * Backward-compatible alias for AdMobNativeMediumAd.
 */
@Composable
fun AdMobNativeCardAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdManager.admobNativeId,
    placement: String = "card_native",
) {
    AdMobNativeMediumAd(
        modifier = modifier,
        adUnitId = adUnitId,
        placement = placement,
    )
}

/**
 * Backward-compatible alias for AdMobNativeMediumAd.
 */
@Composable
fun AdMobNativeCompactAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdManager.admobNativeId,
    placement: String = "compact_native",
) {
    AdMobNativeMediumAd(
        modifier = modifier,
        adUnitId = adUnitId,
        placement = placement,
    )
}
