package com.nightcheck.ui.navigation

/**
 * Type-safe route definitions for Compose Navigation.
 * All argument names are kept as constants to avoid typos.
 */
sealed class Screen(val route: String) {

    // ── Bottom nav destinations ───────────────────────────────────────────────
    data object Home : Screen("home")
    data object Tasks : Screen("tasks")
    data object Notes : Screen("notes")
    data object Settings : Screen("settings")

    // ── Task detail / edit ────────────────────────────────────────────────────
    data object AddEditTask : Screen("add_edit_task?$ARG_TASK_ID={$ARG_TASK_ID}") {
        fun route(taskId: Long? = null): String =
            if (taskId != null) "add_edit_task?$ARG_TASK_ID=$taskId"
            else "add_edit_task"
    }

    // ── Note detail / edit ────────────────────────────────────────────────────
    data object AddEditNote : Screen("add_edit_note?$ARG_NOTE_ID={$ARG_NOTE_ID}") {
        fun route(noteId: Long? = null): String =
            if (noteId != null) "add_edit_note?$ARG_NOTE_ID=$noteId"
            else "add_edit_note"
    }

    companion object {
        const val ARG_TASK_ID = "taskId"
        const val ARG_NOTE_ID = "noteId"

        /** Bottom nav items in display order */
        val bottomNavItems = listOf(Home, Tasks, Notes, Settings)
    }
}
