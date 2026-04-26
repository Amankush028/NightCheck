package com.nightcheck.data.repository

import com.nightcheck.data.local.dao.ReminderDao
import com.nightcheck.data.local.entity.toDomain
import com.nightcheck.data.local.entity.toEntity
import com.nightcheck.domain.model.Reminder
import com.nightcheck.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun observeAllReminders(): Flow<List<Reminder>> =
        reminderDao.observeAllReminders().map { list -> list.map { it.toDomain() } }

    override fun observeRemindersForTask(taskId: Long): Flow<List<Reminder>> =
        reminderDao.observeRemindersForTask(taskId).map { list -> list.map { it.toDomain() } }

    override suspend fun getPendingReminders(): List<Reminder> =
        reminderDao.getPendingReminders(System.currentTimeMillis()).map { it.toDomain() }

    override suspend fun saveReminder(reminder: Reminder): Long =
        reminderDao.insertReminder(reminder.toEntity())

    override suspend fun markReminderFired(id: Long) =
        reminderDao.markReminderFired(id)

    override suspend fun deleteReminder(id: Long) {
        val reminder = reminderDao.getReminderById(id) ?: return
        reminderDao.deleteReminder(reminder)
    }

    override suspend fun deleteRemindersForTask(taskId: Long) =
        reminderDao.deleteRemindersForTask(taskId)
}