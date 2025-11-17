package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationsActivity : AppCompatActivity() {

    private lateinit var notificationsRecyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private val notifications = mutableListOf<Notification>()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        notificationsRecyclerView = findViewById(R.id.notifications_recycler_view)
        notificationsRecyclerView.layoutManager = LinearLayoutManager(this)
        notificationAdapter = NotificationAdapter(notifications)
        notificationsRecyclerView.adapter = notificationAdapter

        loadNotifications()
        setupBottomNavigation()
    }

    private fun loadNotifications() {
        val userId = auth.currentUser?.uid ?: return

        db.child("notifications").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notifications.clear()
                for (notificationSnapshot in snapshot.children) {
                    val notification = notificationSnapshot.getValue(Notification::class.java)
                    notification?.let { notifications.add(it) }
                }
                notifications.reverse()
                notificationAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun setupBottomNavigation() {
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
        btnNotifications.setOnClickListener { /* Already on this screen */ }
        btnMore.setOnClickListener { navigateTo(MoreActivity::class.java) }
    }
}
