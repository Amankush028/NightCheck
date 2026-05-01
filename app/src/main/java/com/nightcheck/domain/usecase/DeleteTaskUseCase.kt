package com.nightcheck.domain.usecase

import com.nightcheck.domain.repository.ReminderRepository
import com.nightcheck.domain.repository.TaskRepository
import com.aco.nightcheck.util.AlarmScheduler
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(taskId: Long) {
        alarmScheduler.cancelTaskReminder(taskId)
        reminderRepository.deleteRemindersForTask(taskId)
        taskRepository.deleteTask(taskId)
    }
}
