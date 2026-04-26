package com.nightcheck.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nightcheck.domain.model.Task
import com.nightcheck.receiver.EndOfDayReceiver
import com.nightcheck.receiver.ReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // ── Task Reminders ────────────────────────────────────────────────────────

    fun scheduleTaskReminder(task: Task) {
        val reminderTime = task.reminderTime ?: return
        val triggerMillis = reminderTime.toEpochMillis()
        if (triggerMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            // Fallback: inexact alarm if permission not granted
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    fun cancelTaskReminder(taskId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    // ── End-of-Day Review ─────────────────────────────────────────────────────

    /**
     * Schedules the nightly End-of-Day review alarm.
     * @param hourOfDay 0-23
     * @param minute    0-59
     */
    fun scheduleEndOfDay(hourOfDay: Int, minute: Int) {
        val triggerMillis = nextOccurrenceMillis(hourOfDay, minute)
        val pendingIntent = endOfDayPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT)

        fun cancelEndOfDay() {
            val pendingIntent = endOfDayPendingIntent(PendingIntent.FLAG_NO_CREATE) ?: return
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun cancelEndOfDay() {
        val pendingIntent = endOfDayPendingIntent(PendingIntent.FLAG_NO_CREATE) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun endOfDayPendingIntent(flags: Int): PendingIntent? =
        PendingIntent.getBroadcast(
            context,
            END_OF_DAY_REQUEST_CODE,
            Intent(context, EndOfDayReceiver::class.java),
            flags or PendingIntent.FLAG_IMMUTABLE
        )

    private fun nextOccurrenceMillis(hour: Int, minute: Int): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= now) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        private const val END_OF_DAY_REQUEST_CODE = 9999
    }
}

private fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
