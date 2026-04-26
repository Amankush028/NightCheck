package com.nightcheck.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Room entity representing a Task row in the database.
 * Maps 1-to-1 with the tasks table.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val description: String? = null,

    /** ISO epoch day – nullable means no due date */
    val dueDateEpochDay: Long? = null,

    /** Priority stored as ordinal: 0=LOW, 1=MEDIUM, 2=HIGH */
    val priority: Int = 1,

    /** Status stored as ordinal: 0=PENDING, 1=COMPLETED, 2=SNOOZED */
    val status: Int = 0,

    /** Epoch millis for the optional reminder, null = no reminder */
    val reminderEpochMillis: Long? = null,

    /** Epoch millis – creation timestamp */
    val createdAtMillis: Long = System.currentTimeMillis(),

    /** Epoch millis – last update */
    val updatedAtMillis: Long = System.currentTimeMillis()
)