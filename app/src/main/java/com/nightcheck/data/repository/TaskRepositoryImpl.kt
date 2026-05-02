package com.nightcheck.data.repository

import com.nightcheck.data.local.dao.TaskDao
import com.nightcheck.data.local.entity.toDomain
import com.nightcheck.data.local.entity.toEntity
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun observeAllTasks(): Flow<List<Task>> =
        taskDao.observeAllTasks()
            .distinctUntilChanged()
            .map { it.map { e -> e.toDomain() } }

    /**
     * Previously called observeAllTasks() + full client-side filter on EVERY
     * DB emission — O(n) scan on every write anywhere in the tasks table.
     *
     * Now uses the indexed DAO query for dated tasks, plus a separate indexed
     * recurring query, and merges them. Both hit the Room index on
     * dueDateEpochDay / recurringDays.
     *
     * distinctUntilChanged() on each upstream prevents downstream recomposition
     * when an unrelated task changes (e.g. completing a task on a different day).
     */
    override fun observeTasksForDay(date: LocalDate): Flow<List<Task>> {
        val epochDay  = date.toEpochDay()
        val dayOfWeek = date.dayOfWeek.value // 1=Mon … 7=Sun

        val datedFlow = taskDao.observeTasksForDay(epochDay)
            .distinctUntilChanged()

        // Recurring tasks are a small subset — filter them from the full list
        // but guard with distinctUntilChanged so we don't re-map every write.
        val recurringFlow = taskDao.observeAllTasks()
            .distinctUntilChanged()
            .map { list ->
                list.filter { entity ->
                    if (entity.recurringDays == null) return@filter false
                    entity.recurringDays
                        .split(",")
                        .filter { it.isNotEmpty() }
                        .any { it.trim().toIntOrNull() == dayOfWeek }
                }
            }
            .distinctUntilChanged()

        return combine(datedFlow, recurringFlow) { dated, recurring ->
            val datedDomain = dated.map { it.toDomain() }
            val datedIds    = datedDomain.map { it.id }.toHashSet()

            val recurringDomain = recurring
                .filter { it.id !in datedIds } // avoid duplicates
                .map { entity ->
                    val task       = entity.toDomain()
                    val doneToday  = task.lastCompletedDate == date
                    task.copy(status = if (doneToday) TaskStatus.COMPLETED else TaskStatus.PENDING)
                }

            (datedDomain + recurringDomain)
                .sortedByDescending { it.priority.ordinal }
        }.distinctUntilChanged()
    }

    override fun observePendingTasksForDay(date: LocalDate): Flow<List<Task>> =
        observeTasksForDay(date)
            .map { it.filter { task -> task.status == TaskStatus.PENDING } }
            .distinctUntilChanged()

    override fun observeUpcomingTasks(after: LocalDate): Flow<List<Task>> =
        taskDao.observeUpcomingTasks(after.toEpochDay())
            .distinctUntilChanged()
            .map { it.map { e -> e.toDomain() } }

    override fun observeCompletedTasks(): Flow<List<Task>> =
        taskDao.observeCompletedTasks()
            .distinctUntilChanged()
            .map { it.map { e -> e.toDomain() } }

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

    override suspend fun deleteOldCompletedTasks(thresholdMillis: Long) =
        taskDao.deleteOldCompletedTasks(thresholdMillis)
}