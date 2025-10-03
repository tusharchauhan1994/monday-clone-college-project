package com.example.mondaycloneapp.models

import java.util.UUID

// --- CORE DATA MODELS FOR MONDAY CLONE ---

/**
 * Represents a Board (Project/Workspace). This is the top-level container for Groups and Items.
 * A Board can be a "Recently visited" item on the Home screen.
 */
data class Board(
    val id: String = UUID.randomUUID().toString(), // Unique ID for this board
    val userId: String = "", // Who owns this board
    val name: String = "New Board",
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastVisitedAt: Long = System.currentTimeMillis() // To sort in "Recently visited"
)

/**
 * Represents a Group within a Board (e.g., "To Do", "Done", "Group Title").
 */
data class Group(
    val id: String = UUID.randomUUID().toString(), // Unique ID for this group
    val boardId: String = "", // Which board this group belongs to
    val name: String = "New Group",
    val orderIndex: Int = 0 // Used for vertical sorting of groups
)

/**
 * Represents an Item (Task) within a Group.
 * This holds the dynamic column data shown in the table view (Status, Person, Date, etc.).
 */
data class Item(
    val id: String = UUID.randomUUID().toString(), // Unique ID for this item
    val userId: String = "", // Who owns/created this item
    val boardId: String = "", // Which board this item is in
    val groupId: String = "", // Which group this item is in
    val name: String = "New Item", // The main text for the task, e.g., "Tushar"
    val updates: Int = 0,
    val person: String? = null, // User ID or name assigned to the task
    val status: String = "Working on it", // Default status, must match StatusOptions
    val date: Long? = null, // Date in milliseconds
    val createdAt: Long = System.currentTimeMillis()
)

// --- Utility: Status Options ---
object StatusOptions {
    const val WORKING_ON_IT = "Working on it"
    const val STUCK = "Stuck"
    const val DONE = "Done"

    val ALL_STATUSES = listOf(WORKING_ON_IT, STUCK, DONE)
}
