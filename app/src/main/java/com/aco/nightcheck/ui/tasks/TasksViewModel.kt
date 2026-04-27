package com.nightcheck.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.domain.repository.TaskRepository
import com.nightcheck.domain.usecase.UpdateTaskStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class TaskFilter { TODAY, UPCOMING, COMPLETED }
enum class TaskSortOrder { PRIORITY, DUE_DATE }

data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val filter: TaskFilter = TaskFilter.TODAY,
    val sortOrder: TaskSortOrder = TaskSortOrder.PRIORITY,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val updateTaskStatus: UpdateTaskStatusUseCase
) : ViewModel() {

    private val _filter = MutableStateFlow(TaskFilter.TODAY)
    private val _sortOrder = MutableStateFlow(TaskSortOrder.PRIORITY)

    val uiState: StateFlow<TasksUiState> = combine(_filter, _sortOrder) { filter, sort ->
        filter to sort
    }.flatMapLatest { (filter, sort) ->
        val today = LocalDate.now()
        val flow: Flow<List<Task>> = when (filter) {
            TaskFilter.TODAY -> taskRepository.observeAllTasks().map { tasks ->
                tasks.filter { it.dueDate == null || it.dueDate == today }
                    .filter { it.status != TaskStatus.COMPLETED }
            }
            TaskFilter.UPCOMING -> taskRepository.observeUpcomingTasks(today)
            TaskFilter.COMPLETED -> taskRepository.observeCompletedTasks()
        }
        flow.map { tasks ->
            val sorted = when (sort) {
                TaskSortOrder.PRIORITY -> tasks.sortedByDescending { it.priority.ordinal }
                TaskSortOrder.DUE_DATE -> tasks.sortedWith(
                    compareBy(nullsLast()) { it.dueDate }
                )
            }
            TasksUiState(tasks = sorted, filter = filter, sortOrder = sort)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())
    fun setFilter(filter: TaskFilter) { _filter.value = filter }
    fun setSortOrder(sort: TaskSortOrder) { _sortOrder.value = sort }

    // Replace markComplete with this function:
    fun toggleTaskStatus(task: Task, newStatus: TaskStatus) {
        viewModelScope.launch {
            // Use the exact name injected in the constructor
            updateTaskStatus(task.id, newStatus)
        }
    }
}
