package com.example.mondaycloneapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Group
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

        taskAdapter = TaskAdapter(listItems)
        tasksRecyclerView.adapter = taskAdapter
    }
}