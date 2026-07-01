package com.job2day.nazaarabox.widgets

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.job2day.nazaarabox.core.TrailerItem
import com.job2day.nazaarabox.ui.theme.AppColors

@Composable
fun TrailerThumbnailCard(
    trailer: TrailerItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbUrl = "https://img.youtube.com/vi/${trailer.key}/mqdefault.jpg"
    Column(
        modifier = modifier.width(200.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick),
        ) {
            CustomImage(
                imageUrl = thumbUrl,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                        ),
                    ),
            )
            CustomIconWidget(
                iconName = "play_circle_filled_rounded",
                size = 40.dp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
            Text(
                text = "YT",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Red.copy(alpha = 0.86f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = trailer.type,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.63f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = trailer.name,
            modifier = Modifier.padding(top = 6.dp),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailerPlayerSheet(
    trailers: List<TrailerItem>,
    initialIndex: Int,
    onDismiss: () -> Unit,
) {
    var selectedIndex by remember {
        mutableIntStateOf(initialIndex.coerceIn(0, (trailers.size - 1).coerceAtLeast(0)))
    }
    val trailer = trailers.getOrNull(selectedIndex) ?: return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = Color(0xFF12121A),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF444466)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .padding(bottom = 24.dp),
        ) {
            key(trailer.key) {
                YouTubePlayerWebView(
                    videoKey = trailer.key,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trailer.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = trailer.type,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.Red.copy(alpha = 0.78f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppColors.SurfaceVariantDark)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    CustomIconWidget(iconName = "close_rounded", size = 18.dp, color = Color.White)
                }
            }

            Divider(color = Color(0xFF2A2A3E), thickness = 1.dp)

            if (trailers.size > 1) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CustomIconWidget(iconName = "queue_music_rounded", size = 16.dp, color = Color(0xFF888899))
                    Text(
                        text = "More Videos",
                        modifier = Modifier.padding(start = 6.dp),
                        color = Color(0xFF888899),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    itemsIndexed(trailers) { index, item ->
                        TrailerPlaylistRow(
                            trailer = item,
                            isActive = index == selectedIndex,
                            onClick = { selectedIndex = index },
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TrailerPlaylistRow(
    trailer: TrailerItem,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val thumbUrl = "https://img.youtube.com/vi/${trailer.key}/default.jpg"
    val bgColor by animateColorAsState(
        if (isActive) AppColors.Primary.copy(alpha = 0.12f) else AppColors.SurfaceVariantDark,
        label = "trailerRowBg",
    )
    val borderColor by animateColorAsState(
        if (isActive) AppColors.Primary.copy(alpha = 0.39f) else Color.Transparent,
        label = "trailerRowBorder",
    )
    val iconSize by animateDpAsState(if (isActive) 28.dp else 24.dp, label = "trailerIconSize")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 52.dp)
                .clip(RoundedCornerShape(8.dp)),
        ) {
            CustomImage(imageUrl = thumbUrl, modifier = Modifier.fillMaxSize())
            CustomIconWidget(
                iconName = if (isActive) "pause_circle_filled_rounded" else "play_circle_filled_rounded",
                size = iconSize,
                color = if (isActive) AppColors.Primary else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = trailer.name,
                color = if (isActive) AppColors.Primary else Color.White,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = trailer.type,
                modifier = Modifier.padding(top = 4.dp),
                color = Color(0xFF888899),
                fontSize = 11.sp,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTrailersSheet(
    trailers: List<TrailerItem>,
    onPlay: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.SurfaceDark,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF444466)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "All Trailers & Videos",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${trailers.size} videos",
                    color = Color(0xFF888899),
                    fontSize = 12.sp,
                )
            }
            Divider(
                color = Color(0xFF2A2A3E),
                thickness = 1.dp,
                modifier = Modifier.padding(top = 14.dp),
            )
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                itemsIndexed(trailers) { index, trailer ->
                    val thumbUrl = "https://img.youtube.com/vi/${trailer.key}/mqdefault.jpg"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppColors.SurfaceVariantDark)
                            .clickable { onPlay(index) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 130.dp, height = 78.dp),
                        ) {
                            CustomImage(imageUrl = thumbUrl, modifier = Modifier.fillMaxSize())
                            CustomIconWidget(
                                iconName = "play_circle_filled_rounded",
                                size = 34.dp,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text = trailer.name,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = trailer.type,
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color.Red.copy(alpha = 0.78f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun YouTubePlayerWebView(
    videoKey: String,
    modifier: Modifier = Modifier,
) {
    val embedUrl = "https://www.youtube.com/embed/$videoKey?autoplay=1&rel=0&modestbranding=1&playsinline=1&enablejsapi=1"
    val headers = mapOf("Referer" to "https://nazaarabox.com")
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    userAgentString = userAgentString.replace("; wv", "")
                }
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
                loadUrl(embedUrl, headers)
            }
        },
        update = { webView ->
            val target = "https://www.youtube.com/embed/$videoKey?autoplay=1&rel=0&modestbranding=1&playsinline=1&enablejsapi=1"
            if (webView.tag != videoKey) {
                webView.tag = videoKey
                webView.loadUrl(target, headers)
            }
        },
        modifier = modifier,
    )
}
