package com.nightcheck.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Pure domain model – no Room or Android dependencies.
 * Mapped to/from [com.nightcheck.data.local.entity.TaskEntity].
 */
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dueDate: LocalDate? = null,
    val recurringDays: Set<DayOfWeek>? = null,
    val lastCompletedDate: LocalDate? = null,
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val reminderTime: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
