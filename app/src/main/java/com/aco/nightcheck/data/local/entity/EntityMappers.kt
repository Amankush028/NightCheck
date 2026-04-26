package com.nightcheck.data.local.entity

import com.nightcheck.domain.model.Note
import com.nightcheck.domain.model.Priority
import com.nightcheck.domain.model.Reminder
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

// ── TaskEntity ↔ Task ────────────────────────────────────────────────────────

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    dueDate = dueDateEpochDay?.let { LocalDate.ofEpochDay(it) },
    priority = Priority.fromOrdinal(priority),
    status = TaskStatus.fromOrdinal(status),
    reminderTime = reminderEpochMillis?.toLocalDateTime(),
    createdAt = createdAtMillis.toLocalDateTime(),
    updatedAt = updatedAtMillis.toLocalDateTime()
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    dueDateEpochDay = dueDate?.toEpochDay(),
    priority = priority.ordinal,
    status = status.ordinal,
    reminderEpochMillis = reminderTime?.toEpochMillis(),
    createdAtMillis = createdAt.toEpochMillis(),
    updatedAtMillis = updatedAt.toEpochMillis()
)

// ── NoteEntity ↔ Note ────────────────────────────────────────────────────────

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    body = body,
    isPinned = isPinned,
    createdAt = createdAtMillis.toLocalDateTime(),
    updatedAt = updatedAtMillis.toLocalDateTime()
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    body = body,
    isPinned = isPinned,
    createdAtMillis = createdAt.toEpochMillis(),
    updatedAtMillis = updatedAt.toEpochMillis()
)

// ── ReminderEntity ↔ Reminder ────────────────────────────────────────────────

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = id,
    taskId = taskId,
    label = label,
    triggerAt = triggerAtMillis.toLocalDateTime(),
    isFired = isFired,
    createdAt = createdAtMillis.toLocalDateTime()
)

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    taskId = taskId,
    label = label,
    triggerAtMillis = triggerAt.toEpochMillis(),
    isFired = isFired,
    createdAtMillis = createdAt.toEpochMillis()
)

// ── Private helpers ──────────────────────────────────────────────────────────

private fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

private fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()