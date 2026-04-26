package com.nightcheck.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nightcheck.domain.repository.TaskRepository
import com.nightcheck.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * WorkManager worker used as a secondary scheduling mechanism.
 *
 * Primary scheduling: AlarmManager via [EndOfDayReceiver] (survives Doze).
 * This worker is a fallback / complement that can be enqueued as a
 * PeriodicWorkRequest in case the alarm is missed (e.g. aggressive battery
 * optimisation). It simply posts the notification; the full-screen Activity
 * is only launched from the BroadcastReceiver path.
 */
@HiltWorker
class EndOfDaySchedulerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val taskRepository: TaskRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val pendingTasks = taskRepository
                .observePendingTasksForDay(LocalDate.now())
                .first()

            if (pendingTasks.isNotEmpty()) {
                notificationHelper.showEndOfDayNotification(pendingTasks.size)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "end_of_day_review_worker"
    }
}
