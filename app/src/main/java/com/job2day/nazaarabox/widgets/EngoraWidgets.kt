package com.job2day.nazaarabox.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.ui.theme.EngoraColors
import com.job2day.nazaarabox.ui.theme.EngoraGradients
import com.job2day.nazaarabox.ui.theme.EngoraShapes
import com.job2day.nazaarabox.ui.theme.EngoraTypography

// =========================================================================
// 1. ENGORA BRAND LOGO & MONOGRAM
// =========================================================================

/**
 * Signature Engora Ribbon "E" Monogram drawn in vector with Netflix-style folding ribbons
 */
@Composable
fun EngoraMonogram(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val ribbonWidth = w * 0.28f

            // Spine (Left vertical)
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(EngoraColors.RedLight, EngoraColors.Red, EngoraColors.RedDark)
                ),
                topLeft = Offset(0f, 0f),
                size = Size(ribbonWidth, h),
            )

            // Top ribbon
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(EngoraColors.RedDark, EngoraColors.Red, EngoraColors.RedLight),
                    startX = ribbonWidth,
                    endX = w,
                ),
                topLeft = Offset(ribbonWidth, 0f),
                size = Size(w - ribbonWidth, ribbonWidth),
            )
            // Top fold shadow
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
                    startX = ribbonWidth,
                    endX = ribbonWidth + 6.dp.toPx(),
                ),
                topLeft = Offset(ribbonWidth, 0f),
                size = Size(6.dp.toPx(), ribbonWidth),
            )

            // Mid ribbon
            val midY = (h - ribbonWidth * 0.85f) / 2f
            val midH = ribbonWidth * 0.85f
            val midW = (w - ribbonWidth) * 0.72f
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(EngoraColors.RedDark, EngoraColors.Red),
                    startX = ribbonWidth,
                    endX = ribbonWidth + midW,
                ),
                topLeft = Offset(ribbonWidth, midY),
                size = Size(midW, midH),
            )
            // Mid fold shadow
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
                    startX = ribbonWidth,
                    endX = ribbonWidth + 5.dp.toPx(),
                ),
                topLeft = Offset(ribbonWidth, midY),
                size = Size(5.dp.toPx(), midH),
            )

            // Bottom ribbon
            val btmY = h - ribbonWidth
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(EngoraColors.RedDeep, EngoraColors.RedDark, EngoraColors.Red),
                    startX = ribbonWidth,
                    endX = w,
                ),
                topLeft = Offset(ribbonWidth, btmY),
                size = Size(w - ribbonWidth, ribbonWidth),
            )
            // Bottom fold shadow
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent),
                    startX = ribbonWidth,
                    endX = ribbonWidth + 6.dp.toPx(),
                ),
                topLeft = Offset(ribbonWidth, btmY),
                size = Size(6.dp.toPx(), ribbonWidth),
            )
        }
    }
}

/**
 * Full Engora Typographic Brand Identity Logo
 */
@Composable
fun EngoraBrandLogo(
    modifier: Modifier = Modifier,
    showMonogram: Boolean = true,
    showBadge: Boolean = false,
    badgeText: String = "STREAM",
    fontSize: Int = 22,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showMonogram) {
            EngoraMonogram(size = (fontSize + 6).dp)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = "ENGORA",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = fontSize.sp,
            letterSpacing = (-0.6).sp,
        )
        if (showBadge) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(EngoraColors.Red.copy(alpha = 0.85f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            ) {
                Text(
                    text = badgeText.uppercase(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

// =========================================================================
// 2. ENGORA BRANDED BUTTONS
// =========================================================================

/**
 * High-impact Cinema Red CTA Button with press micro-animation
 */
@Composable
fun EngoraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(120),
        label = "btnScale",
    )

    Surface(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = EngoraShapes.Button,
        color = Color.Transparent,
        modifier = modifier
            .scale(scale)
            .clip(EngoraShapes.Button)
            .background(
                if (enabled) EngoraGradients.BrandLinear else Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))
            ),
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp,
            )
        }
    }
}

/**
 * Translucent Frosted Glass Button with fine hairline border
 */
