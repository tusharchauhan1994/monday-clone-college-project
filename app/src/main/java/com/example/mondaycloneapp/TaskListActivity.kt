package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Group
import com.example.mondaycloneapp.models.Item
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TaskListActivity : AppCompatActivity(), TaskAdapter.OnItemClickListener {

    private lateinit var boardId: String
    private lateinit var boardName: String
    private lateinit var tasksRecyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    private lateinit var fabAddTask: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        boardId = intent.getStringExtra("BOARD_ID")!!
        boardName = intent.getStringExtra("BOARD_NAME")!!

        val boardNameHeader: TextView = findViewById(R.id.board_name_header)
        boardNameHeader.text = boardName

        tasksRecyclerView = findViewById(R.id.rv_tasks)
        tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(emptyList(), this)
        tasksRecyclerView.adapter = taskAdapter

        fabAddTask = findViewById(R.id.fab_add_task)

        setupBottomNavigation()

        fabAddTask.setOnClickListener {
            if (auth.currentUser != null) {
                DialogFabOptions().show(supportFragmentManager, "FabOptionsDialog")
            } else {
                Toast.makeText(this, "Please log in to access features.", Toast.LENGTH_SHORT).show()
            }
        }

        loadGroupsAndTasks()
    }

    private fun loadGroupsAndTasks() {
        db.child("groups").orderByChild("boardId").equalTo(boardId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(groupSnapshot: DataSnapshot) {
                    val groups = groupSnapshot.children.mapNotNull { it.getValue(Group::class.java) }

                    db.child("items").orderByChild("boardId").equalTo(boardId)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(itemSnapshot: DataSnapshot) {
                                val tasks = itemSnapshot.children.mapNotNull { it.getValue(Item::class.java) }
                                updateRecyclerView(groups, tasks)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun updateRecyclerView(groups: List<Group>, tasks: List<Item>) {
        val sortedGroups = groups.sortedBy { it.orderIndex }
        val tasksByGroup = tasks.groupBy { it.groupId }
        val listItems = mutableListOf<ListItem>()

        for (group in sortedGroups) {
            listItems.add(ListItem.GroupHeader("Tasks"))
            tasksByGroup[group.id]?.let { groupTasks ->
                listItems.addAll(groupTasks.map { ListItem.TaskItem(it) })
            }
        }

        taskAdapter.updateTasks(listItems)
    }

    override fun onItemLongClick(item: Item) {
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Delete") { _, _ ->
                deleteItem(item)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteItem(item: Item) {
        db.child("items").child(item.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show()
            }
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
        btnNotifications.setOnClickListener { navigateTo(NotificationsActivity::class.java) }
        btnMore.setOnClickListener { navigateTo(MoreActivity::class.java) }
    }
}