package com.job2day.nazaarabox.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * In-feed and scattered banner/card ads are disabled.
 * The application exclusively uses a single Google AdMob Collapsible Adaptive Banner
 * sticky with the bottom navigation bar.
 */

@Composable
fun InlineCardAd(
    placement: String = "generic",
    modifier: Modifier = Modifier,
    label: String = "",
) {
    // Disabled: only bottom navigation sticky collapsible banner is active.
}

@Composable
fun InlineBannerAd(
    placement: String = "generic",
    modifier: Modifier = Modifier,
) {
    // Disabled: only bottom navigation sticky collapsible banner is active.
}

@Composable
fun FullWidthAdBanner(
    placement: String = "generic",
    modifier: Modifier = Modifier,
) {
    // Disabled: only bottom navigation sticky collapsible banner is active.
}
