package com.job2day.nazaarabox.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.job2day.nazaarabox.core.MediaItem
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.job2day.nazaarabox.navigation.navigateToActor
import com.job2day.nazaarabox.navigation.navigateToDetail
import com.job2day.nazaarabox.navigation.navigateToPlayer
import com.job2day.nazaarabox.navigation.navigateToSeason
import com.job2day.nazaarabox.routes.AppRoutes
import com.job2day.nazaarabox.ui.components.DetailOverlayAppBar
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AppActions
import com.job2day.nazaarabox.widgets.AllTrailersSheet
import com.job2day.nazaarabox.widgets.DetailBottomActionBar
import com.job2day.nazaarabox.widgets.DetailReviewCard
import com.job2day.nazaarabox.widgets.DetailSectionHeader
import com.job2day.nazaarabox.widgets.DownloadLinksSheet
import com.job2day.nazaarabox.widgets.FullCastSheet
import com.job2day.nazaarabox.widgets.LoadingCenter
import com.job2day.nazaarabox.widgets.MoreMenuSheet
import com.job2day.nazaarabox.widgets.SectionHeader
import com.job2day.nazaarabox.widgets.SimilarTitleCard
import com.job2day.nazaarabox.widgets.TrailerPlayerSheet
import com.job2day.nazaarabox.widgets.TrailerThumbnailCard
import com.job2day.nazaarabox.ads.InlineBannerAd
import com.job2day.nazaarabox.ads.InlineCardAd
import kotlinx.coroutines.delay

