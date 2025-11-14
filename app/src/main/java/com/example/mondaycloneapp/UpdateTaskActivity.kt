package com.example.mondaycloneapp

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.mondaycloneapp.models.Item
import com.example.mondaycloneapp.models.PriorityOptions
import com.example.mondaycloneapp.models.StatusOptions
import com.example.mondaycloneapp.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

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
            val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            updateDueDate.text = selectedDate
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun updateItemInDatabase() {
        val assignedUserEmail = updateAssignee.text.toString()
        val assignedUser = users.find { it.email == assignedUserEmail }

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
                if (assignedUser != null) {
                    addMemberToBoard(assignedUser.id)
                }
                Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
                finish()
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