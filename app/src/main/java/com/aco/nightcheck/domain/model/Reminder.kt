package com.nightcheck.domain.model

import java.time.LocalDateTime

data class Reminder(
    val id: Long = 0,
    val taskId: Long? = null,
    val label: String? = null,
    val triggerAt: LocalDateTime,
    val isFired: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
