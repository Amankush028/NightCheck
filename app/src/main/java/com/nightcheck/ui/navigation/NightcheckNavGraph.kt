package com.nightcheck.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nightcheck.ads.AdManager
import com.nightcheck.ui.addeditnote.AddEditNoteScreen
import com.nightcheck.ui.addedittask.AddEditTaskScreen
import com.nightcheck.ui.home.HomeScreen
import com.nightcheck.ui.notes.NotesScreen
import com.nightcheck.ui.settings.SettingsScreen
import com.nightcheck.ui.tasks.TasksScreen
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AdEntryPoint {
    fun adManager(): AdManager
}

// ── Transition specs ──────────────────────────────────────────────────────────
// Bottom-nav tabs use a fast crossfade. Detail screens slide up/down with fade.

private const val NAV_ANIM_DURATION = 300

private val tabEnter = fadeIn(tween(NAV_ANIM_DURATION))
private val tabExit = fadeOut(tween(NAV_ANIM_DURATION))

private val detailEnter = slideInVertically(
    initialOffsetY = { it / 6 },
    animationSpec = tween(NAV_ANIM_DURATION)
) + fadeIn(tween(NAV_ANIM_DURATION))

private val detailExit = slideOutVertically(
    targetOffsetY = { it / 6 },
    animationSpec = tween(NAV_ANIM_DURATION)
) + fadeOut(tween(NAV_ANIM_DURATION / 2))

private val detailPopEnter = slideInVertically(
    initialOffsetY = { -it / 8 },
    animationSpec = tween(NAV_ANIM_DURATION)
) + fadeIn(tween(NAV_ANIM_DURATION))

private val detailPopExit = slideOutVertically(
    targetOffsetY = { it / 4 },
    animationSpec = tween(NAV_ANIM_DURATION)
) + fadeOut(tween(NAV_ANIM_DURATION / 2))

@Composable
fun NightcheckNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AdEntryPoint::class.java
        ).adManager()
    }

    NavHost(
        navController    = navController,
        startDestination = Screen.Home.route,
        modifier         = modifier,
        // Default transitions for bottom-nav tabs — smooth crossfade
        enterTransition  = { tabEnter },
        exitTransition   = { tabExit },
        popEnterTransition = { tabEnter },
        popExitTransition  = { tabExit }
    ) {

        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToAddTask = { navController.navigate(Screen.AddEditTask.route()) },
                onNavigateToAddNote = { navController.navigate(Screen.AddEditNote.route()) },
                onNavigateToTask    = { taskId -> navController.navigate(Screen.AddEditTask.route(taskId)) },
                onNavigateToNote    = { noteId -> navController.navigate(Screen.AddEditNote.route(noteId)) }
            )
        }

        composable(route = Screen.Tasks.route) {
            TasksScreen(
                onNavigateToAddTask = { navController.navigate(Screen.AddEditTask.route()) },
                onNavigateToTask    = { taskId -> navController.navigate(Screen.AddEditTask.route(taskId)) }
            )
        }

        composable(route = Screen.Notes.route) {
            NotesScreen(
                onNavigateToAddNote = { navController.navigate(Screen.AddEditNote.route()) },
                onNavigateToNote    = { noteId -> navController.navigate(Screen.AddEditNote.route(noteId)) },
                adManager           = adManager
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen()
        }

        // ── Detail screens — slide up/down with fade ──────────────────────

        composable(
            route     = Screen.AddEditTask.route,
            arguments = listOf(
                navArgument(Screen.ARG_TASK_ID) {
                    type         = NavType.LongType
                    defaultValue = -1L
                    nullable     = false
                }
            ),
            enterTransition    = { detailEnter },
            exitTransition     = { detailExit },
            popEnterTransition = { detailPopEnter },
            popExitTransition  = { detailPopExit }
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments
                ?.getLong(Screen.ARG_TASK_ID)
                ?.takeIf { it != -1L }
            AddEditTaskScreen(
                taskId       = taskId,
                onNavigateUp = { navController.navigateUp() },
                adManager    = adManager
            )
        }

        composable(
            route     = Screen.AddEditNote.route,
            arguments = listOf(
                navArgument(Screen.ARG_NOTE_ID) {
                    type         = NavType.LongType
                    defaultValue = -1L
                    nullable     = false
                }
            ),
            enterTransition    = { detailEnter },
            exitTransition     = { detailExit },
            popEnterTransition = { detailPopEnter },
            popExitTransition  = { detailPopExit }
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments
                ?.getLong(Screen.ARG_NOTE_ID)
                ?.takeIf { it != -1L }
            AddEditNoteScreen(
                noteId       = noteId,
                onNavigateUp = { navController.navigateUp() },
                adManager    = adManager
            )
        }
    }
}