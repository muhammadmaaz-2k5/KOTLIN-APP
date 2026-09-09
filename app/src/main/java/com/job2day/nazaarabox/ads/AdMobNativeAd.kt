package com.job2day.nazaarabox.ads

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
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
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.job2day.nazaarabox.R
import com.job2day.nazaarabox.utils.AdManager

private const val TAG = "AdMobNativeAd"

/**
 * Standard Card Native Ad containing MediaView, Icon, Headline, Body, and CTA button.
 * Best fit: In-stream feeds, Detail screen (between Storyline and episodes), Home screen.
 */
@Composable
fun AdMobNativeCardAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdManager.admobNativeId,
    placement: String = "native_card",
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
                Log.d(TAG, "[$placement] Native Card Ad loaded successfully: ${ad.headline}")
                loadedNativeAd?.destroy()
                loadedNativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    Log.w(TAG, "[$placement] Native Card Ad failed: ${error.message} (code ${error.code})")
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    val view = LayoutInflater.from(ctx)
                        .inflate(R.layout.view_admob_native_card, null, false) as NativeAdView
                    populateCardNativeAdView(nativeAd, view)
                    view
                },
                update = { view ->
                    populateCardNativeAdView(nativeAd, view)
                }
            )
        }
    }
}

/**
 * Compact Native Ad without large MediaView (App Icon + Headline + Body + CTA Button).
 * Best fit: Search results, vertical lists, season/episode rows.
 */
@Composable
fun AdMobNativeCompactAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdManager.admobNativeId,
    placement: String = "native_compact",
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
                Log.d(TAG, "[$placement] Native Compact Ad loaded successfully: ${ad.headline}")
                loadedNativeAd?.destroy()
                loadedNativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    Log.w(TAG, "[$placement] Native Compact Ad failed: ${error.message} (code ${error.code})")
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
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
                    val view = LayoutInflater.from(ctx)
                        .inflate(R.layout.view_admob_native_compact, null, false) as NativeAdView
                    populateCompactNativeAdView(nativeAd, view)
                    view
                },
                update = { view ->
                    populateCompactNativeAdView(nativeAd, view)
                }
            )
        }
    }
}

private fun populateCardNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    // Headline
    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
    adView.headlineView = headlineView
    headlineView.text = nativeAd.headline

    // Advertiser / Store
    val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)
    adView.advertiserView = advertiserView
    val secondary = nativeAd.advertiser?.takeIf { it.isNotBlank() } ?: nativeAd.store?.takeIf { it.isNotBlank() }
    if (secondary.isNullOrBlank()) {
        advertiserView.visibility = View.GONE
    } else {
        advertiserView.visibility = View.VISIBLE
        advertiserView.text = secondary
    }

    // Body
    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
    adView.bodyView = bodyView
    if (nativeAd.body.isNullOrBlank()) {
        bodyView.visibility = View.GONE
    } else {
        bodyView.visibility = View.VISIBLE
        bodyView.text = nativeAd.body
    }

    // Media (only show when actual media/video exists to avoid blank voids)
    val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
    adView.mediaView = mediaView
    if (nativeAd.mediaContent != null && (nativeAd.mediaContent?.hasVideoContent() == true || nativeAd.images.isNotEmpty())) {
        mediaView.mediaContent = nativeAd.mediaContent
        mediaView.visibility = View.VISIBLE
    } else {
        mediaView.visibility = View.GONE
    }

    // App Icon
    val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
    adView.iconView = iconView
    if (nativeAd.icon != null && nativeAd.icon?.drawable != null) {
        iconView.setImageDrawable(nativeAd.icon?.drawable)
        iconView.visibility = View.VISIBLE
    } else {
        iconView.visibility = View.GONE
    }

    // Call to Action
    val ctaButton = adView.findViewById<AppCompatButton>(R.id.ad_call_to_action)
    adView.callToActionView = ctaButton
    if (nativeAd.callToAction.isNullOrBlank()) {
        ctaButton.visibility = View.GONE
    } else {
        ctaButton.visibility = View.VISIBLE
        ctaButton.text = nativeAd.callToAction
    }

    // Register NativeAd to NativeAdView
    adView.setNativeAd(nativeAd)
}

private fun populateCompactNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    // Headline
    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
    adView.headlineView = headlineView
    headlineView.text = nativeAd.headline

    // Advertiser / Store
    val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)
    adView.advertiserView = advertiserView
    val secondary = nativeAd.advertiser?.takeIf { it.isNotBlank() } ?: nativeAd.store?.takeIf { it.isNotBlank() }
    if (secondary.isNullOrBlank()) {
        advertiserView.visibility = View.GONE
    } else {
        advertiserView.visibility = View.VISIBLE
        advertiserView.text = secondary
    }

    // Body
    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
    adView.bodyView = bodyView
    if (nativeAd.body.isNullOrBlank()) {
        bodyView.visibility = View.GONE
    } else {
        bodyView.visibility = View.VISIBLE
        bodyView.text = nativeAd.body
    }

    // App Icon
    val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
    adView.iconView = iconView
    if (nativeAd.icon != null && nativeAd.icon?.drawable != null) {
        iconView.setImageDrawable(nativeAd.icon?.drawable)
        iconView.visibility = View.VISIBLE
    } else {
        iconView.visibility = View.GONE
    }

    // Call to Action
    val ctaButton = adView.findViewById<AppCompatButton>(R.id.ad_call_to_action)
    adView.callToActionView = ctaButton
    if (nativeAd.callToAction.isNullOrBlank()) {
        ctaButton.visibility = View.GONE
    } else {
        ctaButton.visibility = View.VISIBLE
        ctaButton.text = nativeAd.callToAction
    }

    // Register NativeAd to NativeAdView
    adView.setNativeAd(nativeAd)
}
