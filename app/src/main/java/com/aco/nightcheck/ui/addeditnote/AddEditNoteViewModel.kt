package com.nightcheck.ui.addeditnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcheck.domain.model.Note
import com.nightcheck.domain.repository.NoteRepository
import com.nightcheck.domain.usecase.SaveNoteUseCase
import com.nightcheck.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditNoteUiState(
    val title: String = "",
    val body: String = "",
    val isPinned: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val saveNoteUseCase: SaveNoteUseCase
) : ViewModel() {

    private val noteId: Long? = savedStateHandle
        .get<Long>(Screen.ARG_NOTE_ID)
        ?.takeIf { it != -1L }

    private val _uiState = MutableStateFlow(AddEditNoteUiState())
    val uiState: StateFlow<AddEditNoteUiState> = _uiState.asStateFlow()

    init { noteId?.let { loadNote(it) } }

    private fun loadNote(id: Long) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        noteRepository.getNoteById(id)?.let { note ->
            _uiState.update { _ ->
                AddEditNoteUiState(
                    title = note.title,
                    body = note.body,
                    isPinned = note.isPinned,
                    isLoading = false
                )
            }
        } ?: _uiState.update { it.copy(isLoading = false, error = "Note not found") }
    }

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v) }
    fun onBodyChange(v: String)  = _uiState.update { it.copy(body = v) }
    fun togglePin() = _uiState.update { it.copy(isPinned = !it.isPinned) }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Title is required") }
            return
        }
        viewModelScope.launch {
            saveNoteUseCase(
                Note(
                    id = noteId ?: 0L,
                    title = state.title.trim(),
                    body = state.body.trim(),
                    isPinned = state.isPinned
                )
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun delete() {
        noteId ?: return
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
