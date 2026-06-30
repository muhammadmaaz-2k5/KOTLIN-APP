package com.job2day.nazaarabox.widgets

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.job2day.nazaarabox.ui.theme.AppColors

private val iconMap: Map<String, ImageVector> = mapOf(
    "home_outlined" to Icons.Outlined.Home,
    "home_rounded" to Icons.Rounded.Home,
    "movie_outlined" to Icons.Outlined.Movie,
    "movie_rounded" to Icons.Rounded.Movie,
    "tv_outlined" to Icons.Outlined.Tv,
    "tv_rounded" to Icons.Rounded.Tv,
    "search_rounded" to Icons.Rounded.Search,
    "language_rounded" to Icons.Default.Language,
    "notifications_none_rounded" to Icons.Default.NotificationsNone,
    "tune_rounded" to Icons.Default.Tune,
    "local_fire_department_rounded" to Icons.Default.LocalFireDepartment,
    "trending_up_rounded" to Icons.Default.TrendingUp,
    "arrow_back_ios_new_rounded" to Icons.Rounded.ArrowBackIosNew,
    "share_rounded" to Icons.Rounded.Share,
    "close_rounded" to Icons.Rounded.Close,
    "play_circle_filled_rounded" to Icons.Rounded.PlayCircle,
    "pause_circle_filled_rounded" to Icons.Rounded.PauseCircle,
    "queue_music_rounded" to Icons.Default.QueueMusic,
    "people_rounded" to Icons.Default.People,
    "rate_review_rounded" to Icons.Default.RateReview,
    "dns_rounded" to Icons.Default.Dns,
    "apps_rounded" to Icons.Rounded.Apps,
    "person_outline_rounded" to Icons.Outlined.Person,
    "movie_outlined_rounded" to Icons.Outlined.Movie,
    "tv_outlined_rounded" to Icons.Outlined.Tv,
    "star_rounded" to Icons.Default.Star,
    "play_circle_outline" to Icons.Default.PlayCircle,
)

@Composable
fun CustomIconWidget(
    iconName: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = AppColors.TextPrimary,
) {
    val icon = iconMap[iconName] ?: Icons.Default.Apps
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = modifier.size(size),
        tint = color,
    )
}
