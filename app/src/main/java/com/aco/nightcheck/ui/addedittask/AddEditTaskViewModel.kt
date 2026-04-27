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
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class AddEditTaskUiState(
    val title: String = "",
    val description: String = "",
    val dueDate: LocalDate? = LocalDate.now(),
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
        val task = taskRepository.getTaskById(id)
        if (task != null) {
            _uiState.update { _ ->
                AddEditTaskUiState(
                    title = task.title,
                    description = task.description ?: "",
                    dueDate = task.dueDate,
                    priority = task.priority,
                    status = task.status,
                    reminderTime = task.reminderTime,
                    isLoading = false
                )
            }
        } else {
            _uiState.update { it.copy(isLoading = false, error = "Task not found") }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onDueDateChange(value: LocalDate?) = _uiState.update { it.copy(dueDate = value) }
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
            val task = Task(
                id = taskId ?: 0L,
                title = state.title.trim(),
                description = state.description.trim().ifBlank { null },
                dueDate = state.dueDate,
                priority = state.priority,
                status = state.status,
                reminderTime = state.reminderTime
            )
            saveTaskUseCase(task)
            _uiState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun delete() {
        taskId ?: return
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
