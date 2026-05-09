package com.nightcheck.domain.usecase

import android.content.Context
import com.nightcheck.domain.repository.ReminderRepository
import com.nightcheck.domain.repository.TaskRepository
import com.nightcheck.ui.widget.WidgetUpdateUtil
import com.nightcheck.util.AlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(taskId: Long) {
        alarmScheduler.cancelTaskReminder(taskId)
        reminderRepository.deleteRemindersForTask(taskId)
        taskRepository.deleteTask(taskId)

        // Notify the widget so deleted tasks disappear immediately
        WidgetUpdateUtil.refreshTodayTasksWidget(context)
    }
}