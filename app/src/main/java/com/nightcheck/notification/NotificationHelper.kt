package com.nightcheck.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nightcheck.R
import com.nightcheck.ui.MainActivity
import com.nightcheck.ui.review.EndOfDayReviewActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        createChannels()
    }

    // ── Channel creation ──────────────────────────────────────────────────────

    private fun createChannels() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Channel 1: Task Reminders (high importance = heads-up)
        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDERS,
            "Task Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Time-based reminders for your tasks"
            enableVibration(true)
        }

        // Channel 2: End-of-Day Review (Upgraded to HIGH)
        val eodChannel = NotificationChannel(
            CHANNEL_END_OF_DAY,
            "End-of-Day Review",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Daily end-of-day review prompt"
            enableVibration(true)
        }

        manager.createNotificationChannels(listOf(reminderChannel, eodChannel))
    }

    // ── Task reminder notification ────────────────────────────────────────────

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showTaskReminderNotification(taskId: Long, taskTitle: String) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val tapPending = PendingIntent.getActivity(
            context, taskId.toInt(), tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Reminder")
            .setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(tapPending)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
    }

    // ── End-of-Day review notification ───────────────────────────────────────

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showEndOfDayNotification(pendingCount: Int) {
        val tapIntent = Intent(context, EndOfDayReviewActivity::class.java).apply {
            // Ensure it launches as a fresh task over everything else
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapPending = PendingIntent.getActivity(
            context, EOD_NOTIFICATION_ID, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_END_OF_DAY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("End of Day Review")
            .setContentText("You have $pendingCount task(s) pending today.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Categorize as alarm to bypass Doze
            .setContentIntent(tapPending)
            .setFullScreenIntent(tapPending, true) // This triggers the "Pop up" over other apps
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(EOD_NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_REMINDERS  = "nightcheck_reminders"
        const val CHANNEL_END_OF_DAY = "nightcheck_end_of_day"
        private const val EOD_NOTIFICATION_ID = 1000
    }
}
