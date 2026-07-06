package com.job2day.nazaarabox.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.job2day.nazaarabox.presentation.actor.ActorScreen
import com.job2day.nazaarabox.presentation.browse.BrowseMode
import com.job2day.nazaarabox.presentation.browse.MediaBrowseScreen
import com.job2day.nazaarabox.presentation.category.CategorySectionScreen
import com.job2day.nazaarabox.presentation.detail.DetailScreen
import com.job2day.nazaarabox.presentation.home.HomeScreen
import com.job2day.nazaarabox.presentation.language.LanguageBrowseScreen
import com.job2day.nazaarabox.presentation.player.PlayerScreen
import com.job2day.nazaarabox.presentation.player.FullscreenPlayerScreen
import com.job2day.nazaarabox.presentation.search.SearchScreen
import com.job2day.nazaarabox.presentation.season.SeasonScreen
import com.job2day.nazaarabox.presentation.seeall.SeeAllScreen
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.routes.AppRoutes
import com.job2day.nazaarabox.screens.PrivacyPolicyScreen
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.widgets.AppBottomBar
import kotlinx.coroutines.flow.filterNotNull

@Composable
fun NazaaraboxNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val mainRoutes = setOf(AppRoutes.HOME, AppRoutes.MOVIES, AppRoutes.TV_SHOWS, AppRoutes.ANIME)

    val showBottomBar = currentRoute in mainRoutes

    LaunchedEffect(Unit) {
        snapshotFlow { NotificationRouter.pendingRoute.value }
            .filterNotNull()
            .collect { route: String ->
                kotlinx.coroutines.delay(100)
                val data = NotificationRouter.pendingData.value
                val dramaSlug = data?.get("drama_slug")
                val tmdbId = data?.get("tmdb_id")
                val itemType = data?.get("item_type")
                val idStr = if (!tmdbId.isNullOrBlank()) tmdbId else dramaSlug
                val id = idStr?.toIntOrNull()

                if (route == AppRoutes.DETAIL && id != null) {
                    val mediaItem = MediaItem(
                        id = id,
                        type = itemType ?: "movie",
                        isCustom = id >= 1000000000,
                        title = data?.get("title") ?: "Details"
                    )
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "mediaItem",
                        AppRoutes.encodeItem(mediaItem)
                    )
                }

                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                NotificationRouter.pendingRoute.value = null
                NotificationRouter.pendingData.value = null
            }
    }

    Box {
        Scaffold(
            containerColor = AppColors.BackgroundDark,
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                if (showBottomBar) {
                    AppBottomBar(
                        currentRoute = currentRoute,
                        onTabSelected = { route, isOverlay ->
                            if (isOverlay) {
                                if (AdManager.isLiveMode) navController.navigate(AppRoutes.SEARCH)
                            } else {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    )
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = AppRoutes.HOME,
                modifier = Modifier.padding(padding),
            ) {
                composable(AppRoutes.HOME) {
                    HomeScreen(navController = navController)
                }
                composable(AppRoutes.MOVIES) {
                    MediaBrowseScreen(
                        mode = BrowseMode.MOVIES,
                        title = "Movies",
                        navController = navController,
                    )
                }
                composable(AppRoutes.TV_SHOWS) {
                    MediaBrowseScreen(
                        mode = BrowseMode.TV,
                        title = "TV Shows",
                        navController = navController,
                    )
                }
                composable(AppRoutes.ANIME) {
                    MediaBrowseScreen(
                        mode = BrowseMode.ANIME,
                        title = "Anime",
                        navController = navController,
                    )
                }
                composable(AppRoutes.SEARCH) {
                    SearchScreen(navController = navController)
                }
                composable(AppRoutes.DETAIL) {
                    DetailScreen(navController = navController)
                }
                composable(AppRoutes.PLAYER) {
                    PlayerScreen(navController = navController)
                }
                composable(AppRoutes.FULLSCREEN_PLAYER) {
                    val url = navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>("url")
                        .orEmpty()
                    val title = navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>("title")
                        .orEmpty()
                    FullscreenPlayerScreen(url = url, title = title, navController = navController)
                }
                composable(
                    route = AppRoutes.ACTOR,
                    arguments = listOf(navArgument("personId") { type = NavType.IntType }),
                ) { backStackEntry ->
                    val personId = backStackEntry.arguments?.getInt("personId") ?: 0
                    ActorScreen(personId = personId, navController = navController)
                }
                composable(AppRoutes.SEASON) {
                    SeasonScreen(navController = navController)
                }
                composable(AppRoutes.SEE_ALL) {
                    SeeAllScreen(navController = navController)
                }
                composable(AppRoutes.CATEGORY) {
                    CategorySectionScreen(navController = navController)
                }
                composable(AppRoutes.LANGUAGE_BROWSE) {
                    LanguageBrowseScreen(navController = navController)
                }
                composable(AppRoutes.PRIVACY_POLICY) {
                    PrivacyPolicyScreen(navController = navController)
                }
            }
        }
    }
}