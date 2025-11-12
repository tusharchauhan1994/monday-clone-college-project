package com.example.mondaycloneapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Item
import com.google.firebase.database.*

class TaskListActivity : AppCompatActivity() {

    private lateinit var boardId: String
    private lateinit var boardName: String
    private lateinit var tasksRecyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        boardId = intent.getStringExtra("BOARD_ID")!!
        boardName = intent.getStringExtra("BOARD_NAME")!!

        val boardNameHeader: TextView = findViewById(R.id.board_name_header)
        boardNameHeader.text = boardName

        tasksRecyclerView = findViewById(R.id.rv_tasks)
        tasksRecyclerView.layoutManager = LinearLayoutManager(this)

        loadTasks()
    }

    private fun loadTasks() {
        db.child("items").orderByChild("boardId").equalTo(boardId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = snapshot.children.mapNotNull { it.getValue(Item::class.java) }
                    taskAdapter = TaskAdapter(tasks)
                    tasksRecyclerView.adapter = taskAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }
}