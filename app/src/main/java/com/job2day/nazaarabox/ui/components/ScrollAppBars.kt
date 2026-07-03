package com.job2day.nazaarabox.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.widgets.CustomIconWidget

@Composable
fun DetailOverlayAppBar(
    title: String,
    showTitle: Boolean,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (showTitle) AppColors.BackgroundDark.copy(alpha = 0.8f)
                else Color.Transparent,
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleNavButton(iconName = "arrow_back_ios_new_rounded", onClick = onBack)
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = title,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!showTitle) Spacer(modifier = Modifier.weight(1f))
            CircleNavButton(iconName = "share_rounded", onClick = onShare)
            Spacer(modifier = Modifier.size(8.dp))
            CircleNavButton(
                icon = Icons.Default.MoreVert,
                onClick = onMore,
            )
        }
    }
}

@Composable
fun HomeGlassAppBar(
    isBlurred: Boolean,
    filterActiveCount: Int,
    onLanguage: () -> Unit,
    onSearch: () -> Unit,
    onFilter: () -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSafeMode = !com.job2day.nazaarabox.utils.AdManager.isLiveMode
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isBlurred) AppColors.BackgroundDark.copy(alpha = 0.75f)
                else Color.Transparent,
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Nazaarabox",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                letterSpacing = (-0.5).sp,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (!isSafeMode) {
                GlassIconButton(iconName = "language_rounded", onClick = onLanguage)
                Spacer(modifier = Modifier.size(8.dp))
                GlassIconButton(iconName = "search_rounded", onClick = onSearch)
                Spacer(modifier = Modifier.size(8.dp))
                GlassFilterButton(activeCount = filterActiveCount, onClick = onFilter)
                Spacer(modifier = Modifier.size(8.dp))
                GlassIconButton(iconName = "notifications_none_rounded", onClick = onNotifications)
            }
        }
    }
}

@Composable
private fun GlassIconButton(
    iconName: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        CustomIconWidget(iconName = iconName, size = 20.dp, color = Color.White)
    }
}

@Composable
private fun GlassFilterButton(
    activeCount: Int,
    onClick: () -> Unit,
) {
    val isActive = activeCount > 0
    Box(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isActive) AppColors.Primary.copy(alpha = 0.16f)
                else Color.White.copy(alpha = 0.08f),
            )
            .border(
                1.dp,
                if (isActive) AppColors.Primary.copy(alpha = 0.47f) else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CustomIconWidget(
                iconName = "tune_rounded",
                size = 16.dp,
                color = if (isActive) AppColors.Primary else Color.White,
            )
            if (isActive) {
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(AppColors.Primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$activeCount",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleNavButton(
    iconName: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when {
            iconName != null -> CustomIconWidget(iconName = iconName, size = 18.dp, color = Color.White)
            icon != null -> androidx.compose.material3.Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}
