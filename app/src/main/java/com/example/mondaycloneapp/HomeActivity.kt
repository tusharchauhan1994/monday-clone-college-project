package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
                    boardAdapter = BoardAdapter(this@HomeActivity, boards, { board ->
                        val intent = Intent(this@HomeActivity, TaskListActivity::class.java)
                        intent.putExtra("BOARD_ID", board.id)
                        intent.putExtra("BOARD_NAME", board.name)
                        startActivity(intent)
                    }) { board ->
                        showBoardOptionsDialog(board)
                    }
                    boardsRecyclerView.adapter = boardAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("HomeActivity", "Failed to load boards.", error.toException())
                }
            })
    }

    private fun showBoardOptionsDialog(board: Board) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_board_options, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.tv_rename_board).setOnClickListener {
            dialog.dismiss()
            showRenameBoardDialog(board)
        }

        dialogView.findViewById<TextView>(R.id.tv_delete_board).setOnClickListener {
            dialog.dismiss()
            showDeleteBoardConfirmationDialog(board)
        }

        dialog.show()
    }

    private fun showRenameBoardDialog(board: Board) {
        val editText = EditText(this)
        editText.setText(board.name)

        AlertDialog.Builder(this)
            .setTitle("Rename Board")
            .setView(editText)
            .setPositiveButton("Rename") { _, _ ->
                val newName = editText.text.toString()
                if (newName.isNotEmpty()) {
                    renameBoard(board, newName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun renameBoard(board: Board, newName: String) {
        db.child("boards").child(board.id).child("name").setValue(newName)
    }

    private fun showDeleteBoardConfirmationDialog(board: Board) {
        AlertDialog.Builder(this)
            .setTitle("Delete Board")
            .setMessage("Are you sure you want to delete this board and all its tasks?")
            .setPositiveButton("Delete") { _, _ ->
                deleteBoard(board)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteBoard(board: Board) {
        val boardId = board.id
        // Delete all tasks associated with the board
        db.child("tasks").orderByChild("boardId").equalTo(boardId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (taskSnapshot in snapshot.children) {
                    taskSnapshot.ref.removeValue()
                }
                // Delete the board itself
                db.child("boards").child(boardId).removeValue()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeActivity", "Error deleting tasks for board", error.toException())
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

        btnHome.setOnClickListener { /* Already on home */ }
        btnMyWork.setOnClickListener { navigateTo(MyWorkActivity::class.java) }
        btnNotifications.setOnClickListener { navigateTo(NotificationsActivity::class.java) }
        btnMore.setOnClickListener { navigateTo(MoreActivity::class.java) }
    }
}