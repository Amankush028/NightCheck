package com.nightcheck.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for standalone Reminders.
 *
 * A reminder can be:
 *  - Attached to a Task  (taskId != null)
 *  - Standalone          (taskId == null, label provided)
 *
 * When a Task is deleted its reminders are cascade-deleted.
 */
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Nullable – null means this is a standalone reminder */
    val taskId: Long? = null,

    /** Human-readable label for standalone reminders */
    val label: String? = null,

    /** Epoch millis when the alarm should fire */
    val triggerAtMillis: Long,

    /** Has this reminder already fired / been dismissed? */
    val isFired: Boolean = false,

    val createdAtMillis: Long = System.currentTimeMillis()
)