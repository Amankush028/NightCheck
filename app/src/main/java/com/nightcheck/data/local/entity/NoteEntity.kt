package com.nightcheck.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a Note row in the database.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val body: String = "",

    /** Whether this note is pinned to the top of the notes list and home screen */
    val isPinned: Boolean = false,

    /** Optional background color for the note in hex format (e.g., #RRGGBB) */
    val colorHex: String? = null,

    val isBold: Boolean = false,
    val isItalic: Boolean = false,

    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)
