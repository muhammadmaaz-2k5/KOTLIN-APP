package com.job2day.nazaarabox.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.navigation.navigateToActor
import com.job2day.nazaarabox.navigation.navigateToDetail
import com.job2day.nazaarabox.presentation.shared.SearchFilterSheet
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.widgets.CustomImage
import com.job2day.nazaarabox.widgets.EmptyState
import com.job2day.nazaarabox.ads.InlineBannerAd

// Define ad constants for better maintainability
private object AdConfig {
    const val BANNER_PLACEMENT = "search_banner"
    const val BANNER_INTERVAL = 5 // Show ad after every 5 results
    const val MAX_BANNERS = 3 // Maximum banners to show
    const val AD_HEIGHT = 100 // Height of the ad banner in dp
}

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
    
    // Track which items are expanded for better UX - using String keys
    var expandedItems by remember { mutableStateOf<MutableSet<String>>(mutableSetOf()) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        containerColor = AppColors.BackgroundDark,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Search", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.BackgroundDark),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            // Search input and filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChanged,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { searchFocused = it.isFocused }
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            width = if (searchFocused) 1.5.dp else 1.dp,
                            color = if (searchFocused) AppColors.Primary.copy(alpha = 0.7f) else AppColors.Outline,
                            shape = RoundedCornerShape(14.dp),
                        ),
                    placeholder = { Text("Search movies, TV, people…", color = AppColors.TextMuted) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = if (searchFocused) AppColors.Primary else AppColors.TextMuted,
                        )
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
                Box {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters", tint = AppColors.Primary)
                    }
                    if (state.filters.activeCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AppColors.Error),
                        )
                    }
                }
            }

            // Type filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("all" to "All", "movie" to "Movies", "tv" to "TV", "person" to "People").forEach { (value, label) ->
                    FilterChip(
                        selected = state.selectedType == value,
                        onClick = { viewModel.setType(value) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Primary,
                            selectedLabelColor = Color.White,
                            containerColor = AppColors.SurfaceVariantDark,
                            labelColor = AppColors.TextMuted,
                        ),
                        shape = RoundedCornerShape(21.dp),
                        modifier = Modifier.height(42.dp),
                    )
                }
            }

            if (state.filters.activeCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Clear filters",
                        color = AppColors.Primary,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { viewModel.applyFilters(com.job2day.nazaarabox.core.SearchFilters()) },
                    )
                }
            }

            when {
                state.isLoading && state.query.isNotBlank() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = AppColors.Primary)
                    }
                }
                state.query.isBlank() -> {
                    FlowRow(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        viewModel.suggestions.forEach { suggestion ->
                            Surface(
                                onClick = { viewModel.onSuggestionTap(suggestion) },
                                shape = RoundedCornerShape(20.dp),
                                color = AppColors.SurfaceVariantDark,
                            ) {
                                Text(
                                    text = suggestion,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    color = AppColors.TextMuted,
                                )
                            }
                        }
                    }
                }
                state.results.isEmpty() -> EmptyState("No results for \"${state.query}\"")
                else -> {
                    Text(
                        text = "${state.results.size} result${if (state.results.size != 1) "s" else ""} for \"${state.query}\"",
                        color = AppColors.TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                    
                    // Build list with ads inserted at regular intervals
                    val itemsWithAds = remember(state.results) {
                        buildSearchResultWithAds(state.results)
                    }
                    
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        itemsIndexed(itemsWithAds) { index, item ->
                            when (item) {
                                is MediaItem -> {
                                    val itemKey = "${item.type}_${item.id}"
                                    SearchResultRow(
                                        item = item,
                                        isExpanded = expandedItems.contains(itemKey),
                                        onToggleExpand = {
                                            if (expandedItems.contains(itemKey)) {
                                                expandedItems.remove(itemKey)
                                            } else {
                                                expandedItems.add(itemKey)
                                            }
                                        },
                                        onClick = {
                                            if (item.type == "person") {
                                                navController.navigateToActor(item.id)
                                            } else {
                                                navController.navigateToDetail(item)
                                            }
                                        }
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
    val id: String = "ad_${System.currentTimeMillis()}"
)

// Build results with ads at regular intervals
private fun buildSearchResultWithAds(
    results: List<MediaItem>,
    interval: Int = AdConfig.BANNER_INTERVAL,
    maxAds: Int = AdConfig.MAX_BANNERS
): List<Any> {
    if (results.isEmpty()) return emptyList()
    
    // Only show ads if placement is enabled
    if (!com.job2day.nazaarabox.utils.AdManager.isAdPlacementEnabled(AdConfig.BANNER_PLACEMENT)) {
        return results
    }
    
    return buildList {
        var adCount = 0
        results.forEachIndexed { index, item ->
            add(item)
            
            // Insert ad after interval, but not at the end, and limited by maxAds
            val shouldInsertAd = 
                (index + 1) % interval == 0 &&
                index < results.lastIndex &&
                adCount < maxAds &&
                index < results.size - 1 // Don't add after last item
            
            if (shouldInsertAd) {
                add(SearchAdItem(
                    placement = AdConfig.BANNER_PLACEMENT,
                    id = "ad_${index}_${System.currentTimeMillis()}"
                ))
                adCount++
            }
        }
    }
}

// Separate composable for ad banner with better styling and UX
@Composable
private fun SearchAdBanner(
    item: SearchAdItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = AppColors.SurfaceVariantDark.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = AppColors.Outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Ad label with proper styling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sponsored",
                    color = AppColors.TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                )
                // Optional: Small close/feedback button
                Text(
                    text = "ⓘ",
                    color = AppColors.TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.clickable {
                        // Could show ad feedback dialog
                    }
                )
            }
            
            // The actual ad banner with increased height
            InlineBannerAd(
                placement = item.placement,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AdConfig.AD_HEIGHT.dp) // Using the constant from AdConfig
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
private fun SearchResultRow(
    item: MediaItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onClick: () -> Unit,
) {
    val isPerson = item.type == "person"
    val imageUrl = if (isPerson) item.posterUrl else item.posterUrl
    val typeColor = when (item.type) {
        "tv" -> AppColors.Secondary
        "person" -> AppColors.Accent
        else -> AppColors.Primary
    }
    val typeLabel = when (item.type) {
        "tv" -> "TV Show"
        "person" -> "Person"
        else -> "Movie"
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = AppColors.SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.SurfaceVariantDark),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CustomImage(
                imageUrl = imageUrl,
                modifier = Modifier
                    .then(
                        if (isPerson) Modifier.size(60.dp).clip(CircleShape)
                        else Modifier.width(54.dp).height(78.dp).clip(RoundedCornerShape(10.dp)),
                    ),
                contentDescription = item.title,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                )
                
                // Expand/collapse for long titles
                if (item.title.length > 30) {
                    Text(
                        text = if (isExpanded) "Show less" else "Show more",
                        color = AppColors.Primary,
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { onToggleExpand() }
                    )
                }
                
                Row(
                    modifier = Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = typeLabel,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(typeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        color = typeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (item.year.isNotBlank()) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = AppColors.TextMuted,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(11.dp),
                        )
                        Text(
                            text = item.year,
                            modifier = Modifier.padding(start = 3.dp),
                            color = AppColors.TextMuted,
                            fontSize = 11.sp,
                        )
                    }
                }
                if (item.overview.isNotBlank() && !isPerson) {
                    Text(
                        text = item.overview,
                        modifier = Modifier.padding(top = 4.dp),
                        color = AppColors.TextMuted,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (!isPerson && item.rating > 0) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.ratingColor(item.rating).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AppColors.ratingColor(item.rating), modifier = Modifier.size(12.dp))
                    Text(
                        text = String.format("%.1f", item.rating),
                        modifier = Modifier.padding(start = 3.dp),
                        color = AppColors.ratingColor(item.rating),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}