@Composable
fun EngoraGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isActive: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(120),
        label = "glassScale",
    )

    Surface(
        onClick = onClick,
        shape = EngoraShapes.Button,
        color = if (isActive) EngoraColors.Red.copy(alpha = 0.20f) else EngoraColors.GlassSurface,
        border = BorderStroke(
            1.dp,
            if (isActive) EngoraColors.Red.copy(alpha = 0.6f) else EngoraColors.GlassBorder
        ),
        modifier = modifier.scale(scale),
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isActive) EngoraColors.RedLight else Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = if (isActive) EngoraColors.RedLight else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/**
 * Circular Frosted Icon Button (Navigation, Share, Search, More)
 */
@Composable
fun EngoraCircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconName: String? = null,
    size: Dp = 38.dp,
    tint: Color = Color.White,
    isActive: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "iconScale",
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isActive) EngoraColors.Red.copy(alpha = 0.22f) else EngoraColors.GlassSurface)
            .border(
                1.dp,
                if (isActive) EngoraColors.Red.copy(alpha = 0.55f) else EngoraColors.GlassBorder,
                CircleShape
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when {
            icon != null -> Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size.value * 0.48f).dp))
            iconName != null -> CustomIconWidget(iconName = iconName, size = (size.value * 0.48f).dp, color = tint)
        }
    }
}

// =========================================================================
// 3. ENGORA BADGES & QUALITY TAGS
// =========================================================================

/**
 * 4K UHD / HDR / Dolby Audio quality pill
 */
@Composable
fun EngoraQualityTag(
    quality: String = "4K ULTRA HD",
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(EngoraShapes.Badge)
            .background(Color.Black.copy(alpha = 0.65f))
            .border(1.dp, EngoraColors.Gold.copy(alpha = 0.60f), EngoraShapes.Badge)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = quality,
            color = EngoraColors.Gold,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.4.sp,
        )
    }
}

/**
 * Gold star rating badge pill
 */
@Composable
fun EngoraRatingPill(
    rating: Double,
    modifier: Modifier = Modifier,
) {
    if (rating <= 0) return
    Box(
        modifier = modifier
            .clip(EngoraShapes.Badge)
            .background(Color.Black.copy(alpha = 0.78f))
            .border(0.5.dp, EngoraColors.Gold.copy(alpha = 0.40f), EngoraShapes.Badge)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = EngoraColors.Gold,
                modifier = Modifier.size(11.dp),
            )
            Text(
                text = String.format("%.1f", rating),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 3.dp),
            )
        }
    }
}

/**
 * Top 10 Trending Fire Rank Badge
 */
@Composable
fun EngoraTrendingBadge(
    rank: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(EngoraGradients.TrendingRank)
            .border(0.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "#$rank",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.3.sp,
        )
    }
}

// =========================================================================
// 4. ENGORA SCALABLE MEDIA CARD
// =========================================================================

enum class EngoraCardMode {
    COMPACT_GRID,      // 3-column dense grid
    EXPANSIVE_CINEMA,  // 2-column showcase with backdrop accents and synopsis
    CAROUSEL_ITEM,     // Horizontal slider item
}

/**
 * Advanced, cinema-grade scalable media card for Engora
 */
