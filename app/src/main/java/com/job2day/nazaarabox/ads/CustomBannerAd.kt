package com.job2day.nazaarabox.ads

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.widgets.DynamicWebView
import kotlinx.coroutines.delay

@Composable
fun CustomBannerAd(
    adUrl: String = AdManager.webviewAdUrl,
    backgroundColor: Color = AppColors.SurfaceVariantDark,
    modifier: Modifier = Modifier,
    alwaysExpanded: Boolean = true,
) {
    if (!AdManager.isAdsEnabled || !AdManager.isWebviewAdsEnabled) {
        return
    }

    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(true) }
    var isExpanded by remember { mutableStateOf(if (alwaysExpanded) true else false) }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                AppColors.Outline.copy(alpha = 0.3f)
            )
        ) {
            Column {
                // Header bar with collapse/expand and close buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(AppColors.SurfaceDark)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sponsored",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextMuted
                    )
                    
                    Row {
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = AppColors.TextMuted
                            )
                        }
                        
                        IconButton(
                            onClick = { isVisible = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Ad",
                                tint = AppColors.TextMuted
                            )
                        }
                    }
                }
                
                // Collapsible WebView content
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        CollapsibleWebView(
                            url = adUrl,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CollapsibleWebView(
    url: String,
    modifier: Modifier = Modifier,
) {
    DynamicWebView(
        url = url,
        modifier = modifier,
        height = null,
        autoClickDelayMs = 2000L,
        autoClickIntervalMs = 2000L,
        clickYFraction = 0.5f,
        wrapInCard = false
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CustomInterstitialAd(
    adUrl: String = AdManager.webviewAdUrl,
    countdownSeconds: Int = 10,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!AdManager.isAdsEnabled || !AdManager.isWebviewAdsEnabled) {
        onDismiss()
        return
    }

    if (adUrl.isBlank()) {
        onDismiss()
        return
    }
    
    var countdown by remember { mutableStateOf(countdownSeconds) }
    var isLoading by remember { mutableStateOf(true) }
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    Dialog(
        onDismissRequest = {
            if (countdown == 0) {
                currentOnDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = countdown == 0,
            dismissOnClickOutside = countdown == 0,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.SurfaceDark)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header with countdown/close
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(AppColors.SurfaceVariantDark)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Advertisement",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.TextPrimary
                        )
                        
                        Box(
                            modifier = Modifier
                                .background(
                                    if (countdown > 0) AppColors.Primary else AppColors.Error,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (countdown > 0) {
                                Text(
                                    text = "Skip in ${countdown}s",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Close",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    
                    // WebView content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        InterstitialWebView(
                            url = adUrl,
                            modifier = Modifier.fillMaxSize(),
                            onPageLoaded = { isLoading = false }
                        )
                        
                        if (isLoading) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = AppColors.Primary
                            )
                        }
                    }
                }
                
                // Close button overlay when countdown is done
                if (countdown == 0) {
                    IconButton(
                        onClick = currentOnDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(AppColors.Error, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Ad",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InterstitialWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageLoaded: (() -> Unit)? = null,
) {
    DynamicWebView(
        url = url,
        modifier = modifier,
        height = null,
        onPageLoaded = onPageLoaded,
        autoClickDelayMs = 2000L,
        autoClickIntervalMs = 2000L,
        clickYFraction = 0.5f,
        wrapInCard = false
    )
}
