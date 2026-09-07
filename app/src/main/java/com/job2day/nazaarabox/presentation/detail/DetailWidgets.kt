package com.job2day.nazaarabox.presentation.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.job2day.nazaarabox.core.AppConfig
import com.job2day.nazaarabox.core.CastMember
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.ReviewItem
import com.job2day.nazaarabox.core.SeasonItem
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.widgets.CustomIconWidget
import com.job2day.nazaarabox.widgets.CustomImage

/**
 * Modern Cinematic Hero Header with full-bleed backdrop art,
 * gradient scrim, and embedded poster badge.
 */
@Composable
fun DetailHeroHeader(
    item: MediaItem,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(390.dp),
    ) {
        // High-res Backdrop Image
        CustomImage(
            imageUrl = item.backdropUrl.ifBlank { item.posterUrl },
            modifier = Modifier.fillMaxSize(),
        )

        // Multi-stop Vertical Gradient Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.65f),
                            0.25f to Color.Transparent,
                            0.60f to AppColors.BackgroundDark.copy(alpha = 0.70f),
                            0.88f to AppColors.BackgroundDark.copy(alpha = 0.95f),
                            1.0f to AppColors.BackgroundDark,
                        ),
                    ),
                ),
        )

        // Bottom Hero Info Card: Poster + Badges
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            // Rounded Poster with Glassmorphic Border & Shadow
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppColors.CardDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                shadowElevation = 14.dp,
                modifier = Modifier
                    .width(115.dp)
                    .height(172.dp),
            ) {
                CustomImage(
                    imageUrl = item.posterUrl.ifBlank { item.backdropUrl },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Metadata Badges Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Type Badge & Quality Tag
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val isTv = item.type.equals("tv", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isTv) AppColors.Secondary.copy(alpha = 0.85f)
                                else AppColors.Primary.copy(alpha = 0.85f)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = if (isTv) "TV SERIES" else "MOVIE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(0.8.dp, AppColors.Accent.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "4K ULTRA HD",
                            color = AppColors.Accent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                // Rating Pill
                if (item.rating > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.70f))
                            .border(0.5.dp, AppColors.Accent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = " / 10",
                                color = AppColors.TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                // Year & Runtime / Status
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (item.year.isNotBlank()) {
                        Text(
                            text = item.year,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (item.runtime.isNotBlank()) {
                        Text(text = "•", color = AppColors.TextMuted, fontSize = 12.sp)
                        Text(
                            text = item.runtime,
                            color = AppColors.TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Title and Genre Chips Row
 */
@Composable
fun DetailTitleHeader(item: MediaItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            text = item.title,
            color = AppColors.TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.4).sp,
            lineHeight = 32.sp,
        )

        if (item.genres.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(item.genres) { genre ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    ) {
                        Text(
                            text = genre,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            color = Color(0xFFDDDDED),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Primary In-Content CTA Action Row (Watch Now, Watchlist, Download, Share)
 */
@Composable
fun DetailHeroActions(
    item: MediaItem,
    isInWatchlist: Boolean,
    onPlay: () -> Unit,
    onWatchlistToggle: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        // Main Watch Button
        Button(
            onClick = onPlay,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary,
                contentColor = Color.White,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Watch Now",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary Action Row (Watchlist, Download, Share)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Watchlist Button (Takes primary secondary space)
            OutlinedButton(
                onClick = onWatchlistToggle,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isInWatchlist) AppColors.Primary else Color.White,
                    containerColor = if (isInWatchlist) AppColors.Primary.copy(alpha = 0.15f) else Color.Transparent,
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isInWatchlist) AppColors.Primary else Color.White.copy(alpha = 0.25f),
                ),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isInWatchlist) AppColors.Primary else Color.White,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isInWatchlist) "In Watchlist" else "Watchlist",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }
            }

            // Download Button
            Surface(
                onClick = onDownload,
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Share Button
            Surface(
                onClick = onShare,
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }
    }
}

/**
 * Expandable Storyline / Overview Card
 */
@Composable
fun DetailOverviewCard(
    overview: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    if (overview.isBlank()) return

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppColors.CardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Storyline",
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = overview,
                color = Color(0xFFC8C8D8),
                fontSize = 13.sp,
                lineHeight = 20.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (isExpanded) "Show Less ▲" else "Read More ▼",
                color = AppColors.Primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { onToggle() },
            )
        }
    }
}

