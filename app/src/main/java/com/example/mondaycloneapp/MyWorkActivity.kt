package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Item
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyWorkActivity : AppCompatActivity() {

    private lateinit var rvItems: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_work)

        // 1. FAB Logic - Now it shows our modern dialog
        val fabAddTask: FloatingActionButton = findViewById(R.id.fab_add_task)
        fabAddTask.setOnClickListener {
            DialogAddItem().show(supportFragmentManager, "AddItemDialog")
        }

        // 2. Set up the RecyclerView
        rvItems = findViewById(R.id.rv_my_work_items)
        rvItems.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with an empty list so the app doesn't crash
        itemAdapter = ItemAdapter(emptyList())
        rvItems.adapter = itemAdapter

        // 3. Load the items from Firestore
        loadItems()

        // 4. Set up Bottom Navigation
        setupBottomNavigation()
    }

    private fun loadItems() {
        val userId = auth.currentUser?.uid ?: return

        // This is a special "collection group" query.
        // It searches all subcollections named "items" across all boards and groups
        // for documents where the userId matches the currently logged-in user.
        db.collectionGroup("items")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toObject(Item::class.java) }
                    Log.d("MyWorkActivity", "Successfully loaded ${items.size} items.")

                    // Update the adapter with the new list of items and refresh the list
                    itemAdapter = ItemAdapter(items)
                    rvItems.adapter = itemAdapter
                }
            }
            .addOnFailureListener { e ->
                Log.e("MyWorkActivity", "Error loading items", e)
            }
    }

    private fun setupBottomNavigation() {
        val btnHome: Button = findViewById(R.id.btn_nav_home)
        val btnMyWork: Button = findViewById(R.id.btn_nav_my_work)
        val btnNotifications: Button = findViewById(R.id.btn_nav_notifications)
        val btnMore: Button = findViewById(R.id.btn_nav_more)

        fun navigateTo(targetActivity: Class<*>) {
            val intent = Intent(this, targetActivity).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        btnHome.setOnClickListener { navigateTo(HomeActivity::class.java) }
        btnMyWork.setOnClickListener { /* Already here */ }
        btnNotifications.setOnClickListener { navigateTo(NotificationsActivity::class.java) }
        btnMore.setOnClickListener { navigateTo(MoreActivity::class.java) }
    }
}