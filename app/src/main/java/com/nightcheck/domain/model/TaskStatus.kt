package com.nightcheck.domain.model

enum class TaskStatus(val label: String) {
    PENDING("Pending"),
    COMPLETED("Completed"),
    SNOOZED("Snoozed");

    companion object {
        fun fromOrdinal(ordinal: Int): TaskStatus =
            entries.getOrElse(ordinal) { PENDING }
    }
}
