package com.nightcheck.domain.usecase

import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.domain.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

class UpdateTaskStatusUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, status: TaskStatus) {
        val task = taskRepository.getTaskById(taskId) ?: return
        
        if (task.recurringDays != null) {
            // For recurring tasks, completing it sets the lastCompletedDate to today.
            // We keep the status in DB as PENDING (or update it, but GetTodayTasksUseCase will override)
            // Actually, setting it to COMPLETED in DB is fine too.
            val lastCompleted = if (status == TaskStatus.COMPLETED) LocalDate.now() else null
            taskRepository.saveTask(
                task.copy(
                    status = status,
                    lastCompletedDate = lastCompleted
                )
            )
        } else {
            taskRepository.updateTaskStatus(taskId, status)
        }
    }
}