@Composable
fun DetailScreen(
    navController: NavController,
    viewModel: DetailViewModel = viewModel(),
) {
    val initialItem = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("mediaItem")
        ?.let(AppRoutes::decodeItem)
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var showDownloads by remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }
    var showAllTrailers by remember { mutableStateOf(false) }
    var showAllCast by remember { mutableStateOf(false) }
    var trailerPlayerIndex by remember { mutableIntStateOf(-1) }
    var pendingTrailerIndex by remember { mutableIntStateOf(-1) }
    var isInWatchlist by remember { mutableStateOf(false) }

    val showAppBarTitle by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 200
        }
    }

    LaunchedEffect(initialItem) {
        initialItem?.let { viewModel.load(it) }
    }

    LaunchedEffect(pendingTrailerIndex) {
        if (pendingTrailerIndex >= 0) {
            delay(200)
            trailerPlayerIndex = pendingTrailerIndex
            pendingTrailerIndex = -1
        }
    }

    if (initialItem == null) {
        LoadingCenter()
        return
    }

    if (state.isLoading || state.item == null) {
        LoadingCenter()
        return
    }

    val item = state.item!!

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (com.job2day.nazaarabox.utils.AdManager.isLiveMode) 
                    Modifier.navigationBarsPadding() 
                else 
                    Modifier
            )
            .background(AppColors.BackgroundDark),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
        ) {
            item { DetailHeroHeader(item = item) }
            item { DetailTitleSection(item = item) }

            if (item.genres.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(item.genres) { genre ->
                            androidx.compose.material3.Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = AppColors.Primary.copy(alpha = 0.15f),
                            ) {
                                androidx.compose.material3.Text(
                                    text = genre,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = AppColors.Primary,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
            }

item { InlineBannerAd() }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                ) {
                    items(4) {
                        InlineCardAd(
                            modifier = Modifier
                                .width(140.dp)
                                .height(200.dp),
                        )
                    }
                }
            }

            val overview = item.overview
                if (overview.isNotBlank()) {
                    item { SectionHeader(title = "Overview", emoji = "📖") }
                    item {
                        androidx.compose.material3.Text(
                            text = overview,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = AppColors.TextPrimary,
                            maxLines = if (state.isOverviewExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                        androidx.compose.material3.Text(
                            text = if (state.isOverviewExpanded) "Show less" else "See More",
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { viewModel.toggleOverview() },
                            color = AppColors.Primary,
                        )
                    }
                }

            if (state.seasons.isNotEmpty()) {
                item {
                    DetailSeasonsSection(
                        seasons = state.seasons,
                        onSeasonClick = { season ->
                            navController.navigateToSeason(item, season.seasonNumber, season.name)
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (state.trailers.isNotEmpty()) {
                item {
                    DetailSectionHeader(
                        title = "Trailers & Videos",
                        iconName = "play_circle_filled_rounded",
                        trailing = if (state.trailers.size > 1) {
                            {
                                androidx.compose.material3.Text(
                                    text = "See all ${state.trailers.size}",
                                    color = AppColors.Primary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable { showAllTrailers = true },
                                )
                            }
                        } else null,
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.trailers.size) { index ->
                            TrailerThumbnailCard(
                                trailer = state.trailers[index],
                                onClick = { trailerPlayerIndex = index },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (state.cast.isNotEmpty()) {
                item {
                    DetailSectionHeader(
                        title = "Cast",
                        iconName = "people_rounded",
                        trailing = {
                            androidx.compose.material3.Text(
                                text = "See all ${state.cast.size}",
                                color = AppColors.Primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { showAllCast = true },
                            )
                        },
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(state.cast) { member ->
                            CastMemberCard(
                                photoUrl = member.photoUrl,
                                name = member.name,
                                character = member.character,
                                onClick = { navController.navigateToActor(member.id) },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (state.reviews.isNotEmpty()) {
                item {
                    DetailSectionHeader(
                        title = "Reviews",
                        iconName = "rate_review_rounded",
                        trailing = {
                            androidx.compose.material3.Text(
                                text = "${state.reviews.size} review${if (state.reviews.size != 1) "s" else ""}",
                                color = Color(0xFF888899),
                                fontSize = 12.sp,
                            )
                        },
                    )
                }
                items(state.reviews.take(2)) { review ->
                    DetailReviewCard(review = review)
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            if (state.similar.isNotEmpty()) {
                item { SectionHeader(title = "Similar Titles", emoji = "🎯") }
                item {
                    val similarWithAds = buildList<MediaItem?> {
                        addAll(state.similar)
                        state.similar.forEachIndexed { index, _ ->
                            if ((index + 1) % 4 == 0 && index < state.similar.lastIndex) {
                                add(null)
                            }
                        }
                    }
                    Box(modifier = Modifier.height(210.dp)) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(similarWithAds) { entry ->
                                if (entry != null) {
                                    SimilarTitleCard(
                                        item = entry,
                                        onClick = { navController.navigateToDetail(entry) },
                                    )
                                } else if (com.job2day.nazaarabox.utils.AdManager.isAdsEnabled && com.job2day.nazaarabox.utils.AdManager.isWebviewAdsEnabled) {
                                    InlineCardAd(
                                        modifier = Modifier.width(140.dp),
                                        label = "",
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = if (com.job2day.nazaarabox.utils.AdManager.isLiveMode) Modifier.height(100.dp) else Modifier)
                }
            }
        }

        DetailOverlayAppBar(
            title = item.title,
            showTitle = showAppBarTitle,
            onBack = { navController.popBackStack() },
            onShare = { AppActions.shareItem(context, item) },
            onMore = { showMore = true },
            modifier = Modifier.align(Alignment.TopCenter),
        )

        DetailBottomActionBar(
            item = item,
            seasons = state.seasons,
            isInWatchlist = isInWatchlist,
            onWatchlistToggle = { isInWatchlist = !isInWatchlist },
            onPlay = { playItem -> navController.navigateToPlayer(playItem) },
            onDownload = { showDownloads = true },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showMore) {
        MoreMenuSheet(
            title = item.title,
            onShare = { AppActions.shareItem(context, item) },
            onDismiss = { showMore = false },
        )
    }

    if (showDownloads) {
        DownloadLinksSheet(
            item = item,
            onDismiss = { showDownloads = false },
        )
    }

    if (trailerPlayerIndex >= 0) {
        TrailerPlayerSheet(
            trailers = state.trailers,
            initialIndex = trailerPlayerIndex,
            onDismiss = { trailerPlayerIndex = -1 },
        )
    }

    if (showAllTrailers) {
        AllTrailersSheet(
            trailers = state.trailers,
            onPlay = { index ->
                showAllTrailers = false
                pendingTrailerIndex = index
            },
            onDismiss = { showAllTrailers = false },
        )
    }

    if (showAllCast) {
        FullCastSheet(
            cast = state.cast,
            onPersonTap = { member -> navController.navigateToActor(member.id) },
            onDismiss = { showAllCast = false },
        )
    }
}
