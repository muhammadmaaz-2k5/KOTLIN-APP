package com.job2day.nazaarabox.presentation.season

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.job2day.nazaarabox.core.EpisodeItem
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.navigation.navigateToPlayer
import com.job2day.nazaarabox.routes.AppRoutes
import com.job2day.nazaarabox.services.MediaRepository
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.utils.MediaParser
import com.job2day.nazaarabox.ads.FullWidthAdBanner
import com.job2day.nazaarabox.widgets.CustomImage
import com.job2day.nazaarabox.widgets.LoadingCenter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonScreen(navController: NavController) {
    val handle = navController.previousBackStackEntry?.savedStateHandle
    val showItem = handle?.get<String>("mediaItem")?.let(AppRoutes::decodeItem)
    val seasonNumber = handle?.get<Int>("seasonNumber") ?: 1
    val seasonName = handle?.get<String>("seasonName").orEmpty()
    var episodes by remember { mutableStateOf<List<EpisodeItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val repository = remember { MediaRepository() }

    LaunchedEffect(showItem, seasonNumber) {
        if (showItem != null) {
            episodes = repository.getEpisodes(showItem.id, seasonNumber)
            loading = false
        }
    }

    if (showItem == null) {
        LoadingCenter()
        return
    }

    Scaffold(
        containerColor = AppColors.BackgroundDark,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(seasonName.ifBlank { "Season $seasonNumber" }, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.BackgroundDark,
                    titleContentColor = AppColors.TextPrimary,
                ),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                loading -> LoadingCenter()
                episodes.isEmpty() -> Text(
                "No episodes found",
                modifier = Modifier.padding(16.dp),
                color = AppColors.TextMuted,
            )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (AdManager.isAdsEnabled && AdManager.isWebviewAdsEnabled) {
                        item {
                            FullWidthAdBanner(
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                    }
                    items(episodes) { episode ->
                        EpisodeRow(
                            episode = episode,
                            showItem = showItem,
                            isLiveMode = AdManager.isLiveMode,
                            onPlay = {
                                val playerItem = showItem.copy(
                                    season = seasonNumber,
                                    episode = episode.episodeNumber,
                                    title = "${showItem.title} S${seasonNumber}E${episode.episodeNumber}",
                                )
                                navController.navigateToPlayer(playerItem)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    episode: EpisodeItem,
    showItem: MediaItem,
    isLiveMode: Boolean,
    onPlay: () -> Unit,
) {
    androidx.compose.material3.Surface(
        onClick = { if (isLiveMode) onPlay() },
        shape = RoundedCornerShape(14.dp),
        color = AppColors.SurfaceDark,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CustomImage(
                imageUrl = MediaParser.imageUrl(episode.stillPath, "w300"),
                modifier = Modifier
                    .weight(0.35f)
                    .padding(0.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "E${episode.episodeNumber} • ${episode.name}",
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
                if (episode.overview.isNotBlank()) {
                    Text(
                        text = episode.overview,
                        color = AppColors.TextMuted,
                        maxLines = 2,
                        fontSize = 12.sp,
                    )
                }
            }
            if (isLiveMode) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = AppColors.Primary)
            }
        }
    }
}
