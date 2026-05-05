package com.nightcheck.ui.addeditnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcheck.billing.PremiumCache
import com.nightcheck.billing.UsageTracker
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
    val colorHex: String? = null,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    // Monetization
    val showNoteLimitDialog: Boolean = false,
    val showPaywall: Boolean = false,
    val shouldShowSessionInterstitial: Boolean = false
)

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val premiumCache: PremiumCache,
    private val usageTracker: UsageTracker
) : ViewModel() {

    private val noteId: Long? = savedStateHandle
        .get<Long>(Screen.ARG_NOTE_ID)
        ?.takeIf { it != -1L }

    private val _uiState = MutableStateFlow(AddEditNoteUiState())
    val uiState: StateFlow<AddEditNoteUiState> = _uiState.asStateFlow()

    init {
        noteId?.let { loadNote(it) }
    }

    private fun loadNote(id: Long) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        noteRepository.getNoteById(id)?.let { note ->
            _uiState.update {
                it.copy(
                    title    = note.title,
                    body     = note.body,
                    isPinned = note.isPinned,
                    colorHex = note.colorHex,
                    isBold   = note.isBold,
                    isItalic = note.isItalic,
                    isLoading = false
                )
            }
        } ?: _uiState.update { it.copy(isLoading = false, error = "Note not found") }
    }

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v) }
    fun onBodyChange(v: String)  = _uiState.update { it.copy(body = v) }
    fun togglePin()              = _uiState.update { it.copy(isPinned = !it.isPinned) }
    fun onColorChange(hex: String?) = _uiState.update { it.copy(colorHex = hex) }
    fun toggleBold()             = _uiState.update { it.copy(isBold = !it.isBold) }
    fun toggleItalic()           = _uiState.update { it.copy(isItalic = !it.isItalic) }

    fun dismissNoteLimitDialog() = _uiState.update { it.copy(showNoteLimitDialog = false) }
    fun dismissPaywall()         = _uiState.update { it.copy(showPaywall = false) }
    fun openPaywallFromLimit()   = _uiState.update { it.copy(showNoteLimitDialog = false, showPaywall = true) }
    fun onSessionInterstitialShown() = _uiState.update { it.copy(shouldShowSessionInterstitial = false) }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank() && state.body.isBlank()) {
            _uiState.update { it.copy(error = "Note cannot be empty") }
            return
        }
        viewModelScope.launch {
            try {
                // ── Free tier limit check (new notes only) ─────────────────
                if (noteId == null) {
                    val isPremium = premiumCache.isCurrentlyPremium()
                    if (!isPremium) {
                        val allNotes = noteRepository.observeAllNotes().first()
                        if (allNotes.size >= UsageTracker.MAX_FREE_NOTES) {
                            _uiState.update { it.copy(showNoteLimitDialog = true) }
                            return@launch
                        }
                    }
                }

                saveNoteUseCase(
                    Note(
                        id       = noteId ?: 0L,
                        title    = state.title.trim(),
                        body     = state.body.trim(),
                        isPinned = state.isPinned,
                        colorHex = state.colorHex,
                        isBold   = state.isBold,
                        isItalic = state.isItalic
                    )
                )

                // ── Session interstitial ───────────────────────────────────
                if (noteId == null) {
                    val isPremium = premiumCache.isCurrentlyPremium()
                    if (!isPremium) {
                        val count = usageTracker.incrementSessionAdds()
                        if (count == UsageTracker.SESSION_INTERSTITIAL_THRESHOLD) {
                            _uiState.update { it.copy(shouldShowSessionInterstitial = true) }
                            return@launch
                        }
                    }
                }

                _uiState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to save note") }
            }
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