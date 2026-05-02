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

    private val _filter    = MutableStateFlow(TaskFilter.TODAY)
    private val _sortOrder = MutableStateFlow(TaskSortOrder.PRIORITY)

    val uiState: StateFlow<TasksUiState> = combine(_filter, _sortOrder) { f, s -> f to s }
        .flatMapLatest { (filter, sort) ->
            val today = LocalDate.now()

            // Each branch returns a flow that is already distinctUntilChanged
            // at the repository layer — no extra suppression needed here.
            val taskFlow: Flow<List<Task>> = when (filter) {
                TaskFilter.TODAY -> taskRepository.observeTasksForDay(today)
                    .map { tasks -> tasks.filter { it.status != TaskStatus.COMPLETED } }

                TaskFilter.UPCOMING -> taskRepository.observeUpcomingTasks(today)

                TaskFilter.COMPLETED -> taskRepository.observeCompletedTasks()
            }

            taskFlow.map { tasks ->
                val sorted = when (sort) {
                    TaskSortOrder.PRIORITY -> tasks.sortedByDescending { it.priority.ordinal }
                    TaskSortOrder.DUE_DATE -> tasks.sortedWith(compareBy(nullsLast()) { it.dueDate })
                }
                TasksUiState(tasks = sorted, filter = filter, sortOrder = sort)
            }
        }
        // Suppress redundant emissions so the LazyColumn doesn't re-layout
        // when the sorted list is structurally equal to the previous one.
        .distinctUntilChanged()
        .stateIn(
            scope            = viewModelScope,
            started          = SharingStarted.WhileSubscribed(5_000),
            initialValue     = TasksUiState()
        )

    fun setFilter(filter: TaskFilter)      { _filter.value    = filter }
    fun setSortOrder(sort: TaskSortOrder)  { _sortOrder.value = sort   }

    fun toggleTaskStatus(task: Task, newStatus: TaskStatus) {
        viewModelScope.launch {
            updateTaskStatus(task.id, newStatus)
        }
    }
}