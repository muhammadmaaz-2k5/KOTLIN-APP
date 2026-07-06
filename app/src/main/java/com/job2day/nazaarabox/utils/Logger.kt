package com.job2day.nazaarabox.utils

import android.util.Log

object Logger {
    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }
}
