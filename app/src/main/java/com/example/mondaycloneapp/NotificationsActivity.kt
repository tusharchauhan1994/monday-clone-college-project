package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class NotificationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val btnHome: LinearLayout = findViewById(R.id.btn_nav_home)
        val btnMyWork: LinearLayout = findViewById(R.id.btn_nav_my_work)
        val btnNotifications: LinearLayout = findViewById(R.id.btn_nav_notifications)
        val btnMore: LinearLayout = findViewById(R.id.btn_nav_more)

        fun navigateTo(targetActivity: Class<*>) {
            val intent = Intent(this, targetActivity).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        btnHome.setOnClickListener { navigateTo(HomeActivity::class.java) }
        btnMyWork.setOnClickListener { navigateTo(MyWorkActivity::class.java) }

        btnNotifications.setOnClickListener { /* Already on Notifications, do nothing */ }

        btnMore.setOnClickListener { navigateTo(MoreActivity::class.java) }
    }
}