package com.job2day.nazaarabox.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.job2day.nazaarabox.ads.CustomSmallCardAd
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.ThemedSection
import com.job2day.nazaarabox.navigation.navigateToDetail
import com.job2day.nazaarabox.navigation.navigateToThemedSection
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.widgets.CustomImage
import com.job2day.nazaarabox.widgets.LoadingSkeleton
import com.job2day.nazaarabox.widgets.SectionHeader

@Composable
fun HomeSectionsWidget(
    navController: NavController,
    viewModel: HomeSectionsViewModel = viewModel(),
    adUrl: String? = null,
) {
    val state by viewModel.uiState.collectAsState()

    Column {
        if (state.isLoadingCustom) {
            SectionBlock(
                emoji = "💎",
                title = "Custom Exclusives",
                isLoading = true,
                items = emptyList(),
                showMore = false,
                adUrl = adUrl,
                onMore = {},
                onItemClick = {},
            )
        } else if (state.customExclusives.isNotEmpty()) {
            SectionBlock(
                emoji = "💎",
                title = "Custom Exclusives",
                isLoading = false,
                items = state.customExclusives,
                showMore = false,
                adUrl = adUrl,
                onMore = {},
                onItemClick = { navController.navigateToDetail(it) },
            )
        }

        viewModel.sections.forEachIndexed { index, section ->
            SectionBlock(
                emoji = section.emoji,
                title = section.title,
                isLoading = state.sectionLoading[index] != false && state.sectionPreviews[index] == null,
                items = state.sectionPreviews[index].orEmpty(),
                showMore = true,
                adUrl = adUrl,
                onMore = { navController.navigateToThemedSection(section) },
                onItemClick = { navController.navigateToDetail(it) },
            )
        }
    }
}

@Composable
private fun SectionBlock(
    emoji: String,
    title: String,
    isLoading: Boolean,
    items: List<MediaItem>,
    showMore: Boolean,
    adUrl: String?,
    onMore: () -> Unit,
    onItemClick: (MediaItem) -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = 28.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (showMore) {
                Surface(
                    onClick = onMore,
                    shape = RoundedCornerShape(20.dp),
                    color = AppColors.Primary.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Primary.copy(alpha = 0.32f)),
                ) {
                    Text(
                        text = "More",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        color = AppColors.Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (isLoading) {
            LoadingSkeleton(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(200.dp),
                height = 200,
            )
        } else if (items.isEmpty()) {
            Text(
                text = "No results",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = AppColors.TextMuted,
            )
        } else {
            Box(modifier = Modifier.height(200.dp)) {
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val adUrlParam = adUrl
                    val allItems = buildList {
                        items.forEachIndexed { index, item ->
                            add(item)
                            if (adUrlParam != null && (index + 1) % 5 == 0 && index < items.lastIndex) {
                                add(null)
                            }
                        }
                    }
                    items(allItems.size) { index ->
                        val entry = allItems[index]
                        if (entry == null && adUrlParam != null) {
                            CustomSmallCardAd(
                                adUrl = adUrlParam,
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(140.dp),
                                backgroundColor = AppColors.CardDark,
                            )
                        } else if (entry != null) {
                            SectionCard(item = entry, onClick = { onItemClick(entry) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(item: MediaItem, onClick: () -> Unit) {
    Column(modifier = Modifier.width(120.dp)) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = AppColors.CardDark,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            ) {
                CustomImage(
                    imageUrl = item.posterUrl,
                    modifier = Modifier.fillMaxSize(),
                )
                if (item.rating > 0) {
                    Text(
                        text = "★ ${String.format("%.1f", item.rating)}",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp),
                        color = AppColors.Accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Text(
            text = item.title,
            modifier = Modifier.padding(top = 6.dp),
            color = AppColors.TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (item.year.isNotBlank()) {
            Text(
                text = item.year,
                color = AppColors.TextMuted,
                fontSize = 10.sp,
            )
        }
    }
}
