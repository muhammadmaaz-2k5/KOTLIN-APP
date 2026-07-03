package com.job2day.nazaarabox.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager

@Composable
fun InlineCardAd(
    modifier: Modifier = Modifier,
    label: String = "Sponsored",
) {
    if (!AdManager.isAdsEnabled || !AdManager.isWebviewAdsEnabled) return

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
            adUrl = AdManager.webviewAdUrl,
            modifier = Modifier
                .width(140.dp)
                .height(200.dp),
            showClose = false,
        )
    }
}

@Composable
fun InlineBannerAd(
    modifier: Modifier = Modifier,
) {
    if (!AdManager.isAdsEnabled || !AdManager.isWebviewAdsEnabled) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(AppColors.SurfaceVariantDark, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        CustomBannerAd(
            adUrl = AdManager.webviewAdUrl,
            modifier = Modifier.fillMaxWidth(),
            alwaysExpanded = true,
        )
    }
}

@Composable
fun FullWidthAdBanner(
    modifier: Modifier = Modifier,
) {
    if (!AdManager.isAdsEnabled || !AdManager.isWebviewAdsEnabled) return

    CustomBannerAd(
        adUrl = AdManager.webviewAdUrl,
        modifier = modifier.fillMaxWidth(),
        alwaysExpanded = true,
    )
}
