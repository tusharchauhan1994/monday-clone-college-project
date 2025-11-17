package com.example.mondaycloneapp.models

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

// --- CORE DATA MODELS FOR MONDAY CLONE ---

/**
 * Represents a User profile.
 */
@IgnoreExtraProperties
@Parcelize
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = ""
) : Parcelable

/**
 * Represents a Board (Project/Workspace).
 */
@IgnoreExtraProperties
@Parcelize
data class Board(
    val id: String = "",
    val name: String = "New Board",
    val ownerId: String = "",
    val members: Map<String, Boolean> = emptyMap(),
    val createdAt: Long = 0L
) : Parcelable

/**
 * Represents a Group within a Board.
 */
@IgnoreExtraProperties
@Parcelize
data class Group(
    val id: String = "",
    val boardId: String = "",
    val name: String = "New Group",
    val orderIndex: Int = 0
) : Parcelable

/**
 * Represents an Item (Task) within a Group.
 */
@IgnoreExtraProperties
@Parcelize
data class Item(
    val id: String = "",
    val boardId: String = "",
    val groupId: String = "",
    val name: String = "New Item",
    val status: String = "Working on it",
    val priority: String = "Medium",
    val assignee: String? = null, // User ID assigned to the task
    val dueDate: String? = null, // Date as String "YYYY-MM-DD"
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

/**
 * Represents a Notification entry to be stored in the database.
 */
@IgnoreExtraProperties
@Parcelize
data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable


// --- Utility: Status Options ---
object StatusOptions {
    const val WORKING_ON_IT = "Working on it"
    const val STUCK = "Stuck"
    const val DONE = "Done"

    val ALL_STATUSES = listOf(WORKING_ON_IT, STUCK, DONE)
}

// --- Utility: Priority Options ---
object PriorityOptions {
    const val HIGH = "High"
    const val MEDIUM = "Medium"
    const val LOW = "Low"

    val ALL_PRIORITIES = listOf(HIGH, MEDIUM, LOW)
}
