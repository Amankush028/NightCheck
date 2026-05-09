package com.nightcheck.domain.usecase

import android.content.Context
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.repository.TaskRepository
import com.nightcheck.ui.widget.WidgetUpdateUtil
import com.nightcheck.util.AlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SaveTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val alarmScheduler: AlarmScheduler,
    @ApplicationContext private val context: Context
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

        // Notify the widget so it reflects the new/updated task immediately
        WidgetUpdateUtil.refreshTodayTasksWidget(context)

        return id
    }
}