package com.nightcheck.data.repository

import com.nightcheck.data.local.dao.NoteDao
import com.nightcheck.data.local.entity.toDomain
import com.nightcheck.data.local.entity.toEntity
import com.nightcheck.domain.model.Note
import com.nightcheck.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun observeAllNotes(): Flow<List<Note>> =
        noteDao.observeAllNotes().map { list -> list.map { it.toDomain() } }

    override fun observePinnedNotes(): Flow<List<Note>> =
        noteDao.observePinnedNotes().map { list -> list.map { it.toDomain() } }

    override suspend fun getNoteById(id: Long): Note? =
        noteDao.getNoteById(id)?.toDomain()

    override suspend fun saveNote(note: Note): Long =
        noteDao.insertNote(note.toEntity())

    override suspend fun togglePin(id: Long, pinned: Boolean) =
        noteDao.updatePinStatus(id, pinned)

    override suspend fun deleteNote(id: Long) =
        noteDao.deleteNoteById(id)
}