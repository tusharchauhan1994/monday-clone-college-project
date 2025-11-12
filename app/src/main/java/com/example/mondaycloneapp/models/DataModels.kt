package com.example.mondaycloneapp.models

// --- CORE DATA MODELS FOR MONDAY CLONE ---

/**
 * Represents a User profile.
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = ""
)

/**
 * Represents a Board (Project/Workspace).
 */
data class Board(
    val id: String = "",
    val name: String = "New Board",
    val ownerId: String = "",
    val members: Map<String, Boolean> = emptyMap(),
    val createdAt: Long = 0L
)

/**
 * Represents a Group within a Board.
 */
data class Group(
    val id: String = "",
    val boardId: String = "",
    val name: String = "New Group",
    val orderIndex: Int = 0
)

/**
 * Represents an Item (Task) within a Group.
 */
data class Item(
    val id: String = "",
    val boardId: String = "",
    val groupId: String = "",
    val name: String = "New Item",
    val status: String = "Working on it",
    val assignee: String? = null, // User ID assigned to the task
    val dueDate: String? = null, // Date as String "YYYY-MM-DD"
    val createdAt: Long = System.currentTimeMillis()
)

// --- Utility: Status Options ---
object StatusOptions {
    const val WORKING_ON_IT = "Working on it"
    const val STUCK = "Stuck"
    const val DONE = "Done"

    val ALL_STATUSES = listOf(WORKING_ON_IT, STUCK, DONE)
}
