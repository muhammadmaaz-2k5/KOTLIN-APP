package com.job2day.nazaarabox.ads

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager

/**
 * In-feed webview article ad cards (CustomSmallCardAd).
 */
@Composable
fun InlineCardAd(
    placement: String = "generic",
    modifier: Modifier = Modifier,
    label: String = "",
) {
    if (!AdManager.isAdPlacementEnabled(placement)) return

    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                color = AppColors.TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
            )
        }
        CustomSmallCardAd(
            adUrl = AdManager.getAdPlacementUrl(placement),
            modifier = Modifier
                .width(140.dp)
                .height(200.dp),
            showClose = false,
        )
    }
}

/**
 * Standard in-feed banners remain disabled.
 * The only banner ad used in the app is the StickyCollapsibleBannerAd
 * anchored with the bottom navigation bar.
 */
@Composable
fun InlineBannerAd(
    placement: String = "generic",
    modifier: Modifier = Modifier,
) {
    // Disabled: exclusive banner is the bottom navigation sticky collapsible banner
}

@Composable
fun FullWidthAdBanner(
    placement: String = "generic",
    modifier: Modifier = Modifier,
) {
    // Disabled: exclusive banner is the bottom navigation sticky collapsible banner
}
