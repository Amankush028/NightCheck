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
        return taskRepository.observeTasksForDay(LocalDate.now())
            .map { tasks -> tasks.sortedByDescending { it.priority.ordinal } }
    }
}
