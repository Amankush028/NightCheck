package com.nightcheck.ui.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Utility to refresh all TodayTasksWidget instances after any task mutation.
 *
 * Called from SaveTaskUseCase and UpdateTaskStatusUseCase so the widget stays
 * in sync with the app without waiting for the 30-minute system update cycle.
 */
object WidgetUpdateUtil {

    // Dedicated scope — survives the call site's coroutine scope
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun refreshTodayTasksWidget(context: Context) {
        scope.launch {
            TodayTasksWidget().updateAll(context)
        }
    }
}