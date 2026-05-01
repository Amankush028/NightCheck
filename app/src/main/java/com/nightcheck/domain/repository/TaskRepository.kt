package com.nightcheck.domain.repository

import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskRepository {
    fun observeAllTasks(): Flow<List<Task>>
    fun observeTasksForDay(date: LocalDate): Flow<List<Task>>
    fun observePendingTasksForDay(date: LocalDate): Flow<List<Task>>
    fun observeUpcomingTasks(after: LocalDate): Flow<List<Task>>
    fun observeCompletedTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: Long): Task?
    suspend fun getTasksWithFutureReminders(): List<Task>
    suspend fun saveTask(task: Task): Long
    suspend fun updateTaskStatus(id: Long, status: TaskStatus)
    suspend fun deleteTask(id: Long)
    suspend fun deleteOldCompletedTasks(thresholdMillis: Long)
}
