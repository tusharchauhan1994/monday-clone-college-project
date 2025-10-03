package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mondaycloneapp.models.Board // Import the new Board blueprint

class HomeActivity : AppCompatActivity() {

    // 1. Initialize Firebase tools outside of onCreate so they can be used anywhere
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var fabAddTask: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Find the Floating Action Button (FAB)
        fabAddTask = findViewById(R.id.fab_add_task)

        // Set up the four buttons at the bottom (Home, My work, Notifications, More)
        setupBottomNavigation()

        // 2. FAB Click Logic: Show the new options menu
        fabAddTask.setOnClickListener {
            // Check if the user is actually signed into Firebase
            if (auth.currentUser != null) {
                // Display the custom menu (Add item / Add Board) using the file we created (DialogFabOptions)
                DialogFabOptions().show(supportFragmentManager, "FabOptionsDialog")
            } else {
                Toast.makeText(this, "Please log in to access features.", Toast.LENGTH_SHORT).show()
            }
        }

        // Start loading the list of boards as soon as the screen opens
        loadRecentlyVisitedBoards()
    }

    /**
     * This function is publicly available, allowing the dialogs (like DialogFabOptions)
     * to tell the Home screen to reload its data after a Board or Item is created.
     */
    fun refreshData() {
        Toast.makeText(this, "Board list refreshed!", Toast.LENGTH_SHORT).show()
        loadRecentlyVisitedBoards() // Call the loading function again
    }

    /**
     * Connects to Firebase Firestore to retrieve the user's recent Boards in real-time.
     */
    private fun loadRecentlyVisitedBoards() {
        // If no user is logged in (e.g., they haven't finished sign-in), stop here.
        val userId = auth.currentUser?.uid ?: return

        // 3. Set up a real-time listener to get the top 5 most recently updated Boards
        db.collection("users").document(userId)
            .collection("boards")
            // Sort to make the most recently updated board show up first
            .orderBy("lastVisitedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(5) // Just showing the top few, like the real app
            .addSnapshotListener { snapshot, e ->
                // Check if there was an error loading the data
                if (e != null) {
                    Log.w("HomeActivity", "Failed to load recent boards.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Convert the data from the database into our Kotlin Board blueprint (Data Class)
                    val boards = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Board::class.java)
                    }
                    Log.d("HomeActivity", "Loaded ${boards.size} recent boards.")

                    // TODO: The next major step will be to display these 'boards' using a RecyclerView
                    // in the section called "Recently visited" in activity_home.xml.
                }
            }
    }


    /**
     * Helper function to handle the bottom navigation between different screens.
     */
    private fun setupBottomNavigation() {
        val btnHome: Button = findViewById(R.id.btn_nav_home)
        val btnMyWork: Button = findViewById(R.id.btn_nav_my_work)
        val btnNotifications: Button = findViewById(R.id.btn_nav_notifications)
        val btnMore: Button = findViewById(R.id.btn_nav_more)

        fun navigateTo(targetActivity: Class<*>) {
            val intent = Intent(this, targetActivity).apply {
                // These flags prevent creating too many screens on top of each other
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        btnHome.setOnClickListener { /* Already on home */ }
        btnMyWork.setOnClickListener { navigateTo(MyWorkActivity::class.java) }
        btnNotifications.setOnClickListener { navigateTo(NotificationsActivity::class.java) }
        btnMore.setOnClickListener { navigateTo(MoreActivity::class.java) }
    }
}
