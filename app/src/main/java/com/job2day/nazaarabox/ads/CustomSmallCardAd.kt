package com.job2day.nazaarabox.ads

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.widgets.DynamicWebView

@Composable
fun CustomSmallCardAd(
    adUrl: String = AdManager.webviewAdUrl,
    backgroundColor: Color = AppColors.SurfaceVariantDark,
    modifier: Modifier = Modifier,
    showClose: Boolean = true,
) {
    if (!AdManager.isAdsEnabled) {
        return
    }

    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(true) }
    var isWebViewLoaded by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    val effectiveUrl = remember(adUrl) {
        AdManager.sanitizeAdUrl(adUrl.ifBlank { AdManager.webviewAdUrl })
    }

    if (isVisible) {
        Surface(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF161726),
            border = BorderStroke(1.dp, Color(0xFF333550)),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Live Article WebView
                if (!hasError) {
                    DynamicWebView(
                        url = effectiveUrl,
                        modifier = Modifier.fillMaxSize(),
                        height = null,
                        autoClickDelayMs = null,
                        wrapInCard = false,
                        onPageLoaded = {
                            isWebViewLoaded = true
                        },
                        onError = {
                            hasError = true
                        }
                    )
                }

                // 2. High-Visibility Sponsored Article Card Overlay (while loading or if error occurs)
                if (!isWebViewLoaded || hasError) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF22243A),
                                        Color(0xFF141524),
                                    )
                                )
                            )
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effectiveUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Top Tag
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFF59E0B))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SPONSORED",
                                        color = Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open",
                                    tint = Color(0xFF93C5FD),
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // Center Content
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "📰 Article Review",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap to read trending drama news & articles",
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Bottom Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppColors.Primary)
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Read Article",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Optional Close Button
                if (showClose) {
                    IconButton(
                        onClick = { isVisible = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .padding(2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Ad",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
