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
                    // Post notification that taps into the full-screen review
                    notificationHelper.showEndOfDayNotification(pendingTasks.size)

                    // Also launch the review Activity directly (works on locked screen)
                    val reviewIntent = Intent(context, EndOfDayReviewActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(reviewIntent)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
