package com.nightcheck.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nightcheck.domain.repository.TaskRepository
import com.nightcheck.notification.NotificationHelper
import com.nightcheck.ui.review.EndOfDayReviewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class EndOfDayReceiver : BroadcastReceiver() {

    @Inject lateinit var taskRepository: TaskRepository
    @Inject lateinit var notificationHelper: NotificationHelper

    // Safe scope for async work inside BroadcastReceiver
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                val pendingTasks = taskRepository
                    .observePendingTasksForDay(LocalDate.now())
                    .first()

                if (pendingTasks.isNotEmpty()) {
                    // Post notification that uses Full-Screen Intent to pop up
                    notificationHelper.showEndOfDayNotification(pendingTasks.size)

                    // DELETE the context.startActivity(reviewIntent) lines that used to be here!
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
