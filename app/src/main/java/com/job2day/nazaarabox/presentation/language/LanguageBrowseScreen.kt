package com.job2day.nazaarabox.presentation.language

import com.job2day.nazaarabox.ads.CustomNativeAd
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.job2day.nazaarabox.ads.CustomBannerAd
import com.job2day.nazaarabox.core.LanguageOption
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.navigation.navigateToDetail
import com.job2day.nazaarabox.routes.AppRoutes
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.widgets.CustomImage
import com.job2day.nazaarabox.widgets.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBrowseScreen(
    navController: NavController,
    viewModel: LanguageBrowseViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val lang = viewModel.languages[state.selectedLangIndex]
    val accent = Color(lang.accentArgb)
    val items = viewModel.currentItems()
    val isLoading = viewModel.isLoading()

    Scaffold(
        containerColor = AppColors.BackgroundDark,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(lang.flag, fontSize = 22.sp)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Column {
                            Text(lang.label, fontWeight = FontWeight.Bold)
                            Text(lang.nativeName, fontSize = 11.sp, color = AppColors.TextMuted)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(AppRoutes.SEARCH) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.SurfaceDark,
                    titleContentColor = AppColors.TextPrimary,
                ),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                viewModel.languages.forEachIndexed { index, option ->
                    LanguageChip(
                        option = option,
                        selected = state.selectedLangIndex == index,
                        onClick = { viewModel.selectLanguage(index) },
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TypeChip("All", "all", Icons.Default.Apps, state.selectedType, accent) { viewModel.selectType(it) }
                TypeChip("Movies", "movie", Icons.Default.Movie, state.selectedType, accent) { viewModel.selectType(it) }
                TypeChip("TV", "tv", Icons.Default.Tv, state.selectedType, accent) { viewModel.selectType(it) }
            }
            HorizontalDivider(color = AppColors.SurfaceVariantDark)

            if (com.job2day.nazaarabox.utils.AdManager.isWebviewAdsEnabled) {
                CustomNativeAd(
                    adUrl = com.job2day.nazaarabox.utils.AdManager.dynamicWebviewUrl,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }

            if (com.job2day.nazaarabox.utils.AdManager.isWebviewAdsEnabled) {
                CustomBannerAd(
                    adUrl = com.job2day.nazaarabox.utils.AdManager.dynamicWebviewUrl
                )
            }

            when {
                isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accent)
                }
                items.isEmpty() -> EmptyState(
                    message = "No results for ${lang.label}\nTry switching to Movies or TV",
                    emoji = lang.flag,
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(items, key = { "${it.type}_${it.id}" }) { item ->
                        LanguageCard(item = item, accent = accent, onClick = { navController.navigateToDetail(item) })
                    }
                }
            }
        }

            if (com.job2day.nazaarabox.utils.AdManager.isWebviewAdsEnabled) {
                Box(
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    CustomBannerAd(
                        adUrl = com.job2day.nazaarabox.utils.AdManager.dynamicWebviewUrl,
                        alwaysExpanded = true
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageChip(option: LanguageOption, selected: Boolean, onClick: () -> Unit) {
    val accent = Color(option.accentArgb)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) accent.copy(alpha = 0.78f) else AppColors.SurfaceVariantDark,
        border = BorderStroke(1.dp, if (selected) accent else AppColors.SurfaceVariantDark),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(option.flag, fontSize = 14.sp)
            Spacer(modifier = Modifier.padding(horizontal = 3.dp))
            Text(
                text = option.label,
                color = if (selected) Color.White else AppColors.TextMuted,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun TypeChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: String,
    accent: Color,
    onSelect: (String) -> Unit,
) {
    val isSelected = selected == value
    Surface(
        onClick = { onSelect(value) },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) accent.copy(alpha = 0.12f) else Color.Transparent,
        border = BorderStroke(1.dp, if (isSelected) accent else AppColors.SurfaceVariantDark),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) accent else AppColors.TextMuted, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            Text(label, color = if (isSelected) accent else AppColors.TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun LanguageCard(item: MediaItem, accent: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), color = AppColors.CardDark) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                CustomImage(imageUrl = item.posterUrl, modifier = Modifier.fillMaxSize())
                Surface(
                    modifier = Modifier.padding(6.dp).align(Alignment.TopStart),
                    shape = RoundedCornerShape(4.dp),
                    color = accent.copy(alpha = 0.78f),
                ) {
                    Text(
                        text = if (item.type == "tv") "TV" else "FILM",
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (item.rating > 0) {
                    Text(
                        text = "★ ${String.format("%.1f", item.rating)}",
                        modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp),
                        color = AppColors.Accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                text = item.title,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                color = AppColors.TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.year.isNotBlank()) {
                Text(
                    text = item.year,
                    modifier = Modifier.padding(start = 7.dp, bottom = 8.dp),
                    color = AppColors.TextMuted,
                    fontSize = 10.sp,
                )
            }
        }
    }
}
