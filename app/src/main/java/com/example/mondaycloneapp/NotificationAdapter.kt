package com.example.mondaycloneapp

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Notification

class NotificationAdapter(private val notifications: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount() = notifications.size

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.notification_title)
        private val messageTextView: TextView = itemView.findViewById(R.id.notification_message)
        private val timeTextView: TextView = itemView.findViewById(R.id.notification_time)
        private val iconImageView: ImageView = itemView.findViewById(R.id.notification_icon)

        fun bind(notification: Notification) {
            titleTextView.text = notification.title
            messageTextView.text = notification.message
            timeTextView.text = getRelativeTime(notification.createdAt)
            // Here you can set the icon based on the notification type
            // For now, we'll use a default icon
            // iconImageView.setImageResource(R.drawable.ic_notification)
        }

        private fun getRelativeTime(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val relativeTime = DateUtils.getRelativeTimeSpanString(
                timestamp,
                now,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            )
            return relativeTime.toString()
        }
    }
}
