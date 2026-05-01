package com.nightcheck.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.domain.repository.TaskRepository
import com.nightcheck.domain.usecase.SnoozeTaskUseCase
import com.nightcheck.domain.usecase.UpdateTaskStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class ReviewAction { NONE, COMPLETED, SNOOZED, DISMISSED }

data class ReviewTaskItem(
    val task: Task,
    val action: ReviewAction = ReviewAction.NONE
)

data class EndOfDayUiState(
    val items: List<ReviewTaskItem> = emptyList(),
    val totalTodayCount: Int = 0,
    val completedTodayCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class EndOfDayReviewViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val updateTaskStatus: UpdateTaskStatusUseCase,
    private val snoozeTask: SnoozeTaskUseCase
) : ViewModel() {

    private val _actionMap = MutableStateFlow<Map<Long, ReviewAction>>(emptyMap())

    val uiState: StateFlow<EndOfDayUiState> = combine(
        taskRepository.observeTasksForDay(LocalDate.now()),
        _actionMap
    ) { tasks, actionMap ->
        // We only want to "review" tasks that were PENDING or that we've acted on in this session.
        // Completed tasks (already done before review) shouldn't show up in the "need attention" list.
        val reviewItems = tasks
            .filter { it.status == TaskStatus.PENDING || actionMap.containsKey(it.id) }
            .map { task ->
                ReviewTaskItem(
                    task = task,
                    action = actionMap[task.id] ?: ReviewAction.NONE
                )
            }

        EndOfDayUiState(
            items = reviewItems,
            totalTodayCount = tasks.size,
            completedTodayCount = tasks.count { it.status == TaskStatus.COMPLETED },
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
}
