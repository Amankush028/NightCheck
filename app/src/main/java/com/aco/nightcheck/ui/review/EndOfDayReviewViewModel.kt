package com.nightcheck.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.domain.usecase.GetPendingTodayTasksUseCase
import com.nightcheck.domain.usecase.SnoozeTaskUseCase
import com.nightcheck.domain.usecase.UpdateTaskStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Tracks per-task actions taken during the review so we can compute the
 * "X of Y completed" summary before the user exits.
 */
enum class ReviewAction { NONE, COMPLETED, SNOOZED, DISMISSED }

data class ReviewTaskItem(
    val task: Task,
    val action: ReviewAction = ReviewAction.NONE
)

data class EndOfDayUiState(
    val items: List<ReviewTaskItem> = emptyList(),
    val isLoading: Boolean = true,
    val showSummary: Boolean = false
) {
    val totalCount: Int get() = items.size
    val completedCount: Int get() = items.count { it.action == ReviewAction.COMPLETED }
    val allActioned: Boolean get() = items.all { it.action != ReviewAction.NONE }
}

@HiltViewModel
class EndOfDayReviewViewModel @Inject constructor(
    getPendingTodayTasks: GetPendingTodayTasksUseCase,
    private val updateTaskStatus: UpdateTaskStatusUseCase,
    private val snoozeTask: SnoozeTaskUseCase
) : ViewModel() {

    private val _actionMap = MutableStateFlow<Map<Long, ReviewAction>>(emptyMap())

    val uiState: StateFlow<EndOfDayUiState> = combine(
        getPendingTodayTasks(),
        _actionMap
    ) { tasks, actionMap ->
        EndOfDayUiState(
            items = tasks.map { task ->
                ReviewTaskItem(
                    task = task,
                    action = actionMap[task.id] ?: ReviewAction.NONE
                )
            },
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        EndOfDayUiState()
    )

    fun markComplete(taskId: Long) = viewModelScope.launch {
        updateTaskStatus(taskId, TaskStatus.COMPLETED)
        _actionMap.update { it + (taskId to ReviewAction.COMPLETED) }
    }

    fun snooze(taskId: Long) = viewModelScope.launch {
        snoozeTask(taskId)
        _actionMap.update { it + (taskId to ReviewAction.SNOOZED) }
    }

    fun dismiss(taskId: Long) {
        _actionMap.update { it + (taskId to ReviewAction.DISMISSED) }
    }

    fun requestSummary() {
        // flip the flag; the Activity observes this to show summary dialog
        viewModelScope.launch {
            // no-op state change needed — Activity calls this when user taps "Done"
        }
    }
}
