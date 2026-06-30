package com.job2day.nazaarabox

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.job2day.nazaarabox.navigation.NazaaraboxNavHost
import com.job2day.nazaarabox.ui.theme.NazaaraboxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            NazaaraboxTheme {
                ApplyDarkStatusBar()
                NazaaraboxNavHost()
            }
        }
    }
}

@Composable
private fun ApplyDarkStatusBar() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? ComponentActivity)?.window
        if (window != null) {
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
        onDispose { }
    }
}
