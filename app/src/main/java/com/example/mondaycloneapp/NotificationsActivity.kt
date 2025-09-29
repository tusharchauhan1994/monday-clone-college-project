package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class NotificationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications) // Links to your notifications XML

        val btnHome: Button = findViewById(R.id.btn_nav_home)
        val btnMyWork: Button = findViewById(R.id.btn_nav_my_work)
        val btnNotifications: Button = findViewById(R.id.btn_nav_notifications)
        val btnMore: Button = findViewById(R.id.btn_nav_more)

        // Navigation Function for stability
        fun navigateTo(targetActivity: Class<*>) {
            val intent = Intent(this, targetActivity).apply {
                // These flags prevent stacking and looping
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        btnHome.setOnClickListener { navigateTo(HomeActivity::class.java) }
        btnMyWork.setOnClickListener { navigateTo(MyWorkActivity::class.java) }

        // Notifications Button (ACTIVE: Does nothing, stays here)
        btnNotifications.setOnClickListener { /* Already on Notifications, do nothing */ }

        btnMore.setOnClickListener { navigateTo(MoreActivity::class.java) }
    }
}