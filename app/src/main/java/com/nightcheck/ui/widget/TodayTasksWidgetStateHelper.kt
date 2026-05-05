package com.nightcheck.ui.widget

import android.content.Context
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Helper object that retrieves today's tasks for the widget's provideGlance() call.
 * Bridges the non-Hilt Glance world with the Hilt dependency graph.
 *
 * FIX 1: Use EntryPointAccessors.fromApplication() (not EntryPoints.get()) because
 *         WidgetEntryPoint is @InstallIn(SingletonComponent::class). Using the wrong
 *         accessor caused a crash/hang → infinite loading screen.
 *
 * FIX 2: Call observeAllTasks() and filter client-side instead of relying on
 *         observeTasksForDay() which (a) may not exist on the interface and (b) was
 *         previously shown to silently drop tasks with a null due date.
 */
object TodayTasksWidgetStateHelper {

    suspend fun getTodayTasks(context: Context): List<Task> {
        // FIX 1: correct accessor for SingletonComponent entry points
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )

        val todayEpochDay = LocalDate.now().toEpochDay()

        // FIX 2: collect all tasks then filter, mirroring the GetTodayTasksUseCase fix
        // that includes tasks with a null due date alongside today's dated tasks.
        return entryPoint.taskRepository()
            .observeAllTasks()
            .first()
            .filter { task ->
                task.status == TaskStatus.PENDING &&
                        task.dueDate?.toEpochDay() == todayEpochDay
            }
    }
}