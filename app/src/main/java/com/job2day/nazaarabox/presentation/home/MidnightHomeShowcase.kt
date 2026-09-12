package com.job2day.nazaarabox.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.job2day.nazaarabox.R
import com.job2day.nazaarabox.core.HomeCategory
import com.job2day.nazaarabox.routes.AppRoutes

private val NeonMagenta = Color(0xFFFF1A75)
private val NeonPurple = Color(0xFF9D4EDD)
private val DarkCardBg = Color(0xFF140F1D)

@Composable
fun MidnightHomeShowcase(
    navController: NavController,
    categories: List<HomeCategory>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // --- 1. Section Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Glowing Hot Girl Icon Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(NeonMagenta.copy(alpha = 0.25f), NeonPurple.copy(alpha = 0.25f))
                            )
                        )
                        .border(
                            BorderStroke(1.2.dp, NeonMagenta),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_midnight_girl),
                        contentDescription = "Midnight Hot Girl",
                        modifier = Modifier.size(24.dp),
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Midnight 18+ Club",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeonMagenta,
                        ) {
                            Text(
                                text = "18+ VIP",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            )
                        }
                    }
                    Text(
                        text = "Hot Nightlife, Adult Cinema & Exclusives",
                        color = NeonMagenta.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // Explore Lounge CTA
            Surface(
                onClick = { navController.navigate(AppRoutes.MIDNIGHT) },
                shape = RoundedCornerShape(16.dp),
                color = NeonMagenta.copy(alpha = 0.15f),
                border = BorderStroke(0.8.dp, NeonMagenta.copy(alpha = 0.4f)),
            ) {
                Text(
                    text = "Lounge →",
                    color = NeonMagenta,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. Hot Category Cards Row ("hos icons") ---
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(categories) { cat ->
                MidnightCategoryCard(
                    category = cat,
                    onClick = { navController.navigate(AppRoutes.MIDNIGHT) },
                )
            }
        }
    }
}

@Composable
private fun MidnightCategoryCard(
    category: HomeCategory,
    onClick: () -> Unit,
) {
    val hotDrawableId = getHotIconForCategory(category.label)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = DarkCardBg,
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    NeonMagenta.copy(alpha = 0.60f),
                    NeonPurple.copy(alpha = 0.35f),
                )
            ),
        ),
        modifier = Modifier
            .width(148.dp)
            .height(82.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            // Background subtle gradient glow
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            listOf(NeonMagenta.copy(alpha = 0.20f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Hot Icon Badge
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonMagenta.copy(alpha = 0.18f))
                            .border(BorderStroke(0.6.dp, NeonMagenta.copy(alpha = 0.4f)), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = hotDrawableId),
                            contentDescription = category.label,
                            tint = NeonMagenta,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    if (category.emoji.isNotBlank()) {
                        Text(text = category.emoji, fontSize = 16.sp)
                    }
                }

                Text(
                    text = category.label,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun getHotIconForCategory(label: String): Int {
    val l = label.lowercase()
    return when {
        l.contains("passion") || l.contains("after") || l.contains("kiss") -> R.drawable.ic_hot_lips
        l.contains("noir") || l.contains("crime") || l.contains("heel") || l.contains("nightlife") -> R.drawable.ic_hot_heels
        l.contains("madness") || l.contains("horror") || l.contains("flame") -> R.drawable.ic_hot_flame
        l.contains("exclusive") || l.contains("bikini") || l.contains("vip") -> R.drawable.ic_hot_bikini
        l.contains("all") || l.contains("lounge") || l.contains("cocktail") -> R.drawable.ic_hot_cocktail
        else -> R.drawable.ic_hot_girl_nav
    }
}
