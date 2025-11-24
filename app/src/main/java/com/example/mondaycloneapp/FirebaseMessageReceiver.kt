package com.example.mondaycloneapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessageReceiver : FirebaseMessagingService() {

    private val CHANNEL_ID = "fcm_default_channel"
    private val NOTIFICATION_ID = 0

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here
        if (remoteMessage.notification != null) {
            // Show the notification using NotificationManager
            showNotification(
                remoteMessage.notification?.title,
                remoteMessage.notification?.body
            )
        }
    }

    private fun showNotification(title: String?, message: String?) {
        // Step 4 (Channel Creation) is integrated here for reliability on Android O+
        createNotificationChannel() 

        val notificationManager = 
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            // Replace R.drawable.ic_launcher_background with your small notification icon
            .setSmallIcon(R.drawable.ic_launcher_background) 
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
    
    // Logic from faculty PDF Step 4 (Channel creation)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "firebase Notifications"
            val descriptionText = "Channel for firebase notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH // Using HIGH for immediate visibility
            
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM_TOKEN", "New token: $token")
        // Implementation to send token to your server goes here.
    }
}
