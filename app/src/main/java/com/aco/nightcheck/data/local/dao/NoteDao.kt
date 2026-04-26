package com.nightcheck.data.local.dao

import androidx.room.*
import com.nightcheck.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAtMillis DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isPinned = 1 ORDER BY updatedAtMillis DESC")
    fun observePinnedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("UPDATE notes SET isPinned = :pinned, updatedAtMillis = :now WHERE id = :id")
    suspend fun updatePinStatus(id: Long, pinned: Boolean, now: Long = System.currentTimeMillis())
}