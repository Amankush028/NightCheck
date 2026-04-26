package com.nightcheck.domain.usecase

import com.nightcheck.domain.model.Task
import com.nightcheck.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetTodayTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> =
        taskRepository.observeTasksForDay(LocalDate.now())
}
