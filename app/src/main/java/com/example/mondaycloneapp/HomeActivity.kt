package com.example.mondaycloneapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Board
import com.example.mondaycloneapp.models.Item
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private val db = FirebaseDatabase.getInstance().reference
    private lateinit var auth: FirebaseAuth
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var tasksChildListener: ChildEventListener? = null
    private val tasksRef = db.child("tasks")
    private var isInitialDataLoaded = false

    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var boardsRecyclerView: RecyclerView
    private lateinit var boardAdapter: BoardAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        fabAddTask = findViewById(R.id.fab_add_task)
        boardsRecyclerView = findViewById(R.id.rv_boards)
        boardsRecyclerView.layoutManager = LinearLayoutManager(this)

        setupBottomNavigation()
        setupAuthStateListener()
        askNotificationPermission()

        fabAddTask.setOnClickListener {
            if (auth.currentUser != null) {
                DialogFabOptions().show(supportFragmentManager, "FabOptionsDialog")
            } else {
                Toast.makeText(this, "Please log in to access features.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        authStateListener?.let { auth.addAuthStateListener(it) }
    }

    override fun onStop() {
        super.onStop()
        authStateListener?.let { auth.removeAuthStateListener(it) }
        tasksChildListener?.let { tasksRef.removeEventListener(it) } // Clean up the listener
    }

    private fun setupAuthStateListener() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.d("NotificationDebug", "Auth state confirmed. User ID: ${user.uid}")
                loadBoards(user.uid)
                listenForTaskAssignments(user.uid)
            } else {
                Log.d("NotificationDebug", "User is signed out.")
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun refreshData() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            loadBoards(uid)
            Toast.makeText(this, "Board list refreshed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listenForTaskAssignments(userId: String) {
        tasksChildListener?.let { tasksRef.removeEventListener(it) }
        isInitialDataLoaded = false

        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isInitialDataLoaded = true
                Log.d("NotificationDebug", "Initial data load complete. Ready for new assignments.")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationDebug", "Initial data load failed.", error.toException())
            }
        })

        tasksChildListener = tasksRef.addChildEventListener(object : ChildEventListener {
            private fun checkAssignment(snapshot: DataSnapshot, eventType: String) {
                val task = snapshot.getValue(Item::class.java)
                if (task == null) {
                    Log.w("NotificationDebug", "$eventType: Received task data is null for key ${snapshot.key}")
                    return
                }

                val assigneeId = task.assignee
                Log.d("NotificationDebug", "$eventType: Task '${task.name}' has assignee: $assigneeId")

                if (assigneeId == userId) {
                    Log.i("NotificationDebug", "SUCCESS: Match found! Showing notification for task '${task.name}'.")
                    showNotification("Task Assigned to You", "You have been assigned the task: ${task.name}")
                } else {
                    Log.d("NotificationDebug", "No match. Current user is '$userId' but assignee is '$assigneeId'.")
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (!isInitialDataLoaded) return // Ignore the initial data dump at startup
                checkAssignment(snapshot, "onChildAdded (NEW TASK)")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                 if (!isInitialDataLoaded) return
                checkAssignment(snapshot, "onChildChanged (UPDATED TASK)")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationDebug", "Failed to listen for task assignments.", error.toException())
            }
        })
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "task_assignment_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Task Assignments",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun loadBoards(userId: String) {
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
        db.child("tasks").orderByChild("boardId").equalTo(boardId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (taskSnapshot in snapshot.children) {
                    taskSnapshot.ref.removeValue()
                }
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