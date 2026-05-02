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
    // One-time mode: dueDate is set, recurringTime is null
    // Recurring mode: recurringTime is set, dueDate is null
    val dueDate: LocalDate? = LocalDate.now(),
    val recurringTime: LocalTime? = null,
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

    // Holds the last-used recurring time so switching back to recurring
    // restores the user's previous time instead of resetting to null.
    private var lastRecurringTime: LocalTime = LocalTime.of(9, 0)

    init {
        taskId?.let { loadTask(it) }
    }

    private fun loadTask(id: Long) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val task = taskRepository.getTaskById(id)
            if (task != null) {
                // If the task has recurringDays set (non-null), it is a recurring task.
                // We restore the recurring time from task.recurringTime if available,
                // otherwise fall back to lastRecurringTime.
                val isRecurring = task.recurringDays != null
                val recurringTime: LocalTime? = if (isRecurring) {
                    // If your Task domain model gains a recurringTime field, read it here:
                    // task.recurringTime ?: lastRecurringTime
                    // For now, fall back to lastRecurringTime since the domain stores no time yet.
                    lastRecurringTime
                } else null

                if (recurringTime != null) lastRecurringTime = recurringTime

                _uiState.update { _ ->
                    AddEditTaskUiState(
                        title         = task.title,
                        description   = task.description ?: "",
                        // For recurring tasks, clear dueDate; for one-time, keep it.
                        dueDate       = if (isRecurring) null else task.dueDate,
                        recurringTime = recurringTime,
                        priority      = task.priority,
                        status        = task.status,
                        reminderTime  = task.reminderTime,
                        isLoading     = false
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

    fun onDueDateChange(value: LocalDate?) = _uiState.update {
        it.copy(dueDate = value, recurringTime = null)
    }

    fun onRecurringTimeChange(time: LocalTime?) {
        if (time != null) lastRecurringTime = time
        _uiState.update { it.copy(recurringTime = time, dueDate = null) }
    }

    // Called when the user switches between One-time / Recurring tabs.
    fun onScheduleModeChange(isRecurring: Boolean) {
        if (isRecurring) {
            _uiState.update { it.copy(recurringTime = lastRecurringTime, dueDate = null) }
        } else {
            _uiState.update { it.copy(dueDate = LocalDate.now(), recurringTime = null) }
        }
    }

    fun onPriorityChange(value: Priority) = _uiState.update { it.copy(priority = value) }
    fun onReminderTimeChange(value: LocalDateTime?) = _uiState.update { it.copy(reminderTime = value) }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Title is required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val isRecurring = state.recurringTime != null

                // FIX: For recurring tasks, pass all 7 days as the recurringDays set so
                // the repository knows this is a daily recurring task (non-empty set = recurring).
                // For one-time tasks, pass null.
                // NOTE: Once Task gains a dedicated `recurringTime: LocalTime?` field in the
                // domain model, store state.recurringTime there instead of encoding it in the set.
                val recurringDays: Set<DayOfWeek>? = if (isRecurring) {
                    DayOfWeek.entries.toSet() // all 7 days = "daily"
                } else {
                    null // not recurring
                }

                val task = Task(
                    id            = taskId ?: 0L,
                    title         = state.title.trim(),
                    description   = state.description.trim().ifBlank { null },
                    // For recurring tasks, dueDate is null; for one-time, use selected date.
                    dueDate       = if (isRecurring) null else state.dueDate,
                    recurringDays = recurringDays,
                    priority      = state.priority,
                    status        = state.status,
                    reminderTime  = state.reminderTime
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