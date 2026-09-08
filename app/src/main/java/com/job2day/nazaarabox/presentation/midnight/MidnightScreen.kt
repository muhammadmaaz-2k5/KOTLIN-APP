package com.job2day.nazaarabox.presentation.midnight

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import com.job2day.nazaarabox.widgets.MidnightMediaCard
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.ThemedSection
import com.job2day.nazaarabox.navigation.navigateToDetail
import com.job2day.nazaarabox.routes.AppRoutes
import com.job2day.nazaarabox.ui.theme.EngoraColors
import com.job2day.nazaarabox.utils.MediaParser
import com.job2day.nazaarabox.utils.MidnightAgeGateManager
import com.job2day.nazaarabox.utils.rememberIsOnline
import com.job2day.nazaarabox.widgets.CustomImage
import com.job2day.nazaarabox.widgets.EngoraCardMode
import com.job2day.nazaarabox.widgets.EngoraLoadingSize
import com.job2day.nazaarabox.widgets.EngoraLoadingWidget
import com.job2day.nazaarabox.widgets.EngoraMediaCard
import com.job2day.nazaarabox.widgets.EngoraNoInternetScreen
import com.job2day.nazaarabox.widgets.EngoraOfflineBanner
import com.job2day.nazaarabox.ads.FullWidthAdBanner
import com.job2day.nazaarabox.utils.AdManager

private val MidnightBg = Color(0xFF07070B)
private val NeonMagenta = Color(0xFFFF1A75)
private val NeonPurple = Color(0xFF9D4EDD)
private val DarkCard = Color(0xFF13131F)

