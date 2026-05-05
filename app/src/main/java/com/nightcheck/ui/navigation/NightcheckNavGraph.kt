package com.nightcheck.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nightcheck.ui.addeditnote.AddEditNoteScreen
import com.nightcheck.ui.addedittask.AddEditTaskScreen
import com.nightcheck.ui.home.HomeScreen
import com.nightcheck.ui.notes.NotesScreen
import com.nightcheck.ui.settings.SettingsScreen
import com.nightcheck.ui.tasks.TasksScreen
import com.nightcheck.ads.AdManager
import com.nightcheck.ui.monetization.MonetizationHooks
import com.nightcheck.ui.paywall.PaywallReason
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.ui.platform.LocalContext
import kotlin.jvm.java


@Composable
fun NightcheckNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {

        // ── Home ──────────────────────────────────────────────────────────────
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToAddTask = { navController.navigate(Screen.AddEditTask.route()) },
                onNavigateToAddNote = { navController.navigate(Screen.AddEditNote.route()) },
                onNavigateToTask = { taskId ->
                    navController.navigate(Screen.AddEditTask.route(taskId))
                },
                onNavigateToNote = { noteId ->
                    navController.navigate(Screen.AddEditNote.route(noteId))
                }
            )
        }

        val adManager: AdManager = EntryPointAccessors.fromApplication(
            LocalContext.current.applicationContext,
            AdEntryPoint::class.java
        ).adManager()

        // ── Tasks ─────────────────────────────────────────────────────────────
        composable(route = Screen.Tasks.route) {
            TasksScreen(
                // Added the missing onNavigateToAddTask callback here
                onNavigateToAddTask = { navController.navigate(Screen.AddEditTask.route()) },
                onNavigateToTask = { taskId ->
                    navController.navigate(Screen.AddEditTask.route(taskId))
                }
            )
        }

        // ── Notes ─────────────────────────────────────────────────────────────
        composable(route = Screen.Notes.route) {
            NotesScreen(
                // Added the missing onNavigateToAddNote callback here
                onNavigateToAddNote = { navController.navigate(Screen.AddEditNote.route()) },
                onNavigateToNote = { noteId ->
                    navController.navigate(Screen.AddEditNote.route(noteId))
                }
            )
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable(route = Screen.Settings.route) {
            SettingsScreen()
        }

        // ── Add / Edit Task ───────────────────────────────────────────────────
        composable(
            route = Screen.AddEditTask.route,
            arguments = listOf(
                navArgument(Screen.ARG_TASK_ID) {
                    type = NavType.LongType
                    defaultValue = -1L    // -1L = new task
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments
                ?.getLong(Screen.ARG_TASK_ID)
                ?.takeIf { it != -1L }
            AddEditTaskScreen(
                taskId = taskId,
                onNavigateUp = { navController.navigateUp() }
            )
        }

        // ── Add / Edit Note ───────────────────────────────────────────────────
        composable(
            route = Screen.AddEditNote.route,
            arguments = listOf(
                navArgument(Screen.ARG_NOTE_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments
                ?.getLong(Screen.ARG_NOTE_ID)
                ?.takeIf { it != -1L }
            AddEditNoteScreen(
                noteId = noteId,
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }

}