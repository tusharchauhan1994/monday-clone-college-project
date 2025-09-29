package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Get references to all 4 buttons in the bottom navigation
        val btnHome: Button = findViewById(R.id.btn_nav_home)
        val btnMyWork: Button = findViewById(R.id.btn_nav_my_work)
        val btnNotifications: Button = findViewById(R.id.btn_nav_notifications)
        val btnMore: Button = findViewById(R.id.btn_nav_more)

        // --- Navigation Logic ---

        // My Work Button
        btnMyWork.setOnClickListener {
            val intent = Intent(this, MyWorkActivity::class.java)
            startActivity(intent)
            finish() // Optional: Closes current screen to prevent stack buildup
        }

        // Notifications Button
        btnNotifications.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
            finish()
        }

        // More Button
        btnMore.setOnClickListener {
            val intent = Intent(this, MoreActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Home Button (Since we are in HomeActivity, this button does nothing or refreshes)
        btnHome.setOnClickListener {
            // Do nothing, or you could add a scroll-to-top feature here.
        }
    }
}