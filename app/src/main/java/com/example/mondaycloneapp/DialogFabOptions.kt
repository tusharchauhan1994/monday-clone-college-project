package com.example.mondaycloneapp

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.mondaycloneapp.models.Board
import com.example.mondaycloneapp.models.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// This dialog shows the options when the FAB is tapped in HomeActivity
class DialogFabOptions : DialogFragment() {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    // Ensures the pop-up window has a clean background
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Use the XML layout for the FAB options
        return inflater.inflate(R.layout.dialog_fab_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btn_close_fab_options).setOnClickListener {
            dismiss()
        }

        // Click listener for "Add item" option
        view.findViewById<View>(R.id.option_add_item).setOnClickListener {
            dismiss() // Close the current dialog
            // Open the new Add Item dialog
            DialogAddItem().show(parentFragmentManager, "AddItemDialog")
        }

        // Click listener for "Add Board" option
        view.findViewById<View>(R.id.option_add_board).setOnClickListener {
            createNewBoard() // The function will now handle its own dismissal
        }
    }

    /**
     * Creates a new default board and a default group inside it in Firebase Realtime Database.
     */
    private fun createNewBoard() {
        val uid = userId
        if (uid == null) {
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val boardName = "Start from scratch"
        val newBoard = Board(userId = uid, name = boardName)
        val defaultGroupName = "Group Title"
        val defaultGroup = Group(boardId = newBoard.id, name = defaultGroupName, orderIndex = 0)

        // Path: users/{uid}/boards/{newBoard.id}
        db.child("users").child(uid).child("boards").child(newBoard.id)
            .setValue(newBoard)
            .addOnSuccessListener {
                // If the board is created successfully, now create the default group inside it
                db.child("users").child(uid).child("boards")
                    .child(newBoard.id).child("groups")
                    .child(defaultGroup.id)
                    .setValue(defaultGroup)
                    .addOnSuccessListener {
                        Log.d("BoardCreation", "SUCCESS: Group was created in Realtime Database.")
                        Toast.makeText(context, "Board '$boardName' created!", Toast.LENGTH_LONG).show()
                        // Tell the Home screen to refresh its list
                        (activity as? HomeActivity)?.refreshData()

                        // --- FINAL FIX: Dismiss the dialog only after everything is successful ---
                        dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error creating default group.", Toast.LENGTH_LONG).show()
                        Log.e("NewBoard", "Error creating group: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error creating board.", Toast.LENGTH_LONG).show()
                Log.e("NewBoard", "Error creating board: ${e.message}")
            }
    }
}