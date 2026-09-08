package com.job2day.nazaarabox.presentation.moreapps

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.job2day.nazaarabox.ads.AdMobBanner
import com.job2day.nazaarabox.ads.FullWidthAdBanner
import com.job2day.nazaarabox.core.PromotedApp
import com.job2day.nazaarabox.services.MediaRepository
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.ui.theme.EngoraColors
import com.job2day.nazaarabox.ui.theme.EngoraShapes
import com.job2day.nazaarabox.ui.theme.EngoraTypography
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.widgets.CustomImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MoreAppsUiState(
    val apps: List<PromotedApp> = emptyList(),
    val categories: List<String> = listOf("All"),
    val selectedCategory: String = "All",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class MoreAppsViewModel : ViewModel() {
    private val repository = MediaRepository()

    private val _uiState = MutableStateFlow(MoreAppsUiState(isLoading = true))
    val uiState: StateFlow<MoreAppsUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val apps = repository.getMoreApps(forceRefresh = forceRefresh)
                val dynamicCategories = mutableListOf("All")
                val uniqueCategories = apps.map { it.category.trim() }
                    .filter { it.isNotEmpty() && it != "All" }
                    .distinct()
                dynamicCategories.addAll(uniqueCategories)

                _uiState.value = _uiState.value.copy(
                    apps = apps,
                    categories = dynamicCategories,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to load apps"
                )
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun filteredApps(): List<PromotedApp> {
        val current = _uiState.value
        return if (current.selectedCategory == "All") {
            current.apps
        } else {
            current.apps.filter { it.category.equals(current.selectedCategory, ignoreCase = true) }
        }
    }
}

@Composable
fun MoreAppsScreen(
    navController: NavController,
    viewModel: MoreAppsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val displayedApps = viewModel.filteredApps()
    val featuredApp = remember(state.apps) { state.apps.firstOrNull { it.isFeatured } }

    Scaffold(
        containerColor = EngoraColors.Background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (AdManager.isAdMobEnabled) {
                Surface(
                    color = EngoraColors.BackgroundElevated,
                    border = BorderStroke(1.dp, EngoraColors.CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        AdMobBanner(
                            adUnitId = AdManager.admobBannerId,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(EngoraColors.Background),
        ) {
            // Top Frosted Glass App Bar
            MoreAppsTopBar(
                onBack = { navController.popBackStack() },
                onRefresh = { viewModel.loadApps(forceRefresh = true) },
                isLoading = state.isLoading,
            )

            if (state.isLoading && state.apps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            color = EngoraColors.Red,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(42.dp),
                        )
                        Text(
                            text = "Loading Partner Apps...",
                            style = EngoraTypography.BodySmall,
                            color = EngoraColors.TextSecondary,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    // 1. Featured Spotlight Hero Banner (if available)
                    if (featuredApp != null && state.selectedCategory == "All") {
                        item(key = "featured_hero") {
                            FeaturedHeroAppCard(
                                app = featuredApp,
                                onInstall = { launchPlayStore(context, featuredApp) },
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // 2. Categories Horizontal Scroll
                    if (state.categories.size > 1) {
                        item(key = "categories_row") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                state.categories.forEach { category ->
                                    val isSelected = category == state.selectedCategory
                                    CategoryPill(
                                        label = category,
                                        isSelected = isSelected,
                                        onClick = { viewModel.selectCategory(category) },
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Top AdMob Banner
                    if (AdManager.isAdPlacementEnabled("moreapps_banner")) {
                        item(key = "moreapps_top_admob") {
                            FullWidthAdBanner(
                                placement = "moreapps_banner",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Section Title
                    item(key = "section_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = if (state.selectedCategory == "All") "Recommended Apps" else "${state.selectedCategory} Apps",
                                style = EngoraTypography.SectionHeader,
                                color = EngoraColors.TextPrimary,
                            )
                            Text(
                                text = "${displayedApps.size} apps available",
                                fontSize = 11.sp,
                                color = EngoraColors.TextMuted,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 3. App Cards List
                    if (displayedApps.isEmpty()) {
                        item(key = "empty_state") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text("📱", fontSize = 36.sp)
                                    Text(
                                        text = "No apps found in this category",
                                        style = EngoraTypography.TitleMedium,
                                        color = EngoraColors.TextSecondary,
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            items = displayedApps,
                            key = { it.id },
                        ) { app ->
                            AppCard(
                                app = app,
                                onInstall = { launchPlayStore(context, app) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                        }
                    }

                    // Bottom AdMob Banner
                    if (AdManager.isAdPlacementEnabled("moreapps_banner")) {
                        item(key = "moreapps_bottom_admob") {
                            Spacer(modifier = Modifier.height(10.dp))
                            FullWidthAdBanner(
                                placement = "moreapps_banner",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreAppsTopBar(
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(EngoraColors.BackgroundElevated)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Circular Glass Back Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(EngoraColors.SurfaceVariant)
                    .border(1.dp, EngoraColors.GlassBorder, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = EngoraColors.TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Apps,
                        contentDescription = null,
                        tint = EngoraColors.Red,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "More Apps & Games",
                        style = EngoraTypography.TitleLarge,
                        color = EngoraColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "Companion tools, anime hubs & utilities",
                    fontSize = 11.sp,
                    color = EngoraColors.TextMuted,
                    maxLines = 1,
                )
            }

            IconButton(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(EngoraColors.SurfaceVariant)
                    .border(1.dp, EngoraColors.GlassBorder, CircleShape),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = EngoraColors.Red,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = EngoraColors.TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundBrush = if (isSelected) {
        Brush.horizontalGradient(listOf(EngoraColors.Red, EngoraColors.RedDark))
    } else {
        Brush.horizontalGradient(listOf(EngoraColors.SurfaceVariant, EngoraColors.SurfaceVariant))
    }
    val borderColor = if (isSelected) EngoraColors.RedLight else EngoraColors.CardBorder

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundBrush)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else EngoraColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun FeaturedHeroAppCard(
    app: PromotedApp,
    onInstall: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = EngoraColors.Surface,
        border = BorderStroke(1.dp, EngoraColors.Red.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = EngoraColors.Red.copy(alpha = 0.3f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            EngoraColors.RedDark.copy(alpha = 0.30f),
                            EngoraColors.Surface,
                            EngoraColors.BackgroundElevated,
                        )
                    )
                )
                .padding(16.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Spotlight Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.horizontalGradient(listOf(EngoraColors.Gold, EngoraColors.GoldDark)))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "★ SPOTLIGHT CHOICE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            letterSpacing = 0.5.sp,
                        )
                    }

                    if (app.badge.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EngoraColors.Red.copy(alpha = 0.25f))
                                .border(1.dp, EngoraColors.Red.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = app.badge.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EngoraColors.RedLight,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // App Icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.5.dp, EngoraColors.Gold.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                            .background(EngoraColors.BackgroundElevated),
                        contentAlignment = Alignment.Center,
                    ) {
                        CustomImage(
                            imageUrl = app.iconUrl,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.name,
                            style = EngoraTypography.TitleMedium,
                            color = EngoraColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = app.tagline.ifEmpty { app.category },
                            fontSize = 12.sp,
                            color = EngoraColors.TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = EngoraColors.Gold,
                                    modifier = Modifier.size(13.dp),
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = String.format("%.1f", app.rating),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EngoraColors.Gold,
                                )
                            }
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = EngoraColors.TextMuted,
                            )
                            Text(
                                text = "${app.downloads} downloads",
                                fontSize = 11.sp,
                                color = EngoraColors.TextMuted,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(EngoraColors.Red, Color(0xFFC20710))
                            )
                        )
                        .clickable(onClick = onInstall),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Get on Google Play",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppCard(
    app: PromotedApp,
    onInstall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = EngoraColors.CardElevated,
        border = BorderStroke(1.dp, EngoraColors.CardBorder),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, EngoraColors.GlassBorder, RoundedCornerShape(16.dp))
                    .background(EngoraColors.Background),
                contentAlignment = Alignment.Center,
            ) {
                CustomImage(
                    imageUrl = app.iconUrl,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = app.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = EngoraColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    if (app.badge.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EngoraColors.Red.copy(alpha = 0.18f))
                                .border(1.dp, EngoraColors.Red.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = app.badge.uppercase(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = EngoraColors.RedLight,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = app.tagline.ifEmpty { app.description.ifEmpty { app.category } },
                    fontSize = 11.sp,
                    color = EngoraColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = EngoraColors.Gold,
                            modifier = Modifier.size(11.dp),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", app.rating),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EngoraColors.Gold,
                        )
                    }

                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = EngoraColors.TextMuted,
                    )

                    Text(
                        text = app.downloads,
                        fontSize = 10.sp,
                        color = EngoraColors.TextMuted,
                        fontWeight = FontWeight.Medium,
                    )

                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = EngoraColors.TextMuted,
                    )

                    Text(
                        text = app.category,
                        fontSize = 10.sp,
                        color = EngoraColors.Cyan,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Quick Install Action Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(EngoraColors.SurfaceVariant)
                    .border(1.dp, EngoraColors.Red.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onInstall)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "GET",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = EngoraColors.RedLight,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

private fun launchPlayStore(context: Context, app: PromotedApp) {
    val packageName = app.packageName.trim()
    val playUrl = app.playStoreUrl.trim()
    try {
        if (packageName.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        }
    } catch (e: Exception) {
        // Market intent not available (e.g. emulator without Play Store)
    }

    val webUrl = if (playUrl.isNotEmpty()) {
        playUrl
    } else if (packageName.isNotEmpty()) {
        "https://play.google.com/store/apps/details?id=$packageName"
    } else {
        ""
    }

    if (webUrl.isNotEmpty()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open store link", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "No store link available for this app", Toast.LENGTH_SHORT).show()
    }
}
