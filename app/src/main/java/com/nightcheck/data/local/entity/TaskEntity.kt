package com.nightcheck.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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

    /**
     * Comma-separated list of DayOfWeek ordinals (1-7, where 1 is Monday).
     * Null means not recurring.
     */
    val recurringDays: String? = null,

    /**
     * Minutes-since-midnight for the daily recurring time (e.g. 9*60+30 = 570 for 09:30).
     * Null means no recurring time set.
     */
    val recurringTimeMinutes: Int? = null,

    /** ISO epoch day of last completion – used for recurring tasks */
    val lastCompletedDateEpochDay: Long? = null,

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