package com.nightcheck.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.material3.GlanceTheme
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.ui.MainActivity

/**
 * 4x2 or larger widget showing today's pending tasks.
 */
class TodayTasksWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val tasks = TodayTasksWidgetStateHelper.getTodayTasks(context)
        provideContent {
            GlanceTheme {
                TodayTasksWidgetContent(tasks = tasks)
            }
        }
    }
}

@Composable
private fun TodayTasksWidgetContent(tasks: List<Task>) {
    // FIX 4: Removed greedy clickable from the root Column.
    // The "open app" action is now only on the header Row.
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(12.dp)
    ) {
        // Header — tapping this area opens the app
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            @Suppress("DEPRECATION")
            Text(
                text = "Today",
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = "${tasks.count { it.status == TaskStatus.PENDING }} pending",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }

        Spacer(GlanceModifier.height(8.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "All done! ✓",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(tasks) { task ->
                    TaskWidgetRow(task = task)
                }
            }
        }
    }
}

@Composable
private fun TaskWidgetRow(task: Task) {
    val isDone = task.status == TaskStatus.COMPLETED

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox - no longer blocked by greedy parent
        Box(
            modifier = GlanceModifier
                .size(20.dp)
                .background(
                    if (isDone) GlanceTheme.colors.primary
                    else GlanceTheme.colors.surfaceVariant
                )
                .clickable(
                    actionRunCallback<MarkTaskCompleteAction>(
                        parameters = actionParametersOf(
                            MarkTaskCompleteAction.taskIdKey to task.id
                        )
                    )
                )
        ) {}

        Spacer(GlanceModifier.width(8.dp))

        Text(
            text = task.title,
            style = TextStyle(
                color = if (isDone)
                    GlanceTheme.colors.onSurfaceVariant
                else
                    GlanceTheme.colors.onSurface,
                fontSize = 13.sp
            ),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )
    }
}

class TodayTasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayTasksWidget()
}
