package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Board
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var boardsRecyclerView: RecyclerView
    private lateinit var boardAdapter: BoardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        fabAddTask = findViewById(R.id.fab_add_task)
        boardsRecyclerView = findViewById(R.id.rv_boards)
        boardsRecyclerView.layoutManager = LinearLayoutManager(this)

        setupBottomNavigation()

        fabAddTask.setOnClickListener {
            if (auth.currentUser != null) {
                DialogFabOptions().show(supportFragmentManager, "FabOptionsDialog")
            } else {
                Toast.makeText(this, "Please log in to access features.", Toast.LENGTH_SHORT).show()
            }
        }

        loadBoards()
    }

    fun refreshData() {
        Toast.makeText(this, "Board list refreshed!", Toast.LENGTH_SHORT).show()
        loadBoards()
    }

    private fun loadBoards() {
        val userId = auth.currentUser?.uid ?: return

        db.child("boards").orderByChild("members/$userId").equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val boards = snapshot.children.mapNotNull { it.getValue(Board::class.java) }.reversed()
                    boardAdapter = BoardAdapter(boards) { board ->
                        val intent = Intent(this@HomeActivity, TaskListActivity::class.java)
                        intent.putExtra("BOARD_ID", board.id)
                        intent.putExtra("BOARD_NAME", board.name)
                        startActivity(intent)
                    }
                    boardsRecyclerView.adapter = boardAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("HomeActivity", "Failed to load boards.", error.toException())
                }
            })
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

        btnHome.setOnClickListener { /* Already on home */ }
        btnMyWork.setOnClickListener { navigateTo(MyWorkActivity::class.java) }
        btnNotifications.setOnClickListener { navigateTo(NotificationsActivity::class.java) }
        btnMore.setOnClickListener { navigateTo(MoreActivity::class.java) }
    }
}