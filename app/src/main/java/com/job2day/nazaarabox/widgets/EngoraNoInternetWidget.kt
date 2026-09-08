package com.job2day.nazaarabox.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.job2day.nazaarabox.ui.theme.EngoraColors
import com.job2day.nazaarabox.ui.theme.EngoraShapes
import com.job2day.nazaarabox.utils.rememberIsOnline
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Universal Cinema-Grade "No Internet Connection" Screen for all pages
 */
@Composable
fun EngoraNoInternetScreen(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "No Internet Connection",
    message: String = "Please check your Wi-Fi or mobile network and try again. ENGORA will restore your stream once you're back online.",
) {
    var isRetrying by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Radar signal wave animations
    val infiniteTransition = rememberInfiniteTransition(label = "radarWaves")
    val wave1Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wave1Scale",
    )
    val wave1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wave1Alpha",
    )

    val wave2Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wave2Scale",
    )
    val wave2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wave2Alpha",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EngoraColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        // Ambient Warm Backlight Glow
        Canvas(modifier = Modifier.size(300.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        EngoraColors.Red.copy(alpha = 0.18f),
                        EngoraColors.Gold.copy(alpha = 0.08f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = size.width / 2f,
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Animated Radar Center Icon with Expanding Signal Waves
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Expanding Ring 1
                Canvas(modifier = Modifier.size(80.dp).scale(wave1Scale)) {
                    drawCircle(
                        color = EngoraColors.RedLight.copy(alpha = wave1Alpha),
                        style = Stroke(width = 1.5.dp.toPx()),
                    )
                }

                // Expanding Ring 2
                Canvas(modifier = Modifier.size(80.dp).scale(wave2Scale)) {
                    drawCircle(
                        color = EngoraColors.Gold.copy(alpha = wave2Alpha),
                        style = Stroke(width = 1.2.dp.toPx()),
                    )
                }

                // Core Frosted Acrylic Circle with Wi-Fi Off Icon
                Surface(
                    shape = CircleShape,
                    color = EngoraColors.CardElevated,
                    border = BorderStroke(1.5.dp, EngoraColors.RedLight.copy(alpha = 0.45f)),
                    shadowElevation = 12.dp,
                    modifier = Modifier.size(76.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        EngoraColors.RedDeep.copy(alpha = 0.6f),
                                        EngoraColors.CardElevated,
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WifiOff,
                            contentDescription = "No Internet",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Headline
            Text(
                text = title,
                color = EngoraColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = (-0.3).sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Descriptive Message
            Text(
                text = message,
                color = EngoraColors.TextMuted,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Interactive Try Again CTA Button with Loading State
            EngoraPrimaryButton(
                text = if (isRetrying) "Connecting..." else "Try Again",
                icon = if (isRetrying) null else Icons.Default.Refresh,
                isLoading = isRetrying,
                onClick = {
                    if (!isRetrying) {
                        isRetrying = true
                        coroutineScope.launch {
                            delay(600)
                            onRetry()
                            delay(400)
                            isRetrying = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.68f),
            )
        }
    }
}

/**
 * Slide-down floating offline banner that displays when network drops during browsing
 */
@Composable
fun EngoraOfflineBanner(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = EngoraShapes.FullPill,
                color = Color(0xFF221711).copy(alpha = 0.94f),
                border = BorderStroke(1.dp, EngoraColors.Gold.copy(alpha = 0.5f)),
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EngoraColors.Gold),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You're currently offline",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (onRetry != null) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Retry",
                            color = EngoraColors.Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EngoraColors.Gold.copy(alpha = 0.15f))
                                .border(0.5.dp, EngoraColors.Gold.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-level wrapper that automatically renders offline screen when device loses internet
 */
@Composable
fun EngoraOfflineWrapper(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isOnline: Boolean = rememberIsOnline().value,
    content: @Composable () -> Unit,
) {
    if (isOnline) {
        content()
    } else {
        EngoraNoInternetScreen(
            onRetry = onRetry,
            modifier = modifier,
        )
    }
}
