package com.job2day.nazaarabox.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex

@Composable
fun VideoPlayerDialog(
    url: String,
    onDismiss: () -> Unit,
    aspectRatio: Float = 9f / 16f // Default to 9:16 for Clips
) {
    // Force 9:16 for vertical videos (Shorts, TikTok, etc.)
    val isVertical = remember(url) {
        url.contains("youtube.com/embed/") || url.contains("youtube.com/shorts/") || url.contains("tiktok.com")
    }
    
    // In Clips context, we almost always want 9:16. 
    // If it's specifically a vertical platform, we definitely force it.
    val effectiveAspectRatio = if (isVertical) 9f / 16f else aspectRatio

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Full screen width
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .zIndex(1f) // Ensure it's on top
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            // Video Player
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (effectiveAspectRatio < 1f) 0.9f else 1f) // Limit width for vertical videos
                    .align(Alignment.Center)
                    .aspectRatio(effectiveAspectRatio)
            ) {
                VideoPlayer(
                    embedUrl = url,
                    modifier = Modifier.fillMaxSize(),
                    aspectRatio = effectiveAspectRatio,
                    showFullscreenButton = false // No fullscreen button in dialog mode
                )
            }
        }
    }
}


