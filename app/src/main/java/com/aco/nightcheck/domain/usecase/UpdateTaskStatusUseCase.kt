package com.nightcheck.domain.usecase

import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskStatusUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, status: TaskStatus) {
        taskRepository.updateTaskStatus(taskId, status)
    }
}
