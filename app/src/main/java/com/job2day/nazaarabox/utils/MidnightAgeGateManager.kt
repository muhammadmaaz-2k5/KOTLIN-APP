package com.job2day.nazaarabox.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Manages 18+ permission verification for ENGORA Midnight Club.
 */
object MidnightAgeGateManager {
    private const val PREFS_NAME = "engora_midnight_prefs"
    private const val KEY_VERIFIED = "midnight_18_plus_verified"

    fun isAgeVerified(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_VERIFIED, false)
    }

    fun setAgeVerified(context: Context, verified: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_VERIFIED, verified).apply()
    }

    @Composable
    fun rememberIsAgeVerified(): Pair<State<Boolean>, (Boolean) -> Unit> {
        val context = LocalContext.current
        val state = remember { mutableStateOf(isAgeVerified(context)) }
        val update: (Boolean) -> Unit = { verified ->
            setAgeVerified(context, verified)
            state.value = verified
        }
        return state to update
    }
}
