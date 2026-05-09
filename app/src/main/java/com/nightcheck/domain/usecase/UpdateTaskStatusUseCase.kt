package com.nightcheck.domain.usecase

import android.content.Context
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.domain.repository.TaskRepository
import com.nightcheck.ui.widget.WidgetUpdateUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject

class UpdateTaskStatusUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(taskId: Long, status: TaskStatus) {
        val task = taskRepository.getTaskById(taskId) ?: return

        if (task.recurringDays != null) {
            // For recurring tasks, never store COMPLETED in DB.
            // Completion is tracked via lastCompletedDate only.
            val lastCompleted = if (status == TaskStatus.COMPLETED) LocalDate.now() else null
            taskRepository.saveTask(
                task.copy(
                    status            = TaskStatus.PENDING,
                    lastCompletedDate = lastCompleted
                )
            )
        } else {
            taskRepository.updateTaskStatus(taskId, status)
        }

        // Notify the widget so checkboxes update immediately
        WidgetUpdateUtil.refreshTodayTasksWidget(context)
    }
}