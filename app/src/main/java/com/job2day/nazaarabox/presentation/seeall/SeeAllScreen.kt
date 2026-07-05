package com.job2day.nazaarabox.presentation.seeall

import com.job2day.nazaarabox.utils.AdManager

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.job2day.nazaarabox.navigation.navigateToDetail
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.adaptiveGridColumns
import com.job2day.nazaarabox.widgets.EmptyState
import com.job2day.nazaarabox.widgets.MovieGridCard
import com.job2day.nazaarabox.ads.InlineBannerAd
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeeAllScreen(navController: NavController) {
    val handle = navController.previousBackStackEntry?.savedStateHandle
    val title = handle?.get<String>("title").orEmpty()
    val items = handle?.get<String>("items")?.let { Json.decodeFromString<List<com.job2day.nazaarabox.core.MediaItem>>(it) }
        .orEmpty()
    val columns = adaptiveGridColumns()

    Scaffold(
        containerColor = AppColors.BackgroundDark,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                        com.job2day.nazaarabox.widgets.CustomIconWidget(
                            iconName = "arrow_back_ios_new_rounded",
                            color = AppColors.TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.BackgroundDark,
                    titleContentColor = AppColors.TextPrimary,
                ),
            )
        },
    ) { padding ->
        if (items.isEmpty()) {
            EmptyState("Nothing here yet", modifier = Modifier.padding(padding))
        } else {
            val gridItems = buildList<Any?> {
                addAll(items)
                items.forEachIndexed { index, _ ->
                    if ((index + 1) % 6 == 0 && index < items.lastIndex && AdManager.isAdPlacementEnabled("seeall_banner")) {
                        add("ad")
                    }
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(gridItems, key = { it?.let { k -> if (k is String) "ad_banner" else "${(k as com.job2day.nazaarabox.core.MediaItem).type}_${k.id}" } ?: "ad" }) { entry ->
                    if (entry is String && entry == "ad") {
                        Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                            InlineBannerAd(
                                placement = "seeall_banner",
                                modifier = Modifier.fillMaxSize(),
                            )
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
    }
}
