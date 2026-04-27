package com.nightcheck.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcheck.domain.model.Note
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus // <-- Add this import
import com.nightcheck.domain.repository.NoteRepository
import com.nightcheck.domain.usecase.GetTodayTasksUseCase
import com.nightcheck.domain.usecase.UpdateTaskStatusUseCase // <-- Add this import
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch // <-- Add this import
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getTodayTasks: GetTodayTasksUseCase,
    noteRepository: NoteRepository,
    private val updateTaskStatus: UpdateTaskStatusUseCase // <-- Add this injected UseCase
) : ViewModel() {

    val todayTasks: StateFlow<List<Task>> = getTodayTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pinnedNotes: StateFlow<List<Note>> = noteRepository.observePinnedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // <-- Add this function to handle the checkbox
    fun toggleTaskStatus(task: Task, newStatus: TaskStatus) {
        viewModelScope.launch {
            updateTaskStatus(task.id, newStatus)
        }
    }
}