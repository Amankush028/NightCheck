package com.nightcheck.domain.usecase

import com.nightcheck.domain.model.Task
import com.nightcheck.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetTodayTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        val today = LocalDate.now()
        // Show tasks due today OR tasks with no due date (undated tasks)
        return taskRepository.observeAllTasks().map { tasks ->
            tasks.filter { task ->
                task.dueDate == null || task.dueDate == today
            }.sortedByDescending { it.priority.ordinal }
        }
    }
}