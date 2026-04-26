package com.nightcheck.data.repository

import com.nightcheck.data.local.dao.TaskDao
import com.nightcheck.data.local.entity.toDomain
import com.nightcheck.data.local.entity.toEntity
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun observeAllTasks(): Flow<List<Task>> =
        taskDao.observeAllTasks().map { list -> list.map { it.toDomain() } }

    override fun observeTasksForDay(date: LocalDate): Flow<List<Task>> =
        taskDao.observeTasksForDay(date.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override fun observePendingTasksForDay(date: LocalDate): Flow<List<Task>> =
        taskDao.observePendingTasksForDay(date.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override fun observeUpcomingTasks(after: LocalDate): Flow<List<Task>> =
        taskDao.observeUpcomingTasks(after.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override fun observeCompletedTasks(): Flow<List<Task>> =
        taskDao.observeCompletedTasks().map { list -> list.map { it.toDomain() } }

    override suspend fun getTaskById(id: Long): Task? =
        taskDao.getTaskById(id)?.toDomain()

    override suspend fun getTasksWithFutureReminders(): List<Task> =
        taskDao.getTasksWithFutureReminders(System.currentTimeMillis()).map { it.toDomain() }

    override suspend fun saveTask(task: Task): Long =
        taskDao.insertTask(task.toEntity())

    override suspend fun updateTaskStatus(id: Long, status: TaskStatus) =
        taskDao.updateTaskStatus(id, status.ordinal)

    override suspend fun deleteTask(id: Long) =
        taskDao.deleteTaskById(id)
}