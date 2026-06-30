package com.job2day.nazaarabox.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Tv
import com.job2day.nazaarabox.core.AppConfig
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.SeasonItem
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.widgets.CustomImage

@Composable
fun DetailHeroHeader(item: MediaItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp),
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
                            0f to Color.Black.copy(alpha = 0.24f),
                            0.4f to Color.Black.copy(alpha = 0.4f),
                            0.75f to AppColors.BackgroundDark.copy(alpha = 0.86f),
                            1f to AppColors.BackgroundDark,
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 16.dp)
                .width(110.dp)
                .height(165.dp)
                .shadow(20.dp, RoundedCornerShape(14.dp), spotColor = AppColors.Primary.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
        ) {
            CustomImage(
                imageUrl = item.posterUrl.ifBlank { item.backdropUrl },
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.End,
        ) {
            val isMovie = item.type == "movie"
            val typeColor = if (isMovie) AppColors.Primary else AppColors.Secondary
            Text(
                text = if (isMovie) "🎬 Movie" else "📺 TV Show",
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(typeColor.copy(alpha = 0.16f))
                    .border(1.dp, typeColor.copy(alpha = 0.47f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                color = typeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
            if (item.rating > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.63f))
                        .border(1.dp, AppColors.ratingColor(item.rating).copy(alpha = 0.47f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = AppColors.ratingColor(item.rating),
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = String.format("%.1f", item.rating),
                        modifier = Modifier.padding(start = 4.dp),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
fun DetailTitleSection(item: MediaItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = item.title,
            color = AppColors.TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
            if (item.year.isNotBlank()) {
                Text(item.year, color = AppColors.TextMuted, fontSize = 13.sp)
            }
            if (item.runtime.isNotBlank()) {
                Text(item.runtime, color = AppColors.TextMuted, fontSize = 13.sp)
            }
            if (item.status.isNotBlank()) {
                Text(item.status, color = AppColors.TextMuted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun CastMemberCard(
    photoUrl: String,
    name: String,
    character: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp),
    ) {
        androidx.compose.material3.Surface(
            onClick = onClick,
            shape = CircleShape,
        ) {
            CustomImage(
                imageUrl = photoUrl,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
            )
        }
        Text(
            text = name,
            color = AppColors.TextPrimary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = character,
            color = AppColors.TextMuted,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}

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
                text = "Seasons",
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
        Box(modifier = Modifier.height(200.dp)) {
            androidx.compose.foundation.lazy.LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            ) {
                items(seasons.size) { index ->
                    DetailSeasonCard(season = seasons[index], onClick = { onSeasonClick(seasons[index]) })
                }
            }
        }
    }
}

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
    Column(modifier = Modifier.width(110.dp)) {
        androidx.compose.material3.Surface(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            color = AppColors.SurfaceVariantDark,
        ) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(140.dp),
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
                        Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White.copy(alpha = 0.24f), modifier = Modifier.size(32.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.5f to Color.Transparent,
                                    1f to Color.Black.copy(alpha = 0.55f),
                                ),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AppColors.Primary.copy(alpha = 0.86f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Text(
            text = season.name,
            modifier = Modifier.padding(top = 6.dp),
            color = AppColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
        Text(
            text = buildString {
                append("${season.episodeCount} ep")
                if (year.isNotBlank()) append(" · $year")
            },
            color = AppColors.TextMuted,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}
