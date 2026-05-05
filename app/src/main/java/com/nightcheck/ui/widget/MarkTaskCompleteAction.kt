package com.nightcheck.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.domain.repository.TaskRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Glance ActionCallback invoked when the user taps a task checkbox in the widget.
 * Uses Hilt EntryPoint to access the repository without a ViewModel.
 *
 * No changes needed here — EntryPointAccessors.fromApplication() was already
 * correctly used in this file.
 */
class MarkTaskCompleteAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[taskIdKey] ?: return

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        entryPoint.taskRepository().updateTaskStatus(taskId, TaskStatus.COMPLETED)

        // Refresh all instances of this widget
        TodayTasksWidget().updateAll(context)
    }

    companion object {
        val taskIdKey = ActionParameters.Key<Long>("task_id")
    }
}