/**
 * Segmented Tab Selector Row
 */
@Composable
fun DetailTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tabs.size) { index ->
            val isSelected = selectedIndex == index
            Surface(
                onClick = { onTabSelected(index) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) AppColors.Primary else Color(0xFF181B26),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) AppColors.Primary else Color.White.copy(alpha = 0.10f),
                ),
            ) {
                Text(
                    text = tabs[index],
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) Color.White else Color(0xFFA5A5BC),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

/**
 * Modern Cast Member Card with Rounded Framing
 */
@Composable
fun CastMemberCard(
    photoUrl: String,
    name: String,
    character: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(88.dp),
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.15f)),
            color = AppColors.CardDark,
        ) {
            CustomImage(
                imageUrl = photoUrl,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            color = AppColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = character,
            color = AppColors.TextMuted,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * TV Show Seasons Section
 */
@Composable
fun DetailSeasonsSection(
    seasons: List<SeasonItem>,
    onSeasonClick: (SeasonItem) -> Unit,
) {
    if (seasons.isEmpty()) return
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Tv, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
            Text(
                text = "Seasons & Episodes",
                modifier = Modifier.padding(start = 8.dp),
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${seasons.size} season${if (seasons.size != 1) "s" else ""}",
                color = AppColors.TextMuted,
                fontSize = 12.sp,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.height(235.dp)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(seasons.size) { index ->
                    DetailSeasonCard(season = seasons[index], onClick = { onSeasonClick(seasons[index]) })
                }
            }
        }
    }
}

/**
 * TV Show Season Card
 */
@Composable
fun DetailSeasonCard(
    season: SeasonItem,
    onClick: () -> Unit,
) {
    val posterUrl = if (season.posterPath.isNotBlank()) {
        if (season.posterPath.startsWith("http")) season.posterPath
        else "${AppConfig.IMAGE_BASE}/w185${season.posterPath}"
    } else {
        ""
    }
    val year = if (season.airDate.length >= 4) season.airDate.substring(0, 4) else ""
    Column(modifier = Modifier.width(125.dp)) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = AppColors.CardDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        ) {
            Box(
                modifier = Modifier
                    .width(125.dp)
                    .height(170.dp),
            ) {
                if (posterUrl.isNotBlank()) {
                    CustomImage(imageUrl = posterUrl, modifier = Modifier.fillMaxSize())
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColors.SurfaceVariantDark),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Tv,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.24f),
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.5f to Color.Transparent,
                                    1f to Color.Black.copy(alpha = 0.70f),
                                ),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AppColors.Primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = season.name,
            color = AppColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = buildString {
                append("${season.episodeCount} episodes")
                if (year.isNotBlank()) append(" • $year")
            },
            color = AppColors.TextMuted,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


/**
 * Production Specifications & Technical Details
 */
@Composable
fun DetailSpecsSection(item: MediaItem) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppColors.CardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Information & Specs",
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))

            SpecItem(label = "Format", value = if (item.type == "tv") "TV Series" else "Feature Film")
            if (item.year.isNotBlank()) SpecItem(label = "Release Year", value = item.year)
            if (item.runtime.isNotBlank()) SpecItem(label = "Runtime", value = item.runtime)
            if (item.status.isNotBlank()) SpecItem(label = "Status", value = item.status)
            if (item.genres.isNotEmpty()) SpecItem(label = "Genres", value = item.genres.joinToString(", "))
            SpecItem(label = "Resolution", value = "4K Ultra HD • HDR10")
            SpecItem(label = "Audio", value = "Dolby Atmos • 5.1 Surround")
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = AppColors.TextMuted, fontSize = 12.sp)
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
