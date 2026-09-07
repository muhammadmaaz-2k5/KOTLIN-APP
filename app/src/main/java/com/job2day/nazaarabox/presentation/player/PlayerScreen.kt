package com.job2day.nazaarabox.presentation.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.job2day.nazaarabox.ads.CustomInterstitialAd
import com.job2day.nazaarabox.ads.InlineBannerAd
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.SeasonItem
import com.job2day.nazaarabox.core.TrailerItem
import com.job2day.nazaarabox.core.VideoServer
import com.job2day.nazaarabox.routes.AppRoutes
import com.job2day.nazaarabox.services.MediaRepository
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.utils.AppActions
import com.job2day.nazaarabox.utils.PlayerWebHelper
import com.job2day.nazaarabox.widgets.EpisodePickerSheet
import com.job2day.nazaarabox.widgets.LoadingCenter
import com.job2day.nazaarabox.widgets.MoreMenuSheet
import com.job2day.nazaarabox.widgets.ServerBottomSheet
import com.job2day.nazaarabox.widgets.YouTubePlayerWebView
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerScreen(navController: NavController) {
    val initialItem = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("mediaItem")
        ?.let { AppRoutes.decodeItem(it) }

    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    if (initialItem == null) {
        LoadingCenter()
        return
    }

    var currentItem by remember { mutableStateOf(initialItem) }
    var servers by remember { mutableStateOf<List<VideoServer>>(emptyList()) }
    var seasons by remember { mutableStateOf<List<SeasonItem>>(emptyList()) }
    var trailers by remember { mutableStateOf<List<TrailerItem>>(emptyList()) }
    var isReviewTrailerMode by remember { mutableStateOf(false) }
    var serverIndex by remember { mutableIntStateOf(0) }
    var isLoadingServers by remember { mutableStateOf(true) }
    var isPageLoading by remember { mutableStateOf(true) }
    var showMore by remember { mutableStateOf(false) }
    var showServerSheet by remember { mutableStateOf(false) }
    var showEpisodePicker by remember { mutableStateOf(false) }
    var showRotateNudge by remember { mutableStateOf(false) }
    var forceLandscape by remember { mutableStateOf(false) }
    var showInterstitial by remember { mutableStateOf(false) }
    var interstitialTimerActive by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    // Fullscreen Controls State
    var controlsVisible by remember { mutableStateOf(true) }
    var isControlsLocked by remember { mutableStateOf(false) }

    // Stream & Episode Transition Loading State
    var showTransitionOverlay by remember { mutableStateOf(true) }
    var statusMessageIndex by remember { mutableIntStateOf(0) }

    val statusMessages = remember(currentItem.season, currentItem.episode) {
        val s = currentItem.season ?: 1
        val e = currentItem.episode ?: 1
        listOf(
            "⚡ Connecting to high-speed stream...",
            "🎬 Preparing Season $s • Episode $e...",
            "🍿 Optimizing video buffer & HD quality...",
            "🔊 Syncing multi-channel audio & subtitles...",
            "🚀 Almost ready, enjoy the episode!",
        )
    }


    LaunchedEffect(showTransitionOverlay) {
        if (showTransitionOverlay) {
            statusMessageIndex = 0
            while (showTransitionOverlay) {
                delay(1300L)
                statusMessageIndex = (statusMessageIndex + 1) % statusMessages.size
            }
        }
    }

    val isOverlayVisible = (showTransitionOverlay || isPageLoading) && !isReviewTrailerMode

    val repository = remember { MediaRepository() }
    val isFullscreen = isLandscape || forceLandscape
    val isTv = currentItem.type.equals("tv", ignoreCase = true)

    // Load servers & seasons
    LaunchedEffect(currentItem.id, currentItem.season, currentItem.episode) {
        val loadedServers = repository.getVideoServers(currentItem, currentItem.season, currentItem.episode)
        servers = loadedServers
        if (loadedServers.isEmpty()) {
            isReviewTrailerMode = true
            trailers = repository.getTrailers(currentItem)
            showTransitionOverlay = false
            isPageLoading = false
        } else {
            isReviewTrailerMode = false
        }
        if (isTv && seasons.isEmpty()) {
            seasons = repository.getSeasons(currentItem)
        }
        isLoadingServers = false
        delay(3000)
        if (!isFullscreen) showRotateNudge = true
        delay(4000)
        showRotateNudge = false
    }

    val activeTrailerKey = remember(trailers) { trailers.firstOrNull()?.key }

    // Auto-hide controls in fullscreen after 4.5 seconds
    LaunchedEffect(controlsVisible, isControlsLocked, isFullscreen) {
        if (isFullscreen && controlsVisible && !isControlsLocked) {
            delay(4500L)
            controlsVisible = false
        }
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val controller = WindowCompat.getInsetsController(activity?.window ?: return@onDispose, view)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    DisposableEffect(isFullscreen) {
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            if (isFullscreen) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                interstitialTimerActive = false
                showInterstitial = false
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose { }
    }

    fun enterFullscreen() {
        forceLandscape = true
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    fun exitFullscreen() {
        forceLandscape = false
        isControlsLocked = false
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    val currentUrl = remember(servers, serverIndex, currentItem, refreshKey) {
        servers.getOrNull(serverIndex)?.buildUrl(currentItem, currentItem.season, currentItem.episode).orEmpty()
    }
    val currentServer = servers.getOrNull(serverIndex)

    LaunchedEffect(currentUrl, refreshKey) {
        showTransitionOverlay = true
        isPageLoading = true
        delay(4200L)
        showTransitionOverlay = false
    }

    fun switchServer(index: Int) {
        if (index == serverIndex || index !in servers.indices) return
        serverIndex = index
        isPageLoading = true
    }

    fun autoSwitchServer() {
        if (servers.size <= 1) return
        val nextIdx = (serverIndex + 1) % servers.size
        Toast.makeText(
            context,
            "Server ${servers[serverIndex].label} stalled. Switching to ${servers[nextIdx].label}...",
            Toast.LENGTH_SHORT,
        ).show()
        switchServer(nextIdx)
    }

    LaunchedEffect(currentUrl, isReviewTrailerMode) {
        if (!isReviewTrailerMode && currentUrl.isNotBlank()) {
            isPageLoading = true
            delay(20_000)
            if (isPageLoading && !PlayerWebHelper.detectEmbedPlayer(currentUrl)) {
                autoSwitchServer()
            }
        }
    }

    LaunchedEffect(isFullscreen) {
        if (isFullscreen && forceLandscape) {
            interstitialTimerActive = true
            while (interstitialTimerActive) {
                delay(5 * 60 * 1000L)
                if (forceLandscape && AdManager.canShowInterstitial()) {
                    showInterstitial = true
                }
            }
        }
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
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = AppColors.Primary)
        }
        return
    }

    // ==========================================
    // FULL SCREEN HUD & IMMERSIVE PLAYER
    // ==========================================
    if (isFullscreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    if (isControlsLocked) {
                        controlsVisible = !controlsVisible
                    } else {
                        controlsVisible = !controlsVisible
                    }
                },
        ) {
            // Player Web View or Official Trailer Player
            if (isReviewTrailerMode && activeTrailerKey != null) {
                key(activeTrailerKey) {
                    YouTubePlayerWebView(
                        videoKey = activeTrailerKey,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            } else if (currentUrl.isNotBlank()) {
                key(currentUrl, refreshKey) {
                    PlayerWebView(
                        url = currentUrl,
                        onPageLoaded = { isPageLoading = false },
                        onUrlChanged = { isPageLoading = true },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Trailer & Stream preview unavailable", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Cinematic Stream Loading / Episode Transition Overlay
            AnimatedVisibility(
                visible = isOverlayVisible,
                enter = fadeIn(animationSpec = tween(250)),
                exit = fadeOut(animationSpec = tween(500)),
                modifier = Modifier.fillMaxSize(),
            ) {
                PlayerLoadingOverlay(
                    item = currentItem,
                    isTv = isTv,
                    statusText = statusMessages[statusMessageIndex],
                    onDismiss = {
                        showTransitionOverlay = false
                        isPageLoading = false
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // --- LOCKED STATE OVERLAY ---
            if (isControlsLocked) {
                AnimatedVisibility(
                    visible = controlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Surface(
                        onClick = {
                            isControlsLocked = false
                            controlsVisible = true
                            Toast.makeText(context, "Controls unlocked", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Black.copy(alpha = 0.80f),
                        border = BorderStroke(1.dp, AppColors.Primary.copy(alpha = 0.6f)),
                        shadowElevation = 12.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.LockOpen,
                                contentDescription = "Unlock",
                                tint = AppColors.Primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Screen Locked • Tap to Unlock",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            // --- FULLSCREEN CONTROLS (FADES IN/OUT) ---
            if (!isControlsLocked) {
                AnimatedVisibility(
                    visible = controlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Top Scrim & Top Bar
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent),
                                    ),
                                )
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Back Button
                                Surface(
                                    onClick = { exitFullscreen() },
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Exit Fullscreen",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // Title & Episode Details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentItem.title,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    val subtitle = if (isTv && currentItem.season != null && currentItem.episode != null) {
                                        "Season ${currentItem.season} • Episode ${currentItem.episode}"
                                    } else {
                                        "${currentItem.year.ifBlank { "Movie" }} • 4K Ultra HD"
                                    }
                                    Text(
                                        text = subtitle,
                                        color = AppColors.TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Server Selector Pill / Trailer Badge
                                if (isReviewTrailerMode) {
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = Color.Black.copy(alpha = 0.60f),
                                        border = BorderStroke(1.dp, AppColors.Primary.copy(alpha = 0.40f)),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text("🎬", fontSize = 12.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Official Trailer",
                                                color = AppColors.Primary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                } else {
                                    currentServer?.let { server ->
                                        Surface(
                                            onClick = { showServerSheet = true },
                                            shape = RoundedCornerShape(18.dp),
                                            color = Color.Black.copy(alpha = 0.60f),
                                            border = BorderStroke(1.dp, AppColors.Primary.copy(alpha = 0.40f)),
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Text(server.icon, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = server.label,
                                                    color = AppColors.Primary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                                Icon(
                                                    Icons.Default.ArrowDropDown,
                                                    contentDescription = null,
                                                    tint = AppColors.Primary,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Reload Stream Button
                                Surface(
                                    onClick = {
                                        refreshKey++
                                        isPageLoading = true
                                        Toast.makeText(context, "Reloading stream...", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                    modifier = Modifier.size(38.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Reload",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Lock Screen Button
                                Surface(
                                    onClick = {
                                        isControlsLocked = true
                                        controlsVisible = true
                                        Toast.makeText(context, "Screen controls locked", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                    modifier = Modifier.size(38.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Lock Controls",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                        }

                        // Center Quick Episode Jump (for TV Series)
                        if (isTv && currentItem.episode != null) {
                            val ep = currentItem.episode!!
                            val season = currentItem.season ?: 1
                            Row(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth()
                                    .padding(horizontal = 30.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Previous Episode
                                if (ep > 1) {
                                    Surface(
                                        onClick = {
                                            val newEp = ep - 1
                                            val baseTitle = currentItem.title.substringBefore(" ·").substringBefore(" •")
                                            currentItem = currentItem.copy(
                                                episode = newEp,
                                                title = "$baseTitle · S${season}E$newEp",
                                            )
                                            refreshKey++
                                            isPageLoading = true
                                            Toast.makeText(context, "Playing Season $season Episode $newEp", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.Black.copy(alpha = 0.65f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "Ep ${ep - 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(10.dp))
                                }

                                // Next Episode
                                Surface(
                                    onClick = {
                                        val newEp = ep + 1
                                        val baseTitle = currentItem.title.substringBefore(" ·").substringBefore(" •")
                                        currentItem = currentItem.copy(
                                            episode = newEp,
                                            title = "$baseTitle · S${season}E$newEp",
                                        )
                                        refreshKey++
                                        isPageLoading = true
                                        Toast.makeText(context, "Playing Season $season Episode $newEp", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.Black.copy(alpha = 0.65f),
                                    border = BorderStroke(1.dp, AppColors.Primary.copy(alpha = 0.4f)),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(text = "Ep ${ep + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.SkipNext, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        // Bottom Scrim & Bottom Bar
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(85.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                    ),
                                )
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Exit Fullscreen Pill
                                Surface(
                                    onClick = { exitFullscreen() },
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.Black.copy(alpha = 0.65f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            Icons.Default.FullscreenExit,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Exit Fullscreen",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }

                                // Quick Episodes Selector Button (for TV series)
                                if (isTv) {
                                    Surface(
                                        onClick = { showEpisodePicker = true },
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.Black.copy(alpha = 0.65f),
                                        border = BorderStroke(1.dp, AppColors.Secondary.copy(alpha = 0.40f)),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                Icons.Default.Tv,
                                                contentDescription = null,
                                                tint = AppColors.Secondary,
                                                modifier = Modifier.size(16.dp),
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Episodes",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                }

                                // Share / Info Button
                                Surface(
                                    onClick = { AppActions.shareItem(context, currentItem) },
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.65f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                                    modifier = Modifier.size(38.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- PERMANENT NEXT EPISODE FLOATING BUTTON (FULLSCREEN TV SERIES) ---
            if (isTv && !isControlsLocked && !isOverlayVisible) {
                val currentEp = currentItem.episode ?: 1
                val currentSeasonNum = currentItem.season ?: 1
                val activeSeason = seasons.firstOrNull { it.seasonNumber == currentSeasonNum }
                val totalEpsInSeason = activeSeason?.episodeCount ?: 999
                val nextSeason = seasons.firstOrNull { it.seasonNumber == currentSeasonNum + 1 }

                val nextTarget: Pair<Int, Int>? = when {
                    currentEp < totalEpsInSeason -> Pair(currentSeasonNum, currentEp + 1)
                    nextSeason != null -> Pair(currentSeasonNum + 1, 1)
                    seasons.isEmpty() -> Pair(currentSeasonNum, currentEp + 1)
                    else -> null
                }

                if (nextTarget != null) {
                    val (nextSeasonNum, nextEpNum) = nextTarget
                    val bottomPadding by animateDpAsState(
                        targetValue = if (controlsVisible) 80.dp else 24.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "permanentNextEpBottom",
                    )
                    val pillAlpha by animateFloatAsState(
                        targetValue = if (controlsVisible) 0.88f else 0.60f,
                        label = "permanentNextEpAlpha",
                    )

                    Surface(
                        onClick = {
                            val baseTitle = currentItem.title.substringBefore(" ·").substringBefore(" •")
                            currentItem = currentItem.copy(
                                season = nextSeasonNum,
                                episode = nextEpNum,
                                title = "$baseTitle · S${nextSeasonNum}E$nextEpNum",
                            )
                            refreshKey++
                            isPageLoading = true
                            Toast.makeText(
                                context,
                                "Playing Season $nextSeasonNum Episode $nextEpNum",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                        shape = RoundedCornerShape(22.dp),
                        color = Color.Black.copy(alpha = pillAlpha),
                        border = BorderStroke(
                            1.dp,
                            if (controlsVisible) AppColors.Primary.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.30f),
                        ),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 24.dp, bottom = bottomPadding),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (nextSeasonNum != currentSeasonNum) "Next: S${nextSeasonNum}E${nextEpNum}" else "Next Ep $nextEpNum",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Next Episode",
                                tint = AppColors.Primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    } else {
        // ==========================================
        // PORTRAIT MODE MODERN PLAYER UI
        // ==========================================
        Scaffold(
            containerColor = Color.Black,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentItem.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp,
                            )
                            if (isReviewTrailerMode) {
                                Text(
                                    text = "🎬 Official Trailer • HD",
                                    color = AppColors.Primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            } else {
                                currentServer?.let {
                                    Text(
                                        text = "${it.icon} ${it.label} • Active",
                                        color = AppColors.Primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            refreshKey++
                            isPageLoading = true
                            Toast.makeText(context, "Reloading stream...", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color.White)
                        }
                        IconButton(onClick = { AppActions.shareItem(context, currentItem) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }
                        IconButton(onClick = { showMore = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                )
            },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    // 16:9 Video Frame with Rounded Corners & Fullscreen Trigger
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color.Black),
                    ) {
                        if (isReviewTrailerMode && activeTrailerKey != null) {
                            key(activeTrailerKey) {
                                YouTubePlayerWebView(
                                    videoKey = activeTrailerKey,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        } else if (currentUrl.isNotBlank()) {
                            key(currentUrl, refreshKey) {
                                PlayerWebView(
                                    url = currentUrl,
                                    onPageLoaded = { isPageLoading = false },
                                    onUrlChanged = { isPageLoading = true },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Movie, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Preview unavailable", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Cinematic Stream Loading / Episode Transition Overlay (Portrait)
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isOverlayVisible,
                            enter = fadeIn(animationSpec = tween(250)),
                            exit = fadeOut(animationSpec = tween(500)),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            PlayerLoadingOverlay(
                                item = currentItem,
                                isTv = isTv,
                                statusText = statusMessages[statusMessageIndex],
                                onDismiss = {
                                    showTransitionOverlay = false
                                    isPageLoading = false
                                },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        // Floating Fullscreen Trigger Button
                        Surface(
                            onClick = { enterFullscreen() },
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.70f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Fullscreen",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    // Metadata & Title Section
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Text(
                            text = currentItem.title,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isTv && currentItem.season != null && currentItem.episode != null) {
                                PlayerMetaBadge("S${currentItem.season} E${currentItem.episode}", AppColors.Secondary)
                            }
                            if (currentItem.year.isNotBlank()) PlayerMetaBadge(currentItem.year)
                            PlayerMetaBadge("4K ULTRA HD", AppColors.Accent)
                            if (currentItem.runtime.isNotBlank()) PlayerMetaBadge(currentItem.runtime)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Quick Episode Switcher Row (for TV series)
                        if (isTv && seasons.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Tv, contentDescription = null, tint = AppColors.Secondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Episodes",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                    )
                                }
                                Surface(
                                    onClick = { showEpisodePicker = true },
                                    shape = RoundedCornerShape(16.dp),
                                    color = AppColors.Secondary.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, AppColors.Secondary.copy(alpha = 0.35f)),
                                ) {
                                    Text(
                                        text = "All Episodes ▾",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = AppColors.Secondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Horizontal Fast-Switch Pills (Ep 1, Ep 2, Ep 3...)
                            val currentSeasonNumber = currentItem.season ?: 1
                            val activeSeason = seasons.firstOrNull { it.seasonNumber == currentSeasonNumber } ?: seasons.first()
                            val episodeCount = activeSeason.episodeCount.coerceAtLeast(1)

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(episodeCount) { epIndex ->
                                    val epNum = epIndex + 1
                                    val isCurrentEp = currentItem.episode == epNum
                                    Surface(
                                        onClick = {
                                            val baseTitle = currentItem.title.substringBefore(" ·").substringBefore(" •")
                                            currentItem = currentItem.copy(
                                                season = currentSeasonNumber,
                                                episode = epNum,
                                                title = "$baseTitle · S${currentSeasonNumber}E$epNum",
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isCurrentEp) AppColors.Primary else AppColors.CardDark,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isCurrentEp) AppColors.Primary else Color.White.copy(alpha = 0.12f),
                                        ),
                                    ) {
                                        Text(
                                            text = "Ep $epNum",
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            color = if (isCurrentEp) Color.White else Color(0xFFC0C0D0),
                                            fontSize = 12.sp,
                                            fontWeight = if (isCurrentEp) FontWeight.Bold else FontWeight.Medium,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Banner Ad
                        if (AdManager.isAdPlacementEnabled("player_banner")) {
                            InlineBannerAd(
                                placement = "player_banner",
                                modifier = Modifier.height(100.dp),
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                        }

                        if (servers.isNotEmpty()) {
                            // Server Selection Header
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡", fontSize = 14.sp)
                                Text(
                                    text = "Select Server",
                                    modifier = Modifier.padding(start = 6.dp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Modern Server Selection Grid
                            servers.chunked(3).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    row.forEach { server ->
                                        val index = servers.indexOf(server)
                                        val selected = index == serverIndex
                                        Surface(
                                            onClick = { switchServer(index) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (selected) AppColors.Primary.copy(alpha = 0.15f) else AppColors.CardDark,
                                            border = BorderStroke(
                                                1.dp,
                                                if (selected) AppColors.Primary else Color.White.copy(alpha = 0.10f),
                                            ),
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 6.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Text(server.icon, fontSize = 13.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = server.label,
                                                    color = if (selected) AppColors.Primary else Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                        }
                                    }
                                    repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else if (isReviewTrailerMode && trailers.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎬", fontSize = 14.sp)
                                Text(
                                    text = "Official Trailers & Clips",
                                    modifier = Modifier.padding(start = 6.dp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            trailers.forEach { trailer ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = AppColors.CardDark,
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = AppColors.Primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = trailer.name,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = trailer.type,
                                                color = AppColors.TextMuted,
                                                fontSize = 11.sp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Rotate Nudge Tooltip Pill
                if (showRotateNudge) {
                    Surface(
                        onClick = { enterFullscreen() },
                        shape = RoundedCornerShape(24.dp),
                        color = AppColors.Primary,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rotate for Fullscreen Experience",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Sheets
    if (showMore) {
        MoreMenuSheet(
            title = currentItem.title,
            onShare = { AppActions.shareItem(context, currentItem) },
            onDismiss = { showMore = false },
        )
    }

    if (showServerSheet) {
        ServerBottomSheet(
            servers = servers,
            selectedIndex = serverIndex,
            onSelect = {
                switchServer(it)
                showServerSheet = false
            },
            onDismiss = { showServerSheet = false },
        )
    }

    if (showEpisodePicker) {
        EpisodePickerSheet(
            item = currentItem,
            seasons = seasons,
            onPlay = { season, episode ->
                showEpisodePicker = false
                val baseTitle = currentItem.title.substringBefore(" ·").substringBefore(" •")
                currentItem = currentItem.copy(
                    season = season,
                    episode = episode,
                    title = "$baseTitle · S${season}E$episode",
                )
            },
            onDismiss = { showEpisodePicker = false },
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun PlayerWebView(
    url: String,
    onPageLoaded: () -> Unit,
    onUrlChanged: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (url.isBlank()) return
    val isEmbed = PlayerWebHelper.detectEmbedPlayer(url)
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                setBackgroundColor(android.graphics.Color.BLACK)
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    javaScriptCanOpenWindowsAutomatically = false
                    setSupportMultipleWindows(false)
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    userAgentString = userAgentString.replace("; wv", "")
                }
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val target = request?.url?.toString().orEmpty()
                        if (target.isBlank()) return false
                        if (PlayerWebHelper.shouldBlockNavigation(target, url, isEmbed)) {
                            return true
                        }
                        if (request?.isForMainFrame == true && target != url && !PlayerWebHelper.isAllowedVideoHosting(target)) {
                            return true
                        }
                        return false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView?, targetUrl: String?): Boolean {
                        val target = targetUrl.orEmpty()
                        if (target.isBlank()) return false
                        if (PlayerWebHelper.shouldBlockNavigation(target, url, isEmbed)) {
                            return true
                        }
                        return false
                    }

                    override fun onPageFinished(view: WebView?, finishedUrl: String?) {
                        onPageLoaded()
                    }
                }
                loadPlayerContent(this, url)
            }
        },
        update = { webView ->
            if (webView.tag != url) {
                webView.tag = url
                onUrlChanged()
                loadPlayerContent(webView, url)
            }
        },
        modifier = modifier,
    )
}

private fun loadPlayerContent(webView: WebView, url: String) {
    val headers = mapOf("Referer" to "https://nazaarabox.com")
    if (PlayerWebHelper.shouldUseHtmlWrapper(url)) {
        webView.loadDataWithBaseURL(
            "https://nazaarabox.com",
            PlayerWebHelper.buildHtmlContent(url),
            "text/html",
            "UTF-8",
            null,
        )
    } else {
        webView.loadUrl(url, headers)
    }
}

@Composable
private fun PlayerLoadingOverlay(
    item: MediaItem,
    isTv: Boolean,
    statusText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )

    Box(
        modifier = modifier
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Backdrop image (if available)
        val imageSource = item.backdropUrl.ifBlank { item.posterUrl }
        if (imageSource.isNotBlank()) {
            AsyncImage(
                model = imageSource,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Dark cinematic vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.72f),
                            Color.Black.copy(alpha = 0.88f),
                            Color.Black.copy(alpha = 0.96f),
                        ),
                    ),
                ),
        )

        // Center Content Card
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
        ) {
            // Pill Badge
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppColors.Primary.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, AppColors.Primary.copy(alpha = 0.55f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTv && item.episode != null) "NOW PLAYING • EPISODE ${item.episode}" else "PREPARING STREAM",
                        color = AppColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Show / Episode Title
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (isTv && item.season != null && item.episode != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Season ${item.season} • Episode ${item.episode}",
                    color = AppColors.TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Pulsing Glowing Loader
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp),
            ) {
                // Outer subtle glowing aura
                Box(
                    modifier = Modifier
                        .size(52.dp * pulseScale)
                        .clip(CircleShape)
                        .background(AppColors.Primary.copy(alpha = 0.20f)),
                )
                CircularProgressIndicator(
                    color = AppColors.Primary,
                    strokeWidth = 3.5.dp,
                    modifier = Modifier.size(38.dp),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Rotating Dynamic Status / Tip Ticker
            AnimatedContent(
                targetState = statusText,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + slideInVertically { it / 2 })
                        .togetherWith(fadeOut(animationSpec = tween(180)))
                },
                label = "statusTextAnim",
            ) { text ->
                Text(
                    text = text,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tap to dismiss",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 11.sp,
            )
        }
    }
}

