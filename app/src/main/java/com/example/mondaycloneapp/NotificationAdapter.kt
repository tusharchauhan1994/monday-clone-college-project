package com.example.mondaycloneapp

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Notification
import com.example.mondaycloneapp.models.NotificationType

class NotificationAdapter(private val notifications: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = notifications.size

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.notification_title)
        private val messageTextView: TextView = itemView.findViewById(R.id.notification_message)
        private val timestampTextView: TextView = itemView.findViewById(R.id.notification_timestamp)
        private val iconImageView: ImageView = itemView.findViewById(R.id.notification_icon)

        fun bind(notification: Notification) {
            titleTextView.text = notification.title
            messageTextView.text = notification.message

            val timeAgo = DateUtils.getRelativeTimeSpanString(
                notification.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
            timestampTextView.text = timeAgo

            val iconRes = when (notification.type) {
                NotificationType.TASK_ASSIGNMENT -> android.R.drawable.ic_menu_myplaces
                NotificationType.STATUS_CHANGE -> android.R.drawable.ic_menu_rotate
                NotificationType.DUE_DATE_UPDATE -> android.R.drawable.ic_menu_my_calendar
                NotificationType.PRIORITY_CHANGE -> android.R.drawable.ic_menu_sort_by_size
                NotificationType.TASK_COMPLETED -> android.R.drawable.ic_menu_agenda
                else -> android.R.drawable.ic_menu_info_details
            }
            iconImageView.setImageResource(iconRes)
        }
    }
}