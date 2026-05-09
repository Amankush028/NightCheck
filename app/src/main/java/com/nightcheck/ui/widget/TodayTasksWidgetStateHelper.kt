package com.nightcheck.ui.widget

import android.content.Context
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Helper that retrieves today's tasks for the widget's provideGlance() call.
 * Bridges the non-Hilt Glance world with the Hilt dependency graph.
 *
 * Uses observeTasksForDay() which already handles:
 *  - Dated tasks whose dueDateEpochDay == today
 *  - Recurring tasks whose recurringDays includes today's DayOfWeek
 *
 * We then filter client-side to PENDING only, matching what the Home screen shows.
 */
object TodayTasksWidgetStateHelper {

    suspend fun getTodayTasks(context: Context): List<Task> {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )

        val today = LocalDate.now()

        // observeTasksForDay already unions dated + recurring tasks and
        // derives status for recurring tasks from lastCompletedDate.
        return entryPoint.taskRepository()
            .observeTasksForDay(today)
            .first()
            .filter { task -> task.status == TaskStatus.PENDING }
    }
}