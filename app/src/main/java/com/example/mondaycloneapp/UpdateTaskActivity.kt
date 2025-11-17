package com.example.mondaycloneapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mondaycloneapp.models.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UpdateTaskActivity : AppCompatActivity() {

    private lateinit var originalItem: Item

    private lateinit var updateItemName: EditText
    private lateinit var updateAssignee: AutoCompleteTextView
    private lateinit var updateStatus: Spinner
    private lateinit var updatePriority: Spinner
    private lateinit var updateDueDate: TextView
    private lateinit var saveItemButton: Button
    private lateinit var deleteItemButton: Button

    private val users = mutableListOf<User>()
    private val userEmails = mutableListOf<String>()
    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_task)

        originalItem = intent.getParcelableExtra<Item>("item")!!

        updateItemName = findViewById(R.id.update_item_name)
        updateAssignee = findViewById(R.id.update_assignee)
        updateStatus = findViewById(R.id.update_status)
        updatePriority = findViewById(R.id.update_priority)
        updateDueDate = findViewById(R.id.update_due_date)
        saveItemButton = findViewById(R.id.save_item_button)
        deleteItemButton = findViewById(R.id.delete_item_button)

        updateItemName.setText(originalItem.name)
        updateDueDate.text = originalItem.dueDate

        val statusAdapter = StatusAdapter(this, StatusOptions.ALL_STATUSES.toTypedArray())
        updateStatus.adapter = statusAdapter
        updateStatus.setSelection(StatusOptions.ALL_STATUSES.indexOf(originalItem.status))

        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, PriorityOptions.ALL_PRIORITIES)
        updatePriority.adapter = priorityAdapter
        updatePriority.setSelection(PriorityOptions.ALL_PRIORITIES.indexOf(originalItem.priority))

        fetchUsers()

        updateDueDate.setOnClickListener { showDatePickerDialog() }
        saveItemButton.setOnClickListener { updateAndNotify() }
        deleteItemButton.setOnClickListener { deleteItem() }
    }

    private fun fetchUsers() {
        db.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                userEmails.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)?.copy(id = userSnapshot.key!!)
                    user?.let { 
                        users.add(it)
                        userEmails.add(it.email)
                    }
                }
                val adapter = ArrayAdapter(this@UpdateTaskActivity, android.R.layout.simple_dropdown_item_1line, userEmails)
                updateAssignee.setAdapter(adapter)
                originalItem.assignee?.let { assigneeId ->
                    users.find { it.id == assigneeId }?.let { updateAssignee.setText(it.email, false) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UpdateTaskActivity, "Failed to load users", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateAndNotify() {
        val assignedUserEmail = updateAssignee.text.toString()
        val assignedUser = users.find { it.email == assignedUserEmail }

        val updatedItem = originalItem.copy(
            name = updateItemName.text.toString(),
            status = updateStatus.selectedItem.toString(),
            priority = updatePriority.selectedItem.toString(),
            assignee = assignedUser?.id,
            dueDate = updateDueDate.text.toString()
        )

        // Save the updated item
        db.child("tasks").child(originalItem.id).setValue(updatedItem).addOnSuccessListener {
            Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
            
            // --- Generate Notifications based on what changed ---
            generateNotifications(originalItem, updatedItem)

            // Add user to board if newly assigned
            if (assignedUser != null && originalItem.assignee != updatedItem.assignee) {
                addMemberToBoard(assignedUser.id, updatedItem.boardId)
            }

            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateNotifications(original: Item, updated: Item) {
        val assigneeId = updated.assignee ?: return

        // 1. Assignment Change
        if (original.assignee != updated.assignee) {
            createNotification(assigneeId, NotificationType.TASK_ASSIGNMENT, "You've been assigned a new task", "Task: ${updated.name}")
        }

        // 2. Status Change
        if (original.status != updated.status) {
            if (updated.status == StatusOptions.DONE) {
                 createNotification(assigneeId, NotificationType.TASK_COMPLETED, "Task Completed!", "You've completed the task: ${updated.name}")
            } else {
                 createNotification(assigneeId, NotificationType.STATUS_CHANGE, "Task Status Updated", "Status for '${updated.name}' changed from ${original.status} to ${updated.status}")
            }
        }

        // 3. Due Date Change
        if (original.dueDate != updated.dueDate) {
            createNotification(assigneeId, NotificationType.DUE_DATE_UPDATE, "Due Date Changed", "The due date for '${updated.name}' has been changed to ${updated.dueDate}")
        }

        // 4. Priority Change
        if (original.priority != updated.priority && updated.priority == PriorityOptions.HIGH) {
             createNotification(assigneeId, NotificationType.PRIORITY_CHANGE, "Priority Set to HIGH", "The priority for task '${updated.name}' is now HIGH.")
        }
    }

    private fun createNotification(userId: String, type: String, title: String, message: String) {
        val notificationsRef = db.child("notifications").child(userId)
        val notificationId = notificationsRef.push().key ?: return
        val notification = Notification(notificationId, userId, title, message, System.currentTimeMillis(), type)
        notificationsRef.child(notificationId).setValue(notification)
    }

    private fun addMemberToBoard(userId: String, boardId: String) {
        db.child("boards").child(boardId).child("members").child(userId).setValue(true)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val (year, month, day) = listOf(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        DatePickerDialog(this, { _, y, m, d ->
            val cal = Calendar.getInstance().apply { set(y, m, d) }
            updateDueDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
        }, year, month, day).show()
    }

    private fun deleteItem() {
        db.child("tasks").child(originalItem.id).removeValue().addOnSuccessListener {
            Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show()
        }
    }
}