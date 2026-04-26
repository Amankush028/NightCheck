package com.nightcheck.domain.model

enum class Priority(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    companion object {
        fun fromOrdinal(ordinal: Int): Priority =
            entries.getOrElse(ordinal) { MEDIUM }
    }
}
