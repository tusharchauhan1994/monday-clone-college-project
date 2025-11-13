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
import com.google.firebase.database.FirebaseDatabase
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

        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, StatusOptions.ALL_STATUSES)
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

        updateDueDate.setOnClickListener {
            showDatePickerDialog()
        }

        saveItemButton.setOnClickListener {
            updateItemInDatabase()
        }

        deleteItemButton.setOnClickListener {
            deleteItem()
        }

        // TODO: Populate assignee AutoCompleteTextView with users from Firebase
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
        val updatedItem = item.copy(
            name = updateItemName.text.toString(),
            status = updateStatus.selectedItem.toString(),
            priority = updatePriority.selectedItem.toString(),
            dueDate = updateDueDate.text.toString()
        )

        FirebaseDatabase.getInstance().getReference("items")
            .child(item.id)
            .setValue(updatedItem)
            .addOnSuccessListener {
                Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show()
            }
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