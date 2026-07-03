package com.job2day.nazaarabox.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.job2day.nazaarabox.MainActivity
import com.job2day.nazaarabox.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }

        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.body ?: "New Notification", it.title ?: "Nazaarabox", it.imageUrl?.toString(), remoteMessage.data)
        } ?: run {
            val title = remoteMessage.data["title"] ?: "Nazaarabox"
            val body = remoteMessage.data["body"] ?: remoteMessage.data["message"] ?: "New Notification"
            val imageUrl = remoteMessage.data["image"] ?: remoteMessage.data["image_url"]
            Log.d(TAG, "Handling data-only message: title=$title, body=$body")
            sendNotification(body, title, imageUrl, remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
    }

    private fun sendNotification(messageBody: String, messageTitle: String, imageUrl: String? = null, data: Map<String, String> = emptyMap()) {
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            val route = buildRouteFromData(data)
            if (route != null) {
                intent.putExtra("route", route)
            } else {
                intent.putExtra("route", com.job2day.nazaarabox.routes.AppRoutes.HOME)
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val channelId = getString(R.string.default_notification_channel_id)
            Log.d(TAG, "Building notification with Channel ID: $channelId")
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.app_icon)
            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.app_icon)
                .setLargeIcon(largeIcon)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            if (imageUrl != null && imageUrl.isNotEmpty()) {
                try {
                    Log.d(TAG, "Loading image from URL: $imageUrl")
                    val url = java.net.URL(imageUrl)
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val input = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(input)
                    notificationBuilder.setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null as android.graphics.Bitmap?)
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load image: ${e.message}")
                    e.printStackTrace()
                }
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "Ensuring notification channel exists")
                val channel = NotificationChannel(
                    channelId,
                    getString(R.string.default_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Default channel for app notifications"
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notificationId = System.currentTimeMillis().toInt()
            Log.d(TAG, "Notifying with ID: $notificationId")
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d(TAG, "Notification sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    private fun buildRouteFromData(data: Map<String, String>): String? {
        val screen = data["screen"] ?: data["navigate"]
        val dramaSlug = data["drama_slug"] ?: data["dramaSlug"]
        return when {
            screen == "home" -> com.job2day.nazaarabox.routes.AppRoutes.HOME
            screen == "search" -> com.job2day.nazaarabox.routes.AppRoutes.SEARCH
            else -> com.job2day.nazaarabox.routes.AppRoutes.HOME
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
