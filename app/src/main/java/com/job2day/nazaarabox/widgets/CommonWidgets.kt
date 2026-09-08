package com.job2day.nazaarabox.widgets

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.ui.theme.AppColors

@Composable
fun CustomImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
) {
    if (imageUrl.isNullOrBlank()) {
        Box(
            modifier = modifier.background(AppColors.SurfaceVariantDark),
            contentAlignment = Alignment.Center,
        ) {
            Text("🎬", fontSize = 24.sp)
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
    }
}

@Composable
fun StatusBadge(rating: Double, modifier: Modifier = Modifier) {
    if (rating <= 0) return
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = AppColors.Accent, modifier = Modifier.size(11.dp))
            Text(
                text = String.format("%.1f", rating),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 2.dp),
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    emoji: String = "",
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false),
        ) {
            if (emoji.isNotBlank()) {
                Text(text = emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (onSeeAll != null) {
            androidx.compose.material3.Surface(
                onClick = onSeeAll,
                shape = RoundedCornerShape(20.dp),
                color = AppColors.Primary.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Primary.copy(alpha = 0.35f)),
            ) {
                Text(
                    text = "See all →",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    color = AppColors.Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    emoji: String = "🎬",
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = emoji, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = message, color = AppColors.TextMuted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun shimmerBrush(
    targetValue: Float = 1000f,
    showShimmer: Boolean = true,
): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color(0xFF1E212B),
            Color(0xFF2C3242),
            Color(0xFF1E212B),
        )
        val transition = rememberInfiniteTransition(label = "shimmerTransition")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "shimmerTranslate",
        )
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero,
        )
    }
}

@Composable
fun LoadingSkeleton(modifier: Modifier = Modifier, height: Int = 180, shape: RoundedCornerShape = RoundedCornerShape(16.dp)) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(shape)
            .background(shimmerBrush()),
    )
}

@Composable
fun LoadingCenter(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppColors.Primary)
    }
}

@Composable
fun MovieGridCard(
    item: MediaItem,
    modifier: Modifier = Modifier,
    showTypeBadge: Boolean = false,
    onClick: () -> Unit,
) {
    val typeLabel = when (item.type) {
        "tv" -> "TV"
        else -> "MOVIE"
    }
    val typeColor = if (item.type == "tv") AppColors.Secondary else AppColors.Primary
    androidx.compose.material3.Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = AppColors.CardDark,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
            ) {
                CustomImage(
                    imageUrl = item.posterUrl,
                    modifier = Modifier.fillMaxSize(),
                )
                if (showTypeBadge) {
                    Text(
                        text = typeLabel,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(typeColor.copy(alpha = 0.85f))
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                StatusBadge(
                    rating = item.rating,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                )
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = AppColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (item.year.isNotBlank()) {
                    Text(text = item.year, color = AppColors.TextMuted, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun BrowseGridCard(
    item: MediaItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val typeLabel = if (item.type == "tv") "TV" else "MOVIE"
    val typeColor = if (item.type == "tv") AppColors.Secondary else AppColors.Primary
    androidx.compose.material3.Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AppColors.CardDark,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
            ) {
                CustomImage(
                    imageUrl = item.posterUrl,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            ),
                        ),
                )
                Text(
                    text = typeLabel,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(typeColor.copy(alpha = 0.85f))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                )
                StatusBadge(
                    rating = item.rating,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                )
            }
            Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)) {
                Text(
                    text = item.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = AppColors.TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (item.year.isNotBlank()) {
                    Text(text = item.year, color = AppColors.TextMuted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun TrendingCard(
    item: MediaItem,
    index: Int = 0,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.width(145.dp),
    ) {
        androidx.compose.material3.Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = AppColors.CardDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp),
        ) {
            Box {
                CustomImage(
                    imageUrl = item.posterUrl,
                    modifier = Modifier.fillMaxSize(),
                )

                // Top-Left Netflix-Style Rank Badge
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFF3366), Color(0xFFFF5E3A))
                            )
                        )
                        .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                        .align(Alignment.TopStart),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "#${index + 1}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                    )
                }

                // Top-Right Star Rating Badge
                if (item.rating > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = AppColors.Accent,
                                modifier = Modifier.size(11.dp),
                            )
                            Text(
                                text = String.format("%.1f", item.rating),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 3.dp),
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = AppColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (item.year.isNotBlank()) item.year else "Trending",
            color = AppColors.TextMuted,
            fontSize = 11.sp,
            maxLines = 1,
        )
    }
}

@Composable
fun TrendingRow(
    items: List<MediaItem>,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    onItemClick: (MediaItem) -> Unit,
) {
    Box(modifier = Modifier.height(265.dp)) {
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(items.size) { index ->
                TrendingCard(
                    item = items[index],
                    index = index,
                    onClick = { onItemClick(items[index]) },
                )
            }
        }
    }
}

