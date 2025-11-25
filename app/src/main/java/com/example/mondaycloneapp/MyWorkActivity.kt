package com.example.mondaycloneapp

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.mondaycloneapp.models.Board
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyWorkActivity : AppCompatActivity() {

    private lateinit var rvBoards: RecyclerView
    private lateinit var boardAdapter: BoardAdapter
    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private var boardsList = mutableListOf<Board>()
    private lateinit var lottieAnimationView: LottieAnimationView
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var boardsQuery: Query? = null
    private var boardsListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_work)

        val fabAddTask: FloatingActionButton = findViewById(R.id.fab_add_task)
        fabAddTask.setOnClickListener {
            DialogAddItem().show(supportFragmentManager, "AddItemDialog")
        }

        rvBoards = findViewById(R.id.rv_my_work_boards)
        rvBoards.layoutManager = LinearLayoutManager(this)
        lottieAnimationView = findViewById(R.id.lottie_animation_view)

        initRecyclerView()
        setupAuthStateListener()
        setupBottomNavigation()
    }

    private fun initRecyclerView() {
        boardAdapter = BoardAdapter(
            context = this@MyWorkActivity,
            boards = boardsList,
            onBoardClick = { board ->
                val intent = Intent(this@MyWorkActivity, TaskListActivity::class.java)
                intent.putExtra("BOARD_ID", board.id)
                intent.putExtra("BOARD_NAME", board.name)
                startActivity(intent)
            },
            onBoardLongClick = { board ->
                showBoardOptionsDialog(board)
            },
            onStarClick = { board ->
                auth.currentUser?.uid?.let { userId ->
                    toggleFavorite(userId, board.id)
                }
            }
        )
        rvBoards.adapter = boardAdapter
    }

    override fun onStart() {
        super.onStart()
        authStateListener?.let { auth.addAuthStateListener(it) }
    }

    override fun onStop() {
        super.onStop()
        authStateListener?.let { auth.removeAuthStateListener(it) }
        boardsListener?.let { boardsQuery?.removeEventListener(it) }
    }

    private fun setupAuthStateListener() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Toast.makeText(this, "Logged in with UID: ${user.uid}", Toast.LENGTH_LONG).show()
                loadBoards(user.uid)
            } else {
                boardsList.clear()
                boardAdapter.updateBoards(boardsList)
                boardsListener?.let { boardsQuery?.removeEventListener(it) }
            }
        }
    }

    private fun loadBoards(userId: String) {
        boardsListener?.let { boardsQuery?.removeEventListener(it) }

        boardsQuery = db.child("boards").orderByChild("members/$userId").equalTo(true)
        boardsListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    boardsList.clear()
                    for (boardSnapshot in snapshot.children) {
                        val board = boardSnapshot.getValue(Board::class.java)
                        if (board != null) {
                            boardsList.add(board)
                        }
                    }
                    if (isDestroyed || isFinishing) return
                    boardAdapter.updateBoards(boardsList)
                    Log.d("MyWorkActivity", "Successfully loaded ${boardsList.size} boards.")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MyWorkActivity", "Error loading boards", error.toException())
                }
            }
        boardsQuery?.addValueEventListener(boardsListener!!)
    }

    private fun toggleFavorite(uid: String, boardId: String) {
        Toast.makeText(this, "Toggling favorite for UID: $uid", Toast.LENGTH_LONG).show()
        val favoriteRef = db.child("users").child(uid).child("favorites").child(boardId)
        favoriteRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                favoriteRef.removeValue()
            } else {
                favoriteRef.setValue(true)
            }
        }
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

        val starView = dialogView.findViewById<TextView>(R.id.tv_favorite_board)
        auth.currentUser?.let { user ->
            db.child("users").child(user.uid).child("favorites").child(board.id).get().addOnSuccessListener {
                if(it.exists()) {
                    starView.text = "Unfavorite"
                } else {
                    starView.text = "Favorite"
                }
            }

            starView.setOnClickListener {
                dialog.dismiss()
                toggleFavorite(user.uid, board.id)
            }
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
        lottieAnimationView.visibility = View.VISIBLE
        lottieAnimationView.playAnimation()
        lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                lottieAnimationView.visibility = View.GONE
                val boardId = board.id
                db.child("tasks").orderByChild("boardId").equalTo(boardId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (taskSnapshot in snapshot.children) {
                            taskSnapshot.ref.removeValue()
                        }
                        db.child("boards").child(boardId).removeValue()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MyWorkActivity", "Error deleting tasks for board", error.toException())
                    }
                })
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
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
        btnMyWork.setOnClickListener { /* Already here */ }
        btnNotifications.setOnClickListener { navigateTo(NotificationsActivity::class.java) }
        btnMore.setOnClickListener { navigateTo(MoreActivity::class.java) }
    }
}
