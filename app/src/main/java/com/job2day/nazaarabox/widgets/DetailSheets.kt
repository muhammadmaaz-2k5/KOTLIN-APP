package com.job2day.nazaarabox.widgets

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.job2day.nazaarabox.core.DownloadLink
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.SeasonItem
import com.job2day.nazaarabox.services.MediaRepository
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager

@Composable
fun DetailBottomActionBar(
    item: MediaItem,
    seasons: List<SeasonItem>,
    isInWatchlist: Boolean,
    onWatchlistToggle: () -> Unit,
    onPlay: (MediaItem) -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.BackgroundDark.copy(alpha = 0.85f))
            .border(0.5.dp, Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconActionButton(
            icon = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            isActive = isInWatchlist,
            onClick = onWatchlistToggle,
        )
        if (AdManager.isLiveMode) {
            Spacer(modifier = Modifier.width(10.dp))
            IconActionButton(
                icon = Icons.Default.Download,
                isActive = false,
                onClick = onDownload,
            )
            Spacer(modifier = Modifier.width(10.dp))
            WatchButton(
                item = item,
                seasons = seasons,
                modifier = Modifier.weight(1f),
                onPlay = onPlay,
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun IconActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val color = if (isActive) AppColors.Primary else AppColors.TextMuted
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isActive) AppColors.Primary.copy(alpha = 0.16f) else AppColors.SurfaceDark)
            .border(1.dp, if (isActive) AppColors.Primary else AppColors.Outline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun WatchButton(
    item: MediaItem,
    seasons: List<SeasonItem>,
    modifier: Modifier = Modifier,
    onPlay: (MediaItem) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val isTv = item.type == "tv"
    val label = if (isTv) "Select Episode" else "Watch Now"
    val icon = if (isTv) Icons.Default.VideoLibrary else Icons.Default.PlayArrow

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(AppColors.Primary, AppColors.Primary.copy(alpha = 0.78f)),
                ),
            )
            .clickable {
                if (isTv) showPicker = true else onPlay(item)
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            Text(
                text = label,
                modifier = Modifier.padding(start = 6.dp),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }

    if (showPicker) {
        EpisodePickerSheet(
            item = item,
            seasons = seasons,
            onPlay = { season, episode ->
                showPicker = false
                onPlay(
                    item.copy(
                        season = season,
                        episode = episode,
                        title = "${item.title} · S${season}E$episode",
                    ),
                )
            },
            onDismiss = { showPicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EpisodePickerSheet(
    item: MediaItem,
    seasons: List<SeasonItem>,
    onPlay: (season: Int, episode: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val seasonList = seasons.ifEmpty {
        List(1) { SeasonItem(name = "Season 1", seasonNumber = 1, episodeCount = 12) }
    }
    var selectedSeasonIndex by remember { mutableIntStateOf(0) }
    val currentSeason = seasonList.getOrNull(selectedSeasonIndex)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = AppColors.SurfaceDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AppColors.Outline),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, color = Color.White, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Select a season & episode", color = AppColors.TextMuted, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.SurfaceVariantDark)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            if (seasonList.size > 1) {
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    seasonList.forEachIndexed { index, season ->
                        val selected = index == selectedSeasonIndex
                        Text(
                            text = season.name,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) AppColors.Primary else AppColors.SurfaceVariantDark)
                                .border(1.dp, if (selected) AppColors.Primary else AppColors.Outline, RoundedCornerShape(20.dp))
                                .clickable { selectedSeasonIndex = index }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            color = if (selected) Color.White else AppColors.TextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppColors.SurfaceVariantDark),
            )
            if (currentSeason == null) {
                Text(
                    "No episodes found",
                    modifier = Modifier.padding(32.dp),
                    color = AppColors.TextMuted,
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(320.dp),
                ) {
                    items((1..currentSeason.episodeCount.coerceAtLeast(1)).toList()) { ep ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.SurfaceDark)
                                .border(1.dp, AppColors.SurfaceVariantDark, RoundedCornerShape(10.dp))
                                .clickable { onPlay(currentSeason.seasonNumber, ep) }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("E$ep", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = AppColors.Primary.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.End),
                                )
                    }
                }
            }
        }
    }
}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadLinksSheet(
    item: MediaItem,
    season: Int? = null,
    episode: Int? = null,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { MediaRepository() }
    var links by remember { mutableStateOf<List<DownloadLink>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val mediaType = if (item.type == "tv") "tv" else "movie"
    val displayTitle = if (season != null && episode != null) {
        "${item.title} · S${season}E$episode"
    } else {
        item.title
    }

    LaunchedEffect(item.id, season, episode) {
        isLoading = true
        links = repository.getDownloadLinks(mediaType, item.id, season, episode)
        isLoading = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.SurfaceDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AppColors.Outline),
            )
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = AppColors.Primary)
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text("Download", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(displayTitle, color = AppColors.TextMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.Primary.copy(alpha = 0.06f))
                    .border(1.dp, AppColors.Primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
            ) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(16.dp))
                Text(
                    text = "Links open in your browser. Download availability depends on the source.",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            when {
                isLoading -> CircularProgressIndicator(
                    color = AppColors.Primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(40.dp),
                )
                links.isEmpty() -> Column(
                    modifier = Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("No download links added yet", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(
                        "An admin can add download links via the Download Manager in the Admin Panel.",
                        modifier = Modifier.padding(top = 6.dp),
                        color = AppColors.TextMuted,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
                else -> links.forEach { link ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                                onDismiss()
                            }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.SurfaceVariantDark),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(link.serverIcon.ifBlank { "🔗" }, fontSize = 20.sp)
                        }
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(
                                link.serverName.ifBlank { link.label },
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                            val subtitle = buildString {
                                append(link.quality.ifBlank { "1080p" })
                                append(" · ")
                                append(link.language.ifBlank { "English" })
                                if (link.fileSize.isNotBlank()) append(" · ${link.fileSize}")
                                if (link.notes.isNotBlank()) append(" · ${link.notes}")
                            }
                            Text(subtitle, color = AppColors.TextMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text(
                            text = "Get Link",
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.Primary.copy(alpha = 0.12f))
                                .border(1.dp, AppColors.Primary.copy(alpha = 0.31f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            color = AppColors.Primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
