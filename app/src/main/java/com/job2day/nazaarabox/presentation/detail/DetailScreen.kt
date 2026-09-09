package com.job2day.nazaarabox.presentation.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.job2day.nazaarabox.ads.AdMobNativeMediumAd
import com.job2day.nazaarabox.ads.FullWidthAdBanner
import com.job2day.nazaarabox.ads.InlineBannerAd
import com.job2day.nazaarabox.ads.InlineCardAd
import com.job2day.nazaarabox.ads.StickyCollapsibleBannerAd
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.navigation.navigateToActor
import com.job2day.nazaarabox.navigation.navigateToDetail
import com.job2day.nazaarabox.navigation.navigateToPlayer
import com.job2day.nazaarabox.navigation.navigateToSeason
import com.job2day.nazaarabox.routes.AppRoutes
import com.job2day.nazaarabox.ui.components.DetailOverlayAppBar
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.utils.AppActions
import com.job2day.nazaarabox.widgets.DetailBottomActionBar
import com.job2day.nazaarabox.widgets.DetailReviewCard
import com.job2day.nazaarabox.widgets.EmptyState
import com.job2day.nazaarabox.widgets.EpisodePickerSheet
import com.job2day.nazaarabox.widgets.FullCastSheet
import com.job2day.nazaarabox.widgets.LoadingCenter
import com.job2day.nazaarabox.widgets.MoreMenuSheet
import com.job2day.nazaarabox.widgets.SectionHeader
import com.job2day.nazaarabox.widgets.SimilarTitleCard

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

    var showMore by remember { mutableStateOf(false) }
    var showAllCast by remember { mutableStateOf(false) }
    var showEpisodePicker by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val showAppBarTitle by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 240
        }
    }

    LaunchedEffect(initialItem) {
        initialItem?.let { viewModel.load(it) }
    }

    if (initialItem == null || state.isLoading || state.item == null) {
        LoadingCenter()
        return
    }

    val item = state.item!!
    val isTv = item.type.equals("tv", ignoreCase = true)

    // Scalable Segmented Tabs definition
    val tabs = remember(isTv) {
        if (isTv) {
            listOf("Episodes", "More Like This", "Cast & Info")
        } else {
            listOf("More Like This", "Cast & Crew", "Details & Reviews")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (AdManager.isLiveMode) Modifier.navigationBarsPadding()
                else Modifier
            )
            .background(AppColors.BackgroundDark),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
        ) {
            // 1. Immersive Cinematic Hero Header
            item(key = "hero_header") {
                DetailHeroHeader(
                    item = item,
                )
            }

            // 2. Bold Title & Genre Chips
            item(key = "title_section") {
                DetailTitleHeader(item = item)
            }

            // 3. Primary Hero CTA Action Row (Watch Now, Watchlist, Download, Share)
            item(key = "hero_actions") {
                DetailHeroActions(
                    item = item,
                    onPlay = {
                        if (isTv) {
                            showEpisodePicker = true
                        } else {
                            navController.navigateToPlayer(item)
                        }
                    },
                    onShare = { AppActions.shareItem(context, item) },
                )
            }

            // 4. Storyline Overview Card
            if (item.overview.isNotBlank()) {
                item(key = "overview_card") {
                    DetailOverviewCard(
                        overview = item.overview,
                        isExpanded = state.isOverviewExpanded,
                        onToggle = { viewModel.toggleOverview() },
                    )
                }
            }

            // 5. Google AdMob Native Medium Template (supports all videos and images)
            item(key = "detail_native_ad") {
                AdMobNativeMediumAd(
                    placement = "detail_native",
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
            }

            // 6. Scalable Segmented Tabs Row
            item(key = "tab_row") {
                DetailTabRow(
                    tabs = tabs,
                    selectedIndex = selectedTab.coerceIn(0, tabs.lastIndex),
                    onTabSelected = { selectedTab = it },
                )
            }

            // 7. Active Tab Content
            if (isTv) {
                when (selectedTab) {
                    0 -> { // TV Tab 0: Episodes & Seasons
                        item(key = "tv_seasons_content") {
                            if (state.seasons.isNotEmpty()) {
                                Column(modifier = Modifier.padding(top = 4.dp)) {
                                    // Quick Season Selector Card
                                    val firstSeason = state.seasons.firstOrNull()
                                    if (firstSeason != null) {
                                        Surface(
                                            onClick = { showEpisodePicker = true },
                                            shape = RoundedCornerShape(16.dp),
                                            color = AppColors.CardDark,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                AppColors.Primary.copy(alpha = 0.35f),
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 6.dp),
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Icon(
                                                    Icons.Default.Tv,
                                                    contentDescription = null,
                                                    tint = AppColors.Primary,
                                                    modifier = Modifier.size(26.dp),
                                                )
                                                Spacer(modifier = Modifier.width(14.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Select Season & Episode",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                    )
                                                    Text(
                                                        text = "${state.seasons.size} Seasons available • Tap to choose",
                                                        color = AppColors.TextMuted,
                                                        fontSize = 12.sp,
                                                    )
                                                }
                                                Icon(
                                                    Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = AppColors.Primary,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                    }

                                    // Horizontal Seasons Row
                                    DetailSeasonsSection(
                                        seasons = state.seasons,
                                        onSeasonClick = { season ->
                                            navController.navigateToSeason(item, season.seasonNumber, season.name)
                                        },
                                    )
                                }
                            } else {
                                EmptyState(
                                    message = "No season information available",
                                    emoji = "📺",
                                    modifier = Modifier.padding(vertical = 24.dp),
                                )
                            }
                        }
                    }
                    1 -> { // TV Tab 1: More Like This
                        item(key = "tv_similar_content") {
                            if (state.similar.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .height(265.dp)
                                        .padding(top = 4.dp),
                                ) {
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    ) {
                                        items(state.similar) { similarItem ->
                                            SimilarTitleCard(
                                                item = similarItem,
                                                onClick = { navController.navigateToDetail(similarItem) },
                                            )
                                        }
                                    }
                                }
                            } else {
                                EmptyState(
                                    message = "No similar titles found",
                                    emoji = "🎯",
                                    modifier = Modifier.padding(vertical = 24.dp),
                                )
                            }
                        }
                    }
                    2 -> { // TV Tab 2: Cast & Info
                        if (state.cast.isNotEmpty()) {
                            item(key = "tv_cast_header") {
                                SectionHeader(
                                    title = "Top Cast",
                                    emoji = "👥",
                                    onSeeAll = if (state.cast.size > 1) { { showAllCast = true } } else null,
                                )
                            }
                            item(key = "tv_cast_row") {
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
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                        item(key = "tv_specs") {
                            DetailSpecsSection(item = item)
                        }
                        if (state.reviews.isNotEmpty()) {
                            item(key = "tv_reviews_header") {
                                SectionHeader(title = "Reviews", emoji = "⭐")
                            }
                            items(state.reviews.take(2)) { review ->
                                DetailReviewCard(review = review)
                            }
                        }
                    }
                }
            } else {
                // Movie Tabs
                when (selectedTab) {
                    0 -> { // Movie Tab 0: More Like This
                        item(key = "movie_similar_content") {
                            if (state.similar.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .height(265.dp)
                                        .padding(top = 4.dp),
                                ) {
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    ) {
                                        items(state.similar) { similarItem ->
                                            SimilarTitleCard(
                                                item = similarItem,
                                                onClick = { navController.navigateToDetail(similarItem) },
                                            )
                                        }
                                    }
                                }
                            } else {
                                EmptyState(
                                    message = "No similar movies found",
                                    emoji = "🎯",
                                    modifier = Modifier.padding(vertical = 24.dp),
                                )
                            }
                        }
                    }
                    1 -> { // Movie Tab 1: Cast & Crew
                        if (state.cast.isNotEmpty()) {
                            item(key = "movie_cast_header") {
                                SectionHeader(
                                    title = "Top Cast",
                                    emoji = "👥",
                                    onSeeAll = if (state.cast.size > 1) { { showAllCast = true } } else null,
                                )
                            }
                            item(key = "movie_cast_row") {
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
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                    }
                    2 -> { // Movie Tab 2: Details & Reviews
                        item(key = "movie_specs") {
                            DetailSpecsSection(item = item)
                        }
                        if (state.reviews.isNotEmpty()) {
                            item(key = "movie_reviews_header") {
                                SectionHeader(title = "Reviews", emoji = "⭐")
                            }
                            items(state.reviews.take(2)) { review ->
                                DetailReviewCard(review = review)
                            }
                        }
                    }
                }
            }

            // 8. Reviews Banner Ad
            if (AdManager.isAdPlacementEnabled("detail_banner_reviews")) {
                item(key = "detail_banner_reviews_ad") {
                    Spacer(modifier = Modifier.height(10.dp))
                    InlineBannerAd(
                        placement = "detail_banner_reviews",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            // 9. Bottom Banner Ad
            if (AdManager.isAdPlacementEnabled("detail_banner_bottom")) {
                item(key = "detail_banner_bottom_ad") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        FullWidthAdBanner(
                            placement = "detail_banner_bottom",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Bottom Navigation Clearance
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(180.dp))
            }
        }

        // Overlay Floating App Bar
        DetailOverlayAppBar(
            title = item.title,
            showTitle = showAppBarTitle,
            onBack = { navController.popBackStack() },
            onShare = { AppActions.shareItem(context, item) },
            onMore = { showMore = true },
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Floating Bottom Bar: Sticky Collapsible Banner Ad ABOVE Detail Bottom Action Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(AppColors.BackgroundDark.copy(alpha = 0.95f))
                .navigationBarsPadding(),
        ) {
            StickyCollapsibleBannerAd(
                modifier = Modifier.fillMaxWidth(),
            )
            DetailBottomActionBar(
                item = item,
                seasons = state.seasons,
                onPlay = { playItem -> navController.navigateToPlayer(playItem) },
            )
        }
    }

    // Modal Sheets
    if (showMore) {
        MoreMenuSheet(
            title = item.title,
            onShare = { AppActions.shareItem(context, item) },
            onDismiss = { showMore = false },
        )
    }

    if (showEpisodePicker) {
        EpisodePickerSheet(
            item = item,
            seasons = state.seasons,
            onPlay = { season, episode ->
                showEpisodePicker = false
                navController.navigateToPlayer(
                    item.copy(
                        season = season,
                        episode = episode,
                        title = "${item.title} · S${season}E$episode",
                    ),
                )
            },
            onDismiss = { showEpisodePicker = false },
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