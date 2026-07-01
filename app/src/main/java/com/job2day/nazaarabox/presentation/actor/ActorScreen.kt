package com.job2day.nazaarabox.presentation.actor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.job2day.nazaarabox.ads.CustomBannerAd
import com.job2day.nazaarabox.ads.CustomNativeAd
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.navigation.navigateToDetail
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AppActions
import com.job2day.nazaarabox.widgets.CustomImage
import com.job2day.nazaarabox.widgets.LoadingCenter
import com.job2day.nazaarabox.widgets.SimilarTitleCard
import com.job2day.nazaarabox.widgets.SimilarTitleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActorScreen(
    personId: Int,
    navController: NavController,
    viewModel: ActorViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(personId) { viewModel.load(personId) }

    Scaffold(
        containerColor = AppColors.BackgroundDark,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(state.person?.name.orEmpty(), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    state.person?.let { person ->
                        IconButton(onClick = { AppActions.sharePerson(context, person) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = AppColors.TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.BackgroundDark,
                    titleContentColor = AppColors.TextPrimary,
                ),
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingCenter(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item { ActorHeroHeader(photoUrl = state.person?.photoUrl.orEmpty()) }

            if (com.job2day.nazaarabox.utils.AdManager.isWebviewAdsEnabled) {
                item {
                    CustomNativeAd(
                        adUrl = com.job2day.nazaarabox.utils.AdManager.dynamicWebviewUrl,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }
            }

            if (com.job2day.nazaarabox.utils.AdManager.isWebviewAdsEnabled) {
                item {
                    CustomBannerAd(
                        adUrl = com.job2day.nazaarabox.utils.AdManager.dynamicWebviewUrl
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = state.person?.name.orEmpty(),
                        color = AppColors.TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    if (!state.person?.knownForDepartment.isNullOrBlank()) {
                        Text(
                            text = state.person?.knownForDepartment.orEmpty(),
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(AppColors.Primary.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            color = AppColors.Primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            if (!state.person?.birthday.isNullOrBlank() || !state.person?.placeOfBirth.isNullOrBlank()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        if (!state.person?.birthday.isNullOrBlank()) {
                            Column {
                                Text("Birthday", color = AppColors.TextMuted, fontSize = 12.sp)
                                Text(state.person?.birthday.orEmpty(), color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (!state.person?.placeOfBirth.isNullOrBlank()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Birthplace", color = AppColors.TextMuted, fontSize = 12.sp)
                                Text(
                                    state.person?.placeOfBirth.orEmpty(),
                                    color = AppColors.TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            if (!state.person?.biography.isNullOrBlank()) {
                item {
                    Text(
                        text = "📖  Biography",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    )
                    Text(
                        text = state.person?.biography.orEmpty(),
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = AppColors.TextPrimary,
                        maxLines = if (state.isBioExpanded) Int.MAX_VALUE else 4,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 22.sp,
                    )
                    Text(
                        text = if (state.isBioExpanded) "Show less" else "Read more",
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .clickable { viewModel.toggleBio() },
                        color = AppColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (state.knownFor.isNotEmpty()) {
                item {
                    Text(
                        text = "⭐  Known For",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.knownFor) { credit ->
                            SimilarTitleCard(
                                item = credit,
                                onClick = { navController.navigateToDetail(credit) },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "🎬  Filmography",
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    )
                    Text(
                        text = "${state.credits.size} titles",
                        color = AppColors.TextMuted,
                        fontSize = 13.sp,
                    )
                }
            }

            val visibleCredits = if (state.showAllFilmography) state.credits else state.credits.take(4)
            items(visibleCredits, key = { "${it.type}_${it.id}" }) { credit ->
                FilmographyRow(
                    item = credit,
                    onClick = { navController.navigateToDetail(credit) },
                )
            }

            if (state.credits.size > 4) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppColors.SurfaceDark)
                            .border(1.dp, AppColors.Outline, RoundedCornerShape(14.dp))
                            .clickable { viewModel.toggleFilmography() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (state.showAllFilmography) "Show Less" else "Show All ${state.credits.size} Titles",
                            color = AppColors.Primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ActorHeroHeader(photoUrl: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
        ) {
            CustomImage(
                imageUrl = photoUrl,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.3f to Color.Transparent,
                                0.7f to AppColors.BackgroundDark.copy(alpha = 0.5f),
                                1f to AppColors.BackgroundDark,
                            ),
                        ),
                    ),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(110.dp)
                .clip(CircleShape)
                .border(3.dp, AppColors.Primary, CircleShape)
                .background(AppColors.SurfaceDark),
        ) {
            CustomImage(
                imageUrl = photoUrl,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun FilmographyRow(
    item: MediaItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.SurfaceDark)
            .border(1.dp, AppColors.Outline, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CustomImage(
            imageUrl = item.posterUrl,
            modifier = Modifier
                .width(48.dp)
                .height(68.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = item.title,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.year,
                color = AppColors.TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (item.rating > 0) {
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
        Icon(
            Icons.Default.PlayArrow,
            contentDescription = null,
            tint = AppColors.Primary,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
