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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (emoji.isNotBlank()) "$emoji  $title" else title,
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        if (onSeeAll != null) {
            Text(
                text = "See all →",
                color = AppColors.Primary,
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { onSeeAll() },
            )
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
fun LoadingSkeleton(modifier: Modifier = Modifier, height: Int = 180) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.SurfaceVariantDark),
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
        modifier = modifier.width(140.dp),
    ) {
        androidx.compose.material3.Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = AppColors.CardDark,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        ) {
            Box {
                CustomImage(
                    imageUrl = item.posterUrl,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.Primary)
                        .align(Alignment.TopStart),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${index + 1}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                StatusBadge(
                    rating = item.rating,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = AppColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun TrendingRow(
    items: List<MediaItem>,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    onItemClick: (MediaItem) -> Unit,
) {
    Box(modifier = Modifier.height(220.dp)) {
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
fun FeaturedBanner(
    items: List<MediaItem>,
    modifier: Modifier = Modifier,
    onItemClick: (MediaItem) -> Unit,
) {
    if (items.isEmpty()) {
        LoadingSkeleton(modifier = modifier.height(420.dp), height = 420)
        return
    }
    val pagerState = rememberPagerState(pageCount = { items.size })
    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
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
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to Color.Transparent,
                                    0.4f to AppColors.BackgroundDark.copy(alpha = 0.78f),
                                    0.75f to AppColors.BackgroundDark.copy(alpha = 0.92f),
                                    1f to AppColors.BackgroundDark,
                                ),
                            ),
                        ),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.type.uppercase(),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AppColors.Primary.copy(alpha = 0.78f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        if (item.rating > 0) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = AppColors.Accent,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(14.dp),
                            )
                            Text(
                                text = String.format("%.1f", item.rating),
                                modifier = Modifier.padding(start = 4.dp),
                                color = AppColors.Accent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Text(
                        text = item.title,
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (item.genres.isNotEmpty()) {
                        Text(
                            text = item.genres.joinToString(" • "),
                            modifier = Modifier.padding(top = 6.dp),
                            color = Color(0xFFAAAAAA),
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
        if (items.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(items.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(if (selected) 20.dp else 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (selected) AppColors.Primary else Color(0xFF444466),
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
        modifier = modifier.width(120.dp),
    ) {
        androidx.compose.material3.Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = AppColors.CardDark,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            ) {
                CustomImage(
                    imageUrl = item.posterUrl,
                    modifier = Modifier.fillMaxSize(),
                )
                StatusBadge(
                    rating = item.rating,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.title,
            color = AppColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (item.year.isNotBlank()) {
            Text(text = item.year, color = AppColors.TextMuted, fontSize = 11.sp)
        }
    }
}
