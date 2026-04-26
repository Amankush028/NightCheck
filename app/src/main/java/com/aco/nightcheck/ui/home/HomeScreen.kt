package com.nightcheck.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcheck.domain.model.Note
import com.nightcheck.domain.model.Task
import com.nightcheck.ui.components.NoteCard
import com.nightcheck.ui.components.TaskCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onNavigateToAddTask: () -> Unit,
    onNavigateToAddNote: () -> Unit,
    onNavigateToTask: (Long) -> Unit,
    onNavigateToNote: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val todayTasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val pinnedNotes by viewModel.pinnedNotes.collectAsStateWithLifecycle()

    var showFabMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date header
            item {
                Text(
                    text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
            }

            // Today's tasks section
            item {
                SectionHeader(
                    title = "Today's Tasks",
                    count = todayTasks.size
                )
            }

            if (todayTasks.isEmpty()) {
                item {
                    EmptyStateHint("No tasks due today — enjoy your day!")
                }
            } else {
                items(todayTasks, key = { it.id }) { task ->
                    TaskCard(task = task, onClick = { onNavigateToTask(task.id) })
                }
            }

            // Pinned notes section (only shown if there are pinned notes)
            if (pinnedNotes.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader(title = "Pinned Notes", count = pinnedNotes.size)
                }
                items(pinnedNotes, key = { "note_${it.id}" }) { note ->
                    NoteCard(note = note, onClick = { onNavigateToNote(note.id) })
                }
            }

            // Bottom padding for FAB
            item { Spacer(Modifier.height(80.dp)) }
        }

        // FAB with expanded menu
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showFabMenu) {
                SmallFab(
                    icon = Icons.Default.NoteAdd,
                    label = "Add Note",
                    onClick = { showFabMenu = false; onNavigateToAddNote() }
                )
                SmallFab(
                    icon = Icons.Default.Add,
                    label = "Add Task",
                    onClick = { showFabMenu = false; onNavigateToAddTask() }
                )
            }

            FloatingActionButton(onClick = { showFabMenu = !showFabMenu }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (showFabMenu) "Close menu" else "Quick add"
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (count > 0) {
            Badge { Text(count.toString()) }
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun EmptyStateHint(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SmallFab(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondaryContainer,
            tonalElevation = 2.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
        SmallFloatingActionButton(onClick = onClick) {
            Icon(icon, contentDescription = label)
        }
    }
}
