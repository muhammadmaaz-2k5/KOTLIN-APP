package com.job2day.nazaarabox.ui.components

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.job2day.nazaarabox.R

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var isVideoCompleted by remember { mutableStateOf(false) }
    var iconAlpha by remember { mutableStateOf(0f) }
    val videoUri = Uri.parse("android.resource://com.job2day.nazaarabox/raw/splashscreen")

    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        if (!isVideoCompleted) {
            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        setVideoURI(videoUri)
                        setOnCompletionListener {
                            isVideoCompleted = true
                        }
                        start()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LaunchedEffect(isVideoCompleted) {
                while (iconAlpha < 1f) {
                    iconAlpha = (iconAlpha + 0.1f).coerceAtMost(1f)
                    kotlinx.coroutines.delay(30)
                }
                kotlinx.coroutines.delay(1500)
                onSplashComplete()
            }
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer { alpha = iconAlpha },
                contentScale = ContentScale.Fit
            )
        }
    }
}
