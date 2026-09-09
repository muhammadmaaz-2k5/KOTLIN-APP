package com.job2day.nazaarabox.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.SearchFilters
import com.job2day.nazaarabox.core.ThemedSection
import com.job2day.nazaarabox.navigation.navigateToDetail
import com.job2day.nazaarabox.navigation.navigateToSeeAll
import com.job2day.nazaarabox.navigation.navigateToThemedSection
import com.job2day.nazaarabox.presentation.shared.SearchFilterSheet
import com.job2day.nazaarabox.routes.AppRoutes
import com.job2day.nazaarabox.ui.components.HomeGlassAppBar
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.widgets.CustomImage
import com.job2day.nazaarabox.widgets.EmptyState
import com.job2day.nazaarabox.widgets.FeaturedBanner
import com.job2day.nazaarabox.widgets.LoadingSkeleton
import com.job2day.nazaarabox.widgets.MovieGridCard
import com.job2day.nazaarabox.widgets.SectionHeader
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import com.job2day.nazaarabox.widgets.PopularMovieCard
import com.job2day.nazaarabox.widgets.TrendingCard
import com.job2day.nazaarabox.ads.AdMobNativeCompactAd
import com.job2day.nazaarabox.ads.CustomSmallCardAd
import com.job2day.nazaarabox.ads.InlineCardAd
import com.job2day.nazaarabox.utils.AdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    var homeFilters by remember { mutableStateOf(SearchFilters()) }
    var showFilters by remember { mutableStateOf(false) }

    val isOnline by com.job2day.nazaarabox.utils.rememberIsOnline()

    val isAppBarBlurred by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 30
        }
    }

    val selected = state.selectedCategoryIndex
    val category = state.categories.getOrNull(selected)
    val trending = state.trending
    val popular = state.popular
    val featured = if (state.featured.isNotEmpty()) state.featured else trending.take(5)

    val trendingLabel = when (category?.label) {
        "All", null -> "Trending This Week"
        "KDrama" -> "Trending KDramas"
        else -> "Trending ${category.label}"
    }

    val popularLabel = when (category?.label) {
        "All", null -> "Popular This Month"
        "KDrama" -> "Popular KDramas"
        else -> "Popular ${category.label}"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundDark),
    ) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
        ) {
            if (!isOnline && state.categories.isEmpty() && state.trending.isEmpty()) {
                com.job2day.nazaarabox.widgets.EngoraNoInternetScreen(
                    onRetry = { viewModel.refresh() },
                )
            } else if (state.isLoading && state.categories.isEmpty()) {
                HomeShimmerPlaceholder()
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // 1. Hero Carousel Banner
                    item(key = "hero_banner") {
                        if (featured.isNotEmpty()) {
                            FeaturedBanner(
                                items = featured,
                                onItemClick = { navController.navigateToDetail(it) },
                            )
                        } else {
                            LoadingSkeleton(modifier = Modifier.height(440.dp), height = 440)
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // 2. Category Filter Chips
                    if (state.categories.isNotEmpty()) {
                        item(key = "categories_row") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                state.categories.forEachIndexed { index, cat ->
                                    val isSelected = selected == index
                                    com.job2day.nazaarabox.widgets.EngoraFilterChip(
                                        label = cat.label,
                                        emoji = cat.emoji,
                                        selected = isSelected,
                                        onClick = { viewModel.selectCategory(index) },
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // 3. Sponsored Ads Row (Native Small Cards / Webview Article Ads)
                    if (AdManager.isAdPlacementEnabled("home_inline")) {
                        item(key = "sponsored_ads_row") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.height(210.dp),
                            ) {
                                items(6) {
                                    CustomSmallCardAd(
                                        adUrl = AdManager.getAdPlacementUrl("home_inline"),
                                        modifier = Modifier
                                            .width(140.dp)
                                            .height(200.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    // 4. Trending Section Header
                    item(key = "trending_header") {
                        com.job2day.nazaarabox.widgets.EngoraSectionHeader(
                            title = trendingLabel,
                            emoji = "🔥",
                            itemCount = trending.size.takeIf { it > 0 },
                            onSeeAll = if (trending.isNotEmpty()) {
                                { navController.navigateToSeeAll(trendingLabel, trending) }
                            } else null,
                        )
                    }

                    // 5. Trending Cards Horizontal Row
                    item(key = "trending_content") {
                        if (state.isCategoryLoading && trending.isEmpty()) {
                            LoadingSkeleton(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .height(220.dp),
                                height = 220,
                            )
                        } else if (trending.isEmpty()) {
                            EmptyState("No trending titles found")
                        } else {
                            Box(modifier = Modifier.height(265.dp)) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    items(trending.size) { index ->
                                        val entry = trending[index]
                                        TrendingCard(
                                            item = entry,
                                            index = index,
                                            onClick = { navController.navigateToDetail(entry) },
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Google AdMob Native Ad (Small Template) between Trending and Popular
                    item(key = "home_native_ad") {
                        AdMobNativeCompactAd(
                            placement = "home_native",
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }

                    // 7. Popular Section Header
                    item(key = "popular_header") {
                        com.job2day.nazaarabox.widgets.EngoraSectionHeader(
                            title = popularLabel,
                            emoji = "⭐",
                            itemCount = popular.size.takeIf { it > 0 },
                            onSeeAll = if (popular.isNotEmpty()) {
                                { navController.navigateToSeeAll(popularLabel, popular) }
                            } else null,
                        )
                    }

                    // 8. Popular Movies Horizontal Carousel
                    item(key = "popular_content") {
                        if (state.isCategoryLoading && popular.isEmpty()) {
                            LoadingSkeleton(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .height(220.dp),
                                height = 220,
                            )
                        } else if (popular.isEmpty()) {
                            EmptyState("No popular titles found")
                        } else {
                            Box(modifier = Modifier.height(265.dp)) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    items(popular.size) { index ->
                                        val entry = popular[index]
                                        PopularMovieCard(
                                            item = entry,
                                            onClick = { navController.navigateToDetail(entry) },
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 9. Dynamic Admin-Configured Sections (from backend home_sections table)
                    if (state.sections.isNotEmpty()) {
                        item(key = "sections_spacer") {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        items(
                            count = state.sections.size,
                            key = { "admin_section_${state.sections[it].id}" },
                        ) { secIndex ->
                            val section = state.sections[secIndex]
                            DynamicSectionRow(
                                section = section,
                                onMore = { navController.navigateToThemedSection(section) },
                                onItemClick = { navController.navigateToDetail(it) },
                            )
                        }
                    }



                    // Bottom Navigation Padding
                    item(key = "bottom_padding") {
                        Spacer(modifier = Modifier.height(110.dp))
                    }
                }
            }
        }

        // Floating Frosted Glass Top App Bar
        HomeGlassAppBar(
            isBlurred = isAppBarBlurred,
            filterActiveCount = homeFilters.activeCount,
            onLanguage = { navController.navigate(AppRoutes.LANGUAGE_BROWSE) },
            onSearch = { navController.navigate(AppRoutes.SEARCH) },
            onFilter = { showFilters = true },
            onMoreApps = { navController.navigate(AppRoutes.MORE_APPS) },
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Floating Offline Alert Banner
        com.job2day.nazaarabox.widgets.EngoraOfflineBanner(
            visible = !isOnline && (state.categories.isNotEmpty() || state.trending.isNotEmpty()),
            onRetry = { viewModel.refresh() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 54.dp),
        )
    }

    if (showFilters) {
        SearchFilterSheet(
            current = homeFilters,
            onDismiss = { showFilters = false },
            onApply = {
                homeFilters = it
                showFilters = false
            },
        )
    }
}

@Composable
private fun DynamicSectionRow(
    section: ThemedSection,
    onMore: () -> Unit,
    onItemClick: (MediaItem) -> Unit,
) {
    if (section.items.isEmpty()) return

    Column(modifier = Modifier.padding(bottom = 22.dp)) {
        com.job2day.nazaarabox.widgets.EngoraSectionHeader(
            title = section.title,
            emoji = section.emoji.ifBlank { "🎬" },
            itemCount = section.items.size.takeIf { it > 0 },
            onSeeAll = onMore,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier.height(265.dp)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                val rowItems = buildList<Any?> {
                    addAll(section.items)
                    section.items.forEachIndexed { index, _ ->
                        if ((index + 1) % 4 == 0 && index < section.items.lastIndex) {
                            add(null)
                        }
                    }
                }
                items(rowItems) { entry ->
                    if (entry is MediaItem) {
                        SectionMovieCard(item = entry, onClick = { onItemClick(entry) })
                    } else if (AdManager.isAdPlacementEnabled("home_inline")) {
                        InlineCardAd(
                            placement = "home_inline",
                            modifier = Modifier.width(140.dp),
                            label = "",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionMovieCard(item: MediaItem, onClick: () -> Unit) {
    Column(modifier = Modifier.width(140.dp)) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = AppColors.CardDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(195.dp),
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
            color = AppColors.TextPrimary,
            fontSize = 13.sp,
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

@Composable
private fun HomeShimmerPlaceholder() {
    Column(modifier = Modifier.fillMaxSize()) {
        LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(440.dp), height = 440)
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(4) {
                LoadingSkeleton(
                    modifier = Modifier.width(84.dp).height(36.dp),
                    height = 36,
                    shape = RoundedCornerShape(20.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        LoadingSkeleton(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(210.dp),
            height = 210,
        )
    }
}