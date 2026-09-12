package com.job2day.nazaarabox.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.job2day.nazaarabox.ads.AdMobNativeMediumAd
import com.job2day.nazaarabox.ads.InlineBannerAd
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.SearchFilters
import com.job2day.nazaarabox.navigation.navigateToActor
import com.job2day.nazaarabox.navigation.navigateToDetail
import com.job2day.nazaarabox.presentation.shared.SearchFilterSheet
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.ads.FullWidthAdBanner
import com.job2day.nazaarabox.widgets.CustomImage
import com.job2day.nazaarabox.widgets.shimmerBrush

private object AdConfig {
    const val BANNER_PLACEMENT = "search_banner"
    const val BANNER_INTERVAL = 6
    const val MAX_BANNERS = 3
    const val AD_HEIGHT = 100
}

private data class GenreCardData(
    val name: String,
    val icon: String,
    val gradient: List<Color>,
)

private val featuredGenres = listOf(
    GenreCardData("Action", "💥", listOf(Color(0xFFE52D27), Color(0xFFB31217))),
    GenreCardData("Sci-Fi", "🚀", listOf(Color(0xFF00C6FF), Color(0xFF0072FF))),
    GenreCardData("Comedy", "😂", listOf(Color(0xFFF7971E), Color(0xFFFFD200))),
    GenreCardData("Horror", "👻", listOf(Color(0xFF434343), Color(0xFF111111))),
    GenreCardData("Drama", "🎭", listOf(Color(0xFF8A2387), Color(0xFFE94057))),
    GenreCardData("Animation", "✨", listOf(Color(0xFF11998E), Color(0xFF38EF7D))),
    GenreCardData("Thriller", "🔪", listOf(Color(0xFF3A6073), Color(0xFF16222A))),
    GenreCardData("Romance", "💖", listOf(Color(0xFFFC466B), Color(0xFF3F5EFB))),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    var showFilters by remember { mutableStateOf(false) }
    var searchFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (state.query.isEmpty()) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        containerColor = AppColors.BackgroundDark,
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            // ==========================================
            // INTEGRATED CINEMATIC SEARCH BAR
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Back Button
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                    )
                }

                // Capsule Search Input
                TextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChanged,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { searchFocused = it.isFocused }
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            width = if (searchFocused) 1.5.dp else 1.dp,
                            color = if (searchFocused) AppColors.Primary.copy(alpha = 0.85f) else AppColors.Outline.copy(alpha = 0.40f),
                            shape = RoundedCornerShape(24.dp),
                        ),
                    placeholder = {
                        Text(
                            "Search movies, TV, people…",
                            color = AppColors.TextMuted,
                            fontSize = 13.sp,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = if (searchFocused) AppColors.Primary else AppColors.TextMuted,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearQuery() }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.White.copy(alpha = 0.70f),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppColors.SurfaceDark,
                        unfocusedContainerColor = AppColors.SurfaceDark,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = AppColors.Primary,
                        focusedTextColor = AppColors.TextPrimary,
                        unfocusedTextColor = AppColors.TextPrimary,
                    ),
                )

                // Filter Trigger with active count badge
                Box {
                    Surface(
                        onClick = { showFilters = true },
                        shape = CircleShape,
                        color = if (state.filters.activeCount > 0) AppColors.Primary.copy(alpha = 0.20f) else AppColors.SurfaceDark,
                        border = BorderStroke(
                            1.dp,
                            if (state.filters.activeCount > 0) AppColors.Primary else AppColors.Outline.copy(alpha = 0.35f),
                        ),
                        modifier = Modifier.size(44.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = if (state.filters.activeCount > 0) AppColors.Primary else Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    if (state.filters.activeCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(AppColors.Primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${state.filters.activeCount}",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                // Grid / List View Toggle Button
                if (state.query.isNotBlank() && state.results.isNotEmpty()) {
                    Surface(
                        onClick = { viewModel.toggleViewMode() },
                        shape = CircleShape,
                        color = AppColors.SurfaceDark,
                        border = BorderStroke(1.dp, AppColors.Outline.copy(alpha = 0.35f)),
                        modifier = Modifier.size(44.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (state.isGridView) Icons.Default.Menu else Icons.Default.Apps,
                                contentDescription = "Toggle View Mode",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp),
                            )
                        }
                    }
                }
            }

            // ==========================================
            // TYPE FILTER CHIPS (ALL, MOVIES, TV, PEOPLE)
            // ==========================================
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val types = listOf(
                    "all" to "🔥 All",
                    "movie" to "🎬 Movies",
                    "tv" to "📺 TV Shows",
                    "person" to "👤 People",
                )
                items(types) { (value, label) ->
                    val isSelected = state.selectedType == value
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setType(value) },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Primary,
                            selectedLabelColor = Color.White,
                            containerColor = AppColors.SurfaceVariantDark.copy(alpha = 0.6f),
                            labelColor = AppColors.TextMuted,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) AppColors.Primary else Color.White.copy(alpha = 0.10f),
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(38.dp),
                    )
                }
            }


            // Active Filters indicator
            if (state.filters.activeCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Filtered by: ${listOfNotNull(
                            state.filters.genre.takeIf { it != "All" }?.let { "Genre: $it" },
                            state.filters.year.takeIf { it != "All" }?.let { "Year: $it" },
                            state.filters.language.takeIf { it != "All" }?.let { "Lang: $it" },
                            state.filters.sortBy.takeIf { it != "Hottest" }?.let { "Sort: $it" },
                        ).joinToString(" • ")}",
                        color = AppColors.TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clear All",
                        color = AppColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.applyFilters(SearchFilters()) },
                    )
                }
            }

            // ==========================================
            // CONTENT BODY STATES
            // ==========================================
            when {
                // --- STATE 1: LOADING SKELETON ---
                state.isLoading && state.query.isNotBlank() -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (state.isGridView) {
                        SearchShimmerGrid()
                    } else {
                        SearchShimmerList()
                    }
                }

                // --- STATE 2: IDLE EXPLORATION HUB ---
                state.query.isBlank() -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 30.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        // 1. Recent Searches (if available)
                        if (state.recentSearches.isNotEmpty()) {
                            item {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "Recent Searches",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                        )
                                        Text(
                                            text = "Clear",
                                            color = AppColors.Primary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.clickable { viewModel.clearRecentSearches() },
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        state.recentSearches.forEach { query ->
                                            Surface(
                                                onClick = { viewModel.onQueryChanged(query) },
                                                shape = RoundedCornerShape(18.dp),
                                                color = AppColors.SurfaceVariantDark.copy(alpha = 0.70f),
                                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Text(
                                                        text = query,
                                                        color = Color.White.copy(alpha = 0.90f),
                                                        fontSize = 12.sp,
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Remove",
                                                        tint = AppColors.TextMuted,
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable { viewModel.removeRecentSearch(query) },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Trending Searches / Suggestions
                        item {
                            Column {
                                Text(
                                    text = "🔥 Trending Searches",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    viewModel.suggestions.forEach { suggestion ->
                                        Surface(
                                            onClick = { viewModel.onSuggestionTap(suggestion) },
                                            shape = RoundedCornerShape(18.dp),
                                            color = AppColors.SurfaceVariantDark.copy(alpha = 0.70f),
                                            border = BorderStroke(1.dp, AppColors.Primary.copy(alpha = 0.25f)),
                                        ) {
                                            Text(
                                                text = suggestion,
                                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Trending Right Now (Horizontal Shelf)
                        if (state.trendingItems.isNotEmpty()) {
                            item {
                                Column {
                                    Text(
                                        text = "⭐ Trending Right Now",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        items(state.trendingItems) { item ->
                                            TrendingItemCard(
                                                item = item,
                                                onClick = {
                                                    if (item.type == "person") {
                                                        navController.navigateToActor(item.id)
                                                    } else {
                                                        navController.navigateToDetail(item)
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Sponsored Native Ad
                        item {
                            AdMobNativeMediumAd(
                                placement = "search_native",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                            )
                        }

                        // 5. Explore by Genre Grid
                        item {
                            Column {
                                Text(
                                    text = "🎭 Explore by Genre",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                FlowRow(
                                    maxItemsInEachRow = 2,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    featuredGenres.forEach { genre ->
                                        GenreCard(
                                            genre = genre,
                                            onClick = { viewModel.selectGenre(genre.name) },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- STATE 3: EMPTY RESULTS ---
                state.results.isEmpty() -> {
                    SearchEmptyState(
                        query = state.query,
                        hasActiveFilters = state.filters.activeCount > 0,
                        onClearFilters = { viewModel.applyFilters(SearchFilters()) },
                        onExploreTrending = { viewModel.clearQuery() },
                    )
                }

                // --- STATE 4: SEARCH RESULTS (GRID OR LIST) ---
                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${state.results.size} result${if (state.results.size != 1) "s" else ""} found",
                            color = AppColors.TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = if (state.isGridView) "Grid View" else "List View",
                            color = AppColors.Primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { viewModel.toggleViewMode() },
                        )
                    }

                    val itemsWithAds = remember(state.results) {
                        buildSearchResultWithAds(state.results)
                    }

                    if (state.isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 30.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(
                                count = itemsWithAds.size,
                                span = { index ->
                                    val item = itemsWithAds[index]
                                    if (item is SearchAdItem) GridItemSpan(2) else GridItemSpan(1)
                                },
                            ) { index ->
                                when (val item = itemsWithAds[index]) {
                                    is MediaItem -> {
                                        SearchGridCard(
                                            item = item,
                                            onClick = {
                                                if (item.type == "person") {
                                                    navController.navigateToActor(item.id)
                                                } else {
                                                    navController.navigateToDetail(item)
                                                }
                                            },
                                        )
                                    }
                                    is SearchAdItem -> {
                                        SearchAdBanner(item = item)
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 30.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(itemsWithAds.size) { index ->
                                when (val item = itemsWithAds[index]) {
                                    is MediaItem -> {
                                        SearchListCard(
                                            item = item,
                                            onClick = {
                                                if (item.type == "person") {
                                                    navController.navigateToActor(item.id)
                                                } else {
                                                    navController.navigateToDetail(item)
                                                }
                                            },
                                        )
                                    }
                                    is SearchAdItem -> {
                                        SearchAdBanner(item = item)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        SearchFilterSheet(
            current = state.filters,
            onDismiss = { showFilters = false },
            onApply = {
                viewModel.applyFilters(it)
                showFilters = false
            },
        )
    }
}

// Data class for ad items in the list
private data class SearchAdItem(
    val placement: String,
    val id: String = "ad_${System.currentTimeMillis()}",
)

// Build results with ads at regular intervals
private fun buildSearchResultWithAds(
    results: List<MediaItem>,
    interval: Int = AdConfig.BANNER_INTERVAL,
    maxAds: Int = AdConfig.MAX_BANNERS,
): List<Any> {
    if (results.isEmpty()) return emptyList()
    if (!AdManager.isAdPlacementEnabled(AdConfig.BANNER_PLACEMENT)) {
        return results
    }

    return buildList {
        var adCount = 0
        results.forEachIndexed { index, item ->
            add(item)
            val shouldInsertAd =
                (index + 1) % interval == 0 &&
                index < results.lastIndex &&
                adCount < maxAds &&
                index < results.size - 1

            if (shouldInsertAd) {
                add(
                    SearchAdItem(
                        placement = AdConfig.BANNER_PLACEMENT,
                        id = "ad_${index}_${System.currentTimeMillis()}",
                    ),
                )
                adCount++
            }
        }
    }
}

@Composable
private fun SearchAdBanner(
    item: SearchAdItem,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = AppColors.SurfaceVariantDark.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, AppColors.Outline.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            InlineBannerAd(
                placement = item.placement,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AdConfig.AD_HEIGHT.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        }
    }
}

@Composable
private fun GenreCard(
    genre: GenreCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        modifier = modifier.height(64.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(genre.gradient))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = genre.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
                Text(
                    text = genre.icon,
                    fontSize = 22.sp,
                )
            }
        }
    }
}

@Composable
private fun TrendingItemCard(
    item: MediaItem,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = AppColors.SurfaceDark,
        border = BorderStroke(1.dp, AppColors.SurfaceVariantDark),
        modifier = Modifier.width(125.dp),
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(165.dp)) {
                CustomImage(
                    imageUrl = item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                )
                if (item.rating > 0) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AppColors.Accent, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", item.rating),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (item.type == "tv") "TV Series" else item.year.ifBlank { "Movie" },
                    color = AppColors.TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun SearchGridCard(
    item: MediaItem,
    onClick: () -> Unit,
) {
    val isPerson = item.type == "person"
    val typeColor = when (item.type) {
        "tv" -> AppColors.Secondary
        "person" -> AppColors.Accent
        else -> AppColors.Primary
    }
    val typeLabel = when (item.type) {
        "tv" -> "TV"
        "person" -> "PERSON"
        else -> "4K"
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = AppColors.SurfaceDark,
        border = BorderStroke(1.dp, AppColors.SurfaceVariantDark.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.68f),
            ) {
                CustomImage(
                    imageUrl = item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = typeLabel,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(typeColor.copy(alpha = 0.85f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                    if (!isPerson && item.rating > 0) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = AppColors.Accent, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format("%.1f", item.rating),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.year.ifBlank { if (isPerson) "Artist" else "Movie" },
                        color = AppColors.TextMuted,
                        fontSize = 11.sp,
                    )
                    if (item.popularity > 0) {
                        Text(
                            text = "🔥 ${item.popularity.toInt()}",
                            color = AppColors.TextMuted,
                            fontSize = 10.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchListCard(
    item: MediaItem,
    onClick: () -> Unit,
) {
    val isPerson = item.type == "person"
    val typeColor = when (item.type) {
        "tv" -> AppColors.Secondary
        "person" -> AppColors.Accent
        else -> AppColors.Primary
    }
    val typeLabel = when (item.type) {
        "tv" -> "TV Series"
        "person" -> "Actor"
        else -> "Movie"
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = AppColors.SurfaceDark,
        border = BorderStroke(1.dp, AppColors.SurfaceVariantDark.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CustomImage(
                imageUrl = item.posterUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .then(
                        if (isPerson) Modifier.size(64.dp).clip(CircleShape)
                        else Modifier.width(62.dp).height(88.dp).clip(RoundedCornerShape(10.dp)),
                    ),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = typeLabel,
                        color = typeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(typeColor.copy(alpha = 0.16f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                    if (item.year.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AppColors.TextMuted, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = item.year,
                                color = AppColors.TextMuted,
                                fontSize = 11.sp,
                            )
                        }
                    }
                    if (!isPerson && item.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = AppColors.Accent, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format("%.1f", item.rating),
                                color = AppColors.Accent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                if (item.overview.isNotBlank() && !isPerson) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = item.overview,
                        color = AppColors.TextMuted,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (!isPerson) {
                Surface(
                    shape = CircleShape,
                    color = AppColors.Primary.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, AppColors.Primary.copy(alpha = 0.40f)),
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Watch",
                            tint = AppColors.Primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchShimmerGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(6) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.SurfaceDark)
                    .padding(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.68f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(shimmerBrush()),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush()),
                )
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.40f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush()),
                )
            }
        }
    }
}

@Composable
private fun SearchShimmerList() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.SurfaceDark)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(62.dp)
                        .height(88.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(shimmerBrush()),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush()))
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.3f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush()))
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.85f).height(24.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush()))
                }
            }
        }
    }
}

@Composable
private fun SearchEmptyState(
    query: String,
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    onExploreTrending: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = CircleShape,
            color = AppColors.Primary.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, AppColors.Primary.copy(alpha = 0.30f)),
            modifier = Modifier.size(72.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(34.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "No results for \"$query\"",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Check the spelling, try searching by general terms, or clear any filters you have applied.",
            color = AppColors.TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (hasActiveFilters) {
                Surface(
                    onClick = onClearFilters,
                    shape = RoundedCornerShape(20.dp),
                    color = AppColors.SurfaceVariantDark,
                ) {
                    Text(
                        text = "Reset Filters",
                        color = AppColors.Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    )
                }
            }
            Surface(
                onClick = onExploreTrending,
                shape = RoundedCornerShape(20.dp),
                color = AppColors.Primary,
            ) {
                Text(
                    text = "Explore Trending",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                )
            }
        }
    }
}