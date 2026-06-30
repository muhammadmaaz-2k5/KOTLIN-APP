package com.job2day.nazaarabox.presentation.browse

import com.job2day.nazaarabox.ads.CustomNativeAd
import com.job2day.nazaarabox.ads.CustomSmallCardAd
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.job2day.nazaarabox.core.AnimeFilters
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.SearchFilters
import com.job2day.nazaarabox.navigation.navigateToDetail
import com.job2day.nazaarabox.presentation.shared.FilterSheet
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.widgets.CustomImage
import com.job2day.nazaarabox.widgets.EmptyState
import com.job2day.nazaarabox.widgets.LoadingCenter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaBrowseScreen(
    mode: BrowseMode,
    title: String,
    navController: NavController,
    viewModel: MediaBrowseViewModel = viewModel(key = mode.name) {
        MediaBrowseViewModel(mode)
    },
) {
    val state by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppColors.BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text(title, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.BackgroundDark),
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters", tint = AppColors.Primary)
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.tabs.forEachIndexed { index, tab ->
                    val chipColors = if (mode == BrowseMode.ANIME) {
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.TabAnime.copy(alpha = 0.25f),
                            selectedLabelColor = AppColors.TabAnime,
                            containerColor = AppColors.SurfaceVariantDark,
                            labelColor = AppColors.TextMuted,
                        )
                    } else {
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Primary.copy(alpha = 0.25f),
                            selectedLabelColor = AppColors.Primary,
                            containerColor = AppColors.SurfaceVariantDark,
                            labelColor = AppColors.TextMuted,
                        )
                    }
                    FilterChip(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        label = { Text(tab.label) },
                        colors = chipColors,
                        shape = RoundedCornerShape(20.dp),
                    )
                }
            }

            val items = state.itemsByTab[state.selectedTab].orEmpty()
            val loading = state.loadingByTab.contains(state.selectedTab)

            if (com.job2day.nazaarabox.utils.AdManager.isWebviewAdsEnabled) {
                CustomNativeAd(
                    adUrl = com.job2day.nazaarabox.utils.AdManager.dynamicWebviewUrl,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }

            when {
                loading && items.isEmpty() -> LoadingCenter()
                items.isEmpty() -> EmptyState("No titles found", modifier = Modifier.fillMaxSize())
                else -> {
                    val adUrl = com.job2day.nazaarabox.utils.AdManager.dynamicWebviewUrl
                    val adEnabled = com.job2day.nazaarabox.utils.AdManager.isWebviewAdsEnabled && adUrl.isNotBlank()
                    val cardRows = items.chunked(3)
                    var itemIndex = 0
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        cardRows.forEachIndexed { rowIndex, rowItems ->
                            item(key = "row_$rowIndex") {
                                androidx.compose.foundation.layout.Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    rowItems.forEach { item ->
                                        androidx.compose.material3.Surface(
                                            onClick = { navController.navigateToDetail(item) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            color = AppColors.CardDark,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(2f / 3f),
                                            ) {
                                                com.job2day.nazaarabox.widgets.CustomImage(
                                                    imageUrl = item.posterUrl,
                                                    modifier = Modifier.fillMaxSize(),
                                                )
                                            }
                                        }
                                        if (itemIndex >= items.size - 4) {
                                            viewModel.loadMoreIfNeeded(itemIndex)
                                        }
                                        itemIndex++
                                    }
                                    if (rowItems.size < 3) {
                                        repeat(3 - rowItems.size) {
                                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                            if (adEnabled && rowIndex < cardRows.lastIndex) {
                                item(key = "ad_$rowIndex") {
                                    androidx.compose.foundation.layout.Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        repeat(3) {
                                            CustomSmallCardAd(
                                                adUrl = adUrl,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(2f / 3f),
                                                backgroundColor = AppColors.CardDark,
                                            )
                                        }
                                    }
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
        FilterSheet(
            mode = mode,
            searchFilters = state.searchFilters,
            animeFilters = state.animeFilters,
            onDismiss = { showFilters = false },
            onApplySearch = {
                viewModel.updateSearchFilters(it)
                showFilters = false
            },
            onApplyAnime = {
                viewModel.updateAnimeFilters(it)
                showFilters = false
            },
        )
    }
}
