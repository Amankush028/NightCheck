package com.nightcheck.domain.repository

import com.nightcheck.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeAllReminders(): Flow<List<Reminder>>
    fun observeRemindersForTask(taskId: Long): Flow<List<Reminder>>
    suspend fun getPendingReminders(): List<Reminder>
    suspend fun saveReminder(reminder: Reminder): Long
    suspend fun markReminderFired(id: Long)
    suspend fun deleteReminder(id: Long)
    suspend fun deleteRemindersForTask(taskId: Long)
}
