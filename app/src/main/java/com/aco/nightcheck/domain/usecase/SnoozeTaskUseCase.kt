package com.nightcheck.domain.usecase

import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.domain.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Snoozes a task by:
 * 1. Setting its status to SNOOZED
 * 2. Moving its due date to tomorrow
 *
 * Called from the End-of-Day Review screen.
 */
class SnoozeTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long) {
        val task = taskRepository.getTaskById(taskId) ?: return
        val tomorrow = LocalDate.now().plusDays(1)
        taskRepository.saveTask(
            task.copy(
                dueDate = tomorrow,
                status = TaskStatus.SNOOZED
            )
        )
    }
}
