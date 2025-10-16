package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mondaycloneapp.models.Item
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyWorkActivity : AppCompatActivity(), ItemAdapter.OnItemClickListener { // Implement the listener

    private lateinit var rvItems: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private var itemsList = mutableListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_work)

        // 1. FAB Logic - Now it shows our modern dialog
        val fabAddTask: FloatingActionButton = findViewById(R.id.fab_add_task)
        fabAddTask.setOnClickListener {
            DialogAddItem().show(supportFragmentManager, "AddItemDialog")
        }

        // 2. Set up the RecyclerView
        rvItems = findViewById(R.id.rv_my_work_items)
        rvItems.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with an empty list and the listener
        itemAdapter = ItemAdapter(itemsList, this)
        rvItems.adapter = itemAdapter

        // 3. Load the items from Realtime Database
        loadItems()

        // 4. Set up Bottom Navigation
        setupBottomNavigation()
    }

    private fun loadItems() {
        val userId = auth.currentUser?.uid ?: return

        val userRef = db.child("users").child(userId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemsList.clear()
                val boardsSnapshot = snapshot.child("boards")
                for (boardSnapshot in boardsSnapshot.children) {
                    val groupsSnapshot = boardSnapshot.child("groups")
                    for (groupSnapshot in groupsSnapshot.children) {
                        val itemsSnapshot = groupSnapshot.child("items")
                        for (itemSnapshot in itemsSnapshot.children) {
                            val item = itemSnapshot.getValue(Item::class.java)
                            if (item != null) {
                                itemsList.add(item)
                            }
                        }
                    }
                }
                itemAdapter.notifyDataSetChanged()
                Log.d("MyWorkActivity", "Successfully loaded ${itemsList.size} items.")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MyWorkActivity", "Error loading items", error.toException())
            }
        })
    }

    // Function to show a dialog for updating an item's name
    private fun showUpdateDialog(item: Item) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update Item Name")

        val input = EditText(this)
        input.setText(item.name)
        builder.setView(input)

        builder.setPositiveButton("Update") { dialog, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateItemName(item, newName)
            } else {
                Toast.makeText(this, "Item name cannot be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    // Function to update the item's name in Realtime Database
    private fun updateItemName(item: Item, newName: String) {
        val itemRef = db.child("users").child(item.userId).child("boards").child(item.boardId).child("groups").child(item.groupId).child("items").child(item.id)

        itemRef.child("name").setValue(newName)
            .addOnSuccessListener {
                Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating item: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("MyWorkActivity", "Error updating item", e)
            }
    }

    // Function to delete an item from Realtime Database
    private fun deleteItem(item: Item) {
        val itemRef = db.child("users").child(item.userId).child("boards").child(item.boardId).child("groups").child(item.groupId).child("items").child(item.id)

        itemRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting item: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("MyWorkActivity", "Error deleting item", e)
            }
    }

    // Handle item click for updating
    override fun onItemClick(item: Item) {
        showUpdateDialog(item)
    }

    // Handle item long click for deleting
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


    private fun setupBottomNavigation() {
        val btnHome: Button = findViewById(R.id.btn_nav_home)
        val btnMyWork: Button = findViewById(R.id.btn_nav_my_work)
        val btnNotifications: Button = findViewById(R.id.btn_nav_notifications)
        val btnMore: Button = findViewById(R.id.btn_nav_more)

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