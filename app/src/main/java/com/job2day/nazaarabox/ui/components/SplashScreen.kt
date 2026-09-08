package com.job2day.nazaarabox.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.job2day.nazaarabox.R
import com.job2day.nazaarabox.ui.theme.AppColors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

private data class CosmicParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radiusDp: Float,
    val baseAlpha: Float,
    val driftSpeed: Float,
    val phaseOffset: Float,
    val color: Color,
)

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
) {
    // --- Animation Controllers ---
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.60f) }
    val textAlpha = remember { Animatable(0f) }
    val textSlide = remember { Animatable(20f) }
    val taglineAlpha = remember { Animatable(0f) }
    val laserProgress = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }
    val exitScale = remember { Animatable(1f) }

    // --- Infinite Subtle Ambient Oscillations ---
    val infiniteTransition = rememberInfiniteTransition(label = "SplashInfinite")

    // Gentle breathing of ambient aura behind logo
    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "AuraPulse",
    )

    // Sheen rotation for the badge border
    val borderSheenRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "BorderSheen",
    )

    // Particle field motion progress
    val particleDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ParticleDrift",
    )

    // --- Procedural Cosmic Star Field ---
    val particles = remember {
        val colorPalette = listOf(
            Color.White,
            AppColors.Secondary.copy(alpha = 0.85f),
            AppColors.Primary.copy(alpha = 0.75f),
            AppColors.Accent.copy(alpha = 0.80f),
        )
        List(36) { index ->
            CosmicParticle(
                xRatio = ((index * 37 + 19) % 100) / 100f,
                yRatio = ((index * 61 + 13) % 100) / 100f,
                radiusDp = 1.0f + (index % 4) * 0.7f,
                baseAlpha = 0.25f + (index % 5) * 0.12f,
                driftSpeed = 0.4f + (index % 3) * 0.3f,
                phaseOffset = (index * 0.35f),
                color = colorPalette[index % colorPalette.size],
            )
        }
    }

    // --- Main Choreography Timeline ---
    LaunchedEffect(Unit) {
        coroutineScope {
            // Stage 1: Logo spring entrance & alpha
            launch {
                logoAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                )
            }
            launch {
                logoScale.animateTo(
                    targetValue = 1.04f,
                    animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
                )
                logoScale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                )
            }

            // Stage 2: Typography & Tagline Reveal
            launch {
                delay(300)
                textAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                )
            }
            launch {
                delay(300)
                textSlide.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                )
            }
            launch {
                delay(550)
                taglineAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                )
            }

            // Stage 3: Glowing laser bar progress
            launch {
                delay(400)
                laserProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
                )
            }
        }

        // Brief dramatic hold before smooth exit
        delay(250)

        // Stage 4: Cinematic Exit Transition
        coroutineScope {
            launch {
                exitAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 350, easing = FastOutLinearInEasing),
                )
            }
            launch {
                exitScale.animateTo(
                    targetValue = 1.08f,
                    animationSpec = tween(durationMillis = 350, easing = FastOutLinearInEasing),
                )
            }
        }

        onSplashComplete()
    }

    // --- Layout ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF090912),
                        Color(0xFF0D0D1A),
                        Color(0xFF0A0A14),
                    )
                )
            )
            .graphicsLayer {
                alpha = exitAlpha.value
                scaleX = exitScale.value
                scaleY = exitScale.value
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                // Tap-to-skip immediately enters the app smoothly
                onSplashComplete()
            },
        contentAlignment = Alignment.Center,
    ) {
        // --- 1. Cosmic Floating Starfield ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val density = this

            particles.forEach { particle ->
                val dynamicAlpha = (particle.baseAlpha * (0.6f + 0.4f * sin((particleDrift * 6.28318f * particle.driftSpeed) + particle.phaseOffset)))
                    .coerceIn(0f, 1f)
                val currentY = (particle.yRatio * height - (particleDrift * 60f * particle.driftSpeed)) % height
                val currentX = particle.xRatio * width

                drawCircle(
                    color = particle.color.copy(alpha = dynamicAlpha),
                    radius = with(density) { particle.radiusDp.dp.toPx() },
                    center = Offset(
                        x = if (currentX < 0) currentX + width else currentX,
                        y = if (currentY < 0) currentY + height else currentY,
                    ),
                )
            }
        }

        // --- 2. Ambient Radiant Aura (Behind Logo) ---
        Box(
            modifier = Modifier
                .size(280.dp)
                .graphicsLayer {
                    scaleX = auraPulse
                    scaleY = auraPulse
                    alpha = logoAlpha.value * 0.85f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AppColors.Primary.copy(alpha = 0.42f),
                            AppColors.Secondary.copy(alpha = 0.18f),
                            AppColors.Primary.copy(alpha = 0.05f),
                            Color.Transparent,
                        ),
                        radius = 420f,
                    ),
                    shape = CircleShape,
                ),
        )

        // --- 3. Center Content (Logo, Brand Name, Tagline) ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            // --- Logo Container with Gradient Border Sheen ---
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .graphicsLayer {
                        alpha = logoAlpha.value
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = AppColors.Primary,
                        spotColor = AppColors.Secondary,
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1C1C2C),
                                Color(0xFF141422),
                            )
                        )
                    )
                    .border(
                        width = 1.8.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                AppColors.Primary,
                                AppColors.Secondary,
                                AppColors.Accent,
                                AppColors.Primary,
                            ),
                            center = Offset(124f, 124f),
                        ),
                        shape = RoundedCornerShape(32.dp),
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "ENGORA Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- "ENGORA" Title with Gradient Shader & Tracking ---
            Text(
                text = "ENGORA",
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White,
                            AppColors.Secondary,
                            AppColors.Primary,
                        )
                    )
                ),
                modifier = Modifier
                    .graphicsLayer {
                        alpha = textAlpha.value
                        translationY = textSlide.value
                    },
            )

            Spacer(modifier = Modifier.height(10.dp))

            // --- Tagline with Expansive Tracking ---
            Text(
                text = "YOUR CINEMATIC UNIVERSE",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 3.8.sp,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = taglineAlpha.value
                        translationY = textSlide.value * 0.5f
                    },
            )

            Spacer(modifier = Modifier.height(44.dp))

            // --- 4. Sleek Neon Glowing Laser Loading Line ---
            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = textAlpha.value }
                    .size(width = 140.dp, height = 3.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White.copy(alpha = 0.10f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth(fraction = laserProgress.value)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AppColors.Primary,
                                    AppColors.Secondary,
                                    AppColors.Accent,
                                )
                            )
                        ),
                )
            }
        }

        // --- 5. Bottom Powered By Marker ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .graphicsLayer { alpha = taglineAlpha.value * 0.50f },
        ) {
            Text(
                text = "POWERED BY NAZAARA ENGINE",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.40f),
            )
        }
    }
}
