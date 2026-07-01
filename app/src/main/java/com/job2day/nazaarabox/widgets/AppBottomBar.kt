package com.job2day.nazaarabox.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.job2day.nazaarabox.ui.theme.AppColors

internal data class TabSpec(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val color: Color,
    val isOverlay: Boolean = false,
)

private val tabs = listOf(
    TabSpec("Home", "home", Icons.Filled.Home, Icons.Outlined.Home, AppColors.TabHome),
    TabSpec("Movies", "movies", Icons.Filled.Movie, Icons.Outlined.Movie, AppColors.TabMovies),
    TabSpec("TV Shows", "tv_shows", Icons.Filled.Tv, Icons.Outlined.Tv, AppColors.TabTv),
    TabSpec("Anime", "anime", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, AppColors.TabAnime),
    TabSpec("Search", "search", Icons.Filled.Search, Icons.Filled.Search, AppColors.TabSearch, isOverlay = true),
)

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onTabSelected: (route: String, isOverlay: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().navigationBarsPadding(),
        color = AppColors.SurfaceDark.copy(alpha = 0.92f),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val isActive = !tab.isOverlay && currentRoute == tab.route
                val color = if (isActive) tab.color else AppColors.TextMuted
                Surface(
                    onClick = { onTabSelected(tab.route, tab.isOverlay) },
                    modifier = Modifier.weight(1f),
                    color = Color.Transparent,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isActive) tab.color.copy(alpha = 0.18f) else Color.Transparent,
                        ) {
                            Icon(
                                imageVector = if (isActive) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                tint = color,
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 5.dp)
                                    .size(22.dp),
                            )
                        }
                        Text(
                            text = tab.label,
                            color = color,
                            fontSize = 9.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

internal fun tabRoutes(): List<String> = tabs.filter { !it.isOverlay }.map { it.route }

internal fun overlayTabRoute(): String = "search"

internal typealias BottomTab = TabSpec

internal fun allTabs(): List<TabSpec> = tabs