@Composable
fun PopularMovieCard(
    item: MediaItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.width(145.dp),
    ) {
        androidx.compose.material3.Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = AppColors.CardDark,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        AppColors.Accent.copy(alpha = 0.40f),
                        Color.White.copy(alpha = 0.08f),
                    )
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp),
        ) {
            Box {
                CustomImage(
                    imageUrl = item.posterUrl,
                    modifier = Modifier.fillMaxSize(),
                )

                // Top-Left Gold Rating Badge
                if (item.rating > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(0.5.dp, AppColors.Accent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = AppColors.Accent,
                                modifier = Modifier.size(11.dp),
                            )
                            Text(
                                text = String.format("%.1f", item.rating),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 3.dp),
                            )
                        }
                    }
                }

                // Top-Right Type Pill (Movie / TV)
                val isTv = item.type.equals("tv", ignoreCase = true)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isTv) AppColors.Secondary.copy(alpha = 0.85f) else AppColors.Primary.copy(alpha = 0.85f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = if (isTv) "TV" else "MOVIE",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                }

                // Bottom subtle gradient scrim
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                            )
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = AppColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (item.year.isNotBlank()) {
                Text(
                    text = item.year,
                    color = AppColors.TextMuted,
                    fontSize = 11.sp,
                )
            }
            if (item.type.isNotBlank()) {
                Text(
                    text = "•",
                    color = AppColors.TextMuted.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                )
                Text(
                    text = if (item.type.equals("tv", ignoreCase = true)) "Series" else "Movie",
                    color = AppColors.TextMuted,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
fun FeaturedBanner(
    items: List<MediaItem>,
    modifier: Modifier = Modifier,
    onItemClick: (MediaItem) -> Unit,
) {
    if (items.isEmpty()) {
        LoadingSkeleton(modifier = modifier.height(470.dp), height = 470)
        return
    }
    val pagerState = rememberPagerState(pageCount = { items.size })

    // Auto-advance every 5 seconds safely without cancelling mid-scroll
    androidx.compose.runtime.LaunchedEffect(items.size) {
        if (items.size > 1) {
            while (true) {
                kotlinx.coroutines.delay(5000L)
                if (!pagerState.isScrollInProgress) {
                    val nextPage = (pagerState.currentPage + 1) % items.size
                    pagerState.animateScrollToPage(
                        page = nextPage,
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 700,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing,
                        ),
                    )
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            key = { index -> items.getOrNull(index)?.id ?: index },
            modifier = Modifier
                .fillMaxWidth()
                .height(470.dp),
        ) { page ->
            val item = items[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onItemClick(item) },
            ) {
                CustomImage(
                    imageUrl = item.backdropUrl.ifBlank { item.posterUrl },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

                // Cinematic Multi-Stop Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Black.copy(alpha = 0.70f),
                                    0.20f to Color.Black.copy(alpha = 0.15f),
                                    0.45f to Color.Transparent,
                                    0.65f to AppColors.BackgroundDark.copy(alpha = 0.75f),
                                    0.88f to AppColors.BackgroundDark.copy(alpha = 0.96f),
                                    1.0f to AppColors.BackgroundDark,
                                ),
                            ),
                        ),
                )

                // Bottom Content Details (Rank, Title, Badges, Buttons all move together)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                ) {
                    // Top 10 Rank Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AppColors.Primary, Color(0xFFFF5252))
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "🔥 # ${page + 1} TRENDING NOW",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = item.title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = (-0.3).sp,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = item.type.uppercase(),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        Text(
                            text = "4K ULTRA HD",
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .border(1.dp, AppColors.Accent.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            color = AppColors.Accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        if (item.rating > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AppColors.Accent,
                                    modifier = Modifier.size(13.dp),
                                )
                                Text(
                                    text = String.format("%.1f", item.rating),
                                    modifier = Modifier.padding(start = 4.dp),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        if (item.year.isNotBlank()) {
                            Text(
                                text = item.year,
                                color = Color(0xFFDDDDDD),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    if (item.genres.isNotEmpty() || item.overview.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (item.genres.isNotEmpty()) item.genres.take(3).joinToString("  •  ") else item.overview,
                            color = Color(0xFFB0B0C0),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        androidx.compose.material3.Button(
                            onClick = { onItemClick(item) },
                            shape = RoundedCornerShape(24.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary,
                                contentColor = Color.White,
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                        ) {
                            Text(text = "▶  Watch Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        androidx.compose.material3.OutlinedButton(
                            onClick = { onItemClick(item) },
                            shape = RoundedCornerShape(24.dp),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        ) {
                            Text(text = "ℹ  Details", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        if (items.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(items.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(5.dp)
                            .width(if (isSelected) 24.dp else 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isSelected) AppColors.Primary else Color.White.copy(alpha = 0.25f),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
fun MediaHorizontalRow(
    items: List<MediaItem>,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    onItemClick: (MediaItem) -> Unit,
) {
    TrendingRow(items = items, contentPadding = contentPadding, onItemClick = onItemClick)
}

@Composable
fun AnimeGridCard(
    item: MediaItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val isMovie = item.type == "movie"
    val badgeLabel = if (isMovie) "MOVIE" else "SHOW"
    val badgeColor = if (isMovie) AppColors.TabAnime else Color(0xFF9B59B6)
    androidx.compose.material3.Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AppColors.CardDark,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
            ) {
                CustomImage(
                    imageUrl = item.posterUrl,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f)),
                            ),
                        ),
                )
                Text(
                    text = badgeLabel,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.78f))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                if (item.rating > 0) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AppColors.Accent, modifier = Modifier.size(11.dp))
                        Text(
                            text = String.format("%.1f", item.rating),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 2.dp),
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp)) {
                Text(
                    text = item.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = AppColors.TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (item.year.isNotBlank()) {
                    Text(text = item.year, color = AppColors.TextMuted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun SimilarTitleCard(
    item: MediaItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.width(135.dp),
    ) {
        androidx.compose.material3.Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = AppColors.CardDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
        ) {
            Box {
                CustomImage(
                    imageUrl = item.posterUrl,
                    modifier = Modifier.fillMaxSize(),
                )
                if (item.rating > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(7.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = AppColors.Accent,
                                modifier = Modifier.size(10.dp),
                            )
                            Text(
                                text = String.format("%.1f", item.rating),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 3.dp),
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = item.title,
            color = AppColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (item.year.isNotBlank()) item.year else "Featured",
            color = AppColors.TextMuted,
            fontSize = 11.sp,
            maxLines = 1,
        )
    }
}
