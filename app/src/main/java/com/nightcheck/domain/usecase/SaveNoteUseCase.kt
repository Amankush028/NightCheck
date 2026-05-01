package com.nightcheck.domain.usecase

import com.nightcheck.domain.model.Note
import com.nightcheck.domain.repository.NoteRepository
import javax.inject.Inject

class SaveNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Long =
        noteRepository.saveNote(note)
}
