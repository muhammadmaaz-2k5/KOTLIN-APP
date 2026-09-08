package com.job2day.nazaarabox.presentation.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.job2day.nazaarabox.ads.InlineBannerAd
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.navigation.getThemedSection
import com.job2day.nazaarabox.navigation.navigateToDetail
import com.job2day.nazaarabox.services.MediaRepository
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.widgets.CustomIconWidget
import com.job2day.nazaarabox.widgets.CustomImage
import com.job2day.nazaarabox.widgets.EmptyState
import com.job2day.nazaarabox.widgets.LoadingCenter
import com.job2day.nazaarabox.widgets.MovieGridCard
import kotlinx.coroutines.launch

private enum class SortOption(val label: String, val emoji: String) {
    POPULAR("Popular", "🔥"),
    TOP_RATED("Top Rated", "⭐"),
    NEWEST("Newest", "📅"),
}

private enum class TypeFilter(val label: String) {
    ALL("All"),
    MOVIES("Movies"),
    TV_SHOWS("Series"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySectionScreen(navController: NavController) {
    val handle = navController.previousBackStackEntry?.savedStateHandle
    val section = handle?.let { navController.getThemedSection(it) }

    var allFetchedItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var page by remember { mutableIntStateOf(1) }
    var totalPages by remember { mutableIntStateOf(1) }

    // UI Controls
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf(SortOption.POPULAR) }
    var selectedTypeFilter by remember { mutableStateOf(TypeFilter.ALL) }
    var isDenseGrid by remember { mutableStateOf(true) } // true = 3 cols, false = 2 cols

    val repository = remember { MediaRepository() }
    val gridState = rememberLazyGridState()
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    // Fetch initial page or refresh
    suspend fun loadPage(targetPage: Int, refresh: Boolean) {
        if (section == null) return
        if (refresh) {
            isRefreshing = true
        } else if (targetPage > 1) {
            loadingMore = true
        } else {
            loading = true
        }

        val params = section.tmdbParams.toMutableMap().apply {
            put("include_adult", "false")
            val sortParam = when (selectedSort) {
                SortOption.POPULAR -> "popularity.desc"
                SortOption.TOP_RATED -> "vote_average.desc"
                SortOption.NEWEST -> if (section.mediaType == "tv") "first_air_date.desc" else "primary_release_date.desc"
            }
            put("sort_by", sortParam)
            if (selectedSort == SortOption.TOP_RATED) {
                put("vote_count.gte", "50")
            }
        }

        val (newItems, pages) = if (section.endpoint.isNotBlank() && !section.endpoint.startsWith("discover/")) {
            repository.fetchSectionPage(section.endpoint, params, targetPage, section.mediaType)
        } else if (section.mediaType == "tv") {
            repository.discover("tv", params, targetPage)
        } else {
            repository.discoverMovies(params, targetPage)
        }

        if (targetPage == 1) {
            allFetchedItems = newItems
        } else {
            allFetchedItems = allFetchedItems + newItems
        }
        totalPages = pages
        page = targetPage
        loading = false
        isRefreshing = false
        loadingMore = false
    }

    LaunchedEffect(section, selectedSort) {
        loadPage(targetPage = 1, refresh = false)
    }

    // Infinite scroll detection
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= allFetchedItems.size - 4 && !loadingMore && !loading && !isRefreshing && page < totalPages && allFetchedItems.isNotEmpty()
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && section != null) {
            loadPage(targetPage = page + 1, refresh = false)
        }
    }

    if (section == null) {
        LoadingCenter()
        return
    }

    // In-Page Filtered and Sorted Items
    val displayedItems by remember {
        derivedStateOf {
            var list = allFetchedItems

            // 1. Type Filter
            if (selectedTypeFilter != TypeFilter.ALL) {
                list = list.filter { item ->
                    if (selectedTypeFilter == TypeFilter.MOVIES) item.type.equals("movie", ignoreCase = true)
                    else item.type.equals("tv", ignoreCase = true)
                }
            }

            // 2. Search Query Filter
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                list = list.filter {
                    it.title.lowercase().contains(q) || it.overview.lowercase().contains(q)
                }
            }

            // 3. Client Sort fallback
            when (selectedSort) {
                SortOption.POPULAR -> list
                SortOption.TOP_RATED -> list.sortedByDescending { it.rating }
                SortOption.NEWEST -> list.sortedByDescending { it.year }
            }
        }
    }

    Scaffold(
        containerColor = AppColors.BackgroundDark,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                AppColors.BackgroundDark.copy(alpha = 0.98f),
                                AppColors.BackgroundDark.copy(alpha = 0.92f),
                            )
                        )
                    )
                    .statusBarsPadding(),
            ) {
                // Main Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Back Button (Frosted Circle)
                    Surface(
                        onClick = { navController.popBackStack() },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier.size(38.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CustomIconWidget(
                                iconName = "arrow_back_ios_new_rounded",
                                size = 16.dp,
                                color = Color.White,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Counter
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = section.emoji.ifBlank { "🎬" },
                                fontSize = 18.sp,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = section.title,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (section.mediaType == "tv") "TV Series" else "Movies",
                                color = AppColors.Primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "  •  ",
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 11.sp,
                            )
                            Text(
                                text = "${displayedItems.size} Titles",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    // Action: In-page Search Toggle
                    IconButton(
                        onClick = {
                            isSearchExpanded = !isSearchExpanded
                            if (!isSearchExpanded) searchQuery = ""
                        },
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchExpanded || searchQuery.isNotBlank()) AppColors.Primary else Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    // Action: View Mode Toggle (2 cols vs 3 cols)
                    IconButton(onClick = { isDenseGrid = !isDenseGrid }) {
                        Icon(
                            imageVector = if (isDenseGrid) Icons.Default.GridView else Icons.Default.ViewModule,
                            contentDescription = "Layout",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                // Expandable Search Field
                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, AppColors.Primary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                cursorBrush = SolidColor(AppColors.Primary),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Filter in ${section.title}...",
                                            color = Color.White.copy(alpha = 0.45f),
                                            fontSize = 13.sp,
                                        )
                                    }
                                    innerTextField()
                                },
                            )
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { searchQuery = "" },
                                )
                            }
                        }
                    }
                }

                // Interactive Filter & Sort Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Sort Chips
                    SortOption.values().forEach { option ->
                        val isSelected = selectedSort == option
                        Surface(
                            onClick = { selectedSort = option },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) AppColors.Primary else Color.White.copy(alpha = 0.07f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) AppColors.Primary else Color.White.copy(alpha = 0.12f),
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = option.emoji, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = option.label,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(18.dp)
                            .width(1.dp)
                            .background(Color.White.copy(alpha = 0.2f)),
                    )

                    // Media Type Chips
                    TypeFilter.values().forEach { type ->
                        val isSelected = selectedTypeFilter == type
                        Surface(
                            onClick = { selectedTypeFilter = type },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) AppColors.Secondary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) AppColors.Secondary else Color.White.copy(alpha = 0.10f),
                            ),
                        ) {
                            Text(
                                text = type.label,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                color = if (isSelected) AppColors.Secondary else Color.White.copy(alpha = 0.65f),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    loadPage(targetPage = 1, refresh = true)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (loading && allFetchedItems.isEmpty()) {
                    LoadingCenter()
                } else if (displayedItems.isEmpty()) {
                    EmptyState(
                        message = if (searchQuery.isNotBlank()) "No titles match \"$searchQuery\"" else "No titles found in this section",
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    val gridItems = buildList<Any?> {
                        addAll(displayedItems)
                        displayedItems.forEachIndexed { index, _ ->
                            if ((index + 1) % 6 == 0 && index < displayedItems.lastIndex && AdManager.isAdPlacementEnabled("category_banner")) {
                                add("ad")
                            }
                        }
                    }

                    val columnsCount = if (isDenseGrid) 3 else 2

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columnsCount),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(
                            gridItems,
                            key = { entry ->
                                if (entry is String) "ad_${entry}_${System.identityHashCode(entry)}"
                                else if (entry is MediaItem) "${entry.type}_${entry.id}"
                                else "null_entry"
                            },
                        ) { entry ->
                            if (entry is String && entry == "ad") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp),
                                ) {
                                    InlineBannerAd(
                                        placement = "category_banner",
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            } else if (entry is MediaItem) {
                                if (isDenseGrid) {
                                    // 3 Columns: Compact Grid Card
                                    MovieGridCard(
                                        item = entry,
                                        showTypeBadge = true,
                                        onClick = { navController.navigateToDetail(entry) },
                                    )
                                } else {
                                    // 2 Columns: Expansive Cinema Card
                                    ExpansiveSectionCard(
                                        item = entry,
                                        onClick = { navController.navigateToDetail(entry) },
                                    )
                                }
                            }
                        }
                    }
                }

                // Loading More Indicator at bottom
                if (loadingMore) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = AppColors.SurfaceDark.copy(alpha = 0.90f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                color = AppColors.Primary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Loading more titles...",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-end expansive card for 2-column view
 */
@Composable
private fun ExpansiveSectionCard(
    item: MediaItem,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF191928))
            .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        // Poster Image with Aspect Ratio & Rating
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
        ) {
            CustomImage(
                imageUrl = item.posterUrl.ifBlank { item.backdropUrl },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f),
                            ),
                            startY = 120f,
                        ),
                    ),
            )

            // Top Badges: Type (Movie / TV)
            Text(
                text = item.type.uppercase(),
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )

            // Bottom Rating & Year
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (item.rating > 0) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = AppColors.Accent,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = String.format("%.1f", item.rating),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (item.year.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.year,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // Title and Genres below poster
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = item.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.genres.isNotEmpty() || item.overview.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (item.genres.isNotEmpty()) item.genres.take(2).joinToString(" • ") else item.overview,
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}