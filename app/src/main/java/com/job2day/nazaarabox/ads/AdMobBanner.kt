package com.job2day.nazaarabox.ads

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.job2day.nazaarabox.utils.AdManager

private const val TAG = "AdMobBanner"

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
