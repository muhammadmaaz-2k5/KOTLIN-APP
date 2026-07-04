package com.job2day.nazaarabox.navigation

import androidx.compose.runtime.mutableStateOf

object NotificationRouter {
    var pendingRoute = mutableStateOf<String?>(null)
    var pendingData = mutableStateOf<Map<String, String>?>(null)
}
