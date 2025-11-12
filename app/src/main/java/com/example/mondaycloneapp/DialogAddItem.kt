package com.example.mondaycloneapp

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.mondaycloneapp.models.Board
import com.example.mondaycloneapp.models.Group
import com.example.mondaycloneapp.models.Item
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DialogAddItem : DialogFragment() {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    private lateinit var etItemName: EditText
    private lateinit var tvSelectedBoard: TextView
    private lateinit var tvSelectedGroup: TextView
    private lateinit var btnSave: Button

    private var boardsList: List<Board> = emptyList()
    private var groupsList: List<Group> = emptyList()

    private var selectedBoard: Board? = null
    private var selectedGroup: Group? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etItemName = view.findViewById(R.id.et_item_name)
        tvSelectedBoard = view.findViewById(R.id.tv_selected_board)
        tvSelectedGroup = view.findViewById(R.id.tv_selected_group)
        btnSave = view.findViewById(R.id.btn_add_item_save)

        btnSave.isEnabled = false

        view.findViewById<View>(R.id.btn_close).setOnClickListener { dismiss() }
        tvSelectedBoard.setOnClickListener { showBoardSelector() }
        tvSelectedGroup.setOnClickListener { showGroupSelector() }
        btnSave.setOnClickListener { checkAndSaveNewItem() }

        loadBoards()
    }

    private fun loadBoards() {
        val uid = userId ?: return

        db.child("boards").orderByChild("members/$uid").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    boardsList = snapshot.children.mapNotNull { it.getValue(Board::class.java) }

                    if (boardsList.isNotEmpty()) {
                        selectedBoard = boardsList.first()
                        updateBoardUI()
                        loadGroups(selectedBoard!!.id)
                    } else {
                        tvSelectedBoard.text = "No Boards available"
                        tvSelectedGroup.text = "No Groups available"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load boards.", Toast.LENGTH_SHORT).show()
                    Log.e("AddItemDialog", "Error loading boards: ${error.message}")
                }
            })
    }

    private fun loadGroups(boardId: String) {
        db.child("groups").orderByChild("boardId").equalTo(boardId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    groupsList = snapshot.children.mapNotNull { it.getValue(Group::class.java) }

                    if (groupsList.isNotEmpty()) {
                        selectedGroup = groupsList.first()
                        updateGroupUI()
                        btnSave.isEnabled = true
                    } else {
                        selectedGroup = null
                        tvSelectedGroup.text = "No Groups available"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load groups.", Toast.LENGTH_SHORT).show()
                    Log.e("AddItemDialog", "Error loading groups: ${error.message}")
                }
            })
    }

    private fun updateBoardUI() {
        tvSelectedBoard.text = selectedBoard?.name ?: "Select a Board..."
        tvSelectedGroup.text = "Select a Group..."
        selectedGroup = null
        groupsList = emptyList()
        btnSave.isEnabled = false
    }

    private fun updateGroupUI() {
        tvSelectedGroup.text = selectedGroup?.name ?: "Select a Group..."
    }

    private fun showBoardSelector() {
        if (boardsList.isEmpty()) {
            Toast.makeText(context, "Please create a Board first!", Toast.LENGTH_SHORT).show()
            return
        }

        val boardNames = boardsList.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Board")
            .setItems(boardNames) { dialog, which ->
                selectedBoard = boardsList[which]
                updateBoardUI()
                loadGroups(selectedBoard!!.id)
                dialog.dismiss()
            }
            .show()
    }

    private fun showGroupSelector() {
        if (selectedBoard == null) {
            Toast.makeText(context, "Please select a Board first.", Toast.LENGTH_SHORT).show()
            return
        }
        if (groupsList.isEmpty()) {
            Toast.makeText(context, "No Groups found in this Board.", Toast.LENGTH_SHORT).show()
            return
        }

        val groupNames = groupsList.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Group")
            .setItems(groupNames) { dialog, which ->
                selectedGroup = groupsList[which]
                updateGroupUI()
                btnSave.isEnabled = true
                dialog.dismiss()
            }
            .show()
    }

    private fun checkAndSaveNewItem() {
        val uid = userId
        val itemName = etItemName.text.toString().trim()

        if (uid == null || selectedBoard == null || selectedGroup == null || itemName.isEmpty()) {
            Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_LONG).show()
            return
        }

        val boardId = selectedBoard!!.id

        db.child("items").orderByChild("boardId").equalTo(boardId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val itemExists = snapshot.children.any { it.getValue(Item::class.java)?.name == itemName }

                    if (itemExists) {
                        Toast.makeText(context, "An item with this name already exists in this board", Toast.LENGTH_SHORT).show()
                    } else {
                        saveNewItem(itemName, uid, boardId, selectedGroup!!.id)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error checking item: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveNewItem(itemName: String, uid: String, boardId: String, groupId: String) {
        val itemId = db.child("items").push().key!!

        val newItem = Item(
            id = itemId,
            boardId = boardId,
            groupId = groupId,
            name = itemName,
            assignee = uid
        )

        db.child("items").child(itemId).setValue(newItem)
            .addOnSuccessListener {
                Toast.makeText(context, "Item '$itemName' added successfully!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error adding item: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("AddItemDialog", "Error adding item: ${e.message}")
            }
    }
}