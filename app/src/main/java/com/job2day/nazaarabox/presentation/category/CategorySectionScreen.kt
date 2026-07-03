package com.job2day.nazaarabox.presentation.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.navigation.getThemedSection
import com.job2day.nazaarabox.navigation.navigateToDetail
import com.job2day.nazaarabox.services.MediaRepository
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.adaptiveGridColumns
import com.job2day.nazaarabox.widgets.LoadingCenter
import com.job2day.nazaarabox.widgets.MovieGridCard
import com.job2day.nazaarabox.ads.InlineBannerAd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySectionScreen(navController: NavController) {
    val handle = navController.previousBackStackEntry?.savedStateHandle
    val section = handle?.let { navController.getThemedSection(it) }
    var items by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    var page by remember { mutableIntStateOf(1) }
    var totalPages by remember { mutableIntStateOf(1) }
    val repository = remember { MediaRepository() }
    val gridState = rememberLazyGridState()

    val columns = adaptiveGridColumns()

    LaunchedEffect(section) {
        if (section == null) return@LaunchedEffect
        loading = true
        val params = section.tmdbParams.toMutableMap().apply {
            put("include_adult", "false")
            if (!containsKey("sort_by")) put("sort_by", "popularity.desc")
        }
        val (newItems, pages) = if (section.mediaType == "tv") {
            repository.discover("tv", params, 1)
        } else {
            repository.discoverMovies(params, 1)
        }
        items = newItems
        totalPages = pages
        page = 1
        loading = false
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= items.size - 4 && !loadingMore && page < totalPages && items.isNotEmpty()
        }
    }

    LaunchedEffect(shouldLoadMore, section) {
        if (!shouldLoadMore || section == null) return@LaunchedEffect
        loadingMore = true
        val params = section.tmdbParams.toMutableMap().apply {
            put("include_adult", "false")
            if (!containsKey("sort_by")) put("sort_by", "popularity.desc")
        }
        val nextPage = page + 1
        val (newItems, pages) = if (section.mediaType == "tv") {
            repository.discover("tv", params, nextPage)
        } else {
            repository.discoverMovies(params, nextPage)
        }
        items = items + newItems
        totalPages = pages
        page = nextPage
        loadingMore = false
    }

    if (section == null) {
        LoadingCenter()
        return
    }

    Scaffold(
        containerColor = AppColors.BackgroundDark,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("${section.emoji} ${section.title}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.SurfaceDark,
                    titleContentColor = AppColors.TextPrimary,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (loading) {
                LoadingCenter()
            } else {
                val gridItems = buildList<Any?> {
                    addAll(items)
                    items.forEachIndexed { index, _ ->
                        if ((index + 1) % 6 == 0 && index < items.lastIndex) {
                            add("ad")
                        }
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(gridItems, key = { it?.let { k -> if (k is String) "category_ad_banner" else "${(k as com.job2day.nazaarabox.core.MediaItem).type}_${k.id}" } ?: "category_ad" }) { entry ->
                        if (entry is String && entry == "ad") {
                            Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                                InlineBannerAd(modifier = Modifier.fillMaxSize())
                            }
                        } else if (entry is com.job2day.nazaarabox.core.MediaItem) {
                            MovieGridCard(
                                item = entry,
                                showTypeBadge = true,
                                onClick = { navController.navigateToDetail(entry) },
                            )
                        }
                    }
                }
            }
            if (loadingMore) {
                CircularProgressIndicator(
                    color = AppColors.Primary,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                )
            }
        }
    }
}