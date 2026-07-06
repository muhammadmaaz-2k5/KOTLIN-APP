package com.job2day.nazaarabox.presentation.player

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Fullscreen
import com.job2day.nazaarabox.widgets.DynamicWebView
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.routes.AppRoutes
import com.job2day.nazaarabox.core.VideoServer
import com.job2day.nazaarabox.services.MediaRepository
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.ui.theme.NazaaraBoxPrimary
import com.job2day.nazaarabox.ui.theme.NazaaraBlackBackground
import com.job2day.nazaarabox.ui.theme.NazaaraBoxCardBackground
import com.job2day.nazaarabox.ui.theme.NazaaraBoxHeaderBackground
import com.job2day.nazaarabox.utils.AppActions
import com.job2day.nazaarabox.widgets.LoadingCenter
import com.job2day.nazaarabox.widgets.MoreMenuSheet
import com.job2day.nazaarabox.widgets.ServerBottomSheet
import com.job2day.nazaarabox.ads.InlineBannerAd
import com.job2day.nazaarabox.ads.CustomInterstitialAd
import com.job2day.nazaarabox.utils.AdManager
import kotlinx.coroutines.delay
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerScreen(navController: NavController) {
    val item = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("mediaItem")
        ?.let { com.job2day.nazaarabox.routes.AppRoutes.decodeItem(it) }
    val context = LocalContext.current

    if (item == null) {
        LoadingCenter()
        return
    }

    var servers by remember { mutableStateOf<List<VideoServer>>(emptyList()) }
    var serverIndex by remember { mutableIntStateOf(0) }
    var isLoadingServers by remember { mutableStateOf(true) }
    var showMore by remember { mutableStateOf(false) }
    var showServerSheet by remember { mutableStateOf(false) }
    var showInterstitial by remember { mutableStateOf(false) }
    val repository = remember { MediaRepository() }

    LaunchedEffect(item) {
        servers = repository.getVideoServers(item, item.season, item.episode)
        isLoadingServers = false
    }

    val currentServer = servers.getOrNull(serverIndex)

    fun switchServer(index: Int) {
        if (index == serverIndex || index !in servers.indices) return
        serverIndex = index
    }

    if (showInterstitial && AdManager.isAdPlacementEnabled("player_banner")) {
        CustomInterstitialAd(
            adUrl = AdManager.getAdPlacementUrl("player_banner"),
            onDismiss = {
                showInterstitial = false
                AdManager.recordInterstitial()
            },
        )
    }

    if (isLoadingServers) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NazaaraBlackBackground),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = NazaaraBoxPrimary)
        }
        return
    }

    Scaffold(
        containerColor = NazaaraBlackBackground,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = item.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        currentServer?.let {
                            Text(
                                text = "${it.icon} ${it.label}",
                                color = NazaaraBoxPrimary,
                                fontSize = 12.sp,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { AppActions.shareItem(context, item) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                    IconButton(onClick = { showMore = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NazaaraBlackBackground),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        val videoUrl = currentServer?.buildUrl(item, item.season, item.episode)
                        if (!videoUrl.isNullOrBlank()) {
                            DynamicWebView(
                                url = videoUrl,
                                modifier = Modifier.fillMaxSize(),
                                height = null,
                                wrapInCard = false,
                                autoClickDelayMs = null,
                                enableVideoNavigationGuard = true
                            )
                            
                            // Fullscreen overlay button
                            IconButton(
                                onClick = {
                                    navController.currentBackStackEntry?.savedStateHandle?.set("url", videoUrl)
                                    navController.currentBackStackEntry?.savedStateHandle?.set("title", item.title)
                                    navController.navigate(AppRoutes.FULLSCREEN_PLAYER)
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White
                                )
                            }
                        } else {
                            val imageUrl = item.backdropUrl.ifBlank { item.posterUrl }
                            if (imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (item.type == "tv" && item.season != null && item.episode != null) {
                            PlayerMetaBadge("S${item.season} E${item.episode}", NazaaraBoxPrimary)
                        }
                        if (item.year.isNotBlank()) PlayerMetaBadge(item.year)
                        if (item.runtime.isNotBlank()) PlayerMetaBadge(item.runtime)
                    }
                    Spacer(modifier = Modifier.height(18.dp))

                    if (AdManager.isAdPlacementEnabled("player_banner")) {
                        InlineBannerAd(
                            placement = "player_banner",
                            modifier = Modifier.height(100.dp),
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    if (servers.isNotEmpty()) {
                        Text(
                            text = "Select Server Source",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            servers.forEachIndexed { index, server ->
                                val isActive = index == serverIndex
                                val serverBg = if (isActive) NazaaraBoxPrimary else Color.White.copy(alpha = 0.05f)
                                val borderCol = if (isActive) NazaaraBoxPrimary else Color.White.copy(alpha = 0.12f)
                                val textCol = if (isActive) Color.White else Color.Gray
                                
                                Surface(
                                    onClick = { switchServer(index) },
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = serverBg,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, borderCol)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = textCol,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = server.label,
                                            fontWeight = FontWeight.Bold,
                                            color = textCol,
                                            fontSize = 14.sp
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

    if (showMore) {
        MoreMenuSheet(
            title = item.title,
            onShare = { AppActions.shareItem(context, item) },
            onDismiss = { showMore = false },
        )
    }

    if (showServerSheet) {
        ServerBottomSheet(
            servers = servers,
            selectedIndex = serverIndex,
            onSelect = { switchServer(it) },
            onDismiss = { showServerSheet = false },
        )
    }
}

@Composable
private fun PlayerMetaBadge(label: String, color: Color = AppColors.TextMuted) {
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

