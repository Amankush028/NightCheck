package com.nightcheck.ui.widget

import android.content.Context
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.repository.TaskRepository
import dagger.hilt.EntryPoints
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Helper object that retrieves today's tasks for the widget's provideGlance() call.
 * Bridges the non-Hilt Glance world with the Hilt dependency graph.
 */
object TodayTasksWidgetStateHelper {

    suspend fun getTodayTasks(context: Context): List<Task> {
        val entryPoint = EntryPoints.get(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        return entryPoint.taskRepository()
            .observeTasksForDay(LocalDate.now())
            .first()
    }
}
