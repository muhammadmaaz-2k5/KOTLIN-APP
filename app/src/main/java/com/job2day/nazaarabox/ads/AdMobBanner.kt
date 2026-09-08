package com.job2day.nazaarabox.ads

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.job2day.nazaarabox.ui.theme.AppColors
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

    val bannerHeightDp: Dp = remember(effectiveAdSize) {
        val h = effectiveAdSize.getHeightInPixels(context)
        val density = context.resources.displayMetrics.density
        if (density > 0 && h > 0) {
            maxOf(56.dp, (h / density).dp)
        } else {
            56.dp
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeightDp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF13131F)),
        contentAlignment = Alignment.Center,
    ) {
        // 1. Guaranteed Visible AdMob Test Banner Placeholder (Shown while loading or on error)
        if (!isLoaded) {
            AdMobTestAdPlaceholder(
                bannerHeightDp = bannerHeightDp,
                hasFailed = hasFailed,
            )
        }

        // 2. Real Google Mobile Ads AdView
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
    }
}

/**
 * Modern Google AdMob Test Ad Placeholder.
 * Ensures an authentic, prominent test banner is immediately visible on screen
 * while Google's test ad loads or during network fill latency.
 */
@Composable
fun AdMobTestAdPlaceholder(
    bannerHeightDp: Dp = 56.dp,
    hasFailed: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeightDp),
        color = Color(0xFF13131F),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF2A2A3E)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Left Group: Test Ad Green Badge + AdMob Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Official style [TEST AD] Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF064E3B))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "TEST AD",
                        color = Color(0xFF34D399),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                }

                // Yellow Ad tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFF59E0B))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Ad",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }

                // Ad Text Description
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "Google AdMob Banner",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (hasFailed) "Test Mode Active • Fallback Creative" else "Loading Google Test Ad Creative...",
                        color = if (hasFailed) Color(0xFF9CA3AF) else Color(0xFF60A5FA),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Right Indicator: Active Test Dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                )
                Text(
                    text = "AdMob",
                    color = Color(0xFF8888AA),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
