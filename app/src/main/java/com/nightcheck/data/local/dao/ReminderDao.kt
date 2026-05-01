package com.nightcheck.data.local.dao

import androidx.room.*
import com.nightcheck.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY triggerAtMillis ASC")
    fun observeAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE taskId = :taskId")
    fun observeRemindersForTask(taskId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    /** Fetch all non-fired reminders in the future – used after device reboot */
    @Query("""
        SELECT * FROM reminders 
        WHERE isFired = 0 
          AND triggerAtMillis > :nowMillis 
        ORDER BY triggerAtMillis ASC
    """)
    suspend fun getPendingReminders(nowMillis: Long): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE taskId = :taskId")
    suspend fun deleteRemindersForTask(taskId: Long)

    @Query("UPDATE reminders SET isFired = 1 WHERE id = :id")
    suspend fun markReminderFired(id: Long)
}