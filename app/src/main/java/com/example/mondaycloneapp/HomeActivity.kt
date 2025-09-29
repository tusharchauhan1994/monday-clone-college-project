package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnHome: Button = findViewById(R.id.btn_nav_home)
        val btnMyWork: Button = findViewById(R.id.btn_nav_my_work)
        val btnNotifications: Button = findViewById(R.id.btn_nav_notifications)
        val btnMore: Button = findViewById(R.id.btn_nav_more)

        // Home Button (ACTIVE: Does nothing)
        btnHome.setOnClickListener { /* Already on home, do nothing */ }

        // Navigation Function
        fun navigateTo(targetActivity: Class<*>) {
            val intent = Intent(this, targetActivity).apply {
                // These flags prevent stacking and looping
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        btnMyWork.setOnClickListener { navigateTo(MyWorkActivity::class.java) }
        btnNotifications.setOnClickListener { navigateTo(NotificationsActivity::class.java) }
        btnMore.setOnClickListener { navigateTo(MoreActivity::class.java) }
    }
}
