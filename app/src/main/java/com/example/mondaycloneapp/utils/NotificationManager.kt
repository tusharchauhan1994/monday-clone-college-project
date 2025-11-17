package com.example.mondaycloneapp.utils

import com.example.mondaycloneapp.models.Notification
import com.google.firebase.database.FirebaseDatabase

object NotificationManager {

    private val db = FirebaseDatabase.getInstance().reference.child("notifications")

    fun createNotification(notification: Notification) {
        // Create a unique ID for the notification
        val notificationId = db.child(notification.userId).push().key ?: return

        val notificationWithId = notification.copy(id = notificationId)

        db.child(notification.userId).child(notificationId).setValue(notificationWithId)
    }
}
