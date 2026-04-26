package com.nightcheck.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcheck.domain.model.Note
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.repository.NoteRepository
import com.nightcheck.domain.usecase.GetTodayTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getTodayTasks: GetTodayTasksUseCase,
    noteRepository: NoteRepository
) : ViewModel() {

    val todayTasks: StateFlow<List<Task>> = getTodayTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pinnedNotes: StateFlow<List<Note>> = noteRepository.observePinnedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
