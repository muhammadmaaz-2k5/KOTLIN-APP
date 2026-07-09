package com.job2day.nazaarabox.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.SearchFilters
import com.job2day.nazaarabox.navigation.navigateToDetail
import com.job2day.nazaarabox.navigation.navigateToSeeAll
import com.job2day.nazaarabox.presentation.shared.SearchFilterSheet
import com.job2day.nazaarabox.routes.AppRoutes
import com.job2day.nazaarabox.ui.components.HomeGlassAppBar
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.widgets.EmptyState
import com.job2day.nazaarabox.widgets.FeaturedBanner
import com.job2day.nazaarabox.widgets.LoadingCenter
import com.job2day.nazaarabox.widgets.LoadingSkeleton
import com.job2day.nazaarabox.widgets.MovieGridCard
import com.job2day.nazaarabox.widgets.SectionHeader
import com.job2day.nazaarabox.widgets.TrendingCard
import com.job2day.nazaarabox.ads.CustomSmallCardAd
import com.job2day.nazaarabox.ads.FullWidthAdBanner
import com.job2day.nazaarabox.utils.AdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var homeFilters by remember { mutableStateOf(SearchFilters()) }
    var showFilters by remember { mutableStateOf(false) }

    val isAppBarBlurred by remember {
        derivedStateOf { scrollState.value > 10 }
    }

    if (state.isLoadingCategories) {
        LoadingCenter()
        return
    }

    val selected = state.selectedCategoryIndex
    val trending = state.trendingByCategory[selected].orEmpty()
    val popular = state.popularByCategory[selected].orEmpty()
    val featured = state.trendingByCategory[0].orEmpty().take(3)
    val category = state.categories.getOrNull(selected)
    val trendingLabel = when (category?.label) {
        "All" -> "Trending This Week"
        "KDrama" -> "Trending KDramas"
        null -> "Trending"
        else -> "Trending ${category.label}"
    }
    val popularLabel = when (category?.label) {
        "All" -> "Popular This Month"
        "KDrama" -> "Popular KDramas"
        null -> "Popular"
        else -> "Popular ${category.label}"
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundDark),
    ) {
        PullToRefreshBox(
            isRefreshing = state.loadingTrending.contains(selected),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            ) {
                Spacer(modifier = Modifier.height(72.dp))

                if (featured.isNotEmpty()) {
                    FeaturedBanner(
                        items = featured,
                        onItemClick = { navController.navigateToDetail(it) },
                    )
                } else if (state.loadingTrending.contains(0)) {
                    LoadingSkeleton(modifier = Modifier.height(420.dp), height = 420)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.categories.forEachIndexed { index, cat ->
                        val isSelected = selected == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(index) },
                            label = {
                                Text(
                                    text = "${cat.emoji} ${cat.label}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.Accent.copy(alpha = 0.86f),
                                selectedLabelColor = Color.Black,
                                containerColor = AppColors.SurfaceVariantDark,
                                labelColor = Color(0xFF888899),
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) AppColors.Accent else Color(0xFF444466),
                                selectedBorderColor = AppColors.Accent,
                            ),
                            shape = RoundedCornerShape(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 🔄 REPLACED: Native Card Ads Row (6 cards) instead of banner
                if (AdManager.isAdPlacementEnabled("home_inline")) {
                    // Ad row with 6 cards - horizontally scrollable
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        // Optional: Add a small header to indicate sponsored content
                        Text(
                            text = "Sponsored",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = Color(0xFF888899),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp,
                        )
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.height(220.dp), // Fixed height for consistency
                        ) {
                            items(6) { index ->
                                CustomSmallCardAd(
                                    adUrl = AdManager.getAdPlacementUrl("home_inline"),
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(200.dp),
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                SectionHeader(
                    title = trendingLabel,
                    emoji = "🔥",
                    onSeeAll = if (trending.isNotEmpty()) {
                        { navController.navigateToSeeAll(trendingLabel, trending) }
                    } else null,
                )
                if (state.loadingTrending.contains(selected) && trending.isEmpty()) {
                    LoadingSkeleton(modifier = Modifier.padding(horizontal = 16.dp))
                } else if (trending.isEmpty()) {
                    EmptyState("No trending titles found")
                } else {
                    val trendingWithAds = buildList<MediaItem?> {
                        addAll(trending)
                        trending.forEachIndexed { index, item ->
                            if ((index + 1) % 4 == 0 && index < trending.lastIndex) {
                                add(null)
                            }
                        }
                    }
                    Box(modifier = Modifier.height(220.dp)) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(trendingWithAds) { entry ->
                                if (entry != null) {
                                    TrendingCard(
                                        item = entry,
                                        onClick = { navController.navigateToDetail(entry) },
                                    )
                                } else if (AdManager.isAdPlacementEnabled("home_inline")) {
                                    CustomSmallCardAd(
                                        adUrl = AdManager.getAdPlacementUrl("home_inline"),
                                        modifier = Modifier
                                            .width(140.dp)
                                            .height(200.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 🆕 NEW: 6 Native Cards below Trending section
                if (AdManager.isAdPlacementEnabled("home_inline")) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Sponsored Recommendations",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = Color(0xFF888899),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp,
                        )
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.height(220.dp),
                        ) {
                            items(6) { index ->
                                CustomSmallCardAd(
                                    adUrl = AdManager.getAdPlacementUrl("home_inline"),
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(200.dp),
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                SectionHeader(
                    title = popularLabel,
                    emoji = "⭐",
                    onSeeAll = if (popular.isNotEmpty()) {
                        { navController.navigateToSeeAll(popularLabel, popular) }
                    } else null,
                )
                if (state.loadingPopular.contains(selected) && popular.isEmpty()) {
                    LoadingSkeleton(modifier = Modifier.padding(horizontal = 16.dp), height = 240)
                } else if (popular.isEmpty()) {
                    EmptyState("No popular titles found")
                } else {
                    val popularList = popular.take(12)
                    val allItems = buildList<MediaItem?> {
                        popularList.forEachIndexed { index, item ->
                            add(item)
                            if ((index + 1) % 4 == 0 && index < popularList.lastIndex) {
                                add(null)
                            }
                        }
                    }
                    allItems.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            rowItems.forEach { gridItem ->
                                if (gridItem != null) {
                                    MovieGridCard(
                                        item = gridItem,
                                        modifier = Modifier.weight(1f),
                                        onClick = { navController.navigateToDetail(gridItem) },
                                    )
                                } else if (AdManager.isAdPlacementEnabled("home_inline")) {
                                    CustomSmallCardAd(
                                        adUrl = AdManager.getAdPlacementUrl("home_inline"),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(240.dp),
                                    )
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                HomeSectionsWidget(navController = navController)

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ MOVED: Banner Ad to the end of the page
                if (AdManager.isAdPlacementEnabled("home_banner")) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    ) {
                        Text(
                            text = "Advertisement",
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = Color(0xFF888899),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp,
                        )
                        FullWidthAdBanner(
                            placement = "home_banner",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        HomeGlassAppBar(
            isBlurred = isAppBarBlurred,
            filterActiveCount = homeFilters.activeCount,
            onLanguage = { navController.navigate(AppRoutes.LANGUAGE_BROWSE) },
            onSearch = { navController.navigate(AppRoutes.SEARCH) },
            onFilter = { showFilters = true },
            onNotifications = { },
            modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter),
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