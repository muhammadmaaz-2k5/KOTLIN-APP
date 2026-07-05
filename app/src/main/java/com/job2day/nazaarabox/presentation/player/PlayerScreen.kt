package com.job2day.nazaarabox.presentation.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.VideoServer
import com.job2day.nazaarabox.services.MediaRepository
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AppActions
import com.job2day.nazaarabox.utils.PlayerWebHelper
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
    val activity = context as? Activity
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    if (item == null) {
        LoadingCenter()
        return
    }

    var servers by remember { mutableStateOf<List<VideoServer>>(emptyList()) }
    var serverIndex by remember { mutableIntStateOf(0) }
    var isLoadingServers by remember { mutableStateOf(true) }
    var isPageLoading by remember { mutableStateOf(true) }
    var showMore by remember { mutableStateOf(false) }
    var showServerSheet by remember { mutableStateOf(false) }
    var showRotateNudge by remember { mutableStateOf(false) }
    var forceLandscape by remember { mutableStateOf(false) }
    var showInterstitial by remember { mutableStateOf(false) }
    var interstitialTimerActive by remember { mutableStateOf(false) }
    val repository = remember { MediaRepository() }

    val isFullscreen = isLandscape || forceLandscape

    LaunchedEffect(item) {
        servers = repository.getVideoServers(item, item.season, item.episode)
        isLoadingServers = false
        delay(3000)
        if (!isFullscreen) showRotateNudge = true
        delay(4000)
        showRotateNudge = false
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
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    val currentUrl = remember(servers, serverIndex, item) {
        servers.getOrNull(serverIndex)?.buildUrl(item, item.season, item.episode).orEmpty()
    }
    val currentServer = servers.getOrNull(serverIndex)

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
            "Server ${servers[serverIndex].label} failed. Switching to ${servers[nextIdx].label}...",
            Toast.LENGTH_SHORT,
        ).show()
        switchServer(nextIdx)
    }

    LaunchedEffect(currentUrl) {
        isPageLoading = true
        delay(20_000)
        if (isPageLoading && !PlayerWebHelper.detectVidsrc(currentUrl)) {
            autoSwitchServer()
        }
    }

    LaunchedEffect(isFullscreen) {
        if (isFullscreen && forceLandscape) {
            interstitialTimerActive = true
            while (interstitialTimerActive) {
                kotlinx.coroutines.delay(5 * 60 * 1000L)
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

    if (isFullscreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            PlayerWebView(
                url = currentUrl,
                onPageLoaded = { isPageLoading = false },
                onUrlChanged = { isPageLoading = true },
                modifier = Modifier.fillMaxSize(),
            )
            if (isPageLoading) {
                CircularProgressIndicator(
                    color = AppColors.Primary,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            currentServer?.let { server ->
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showServerSheet = true }
                        .background(Color.Black.copy(alpha = 0.55f))
                        .border(1.dp, AppColors.Primary.copy(alpha = 0.31f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(server.icon, fontSize = 13.sp)
                    Text(
                        text = server.label,
                        modifier = Modifier.padding(start = 5.dp),
                        color = AppColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { exitFullscreen() }
                    .background(Color.Black.copy(alpha = 0.63f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.FullscreenExit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text(
                    text = "Exit fullscreen",
                    modifier = Modifier.padding(start = 6.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                )
            }
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
    } else {
    Scaffold(
        containerColor = Color.Black,
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
                                color = AppColors.Primary,
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black),
                ) {
                    PlayerWebView(
                        url = currentUrl,
                        onPageLoaded = { isPageLoading = false },
                        onUrlChanged = { isPageLoading = true },
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (isPageLoading) {
                        CircularProgressIndicator(
                            color = AppColors.Primary,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { enterFullscreen() }
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White, modifier = Modifier.size(20.dp))
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
                            PlayerMetaBadge("S${item.season} E${item.episode}", AppColors.Secondary)
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌐", fontSize = 14.sp)
                        Text(
                            text = "Select Server",
                            modifier = Modifier.padding(start = 6.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    servers.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            row.forEachIndexed { _, server ->
                                val index = servers.indexOf(server)
                                val selected = index == serverIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            switchServer(index)
                                        }
                                        .background(if (selected) AppColors.Primary.copy(alpha = 0.12f) else AppColors.SurfaceDark)
                                        .border(
                                            1.dp,
                                            if (selected) AppColors.Primary else AppColors.SurfaceVariantDark,
                                            RoundedCornerShape(8.dp),
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(server.icon, fontSize = 12.sp)
                                        Text(
                                            text = server.label,
                                            modifier = Modifier.padding(start = 4.dp),
                                            color = if (selected) AppColors.Primary else Color.White,
                                            fontSize = 11.sp,
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
                }
            }

            if (showRotateNudge) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { enterFullscreen() }
                        .background(AppColors.Primary.copy(alpha = 0.9f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Rotate for fullscreen",
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun PlayerWebView(
    url: String,
    onPageLoaded: () -> Unit,
    onUrlChanged: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (url.isBlank()) return
    val isVidsrc = PlayerWebHelper.detectVidsrc(url)
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
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
                        if (PlayerWebHelper.shouldBlockNavigation(target, url, isVidsrc)) {
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
                        if (PlayerWebHelper.shouldBlockNavigation(target, url, isVidsrc)) {
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
