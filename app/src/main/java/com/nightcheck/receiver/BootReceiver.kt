package com.nightcheck.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nightcheck.domain.repository.TaskRepository
import com.nightcheck.util.AlarmScheduler
import com.nightcheck.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fired on BOOT_COMPLETED and QUICKBOOT_POWERON.
 * AlarmManager alarms are cleared on reboot, so we reschedule everything here.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var taskRepository: TaskRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var preferencesManager: PreferencesManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in listOf(
                Intent.ACTION_BOOT_COMPLETED,
                "android.intent.action.QUICKBOOT_POWERON"
            )
        ) return

        val pendingResult = goAsync()
        scope.launch {
            try {
                // 1. Reschedule End-of-Day alarm
                val hour   = preferencesManager.endOfDayHour.first()
                val minute = preferencesManager.endOfDayMinute.first()
                alarmScheduler.scheduleEndOfDay(hour, minute)

                // 2. Reschedule all task reminder alarms
                taskRepository.getTasksWithFutureReminders().forEach { task ->
                    alarmScheduler.scheduleTaskReminder(task)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