@Composable
fun MidnightScreen(
    navController: NavController,
    viewModel: MidnightViewModel = viewModel(),
) {
    val (isVerifiedState, setVerified) = MidnightAgeGateManager.rememberIsAgeVerified()
    val isVerified = isVerifiedState.value

    val isOnline by rememberIsOnline()
    val state by viewModel.uiState.collectAsState()

    // 1. Age Verification Permission Gate (18+ Only)
    if (!isVerified) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBg),
        ) {
            MidnightAgeGateDialog(
                onConfirm = { setVerified(true) },
                onDismiss = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
        return
    }

    // 2. Offline with No Data State
    if (!isOnline && state.feed.sections.isEmpty() && state.feed.featured.isEmpty()) {
        EngoraNoInternetScreen(
            onRetry = { viewModel.loadFeed(forceRefresh = true) },
        )
        return
    }

    // 3. Loading State
    if (state.isLoading && state.feed.sections.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBg),
            contentAlignment = Alignment.Center,
        ) {
            EngoraLoadingWidget(
                size = EngoraLoadingSize.FULLSCREEN,
                message = "Entering Midnight Club...",
            )
        }
        return
    }

    // 4. Main Midnight Cinema Experience
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBg),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
        ) {
            // A. Top Nightclub VIP Bar
            item(key = "midnight_top_bar") {
                MidnightTopBar(
                    onLock = { setVerified(false) },
                )
            }

            // B. Hero Featured Carousel (Spotlight 18+ Cinema)
            if (state.feed.featured.isNotEmpty()) {
                item(key = "midnight_hero") {
                    MidnightHeroSpotlight(
                        items = state.feed.featured,
                        onItemClick = { navController.navigateToDetail(it) },
                    )
                }
            }

            // C. Nightclub Sub-Category Filter Chips
            if (state.feed.categories.isNotEmpty()) {
                item(key = "midnight_chips") {
                    MidnightCategoryChipsRow(
                        categories = state.feed.categories,
                        selectedId = state.selectedCategoryId,
                        onSelect = { viewModel.selectCategory(it) },
                    )
                }
            }

            // Top AdMob Banner
            if (AdManager.isAdPlacementEnabled("midnight_banner")) {
                item(key = "midnight_top_admob") {
                    FullWidthAdBanner(
                        placement = "midnight_banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
            }

            // D. Themed Nightclub Sections or 2-Column Category Grid
            if (state.selectedCategoryId == 0) {
                // All Midnight: Show All Category Sections in Horizontal Shelves
                items(
                    items = state.feed.sections,
                    key = { "midnight_sec_${it.id}" },
                ) { section ->
                    MidnightSectionRow(
                        section = section,
                        onItemClick = { navController.navigateToDetail(it) },
                    )
                }
            } else {
                // Specific Category Selected: Render as Responsive 2-Column Grid
                val selectedSection = state.feed.sections.getOrNull(state.selectedCategoryId - 1)
                    ?: state.feed.sections.find { it.id == state.selectedCategoryId }

                if (selectedSection != null) {
                    item(key = "selected_cat_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(text = selectedSection.emoji, fontSize = 20.sp)
                                Text(
                                    text = selectedSection.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NeonMagenta.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, NeonMagenta.copy(alpha = 0.4f)),
                            ) {
                                Text(
                                    text = "${selectedSection.items.size} VIP STREAMS",
                                    color = NeonMagenta,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        }
                    }

                    val chunkedItems = selectedSection.items.chunked(2)
                    items(chunkedItems.size, key = { "cat_grid_row_$it" }) { rowIndex ->
                        val pair = chunkedItems[rowIndex]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                MidnightMediaCard(
                                    item = pair[0],
                                    isGrid = true,
                                    onClick = { navController.navigateToDetail(pair[0]) },
                                )
                            }
                            if (pair.size > 1) {
                                Box(modifier = Modifier.weight(1f)) {
                                    MidnightMediaCard(
                                        item = pair[1],
                                        isGrid = true,
                                        onClick = { navController.navigateToDetail(pair[1]) },
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (AdManager.isAdPlacementEnabled("midnight_banner")) {
                item(key = "midnight_bottom_admob") {
                    FullWidthAdBanner(
                        placement = "midnight_banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }

        // Floating Offline Banner (Mid-session loss)
        EngoraOfflineBanner(
            visible = !isOnline,
            onRetry = { viewModel.loadFeed(forceRefresh = true) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 60.dp),
        )
    }
}

@Composable
private fun MidnightTopBar(
    onLock: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF140714).copy(alpha = 0.95f),
                        MidnightBg,
                    ),
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Brand Logo + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "ENGORA",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    letterSpacing = 2.sp,
                )
                Text(
                    text = "MIDNIGHT",
                    color = NeonMagenta,
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    letterSpacing = 1.5.sp,
                )
            }

            // 18+ Badge and Re-lock Option
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonMagenta.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.5f)),
                ) {
                    Text(
                        text = "18+ VIP",
                        color = NeonMagenta,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }

                IconButton(
                    onClick = onLock,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.07f)),
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Lock Midnight",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MidnightHeroSpotlight(
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
) {
    if (items.isEmpty()) return
    val pagerState = rememberPagerState(initialPage = 0) { items.size }

    // Auto-advance loop every 5.5s
    LaunchedEffect(pagerState.pageCount) {
        if (items.size > 1) {
            while (true) {
                delay(5500)
                if (!pagerState.isScrollInProgress) {
                    val nextPage = (pagerState.currentPage + 1) % items.size
                    pagerState.animateScrollToPage(
                        page = nextPage,
                        animationSpec = tween(700, easing = FastOutSlowInEasing)
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 12.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            key = { items.getOrNull(it)?.id ?: it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(16f / 9.5f)
                .clip(RoundedCornerShape(22.dp))
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                NeonMagenta.copy(alpha = 0.55f),
                                NeonPurple.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        ),
                    ),
                    RoundedCornerShape(22.dp),
                ),
        ) { page ->
            val item = items[page]
            val backdropUrl = MediaParser.imageUrl(item.backdropUrl.ifBlank { item.posterUrl }, "w780")

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onItemClick(item) },
            ) {
                CustomImage(
                    imageUrl = backdropUrl,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

                // Cinema Gradient Vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0x99000000),
                                    Color(0xF007070B),
                                ),
                            ),
                        ),
                )

                // In-Content Overlay Text & Actions
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonMagenta,
                        ) {
                            Text(
                                text = "MIDNIGHT SPOTLIGHT",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.60f),
                            border = BorderStroke(0.5.dp, NeonMagenta.copy(alpha = 0.6f)),
                        ) {
                            Text(
                                text = "18+ VIP",
                                color = NeonMagenta,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            )
                        }

                        if (item.rating > 0.0) {
                            Text(
                                text = "★ ${String.format("%.1f", item.rating)}",
                                color = Color(0xFFFFD166),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (item.overview.isNotBlank()) {
                        Text(
                            text = item.overview,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 15.sp,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onItemClick(item) },
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(17.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonMagenta,
                            contentColor = Color.White,
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Watch Stream", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Pagination Dots Indicator
        if (items.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.indices.forEach { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(4.dp)
                            .width(if (isSelected) 18.dp else 6.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isSelected) NeonMagenta else Color.White.copy(alpha = 0.20f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun MidnightCategoryChipsRow(
    categories: List<com.job2day.nazaarabox.core.HomeCategory>,
    selectedId: Int,
    onSelect: (Int) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categories, key = { it.id }) { cat ->
            val isSelected = cat.id == selectedId
            Surface(
                onClick = { onSelect(cat.id) },
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) NeonMagenta else DarkCard,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) NeonMagenta else Color.White.copy(alpha = 0.12f),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (cat.emoji.isNotBlank()) {
                        Text(text = cat.emoji, fontSize = 13.sp)
                    }
                    Text(
                        text = cat.label,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun MidnightSectionRow(
    section: ThemedSection,
    onItemClick: (MediaItem) -> Unit,
) {
    if (section.items.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = section.emoji,
                    fontSize = 17.sp,
                )
                Text(
                    text = section.title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.3.sp,
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NeonMagenta.copy(alpha = 0.15f),
                border = BorderStroke(0.5.dp, NeonMagenta.copy(alpha = 0.35f)),
            ) {
                Text(
                    text = "${section.items.size} VIP",
                    color = NeonMagenta,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Row of Media Items
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(section.items, key = { it.id }) { item ->
                MidnightMediaCard(
                    item = item,
                    isGrid = false,
                    onClick = { onItemClick(item) },
                )
            }
        }
    }
}
