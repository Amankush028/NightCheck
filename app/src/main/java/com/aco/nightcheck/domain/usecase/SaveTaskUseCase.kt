package com.nightcheck.domain.usecase

import com.nightcheck.domain.model.Task
import com.nightcheck.domain.repository.TaskRepository
import com.aco.nightcheck.util.AlarmScheduler
import javax.inject.Inject

class SaveTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(task: Task): Long {
        val id = taskRepository.saveTask(task)

        // Schedule or cancel reminder alarm based on the saved task
        val savedTask = task.copy(id = id)
        if (savedTask.reminderTime != null) {
            alarmScheduler.scheduleTaskReminder(savedTask)
        } else {
            alarmScheduler.cancelTaskReminder(id)
        }

        return id
    }
}
