package com.nightcheck.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcheck.domain.model.Note
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.domain.repository.NoteRepository
import com.nightcheck.domain.usecase.GetTodayTasksUseCase
import com.nightcheck.domain.usecase.UpdateTaskStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getTodayTasks: GetTodayTasksUseCase,
    noteRepository: NoteRepository,
    private val updateTaskStatus: UpdateTaskStatusUseCase
) : ViewModel() {

    // distinctUntilChanged() prevents the LazyColumn from re-laying-out when
    // the same list is re-emitted (e.g. a note update triggers a task flow re-read)
    val todayTasks: StateFlow<List<Task>> = getTodayTasks()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pinnedNotes: StateFlow<List<Note>> = noteRepository.observePinnedNotes()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleTaskStatus(task: Task, newStatus: TaskStatus) {
        viewModelScope.launch {
            updateTaskStatus(task.id, newStatus)
        }
    }
}