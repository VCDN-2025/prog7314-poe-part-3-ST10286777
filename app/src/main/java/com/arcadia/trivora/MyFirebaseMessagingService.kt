package com.arcadia.trivora

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCMService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data: ${remoteMessage.data}")

            // Handle different notification types
            val title = remoteMessage.data["title"] ?: "Trivora"
            val body = remoteMessage.data["message"] ?: remoteMessage.data["body"] ?: ""
            val type = remoteMessage.data["type"] ?: "general"

            showNotification(title, body, type)
        }

        // Check if message contains notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message Notification: ${notification.body}")
            showNotification(
                notification.title ?: "Trivora",
                notification.body ?: "",
                "notification"
            )
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token: $token")
        saveFCMToken(token)
    }

    private fun saveFCMToken(token: String) {
        // Save to SharedPrefs
        SharedPrefs.saveFCMToken(token)

        // Send to backend if user is logged in
        sendTokenToBackend(token)
    }

    private fun sendTokenToBackend(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.updateFCMToken(UpdateFCMTokenRequest(token))

                if (response.isSuccessful) {
                    Log.d(TAG, "FCM token sent to backend successfully")
                } else {
                    Log.e(TAG, "Failed to send FCM token: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error sending FCM token", e)
            }
        }
    }

    private fun showNotification(title: String, body: String, type: String) {
        // Create intent to open MainActivity when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("notification_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "trivora_notifications"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Create notification channel (required for Android 8.0+)
        createNotificationChannel(channelId)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.trivora_logo) // You'll need to create this
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())

        Log.d(TAG, "Notification shown: $title - $body")
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Trivora Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for daily reminders and quiz updates"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}