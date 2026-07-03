package com.job2day.nazaarabox.navigation

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.job2day.nazaarabox.presentation.search.SearchScreen
import com.job2day.nazaarabox.presentation.season.SeasonScreen
import com.job2day.nazaarabox.presentation.seeall.SeeAllScreen
import com.job2day.nazaarabox.routes.AppRoutes
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager
import com.job2day.nazaarabox.widgets.AdInterstitialOverlay
import com.job2day.nazaarabox.widgets.AppBottomBar

@Composable
fun NazaaraboxNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val mainRoutes = setOf(AppRoutes.HOME, AppRoutes.MOVIES, AppRoutes.TV_SHOWS, AppRoutes.ANIME)
    val showBottomBar = currentRoute in mainRoutes

    val context = androidx.compose.ui.platform.LocalContext.current
    var appOpenAdShown by remember { mutableStateOf(false) }
    val interstitialRoutes = remember {
        mutableSetOf(
            AppRoutes.MOVIES,
            AppRoutes.TV_SHOWS,
            AppRoutes.ANIME,
            AppRoutes.SEARCH,
            AppRoutes.DETAIL,
            AppRoutes.PLAYER,
            AppRoutes.ACTOR,
            AppRoutes.SEE_ALL,
            AppRoutes.CATEGORY,
            AppRoutes.LANGUAGE_BROWSE,
        )
    }
    var shownInterstitialFor by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(currentRoute) {
        val route = currentRoute ?: return@LaunchedEffect
        if (route in interstitialRoutes && route !in shownInterstitialFor && AdManager.canShowInterstitial()) {
            kotlinx.coroutines.delay(400)
            val activity = context as? Activity
            if (activity != null && AdManager.canShowInterstitial()) {
                AdManager.showInterstitial(activity) {
                    shownInterstitialFor = shownInterstitialFor + route
                }
            } else {
                shownInterstitialFor = shownInterstitialFor + route
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!appOpenAdShown && AdManager.canShowInterstitial()) {
            kotlinx.coroutines.delay(600)
            val activity = context as? Activity
            if (activity != null && AdManager.canShowInterstitial()) {
                AdManager.showAppOpenAd(activity) {
                    appOpenAdShown = true
                }
            } else {
                appOpenAdShown = true
            }
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
            }
        }

        AdInterstitialOverlay()
    }
}
