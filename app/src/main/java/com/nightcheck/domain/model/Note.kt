package com.nightcheck.domain.model

import java.time.LocalDateTime

data class Note(
    val id: Long = 0,
    val title: String,
    val body: String = "",
    val isPinned: Boolean = false,
    val colorHex: String? = null,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
