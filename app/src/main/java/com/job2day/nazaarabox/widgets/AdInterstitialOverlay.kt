package com.job2day.nazaarabox.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.job2day.nazaarabox.ads.CustomInterstitialAd
import com.job2day.nazaarabox.utils.AdManager

@Composable
fun AdInterstitialOverlay() {
    val showInterstitial by AdManager.showInterstitial.collectAsState()

    if (showInterstitial && AdManager.isWebviewAdsEnabled) {
        CustomInterstitialAd(
            adUrl = AdManager.webviewAdUrl,
            onDismiss = { AdManager.dismissInterstitial() },
        )
    }
}
