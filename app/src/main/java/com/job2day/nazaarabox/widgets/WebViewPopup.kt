package com.job2day.nazaarabox.widgets

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewPopup(
    url: String,
    countdownSeconds: Int = 10,
    onDismiss: () -> Unit
) {
    if (url.isBlank()) return
    if (!com.job2day.nazaarabox.utils.AdManager.isWebviewAdsEnabled) return

    var countdown by remember { mutableStateOf(countdownSeconds) }
    var countdownJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Cancel previous countdown job
        countdownJob?.cancel()
        
        countdownJob = coroutineScope.launch {
            while (countdown > 0) {
                kotlinx.coroutines.delay(1000)
                countdown--
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Cancel countdown job when composable is disposed
            countdownJob?.cancel()
        }
    }

    Dialog(
        onDismissRequest = {
            if (countdown == 0) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = countdown == 0,
            dismissOnClickOutside = countdown == 0,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.7f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                DynamicWebView(
                    url = url,
                    modifier = Modifier.fillMaxSize(),
                    height = null,
                    autoClickDelayMs = com.job2day.nazaarabox.utils.AdManager.webviewAutoClickDelayMs,
                    clickYFraction = com.job2day.nazaarabox.utils.AdManager.webviewClickFraction
                )

                // Close Button / Countdown overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    if (countdown > 0) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = countdown.toString(),
                                color = Color.White
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
