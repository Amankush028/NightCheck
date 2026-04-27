package com.nightcheck.data.local.dao

import androidx.room.*
import com.nightcheck.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // ── Queries ──────────────────────────────────────────────────────────────

    @Query("SELECT * FROM tasks ORDER BY createdAtMillis DESC")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    /** Tasks whose dueDateEpochDay equals today's epoch day */
    @Query("""
        SELECT * FROM tasks 
        WHERE dueDateEpochDay = :todayEpochDay 
        ORDER BY priority DESC
    """)
    fun observeTasksForDay(todayEpochDay: Long): Flow<List<TaskEntity>>

    /** Pending tasks due today – used by End-of-Day Review */
    @Query("""
        SELECT * FROM tasks 
        WHERE dueDateEpochDay = :todayEpochDay 
          AND status = 0
        ORDER BY priority DESC
    """)
    fun observePendingTasksForDay(todayEpochDay: Long): Flow<List<TaskEntity>>

    /** Upcoming tasks – due strictly after today */
    @Query("""
        SELECT * FROM tasks 
        WHERE dueDateEpochDay > :todayEpochDay 
          AND status = 0
        ORDER BY dueDateEpochDay ASC, priority DESC
    """)
    fun observeUpcomingTasks(todayEpochDay: Long): Flow<List<TaskEntity>>

    /** All completed tasks, newest first */
    @Query("SELECT * FROM tasks WHERE status = 1 ORDER BY updatedAtMillis DESC")
    fun observeCompletedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    /** All tasks that have a future reminder set – needed to reschedule after reboot */
    @Query("""
        SELECT * FROM tasks 
        WHERE reminderEpochMillis IS NOT NULL 
          AND reminderEpochMillis > :nowMillis
    """)
    suspend fun getTasksWithFutureReminders(nowMillis: Long): List<TaskEntity>

    // ── Mutations ────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    /** All pending tasks with no due date – undated inbox */
    @Query("SELECT * FROM tasks WHERE dueDateEpochDay IS NULL AND status = 0 ORDER BY priority DESC")
    fun observeUndatedPendingTasks(): Flow<List<TaskEntity>>

    /** Quick status update used by widgets and End-of-Day Review */
    @Query("UPDATE tasks SET status = :status, updatedAtMillis = :now WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, status: Int, now: Long = System.currentTimeMillis())
}