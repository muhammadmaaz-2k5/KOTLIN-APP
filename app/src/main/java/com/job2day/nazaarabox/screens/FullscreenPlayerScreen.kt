package com.job2day.nazaarabox.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.job2day.nazaarabox.BuildConfig
import com.job2day.nazaarabox.components.VideoPlayer
import com.job2day.nazaarabox.ui.theme.NazaaraBlackBackground
import com.job2day.nazaarabox.ui.theme.NazaaraBoxHeaderBackground
import com.job2day.nazaarabox.ui.theme.NazaaraBoxPrimary
import com.job2day.nazaarabox.model.EmbedServer
import com.job2day.nazaarabox.utils.PlaylistManager
import com.job2day.nazaarabox.viewmodel.EpisodeItem
import com.job2day.nazaarabox.widgets.WebViewPopup
import com.job2day.nazaarabox.utils.AdManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun FullscreenPlayerScreen(
    initialEmbedUrl: String,
    initialTitle: String,
    availableServers: List<EmbedServer> = emptyList(),
    episodes: List<EpisodeItem> = emptyList(),
    dramaSlug: String = "",
    tvId: Int = -1,
    totalSeasons: Int = 0,
    aniId: Int = -1,
    totalEpisodes: Int = 0,
    onDownloadClick: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val window = activity?.window
    
    var showControls by remember { mutableStateOf(true) }
    var isScreenActive by remember { mutableStateOf(true) }
    
    var currentUrl by remember { mutableStateOf(initialEmbedUrl) }
    var currentTitle by remember { mutableStateOf(initialTitle) }
    var showEpisodeList by remember { mutableStateOf(false) }
    
    var selectedSeason by remember { mutableStateOf(1) }
    var isSeasonDropdownExpanded by remember { mutableStateOf(false) }
    var isPlaylistLoading by remember { mutableStateOf(false) }

    var showCenterFeedback by remember { mutableStateOf(false) }
    var hideControlsJob: kotlinx.coroutines.Job? by remember { mutableStateOf(null) }

    var showPopup by remember { mutableStateOf(false) }

    // Download availability â€” checked once when dramaSlug is provided
    var isDownloadAvailable by remember { mutableStateOf(false) }
    LaunchedEffect(dramaSlug) {
        if (dramaSlug.isBlank()) return@LaunchedEffect
        try {
            val repository = com.job2day.nazaarabox.data.Repository(
                com.job2day.nazaarabox.data.api.ApiClient.apiService
            )
            val response = repository.getDownloadingDramaDetail(dramaSlug)
            isDownloadAvailable = response.isSuccessful &&
                response.body()?.success == true &&
                response.body()?.data != null
        } catch (e: Exception) {
            isDownloadAvailable = false
        }
    }

    // Server fallback state
    var serverLinks by remember { mutableStateOf(availableServers) }
    var currentServerIndex by remember { 
        mutableIntStateOf(serverLinks.indexOfFirst { it.link == initialEmbedUrl }.coerceAtLeast(0)) 
    }
    
    // Update currentUrl when server index changes
    LaunchedEffect(currentServerIndex, serverLinks) {
        if (serverLinks.isNotEmpty() && currentServerIndex < serverLinks.size) {
            currentUrl = serverLinks[currentServerIndex].link
        }
    }

    val currentEpisodeIndex = remember(currentUrl, episodes) {
        episodes.indexOfFirst { it.url == currentUrl }
    }
    
    val scope = rememberCoroutineScope()
    val okHttpClient = remember { OkHttpClient() }

    // Fetch episodes for a specific season
    fun loadSeasonEpisodes(season: Int) {
        if (tvId == -1) return
        
        scope.launch {
            try {
                isPlaylistLoading = true
                selectedSeason = season
                PlaylistManager.updateSeason(season) // Sync back to manager
                
                val result = withContext(Dispatchers.IO) {
                    val url = "https://api.themoviedb.org/3/tv/$tvId/season/$season?api_key=${BuildConfig.TMDB_API_KEY}"
                    val req = Request.Builder().url(url).build()
                    val resp = okHttpClient.newCall(req).execute()
                    val body = resp.body?.string() ?: "{}"
                    val obj = JsonParser().parse(body).asJsonObject
                    val epsArray = obj.getAsJsonArray("episodes") ?: JsonArray()
                    
                    val items = mutableListOf<EpisodeItem>()
                    for (el in epsArray) {
                        val e = el.asJsonObject
                        val epNum = e.get("episode_number").asInt
                        val name = if (e.get("name") != null && !e.get("name").isJsonNull) e.get("name").asString else "Episode $epNum"
                        val epUrl = "https://vidnest.fun/tv/$tvId/$season/$epNum"
                        items.add(EpisodeItem(epNum, name, epUrl))
                    }
                    items
                }
                
                PlaylistManager.setPlaylist(result)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isPlaylistLoading = false
            }
        }
    }

    // Generate anime episodes if missing
    fun loadAnimeEpisodes() {
        if (aniId == -1) return
        scope.launch {
            val count = if (totalEpisodes > 0) totalEpisodes else 100
            val items = (1..count).map { i ->
                EpisodeItem(i, "Episode $i", "https://vidnest.fun/anime/$aniId/$i")
            }
            PlaylistManager.setPlaylist(items)
        }
    }

    // Auto-load episodes from "backend" if list is empty
    LaunchedEffect(Unit) {
        if (episodes.isEmpty()) {
            if (tvId != -1) {
                loadSeasonEpisodes(1)
            } else if (aniId != -1) {
                loadAnimeEpisodes()
            }
        }
    }

    fun playEpisode(episode: EpisodeItem) {
        // Update server links for the new episode
        serverLinks = episode.urls ?: emptyList()
        currentUrl = episode.url
        currentServerIndex = serverLinks.indexOfFirst { it.link == episode.url }.coerceAtLeast(0)
        
        // Try to keep the same series title prefix if possible
        val prefix = initialTitle.substringBeforeLast(" - ")
        currentTitle = if (prefix != initialTitle) "$prefix - ${episode.title ?: "Episode ${episode.number}"}" else episode.title ?: "Episode ${episode.number}"
        showEpisodeList = false
        showControls = true
    }
    
    // Handle system back press
    BackHandler {
        if (showEpisodeList) {
            showEpisodeList = false
        } else {
            onBack()
        }
    }

    // Set immersive mode and landscape orientation
    LaunchedEffect(Unit) {
        activity?.let { act ->
            val win = act.window
            // Make content appear behind system bars
            WindowCompat.setDecorFitsSystemWindows(win, false)
            
            // Set landscape orientation
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            
            // Keep screen on
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            
            // Initial hide of system bars
            val controller = WindowInsetsControllerCompat(win, win.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        // Auto-hide controls after 3 seconds
        delay(3000)
        showControls = false
    }

    // 10-Minute Timer for Webview Popup
    LaunchedEffect(Unit) {
        delay(600000L) // 10 minutes in milliseconds
        if (isScreenActive && AdManager.popupWebviewUrl.isNotBlank()) {
            showPopup = true
        }
    }

    // Sync system bars with controls visibility using modern API
    LaunchedEffect(showControls) {
        window?.let { win ->
            val controller = WindowInsetsControllerCompat(win, win.decorView)
            if (showControls) {
                controller.show(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
    
    // Reset when leaving
    DisposableEffect(Unit) {
        onDispose {
            isScreenActive = false
            
            activity?.let { act ->
                val win = act.window
                // Reset decor fits system windows
                WindowCompat.setDecorFitsSystemWindows(win, true)
                
                // Reset system UI
                val controller = WindowInsetsControllerCompat(win, win.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
                
                // Reset orientation
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                
                // Remove keep screen on flag
                win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
    
    // Toggle controls on tap
    fun toggleControls() {
        if (showEpisodeList) return // Don't toggle controls if episode list is open
        
        showControls = true
        showCenterFeedback = true
        
        // Reset and start auto-hide timer
        hideControlsJob?.cancel()
        hideControlsJob = scope.launch {
            // Show feedback icon for 1.5 seconds
            launch {
                delay(1500)
                showCenterFeedback = false
            }
            // Hide all controls after 5 seconds
            delay(5000)
            if (!showEpisodeList && isScreenActive) {
                showControls = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NazaaraBlackBackground)
            .pointerInput(Unit) {
                detectTapGestures {
                    toggleControls()
                }
            }
    ) {
        // Video Player (Full Screen)
        key(currentUrl) {
            VideoPlayer(
                embedUrl = currentUrl,
                modifier = Modifier.fillMaxSize(),
                aspectRatio = 16f / 9f,
                showFullscreenButton = false, // Hide fullscreen button in fullscreen mode
                isFullscreen = true,
                onServerError = {
                    // Automatically try next server if available
                    if (serverLinks.isNotEmpty() && currentServerIndex < serverLinks.size - 1) {
                        currentServerIndex++
                        android.widget.Toast.makeText(context, "Server not working, trying mirror...", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        
        // Semi-transparent overlay when list is open
        AnimatedVisibility(
            visible = showEpisodeList,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { showEpisodeList = false }
            )
        }

        // Top Controls Bar
        AnimatedVisibility(
            visible = showControls && !showEpisodeList,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Text(
                        text = currentTitle,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (episodes.isNotEmpty()) {
                        IconButton(onClick = { 
                            showEpisodeList = true
                            showControls = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Episodes",
                                tint = Color.White
                            )
                        }
                    }

                    // Download icon â€” shown only when download page is available
                    if (isDownloadAvailable && onDownloadClick != null) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.Black.copy(alpha = 0.55f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                                )
                                .clickable { onDownloadClick() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = NazaaraBoxPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Download",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Center Controls
        AnimatedVisibility(
            visible = showControls && !showEpisodeList,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(48.dp)
                ) {
                    if (episodes.isNotEmpty() && currentEpisodeIndex > 0) {
                        IconButton(
                            onClick = { playEpisode(episodes[currentEpisodeIndex - 1]) },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(64.dp))
                    }

                    // Temporary Feedback Pulse for Center Icon
                    androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (showCenterFeedback) 1.2f else 1.0f,
                        animationSpec = tween(300),
                        label = "pulse"
                    ).value.let { scale ->
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = if (showCenterFeedback) 0.7f else 0.5f),
                            modifier = Modifier
                                .size((96 * scale).dp)
                                .clickable {
                                    // Handle play/pause logic here if needed
                                    toggleControls()
                                }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size((48 * scale).dp)
                                )
                            }
                        }
                    }

                    if (episodes.isNotEmpty() && currentEpisodeIndex >= 0 && currentEpisodeIndex < episodes.size - 1) {
                        IconButton(
                            onClick = { playEpisode(episodes[currentEpisodeIndex + 1]) },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(64.dp))
                    }
                }
            }
        }
        
        // Episode List Sidebar Overlay
        AnimatedVisibility(
            visible = showEpisodeList,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(durationMillis = 300)
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp),
                color = Color.Black.copy(alpha = 0.95f),
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Sidebar Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Episodes",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        IconButton(onClick = { showEpisodeList = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    
                    // Season Selection for TV shows
                    if (tvId != -1 && totalSeasons > 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isSeasonDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White,
                                    containerColor = Color.White.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Season $selectedSeason", color = Color.White)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                                }
                            }

                            DropdownMenu(
                                expanded = isSeasonDropdownExpanded,
                                onDismissRequest = { isSeasonDropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .background(NazaaraBoxHeaderBackground)
                            ) {
                                repeat(totalSeasons) { index ->
                                    val seasonNum = index + 1
                                    DropdownMenuItem(
                                        text = { Text("Season $seasonNum", color = Color.White) },
                                        onClick = {
                                            isSeasonDropdownExpanded = false
                                            loadSeasonEpisodes(seasonNum)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (isPlaylistLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NazaaraBoxPrimary)
                        }
                    } else if (episodes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No episodes found", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            itemsIndexed(episodes) { index, ep ->
                                val isSelected = ep.url == currentUrl
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { playEpisode(ep) }
                                        .background(if (isSelected) NazaaraBoxPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = if (isSelected) NazaaraBoxPrimary else Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.width(32.dp),
                                        textAlign = TextAlign.Center,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    
                                    Text(
                                        text = ep.title ?: "Episode ${ep.number}",
                                        color = if (isSelected) NazaaraBoxPrimary else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Instructions
        AnimatedVisibility(
            visible = showControls && !showEpisodeList,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tap anywhere to show/hide controls",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (showPopup && AdManager.popupWebviewUrl.isNotBlank()) {
            WebViewPopup(
                url = AdManager.popupWebviewUrl,
                onDismiss = { showPopup = false }
            )
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

