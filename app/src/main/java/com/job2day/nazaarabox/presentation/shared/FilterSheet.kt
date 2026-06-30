package com.job2day.nazaarabox.presentation.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.job2day.nazaarabox.core.AnimeFilters
import com.job2day.nazaarabox.core.SearchFilters
import com.job2day.nazaarabox.presentation.browse.BrowseMode
import com.job2day.nazaarabox.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    mode: BrowseMode,
    searchFilters: SearchFilters,
    animeFilters: AnimeFilters,
    onDismiss: () -> Unit,
    onApplySearch: (SearchFilters) -> Unit,
    onApplyAnime: (AnimeFilters) -> Unit,
) {
    var localSearch by remember { mutableStateOf(searchFilters) }
    var localAnime by remember { mutableStateOf(animeFilters) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val searchTabs = listOf("Genre", "Country", "Year", "Language", "Sort by")
    val animeTabs = listOf("Sort", "Country", "Year")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.SurfaceDark,
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Filters", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    text = "Reset",
                    color = AppColors.Primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        if (mode == BrowseMode.ANIME) {
                            localAnime = AnimeFilters()
                        } else {
                            localSearch = SearchFilters()
                        }
                    },
                )
            }

            if (mode == BrowseMode.ANIME) {
                TabRow(
                    selectedTabIndex = selectedTab.coerceAtMost(animeTabs.lastIndex),
                    containerColor = AppColors.SurfaceDark,
                    contentColor = AppColors.Primary,
                ) {
                    animeTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, maxLines = 1) },
                        )
                    }
                }
                val options = when (selectedTab) {
                    0 -> AnimeFilters.sortOptions
                    1 -> AnimeFilters.countries
                    else -> AnimeFilters.years
                }
                val selected = when (selectedTab) {
                    0 -> localAnime.sortBy
                    1 -> localAnime.country
                    else -> localAnime.year
                }
                FilterOptionsGrid(options = options, selected = selected) { option ->
                    localAnime = when (selectedTab) {
                        0 -> localAnime.copy(sortBy = option)
                        1 -> localAnime.copy(country = option)
                        else -> localAnime.copy(year = option)
                    }
                }
                Button(
                    onClick = { onApplyAnime(localAnime) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Apply Filters", fontWeight = FontWeight.Bold)
                }
            } else {
                TabRow(
                    selectedTabIndex = selectedTab.coerceAtMost(searchTabs.lastIndex),
                    containerColor = AppColors.SurfaceDark,
                    contentColor = AppColors.Primary,
                ) {
                    searchTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, maxLines = 1) },
                        )
                    }
                }
                val options = when (selectedTab) {
                    0 -> SearchFilters.genres
                    1 -> SearchFilters.countries
                    2 -> SearchFilters.years
                    3 -> SearchFilters.languages
                    else -> SearchFilters.sortOptions
                }
                val selected = when (selectedTab) {
                    0 -> localSearch.genre
                    1 -> localSearch.country
                    2 -> localSearch.year
                    3 -> localSearch.language
                    else -> localSearch.sortBy
                }
                FilterOptionsGrid(options = options, selected = selected) { option ->
                    localSearch = when (selectedTab) {
                        0 -> localSearch.copy(genre = option)
                        1 -> localSearch.copy(country = option)
                        2 -> localSearch.copy(year = option)
                        3 -> localSearch.copy(language = option)
                        else -> localSearch.copy(sortBy = option)
                    }
                }
                Button(
                    onClick = { onApplySearch(localSearch) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Apply Filters", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FilterOptionsGrid(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 320.dp),
    ) {
        items(options) { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option, maxLines = 1) },
            )
        }
    }
}
