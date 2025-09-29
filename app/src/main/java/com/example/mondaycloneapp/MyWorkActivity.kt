package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MyWorkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_work)

        // 1. FAB Logic (for starting the CRUD process)
        val fabAddTask: FloatingActionButton = findViewById(R.id.fab_add_task)
        fabAddTask.setOnClickListener {
            // Opens the screen where the user enters the new task
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }

        // 2. Bottom Navigation Logic
        // NOTE: This assumes you added IDs (btn_nav_home, etc.) to your XML buttons.
        val btnHome: Button = findViewById(R.id.btn_nav_home)
        val btnMyWork: Button = findViewById(R.id.btn_nav_my_work) // Current screen
        val btnNotifications: Button = findViewById(R.id.btn_nav_notifications)
        val btnMore: Button = findViewById(R.id.btn_nav_more)

        // Home Button
        btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Prevents screens from stacking up
        }

        // My Work Button (Current screen, does nothing or refreshes)
        btnMyWork.setOnClickListener {
            // Already here, no action needed
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
    }
}
