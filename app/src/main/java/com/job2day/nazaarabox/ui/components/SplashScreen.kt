package com.job2day.nazaarabox.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * High-fidelity Netflix-style vertical spectrum light beam
 */
private data class SpectrumBeam(
    val xFraction: Float,       // Center position relative to screen width (-0.5f to 0.5f)
    val widthDp: Float,         // Beam width
    val color: Color,           // Spectrum chromatic color
    val speedMultiplier: Float, // Dispersion rate
    val maxAlpha: Float,        // Peak brightness
    val delayOffset: Int,       // Delay before beam ignites
)

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
) {
    // --- 1. Ribbon "E" Construction Animatables ---
    val spineProgress = remember { Animatable(0f) }
    val topRibbonProgress = remember { Animatable(0f) }
    val midRibbonProgress = remember { Animatable(0f) }
    val btmRibbonProgress = remember { Animatable(0f) }
    val sheenProgress = remember { Animatable(-0.4f) }
    val bloomAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textTracking = remember { Animatable(1.sp.value) }

    // --- 2. Camera Dolly Zoom & Light Explosion Animatables ---
    val cameraZoom = remember { Animatable(1.0f) }
    val spectrumProgress = remember { Animatable(0f) }
    val letterFadeOut = remember { Animatable(1.0f) }
    val overallExitAlpha = remember { Animatable(1.0f) }

    // --- 3. Pre-generate Netflix Chromatic Spectrum Beams ---
    val spectrumBeams = remember {
        val colors = listOf(
            Color(0xFFE50914), // Netflix Iconic Red
            Color(0xFFFF1E27), // Bright Laser Red
            Color(0xFFB81D24), // Crimson Shadow
            Color(0xFFFF4757), // Neon Coral
            Color(0xFF831010), // Deep Burgundy
            Color(0xFF6C5CE7), // Electric Violet
            Color(0xFF8A2BE2), // Royal Purple
            Color(0xFF00CEC9), // Cyan Flare
            Color(0xFF0984E3), // Deep Blue
            Color(0xFFFFA801), // Golden Amber
            Color(0xFFFFFFFF), // Pure White Core
        )
        List(52) { i ->
            val sign = if (i % 2 == 0) 1f else -1f
            val distanceRatio = ((i / 2) * 19 % 100) / 100f
            SpectrumBeam(
                xFraction = sign * (0.02f + distanceRatio * 0.48f),
                widthDp = 1.5f + (i % 5) * 1.6f,
                color = colors[i % colors.size],
                speedMultiplier = 0.7f + (i % 4) * 0.35f,
                maxAlpha = 0.45f + (i % 6) * 0.10f,
                delayOffset = (i % 7) * 40,
            )
        }
    }

    // --- 4. Main Animation Orchestration ---
    LaunchedEffect(Unit) {
        coroutineScope {
            // Stage 1: The "E" Ribbon Construction (0ms - 550ms)
            launch {
                spineProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                )
            }
            launch {
                delay(120)
                topRibbonProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                )
            }
            launch {
                delay(180)
                midRibbonProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                )
            }
            launch {
                delay(220)
                btmRibbonProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                )
            }

            // Stage 2: Specular Sheen Glint & Radial Surge (500ms - 900ms)
            launch {
                delay(400)
                sheenProgress.animateTo(
                    targetValue = 1.4f,
                    animationSpec = tween(durationMillis = 550, easing = LinearEasing),
                )
            }
            launch {
                delay(550)
                bloomAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                )
            }

            // Stage 3: "ENGORA" Wordmark Reveal (650ms - 1200ms)
            launch {
                delay(650)
                textAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                )
            }
            launch {
                delay(650)
                textTracking.animateTo(
                    targetValue = 7.sp.value,
                    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                )
            }
        }

        // Brief cinematic breath before the famous explosion
        delay(200)

        // Stage 4: Netflix Camera Dolly Zoom & Light Spectrum Explosion (1300ms - 2200ms)
        coroutineScope {
            // Dramatic camera push into the letter
            launch {
                cameraZoom.animateTo(
                    targetValue = 14.0f,
                    animationSpec = tween(
                        durationMillis = 850,
                        easing = CubicBezierEasing(0.3f, 0.0f, 0.1f, 1.0f),
                    ),
                )
            }
            // Letter dissolves as the camera pierces through it
            launch {
                delay(180)
                letterFadeOut.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 320, easing = FastOutLinearInEasing),
                )
            }
            // Vertical chromatic light spectrum beams rush outwards past the screen
            launch {
                spectrumProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 850,
                        easing = CubicBezierEasing(0.2f, 0.0f, 0.1f, 1.0f),
                    ),
                )
            }
            // Hide bottom text quickly as warp begins
            launch {
                textAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 200, easing = LinearEasing),
                )
            }
        }

        // Stage 5: Dissolve into Main App
        overallExitAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing),
        )

        onSplashComplete()
    }

    // --- Layout ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .graphicsLayer { alpha = overallExitAlpha.value }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                // Tap to skip
                onSplashComplete()
            },
        contentAlignment = Alignment.Center,
    ) {
        // --- 1. Radial Red Bloom Aura (Netflix Surge) ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (bloomAlpha.value > 0.01f && cameraZoom.value < 4.0f) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f - 40.dp.toPx())
                val auraRadius = (size.width * 0.75f) * (1f + (cameraZoom.value - 1f) * 0.5f)
                val auraAlpha = (bloomAlpha.value * (1f - (cameraZoom.value - 1f) / 3f).coerceIn(0f, 1f))

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE50914).copy(alpha = 0.45f * auraAlpha),
                            Color(0xFFB81D24).copy(alpha = 0.20f * auraAlpha),
                            Color(0xFF831010).copy(alpha = 0.05f * auraAlpha),
                            Color.Transparent,
                        ),
                        center = centerOffset,
                        radius = auraRadius,
                    ),
                    radius = auraRadius,
                    center = centerOffset,
                )
            }
        }

        // --- 2. 3D Ribbon Letter "E" (Netflix Style Folded Ribbon) ---
        Box(
            modifier = Modifier
                .size(width = 130.dp, height = 170.dp)
                .graphicsLayer {
                    scaleX = cameraZoom.value
                    scaleY = cameraZoom.value
                    alpha = letterFadeOut.value
                    translationY = -40.dp.toPx()
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val totalWidth = size.width
                val totalHeight = size.height
                val ribbonWidth = totalWidth * 0.28f // ~36dp

                // --- Palette: Netflix Gradients & Shadows ---
                val spineBrush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFE50914), // bright top
                        Color(0xFFB81D24), // mid ruby
                        Color(0xFF831010), // deep base shadow
                    )
                )

                val topRibbonBrush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF991218), // fold shadow where it connects to spine
                        Color(0xFFE50914), // vibrant red
                        Color(0xFFFF242E), // bright tip
                    ),
                    startX = ribbonWidth,
                    endX = totalWidth,
                )

                val midRibbonBrush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF991218),
                        Color(0xFFE50914),
                        Color(0xFFE50914),
                    ),
                    startX = ribbonWidth,
                    endX = totalWidth * 0.78f,
                )

                val btmRibbonBrush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF7A0E13),
                        Color(0xFFB81D24),
                        Color(0xFFE50914),
                    ),
                    startX = ribbonWidth,
                    endX = totalWidth,
                )

                // 2A. Vertical Spine (Left)
                val currentSpineHeight = totalHeight * spineProgress.value
                clipRect(top = 0f, bottom = currentSpineHeight, left = 0f, right = ribbonWidth) {
                    drawRect(
                        brush = spineBrush,
                        topLeft = Offset(0f, 0f),
                        size = Size(ribbonWidth, totalHeight),
                    )
                }

                // 2B. Top Horizontal Ribbon
                val maxTopWidth = totalWidth - ribbonWidth
                val currentTopWidth = maxTopWidth * topRibbonProgress.value
                if (topRibbonProgress.value > 0f) {
                    clipRect(left = ribbonWidth, right = ribbonWidth + currentTopWidth, top = 0f, bottom = ribbonWidth) {
                        drawRect(
                            brush = topRibbonBrush,
                            topLeft = Offset(ribbonWidth, 0f),
                            size = Size(maxTopWidth, ribbonWidth),
                        )
                        // Fold Drop Shadow over horizontal ribbon
                        drawRect(
                            brush = Brush.horizontalGradient(
                                listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent),
                                startX = ribbonWidth,
                                endX = ribbonWidth + 14.dp.toPx(),
                            ),
                            topLeft = Offset(ribbonWidth, 0f),
                            size = Size(14.dp.toPx(), ribbonWidth),
                        )
                    }
                }

                // 2C. Middle Horizontal Ribbon
                val midHeight = ribbonWidth * 0.90f
                val midY = (totalHeight - midHeight) / 2f
                val maxMidWidth = totalWidth * 0.76f - ribbonWidth
                val currentMidWidth = maxMidWidth * midRibbonProgress.value
                if (midRibbonProgress.value > 0f) {
                    clipRect(left = ribbonWidth, right = ribbonWidth + currentMidWidth, top = midY, bottom = midY + midHeight) {
                        drawRect(
                            brush = midRibbonBrush,
                            topLeft = Offset(ribbonWidth, midY),
                            size = Size(maxMidWidth, midHeight),
                        )
                        // Fold Drop Shadow
                        drawRect(
                            brush = Brush.horizontalGradient(
                                listOf(Color.Black.copy(alpha = 0.60f), Color.Transparent),
                                startX = ribbonWidth,
                                endX = ribbonWidth + 12.dp.toPx(),
                            ),
                            topLeft = Offset(ribbonWidth, midY),
                            size = Size(12.dp.toPx(), midHeight),
                        )
                    }
                }

                // 2D. Bottom Horizontal Ribbon
                val btmY = totalHeight - ribbonWidth
                val maxBtmWidth = totalWidth - ribbonWidth
                val currentBtmWidth = maxBtmWidth * btmRibbonProgress.value
                if (btmRibbonProgress.value > 0f) {
                    clipRect(left = ribbonWidth, right = ribbonWidth + currentBtmWidth, top = btmY, bottom = totalHeight) {
                        drawRect(
                            brush = btmRibbonBrush,
                            topLeft = Offset(ribbonWidth, btmY),
                            size = Size(maxBtmWidth, ribbonWidth),
                        )
                        // Fold Drop Shadow
                        drawRect(
                            brush = Brush.horizontalGradient(
                                listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent),
                                startX = ribbonWidth,
                                endX = ribbonWidth + 14.dp.toPx(),
                            ),
                            topLeft = Offset(ribbonWidth, btmY),
                            size = Size(14.dp.toPx(), ribbonWidth),
                        )
                    }
                }

                // 2E. Specular Sheen Sweep (Glinting Across the Monogram)
                if (sheenProgress.value in -0.3f..1.3f) {
                    val sheenX = totalWidth * sheenProgress.value
                    val sheenPath = Path().apply {
                        moveTo(sheenX - 25.dp.toPx(), 0f)
                        lineTo(sheenX + 25.dp.toPx(), 0f)
                        lineTo(sheenX - 5.dp.toPx(), totalHeight)
                        lineTo(sheenX - 55.dp.toPx(), totalHeight)
                        close()
                    }
                    drawPath(
                        path = sheenPath,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.38f),
                                Color.Transparent,
                            ),
                            startX = sheenX - 40.dp.toPx(),
                            endX = sheenX + 40.dp.toPx(),
                        ),
                    )
                }
            }
        }

        // --- 3. Signature Netflix Chromatic Light-Spectrum Explosion ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (spectrumProgress.value > 0.01f) {
                val screenW = size.width
                val screenH = size.height
                val centerX = screenW / 2f
                val centerY = screenH / 2f - 40.dp.toPx()
                val progress = spectrumProgress.value

                // The light beams accelerate outward from the center origin
                spectrumBeams.forEach { beam ->
                    val beamProgress = (progress * beam.speedMultiplier).coerceIn(0f, 1.2f)
                    if (beamProgress > 0.02f) {
                        // Position expands radially from center
                        val expansion = 1f + beamProgress * 3.2f
                        val currentX = centerX + (beam.xFraction * screenW * expansion)
                        val beamAlpha = (beam.maxAlpha * (1f - (beamProgress - 0.2f).coerceAtLeast(0f) / 1.0f))
                            .coerceIn(0f, 1f)

                        val beamW = beam.widthDp.dp.toPx() * (1f + beamProgress * 2.5f)

                        // Vertical streak from top to bottom through the view
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    beam.color.copy(alpha = beamAlpha * 0.4f),
                                    beam.color.copy(alpha = beamAlpha),
                                    Color.White.copy(alpha = beamAlpha * 0.8f),
                                    beam.color.copy(alpha = beamAlpha),
                                    beam.color.copy(alpha = beamAlpha * 0.4f),
                                    Color.Transparent,
                                ),
                                startY = 0f,
                                endY = screenH,
                            ),
                            topLeft = Offset(currentX - beamW / 2f, 0f),
                            size = Size(beamW, screenH),
                        )
                    }
                }

                // Central Hyperdrive Warp Core Flash
                val coreAlpha = (1f - (progress * 1.5f)).coerceIn(0f, 1f)
                if (coreAlpha > 0f) {
                    val coreRadius = screenW * 0.35f * (1f + progress * 2f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.80f * coreAlpha),
                                Color(0xFFFF1E27).copy(alpha = 0.50f * coreAlpha),
                                Color(0xFF6C5CE7).copy(alpha = 0.20f * coreAlpha),
                                Color.Transparent,
                            ),
                            center = Offset(centerX, centerY),
                            radius = coreRadius,
                        ),
                        radius = coreRadius,
                        center = Offset(centerX, centerY),
                    )
                }
            }
        }

        // --- 4. "ENGORA" Wordmark (Netflix Style Cinematic Typography) ---
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    translationY = 90.dp.toPx()
                    alpha = textAlpha.value
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ENGORA",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                letterSpacing = textTracking.value.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFE50914),
                            Color(0xFFFF3E47),
                            Color(0xFFE50914),
                        )
                    )
                ),
            )
        }
    }
}
