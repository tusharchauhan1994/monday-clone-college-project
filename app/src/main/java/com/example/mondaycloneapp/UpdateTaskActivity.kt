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
import com.example.mondaycloneapp.models.Board
import com.example.mondaycloneapp.models.Item
import com.example.mondaycloneapp.models.Notification
import com.example.mondaycloneapp.models.PriorityOptions
import com.example.mondaycloneapp.models.StatusOptions
import com.example.mondaycloneapp.models.User
import com.example.mondaycloneapp.utils.NotificationManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UpdateTaskActivity : AppCompatActivity() {

    private lateinit var item: Item

    private lateinit var updateItemName: EditText
    private lateinit var updateAssignee: AutoCompleteTextView
    private lateinit var updateStatus: Spinner
    private lateinit var updatePriority: Spinner
    private lateinit var updateDueDate: TextView
    private lateinit var saveItemButton: Button
    private lateinit var deleteItemButton: Button

    private val users = mutableListOf<User>()
    private val userEmails = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_task)

        item = intent.getParcelableExtra<Item>("item")!!

        updateItemName = findViewById(R.id.update_item_name)
        updateAssignee = findViewById(R.id.update_assignee)
        updateStatus = findViewById(R.id.update_status)
        updatePriority = findViewById(R.id.update_priority)
        updateDueDate = findViewById(R.id.update_due_date)
        saveItemButton = findViewById(R.id.save_item_button)
        deleteItemButton = findViewById(R.id.delete_item_button)

        updateItemName.setText(item.name)
        updateDueDate.text = item.dueDate

        val statusAdapter = StatusAdapter(this, StatusOptions.ALL_STATUSES.toTypedArray())
        updateStatus.adapter = statusAdapter
        val statusPosition = StatusOptions.ALL_STATUSES.indexOf(item.status)
        if (statusPosition != -1) {
            updateStatus.setSelection(statusPosition)
        }

        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, PriorityOptions.ALL_PRIORITIES)
        updatePriority.adapter = priorityAdapter
        val priorityPosition = PriorityOptions.ALL_PRIORITIES.indexOf(item.priority)
        if (priorityPosition != -1) {
            updatePriority.setSelection(priorityPosition)
        }

        fetchUsers()

        updateDueDate.setOnClickListener {
            showDatePickerDialog()
        }

        saveItemButton.setOnClickListener {
            updateItemInDatabase()
        }

        deleteItemButton.setOnClickListener {
            deleteItem()
        }
    }

    private fun fetchUsers() {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                userEmails.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        val userWithId = user.copy(id = userSnapshot.key!!)
                        users.add(userWithId)
                        userEmails.add(userWithId.email)
                    }
                }
                val adapter = ArrayAdapter(this@UpdateTaskActivity, android.R.layout.simple_dropdown_item_1line, userEmails)
                updateAssignee.setAdapter(adapter)

                item.assignee?.let { assigneeId ->
                    val assignedUser = users.find { it.id == assigneeId }
                    assignedUser?.let {
                        updateAssignee.setText(it.email, false)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UpdateTaskActivity, "Failed to load users", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, {
            _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            updateDueDate.text = dateFormat.format(selectedCalendar.time)
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun updateItemInDatabase() {
        val assignedUserEmail = updateAssignee.text.toString()
        val assignedUser = users.find { it.email == assignedUserEmail }
        val originalAssigneeId = item.assignee

        val updatedItem = item.copy(
            name = updateItemName.text.toString(),
            status = updateStatus.selectedItem.toString(),
            priority = updatePriority.selectedItem.toString(),
            assignee = assignedUser?.id,
            dueDate = updateDueDate.text.toString()
        )

        FirebaseDatabase.getInstance().getReference("items")
            .child(item.id)
            .setValue(updatedItem)
            .addOnSuccessListener {
                Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()

                val boardRef = FirebaseDatabase.getInstance().getReference("boards").child(updatedItem.boardId)
                boardRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val board = snapshot.getValue(Board::class.java)

                        if (board == null) {
                            finish()
                            return
                        }

                        if (assignedUser != null) {
                            addMemberToBoard(assignedUser.id)

                            val (notificationTitle, notificationMessage) = if (assignedUser.id != originalAssigneeId) {
                                "You were assigned a new task" to "You have been assigned to '${updatedItem.name}' on board '${board.name}'."
                            } else {
                                "Task Updated" to "The task '${updatedItem.name}' on board '${board.name}' has been updated."
                            }

                            val assigneeNotification = Notification(
                                userId = assignedUser.id,
                                title = notificationTitle,
                                message = notificationMessage,
                                type = if (assignedUser.id != originalAssigneeId) "task_assignment" else "task_update",
                                itemId = updatedItem.id,
                                boardId = updatedItem.boardId
                            )
                            NotificationManager.createNotification(assigneeNotification)
                        }

                        if (board.ownerId.isNotEmpty() && board.ownerId != assignedUser?.id) {
                            val ownerNotification = Notification(
                                userId = board.ownerId,
                                title = "Task Updated on Your Board",
                                message = "The task '${updatedItem.name}' on board '${board.name}' was updated.",
                                type = "task_update",
                                itemId = updatedItem.id,
                                boardId = updatedItem.boardId
                            )
                            NotificationManager.createNotification(ownerNotification)
                        }

                        finish()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        finish()
                    }
                })
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addMemberToBoard(userId: String) {
        val boardRef = FirebaseDatabase.getInstance().getReference("boards").child(item.boardId)
        boardRef.child("members").child(userId).setValue(true)
    }

    private fun deleteItem() {
        FirebaseDatabase.getInstance().getReference("items")
            .child(item.id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show()
            }
    }
}
