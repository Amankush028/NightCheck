package com.nightcheck.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nightcheck.notification.NotificationHelper
import com.nightcheck.util.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val taskId    = intent.getLongExtra(AlarmScheduler.EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_TITLE) ?: return
        if (taskId == -1L) return

        notificationHelper.showTaskReminderNotification(taskId, taskTitle)
    }
}
