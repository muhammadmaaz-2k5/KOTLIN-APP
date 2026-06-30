package com.job2day.nazaarabox.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.job2day.nazaarabox.core.AppConfig
import com.job2day.nazaarabox.core.CastMember
import com.job2day.nazaarabox.core.ReviewItem
import com.job2day.nazaarabox.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullCastSheet(
    cast: List<CastMember>,
    onPersonTap: (CastMember) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.SurfaceDark,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF444466)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Full Cast",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${cast.size} members",
                    color = Color(0xFF888899),
                    fontSize = 13.sp,
                )
            }
            Divider(
                color = Color(0xFF2A2A3E),
                modifier = Modifier.padding(top = 16.dp),
            )
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(cast, key = { it.id }) { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.SurfaceVariantDark)
                            .clickable {
                                onDismiss()
                                onPersonTap(member)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CastAvatar(photoUrl = member.photoUrl, name = member.name, size = 52.dp)
                        Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                            Text(
                                text = member.name,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "as ${member.character}",
                                color = Color(0xFF888899),
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF444466),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CastAvatar(
    photoUrl: String,
    name: String,
    size: androidx.compose.ui.unit.Dp,
) {
    if (photoUrl.isNotBlank()) {
        CustomImage(
            imageUrl = photoUrl,
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(AppColors.SurfaceVariantDark),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                color = AppColors.Primary,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.34f).sp,
            )
        }
    }
}

@Composable
fun DetailReviewCard(review: ReviewItem) {
    var expanded by remember { mutableStateOf(false) }
    val avatarUrl = review.avatarPath.takeIf { it.isNotBlank() }?.let { path ->
        if (path.startsWith("http")) path else "${AppConfig.IMAGE_BASE}/w45$path"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 0.dp)
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.SurfaceDark)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (avatarUrl != null) {
                CustomImage(
                    imageUrl = avatarUrl,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppColors.Primary.copy(alpha = 0.24f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = review.author.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = AppColors.Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
            }
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(
                    text = review.author,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                review.rating?.let { rating ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CustomIconWidget(iconName = "star_rounded", size = 12.dp, color = AppColors.Accent)
                        Text(
                            text = "${String.format("%.0f", rating)}/10",
                            modifier = Modifier.padding(start = 3.dp),
                            color = AppColors.Accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
        Text(
            text = review.content,
            modifier = Modifier.padding(top = 10.dp),
            color = Color(0xFFAAAAAA),
            fontSize = 13.sp,
            lineHeight = 19.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis,
        )
        if (review.content.length > 150) {
            Text(
                text = if (expanded) "Show Less" else "Read More",
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clickable { expanded = !expanded },
                color = AppColors.Primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun DetailSectionHeader(
    title: String,
    iconName: String,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CustomIconWidget(iconName = iconName, size = 20.dp, color = AppColors.Primary)
        Text(
            text = title,
            modifier = Modifier.padding(start = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}
