package com.example.mondaycloneapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.mondaycloneapp.models.Board
import com.example.mondaycloneapp.models.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DialogAddBoard : DialogFragment() {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_board, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etBoardName = view.findViewById<EditText>(R.id.et_board_name)
        val btnCreateBoard = view.findViewById<Button>(R.id.btn_create_board)

        btnCreateBoard.setOnClickListener {
            val boardName = etBoardName.text.toString().trim()
            if (boardName.isNotEmpty()) {
                checkAndCreateBoard(boardName)
            } else {
                Toast.makeText(context, "Please enter a board name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndCreateBoard(boardName: String) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.child("boards").orderByChild("members/$uid").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val boardExists = snapshot.children.any { it.getValue(Board::class.java)?.name == boardName }

                    if (boardExists) {
                        Toast.makeText(context, "A board with this name already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        createNewBoard(boardName, uid)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error checking board: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun createNewBoard(boardName: String, uid: String) {
        val boardId = db.child("boards").push().key ?: return
        val groupId = db.child("groups").push().key ?: return

        val newBoard = Board(
            id = boardId,
            name = boardName,
            ownerId = uid,
            members = mapOf(uid to true)
        )

        val defaultGroup = Group(
            id = groupId,
            boardId = boardId,
            name = "Group Title"
        )

        val childUpdates = hashMapOf<String, Any>(
            "/boards/$boardId" to newBoard,
            "/groups/$groupId" to defaultGroup
        )

        db.updateChildren(childUpdates)
            .addOnSuccessListener {
                Toast.makeText(context, "Board '$boardName' created!", Toast.LENGTH_SHORT).show()
                (activity as? HomeActivity)?.refreshData()
                dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error creating board: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}