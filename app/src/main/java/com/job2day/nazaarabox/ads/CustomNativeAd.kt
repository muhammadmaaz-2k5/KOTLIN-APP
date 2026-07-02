package com.job2day.nazaarabox.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.job2day.nazaarabox.ui.theme.AppColors
import com.job2day.nazaarabox.utils.AdManager

data class NativeAdItem(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val buttonText: String = "Learn More",
    val buttonLink: String = "",
    val buttonColor: String = "#FF6B6B"
)

@Composable
fun CustomNativeAd(
    ad: NativeAdItem? = null,
    adUrl: String = AdManager.webviewAdUrl,
    backgroundColor: Color = AppColors.SurfaceVariantDark,
    modifier: Modifier = Modifier,
) {
    if (!AdManager.isAdsEnabled) {
        return
    }

    val adData = ad ?: NativeAdItem(
        title = "Sponsored Content",
        description = "Check out this amazing offer!",
        imageUrl = "https://nazaarabox.com/logo.png",
        buttonText = "Learn More",
        buttonLink = adUrl
    )
    
    var isVisible by remember { mutableStateOf(true) }
    
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(backgroundColor, RoundedCornerShape(12.dp)),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    // Image on the left
                    if (adData.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = adData.imageUrl,
                            contentDescription = adData.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(8.dp)
                        )
                    }
                    
                    // Content on the right
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = adData.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = adData.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextMuted,
                            maxLines = 2
                        )
                    }
                }
            }
            
            IconButton(
                onClick = { isVisible = false },
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Ad",
                    tint = AppColors.TextMuted,
                )
            }
        }
    }
}