@Composable
fun EngoraMediaCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mode: EngoraCardMode = EngoraCardMode.COMPACT_GRID,
    rankIndex: Int? = null,
    showTypeBadge: Boolean = true,
    showQualityTag: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100),
        label = "mediaCardScale",
    )

    val isTv = item.type.equals("tv", ignoreCase = true)
    val typeLabel = if (isTv) "SERIES" else "MOVIE"
    val typeColor = if (isTv) EngoraColors.Cyan else EngoraColors.Red

    when (mode) {
        // --- 1. COMPACT POSTER (3-COLUMN) ---
        EngoraCardMode.COMPACT_GRID -> {
            Surface(
                onClick = onClick,
                shape = EngoraShapes.Card,
                color = EngoraColors.Card,
                border = BorderStroke(1.dp, EngoraGradients.CardBorderGradient),
                modifier = modifier
                    .fillMaxWidth()
                    .scale(scale),
                interactionSource = interactionSource,
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f),
                    ) {
                        CustomImage(
                            imageUrl = item.posterUrl,
                            modifier = Modifier.fillMaxSize(),
                        )
                        // Top Left Badge
                        if (rankIndex != null) {
                            EngoraTrendingBadge(
                                rank = rankIndex + 1,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp),
                            )
                        } else if (showTypeBadge) {
                            Text(
                                text = typeLabel,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp)
                                    .clip(EngoraShapes.Badge)
                                    .background(typeColor.copy(alpha = 0.88f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        // Top Right Rating
                        if (item.rating > 0) {
                            EngoraRatingPill(
                                rating = item.rating,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp),
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                        Text(
                            text = item.title,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = EngoraColors.TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp,
                        )
                        if (item.year.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = item.year, color = EngoraColors.TextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // --- 2. EXPANSIVE CINEMA CARD (2-COLUMN) ---
        EngoraCardMode.EXPANSIVE_CINEMA -> {
            Surface(
                onClick = onClick,
                shape = RoundedCornerShape(16.dp),
                color = EngoraColors.CardElevated,
                border = BorderStroke(1.dp, EngoraColors.CardBorder),
                modifier = modifier
                    .fillMaxWidth()
                    .scale(scale),
                interactionSource = interactionSource,
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.72f),
                    ) {
                        CustomImage(
                            imageUrl = item.posterUrl,
                            modifier = Modifier.fillMaxSize(),
                        )
                        // Gradient bottom scrim over image
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colorStops = arrayOf(
                                            0.0f to Color.Transparent,
                                            0.55f to Color.Transparent,
                                            0.85f to EngoraColors.CardElevated.copy(alpha = 0.85f),
                                            1.0f to EngoraColors.CardElevated,
                                        )
                                    )
                                ),
                        )
                        // Top Badges
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (rankIndex != null) {
                                EngoraTrendingBadge(rank = rankIndex + 1)
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(EngoraShapes.Badge)
                                        .background(typeColor.copy(alpha = 0.90f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = typeLabel,
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                    )
                                }
                            }
                            if (item.rating > 0) {
                                EngoraRatingPill(rating = item.rating)
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                    ) {
                        Text(
                            text = item.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = EngoraColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (item.year.isNotBlank()) {
                                Text(
                                    text = item.year,
                                    color = EngoraColors.TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            if (showQualityTag) {
                                Text(
                                    text = "•",
                                    color = EngoraColors.TextMuted,
                                    fontSize = 9.sp,
                                )
                                Text(
                                    text = "4K HDR",
                                    color = EngoraColors.Gold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        if (item.overview.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.overview,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = EngoraColors.TextMuted,
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                            )
                        }
                    }
                }
            }
        }

        // --- 3. CAROUSEL ITEM (HORIZONTAL ROW) ---
        EngoraCardMode.CAROUSEL_ITEM -> {
            Column(
                modifier = modifier
                    .width(148.dp)
                    .scale(scale),
            ) {
                Surface(
                    onClick = onClick,
                    shape = RoundedCornerShape(16.dp),
                    color = EngoraColors.Card,
                    border = BorderStroke(1.dp, EngoraGradients.CardBorderGradient),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    interactionSource = interactionSource,
                ) {
                    Box {
                        CustomImage(
                            imageUrl = item.posterUrl,
                            modifier = Modifier.fillMaxSize(),
                        )
                        if (rankIndex != null) {
                            EngoraTrendingBadge(
                                rank = rankIndex + 1,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp),
                            )
                        } else if (showTypeBadge) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .clip(EngoraShapes.Badge)
                                    .background(typeColor.copy(alpha = 0.88f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = typeLabel,
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        if (item.rating > 0) {
                            EngoraRatingPill(
                                rating = item.rating,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = EngoraColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (item.year.isNotBlank()) item.year else "Engora Stream",
                    color = EngoraColors.TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

// =========================================================================
// 5. ENGORA SECTION HEADER
// =========================================================================

/**
 * Cinema-Grade Section Header with vertical brand indicator bar & "See All" action
 */
@Composable
fun EngoraSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    emoji: String = "",
    itemCount: Int? = null,
    onSeeAll: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false),
        ) {
            // Cinema Red Vertical Accent Bar
            Box(
                modifier = Modifier
                    .size(width = 3.5.dp, height = 18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(EngoraGradients.BrandVertical),
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (emoji.isNotBlank()) {
                Text(text = emoji, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = title,
                color = EngoraColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                letterSpacing = (-0.2).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (itemCount != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(EngoraShapes.FullPill)
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "$itemCount",
                        color = EngoraColors.TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        if (onSeeAll != null) {
            Surface(
                onClick = onSeeAll,
                shape = EngoraShapes.FullPill,
                color = EngoraColors.Red.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, EngoraColors.Red.copy(alpha = 0.35f)),
            ) {
                Text(
                    text = "See all →",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    color = EngoraColors.RedLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// =========================================================================
// 6. ENGORA FROSTED GLASS TOP BAR
// =========================================================================

/**
 * Top navigation bar with status bar padding, frosted acrylic background, and brand identity
 */
@Composable
fun EngoraGlassTopBar(
    modifier: Modifier = Modifier,
    isScrolled: Boolean = false,
    title: String? = null,
    onBack: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    val backgroundBrush = if (isScrolled) {
        Brush.verticalGradient(
            listOf(
                EngoraColors.Background.copy(alpha = 0.96f),
                EngoraColors.Background.copy(alpha = 0.92f),
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.Black.copy(alpha = 0.85f),
                Color.Black.copy(alpha = 0.40f),
                Color.Transparent,
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundBrush)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                EngoraCircleIconButton(
                    onClick = onBack,
                    icon = Icons.AutoMirrored.Rounded.ArrowBackIos,
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else if (leadingContent != null) {
                leadingContent()
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                EngoraBrandLogo()
            }

            if (title != null) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            if (actions != null) {
                actions()
            }
        }
    }
}

// =========================================================================
// 7. ENGORA FILTER & SORT CHIPS
// =========================================================================

/**
 * Sleek filter pill chip with active cinema red gradient
 */
@Composable
fun EngoraFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String = "",
    count: Int? = null,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) EngoraColors.Red else EngoraColors.Card,
        animationSpec = tween(150),
        label = "chipBg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) EngoraColors.RedLight else EngoraColors.GlassBorder,
        animationSpec = tween(150),
        label = "chipBorder",
    )

    Surface(
        onClick = onClick,
        shape = EngoraShapes.FullPill,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (emoji.isNotBlank()) {
                Text(text = emoji, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = label,
                color = if (selected) Color.White else EngoraColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
            if (count != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (selected) Color.White.copy(alpha = 0.25f) else EngoraColors.Surface)
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                ) {
                    Text(
                        text = "$count",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/**
 * Horizontally scrollable row of Engora filter chips
 */
@Composable
fun EngoraFilterRow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, option ->
            EngoraFilterChip(
                label = option,
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
            )
        }
    }
}

// =========================================================================
// 8. ENGORA SHIMMER LOADER & EMPTY STATE
// =========================================================================

/**
 * Shimmer effect brush with cinema obsidian tones
 */
@Composable
fun engoraShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "engoraShimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "engoraShimmerTranslate",
    )
    return Brush.linearGradient(
        colors = listOf(
            EngoraColors.Card,
            EngoraColors.CardElevated,
            EngoraColors.Card,
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value),
    )
}

/**
 * Scalable skeleton placeholder card
 */
@Composable
fun EngoraShimmerCard(
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    shape: RoundedCornerShape = EngoraShapes.Card,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(engoraShimmerBrush()),
    )
}

/**
 * Scalable Cinema Empty State view
 */
@Composable
fun EngoraEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "No Titles Found",
    emoji: String = "🎬",
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = emoji, fontSize = 46.sp)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            color = EngoraColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            color = EngoraColors.TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(18.dp))
            EngoraGlassButton(text = actionText, onClick = onAction)
        }
    }
}
