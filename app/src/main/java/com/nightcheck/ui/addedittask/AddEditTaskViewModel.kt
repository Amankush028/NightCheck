package com.nightcheck.ui.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcheck.domain.model.Priority
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.domain.repository.TaskRepository
import com.nightcheck.domain.usecase.DeleteTaskUseCase
import com.nightcheck.domain.usecase.SaveTaskUseCase
import com.nightcheck.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class AddEditTaskUiState(
    val title: String = "",
    val description: String = "",
    // One-time mode
    val dueDate: LocalDate? = LocalDate.now(),
    val dueTime: LocalTime? = null,          // optional time for one-time tasks
    // Recurring mode
    val recurringDays: Set<DayOfWeek> = emptySet(),
    val recurringTime: LocalTime = LocalTime.of(9, 0),
    // Shared
    val isRecurring: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val reminderTime: LocalDateTime? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    private val saveTaskUseCase: SaveTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val taskId: Long? = savedStateHandle
        .get<Long>(Screen.ARG_TASK_ID)
        ?.takeIf { it != -1L }

    private val _uiState = MutableStateFlow(AddEditTaskUiState())
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()

    init {
        taskId?.let { loadTask(it) }
    }

    private fun loadTask(id: Long) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val task = taskRepository.getTaskById(id)
            if (task != null) {
                val isRecurring = task.recurringDays != null
                _uiState.update { _ ->
                    AddEditTaskUiState(
                        title        = task.title,
                        description  = task.description ?: "",
                        dueDate      = if (isRecurring) null else task.dueDate,
                        dueTime      = null,
                        recurringDays = task.recurringDays ?: emptySet(),
                        recurringTime = task.recurringTime ?: LocalTime.of(9, 0),
                        isRecurring  = isRecurring,
                        priority     = task.priority,
                        status       = task.status,
                        reminderTime = task.reminderTime,
                        isLoading    = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Task not found") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = "Failed to load task") }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onDueDateChange(value: LocalDate?) = _uiState.update { it.copy(dueDate = value) }
    fun onDueTimeChange(value: LocalTime?) = _uiState.update { it.copy(dueTime = value) }
    fun onRecurringTimeChange(time: LocalTime) = _uiState.update { it.copy(recurringTime = time) }
    fun onPriorityChange(value: Priority) = _uiState.update { it.copy(priority = value) }
    fun onReminderTimeChange(value: LocalDateTime?) = _uiState.update { it.copy(reminderTime = value) }

    fun toggleRecurringDay(day: DayOfWeek) {
        _uiState.update { state ->
            val days = state.recurringDays.toMutableSet()
            if (day in days) days.remove(day) else days.add(day)
            state.copy(recurringDays = days)
        }
    }

    fun onScheduleModeChange(isRecurring: Boolean) {
        _uiState.update {
            it.copy(
                isRecurring = isRecurring,
                dueDate = if (isRecurring) null else LocalDate.now()
            )
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Title is required") }
            return
        }
        if (state.isRecurring && state.recurringDays.isEmpty()) {
            _uiState.update { it.copy(error = "Select at least one day for recurring tasks") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // For one-time tasks with a time, combine date+time into reminderTime
                // if no explicit reminder was set by the user.
                val effectiveReminder = when {
                    state.reminderTime != null -> state.reminderTime
                    !state.isRecurring && state.dueDate != null && state.dueTime != null ->
                        LocalDateTime.of(state.dueDate, state.dueTime)
                    else -> null
                }

                val task = Task(
                    id            = taskId ?: 0L,
                    title         = state.title.trim(),
                    description   = state.description.trim().ifBlank { null },
                    dueDate       = if (state.isRecurring) null else state.dueDate,
                    recurringDays = if (state.isRecurring) state.recurringDays else null,
                    recurringTime = if (state.isRecurring) state.recurringTime else null,
                    priority      = state.priority,
                    status        = state.status,
                    reminderTime  = effectiveReminder
                )
                saveTaskUseCase(task)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to save task") }
            }
        }
    }

    fun delete() {
        taskId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                deleteTaskUseCase(taskId)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to delete task") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}