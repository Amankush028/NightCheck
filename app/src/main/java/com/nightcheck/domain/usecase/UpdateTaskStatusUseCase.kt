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
            // For recurring tasks, never store COMPLETED in DB.
            // Completion is tracked via lastCompletedDate only.
            // The repository layer derives the display status from that field.
            val lastCompleted = if (status == TaskStatus.COMPLETED) LocalDate.now() else null
            taskRepository.saveTask(
                task.copy(
                    status            = TaskStatus.PENDING,  // always keep as PENDING in DB
                    lastCompletedDate = lastCompleted
                )
            )
        } else {
            taskRepository.updateTaskStatus(taskId, status)
        }
    }
}
