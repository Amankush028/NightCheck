package com.nightcheck.domain.repository

import com.nightcheck.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeAllNotes(): Flow<List<Note>>
    fun observePinnedNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    suspend fun saveNote(note: Note): Long
    suspend fun togglePin(id: Long, pinned: Boolean)
    suspend fun deleteNote(id: Long)
}
