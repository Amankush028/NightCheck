package com.nightcheck.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nightcheck.domain.repository.TaskRepository
import com.nightcheck.notification.NotificationHelper
import com.nightcheck.ui.review.EndOfDayReviewActivity
import com.nightcheck.util.AlarmScheduler
import com.nightcheck.util.PreferencesManager
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
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var alarmScheduler: AlarmScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                // Clean up old completed tasks (older than 7 days)
                val sevenDaysAgoMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
                taskRepository.deleteOldCompletedTasks(sevenDaysAgoMillis)

                val pendingTasks = taskRepository
                    .observePendingTasksForDay(LocalDate.now())
                    .first()

                if (pendingTasks.isNotEmpty()) {
                    val reviewIntent = Intent(context, EndOfDayReviewActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(reviewIntent)

                    notificationHelper.showEndOfDayNotification(pendingTasks.size)
                }
            } finally {
                // Safely grab the saved time and schedule tomorrow's alarm
                val hour = preferencesManager.endOfDayHour.first()
                val minute = preferencesManager.endOfDayMinute.first()
                alarmScheduler.scheduleEndOfDay(hour, minute)

                pendingResult.finish()
            }
        }
    }
}