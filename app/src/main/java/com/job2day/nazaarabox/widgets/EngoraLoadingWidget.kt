package com.job2day.nazaarabox.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.job2day.nazaarabox.ui.theme.EngoraColors

enum class EngoraLoadingSize(val ringSize: Dp, val monogramSize: Dp, val strokeWidth: Dp, val fontSize: Int) {
    FULLSCREEN(ringSize = 78.dp, monogramSize = 34.dp, strokeWidth = 3.5.dp, fontSize = 13),
    MEDIUM(ringSize = 54.dp, monogramSize = 24.dp, strokeWidth = 2.8.dp, fontSize = 11),
    COMPACT(ringSize = 32.dp, monogramSize = 14.dp, strokeWidth = 2.2.dp, fontSize = 9),
}

/**
 * Signature Cinema-Grade ENGORA Loading Component
 * Features:
 * - Continuous rotating chromatic gradient orbit ring
 * - Centered glowing Engora Ribbon "E" Monogram with breathing ruby bloom
 * - Pulsing subtitle typography
 */
@Composable
fun EngoraLoadingWidget(
    modifier: Modifier = Modifier,
    message: String? = null,
    size: EngoraLoadingSize = EngoraLoadingSize.FULLSCREEN,
    showBloom: Boolean = true,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "engoraLoader")

    // 1. Continuous Orbit Rotation (360 degrees)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbitRotation",
    )

    // 2. Breathing Ruby Bloom Pulse
    val bloomScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bloomScale",
    )

    val bloomAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bloomAlpha",
    )

    // 3. Subtitle Text Alpha Pulse
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.60f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "textPulse",
    )

    val contentAlignment = if (size == EngoraLoadingSize.FULLSCREEN) Alignment.Center else Alignment.Center

    Box(
        modifier = modifier.then(
            if (size == EngoraLoadingSize.FULLSCREEN) Modifier.fillMaxSize() else Modifier
        ),
        contentAlignment = contentAlignment,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(size.ringSize),
                contentAlignment = Alignment.Center,
            ) {
                // Background Soft Ruby Bloom / Ambient Halo
                if (showBloom && size != EngoraLoadingSize.COMPACT) {
                    Canvas(
                        modifier = Modifier
                            .size(size.ringSize * 1.3f)
                            .scale(bloomScale)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    EngoraColors.RedLight.copy(alpha = bloomAlpha * 0.45f),
                                    EngoraColors.Red.copy(alpha = bloomAlpha * 0.20f),
                                    Color.Transparent,
                                ),
                                center = center,
                                radius = size.ringSize.toPx() * 0.65f,
                            )
                        )
                    }
                }

                // Rotating Chromatic Orbit Ring
                Canvas(modifier = Modifier.size(size.ringSize)) {
                    val strokePx = size.strokeWidth.toPx()
                    val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
                    val topLeft = Offset(strokePx / 2f, strokePx / 2f)

                    // Track Ring (Subtle obsidian base)
                    drawArc(
                        color = EngoraColors.RedDeep.copy(alpha = 0.22f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    )

                    // Active Chromatic Orbit Arc with gradient sweep
                    rotate(degrees = rotation) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    EngoraColors.RedDeep.copy(alpha = 0.4f),
                                    EngoraColors.Red,
                                    EngoraColors.RedLight,
                                    Color.White,
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round),
                        )
                    }
                }

                // Centered Engora Ribbon "E" Monogram
                EngoraMonogram(
                    size = size.monogramSize,
                    modifier = Modifier.scale(if (size != EngoraLoadingSize.COMPACT) bloomScale else 1f),
                )
            }

            // Optional Animated Subtitle Message
            if (!message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = message,
                    color = EngoraColors.TextPrimary.copy(alpha = textAlpha),
                    fontSize = size.fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Fullscreen Frosted Glass Loading Overlay for screen transitions
 */
@Composable
fun EngoraLoadingOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    message: String = "Loading...",
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(180)),
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EngoraColors.Background.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center,
        ) {
            EngoraLoadingWidget(
                message = message,
                size = EngoraLoadingSize.FULLSCREEN,
            )
        }
    }
}
