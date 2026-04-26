package com.nightcheck.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nightcheck.domain.model.Note
import com.nightcheck.domain.model.Priority
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import java.time.format.DateTimeFormatter

// ── TaskCard ──────────────────────────────────────────────────────────────────

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when (task.status) {
                TaskStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
                TaskStatus.SNOOZED   -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else                  -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator strip
            PriorityIndicator(priority = task.priority)
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!task.description.isNullOrBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                task.dueDate?.let { date ->
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("MMM d")),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Status chip
            StatusChip(status = task.status)
        }
    }
}

@Composable
private fun PriorityIndicator(priority: Priority) {
    val color = when (priority) {
        Priority.HIGH   -> MaterialTheme.colorScheme.error
        Priority.MEDIUM -> MaterialTheme.colorScheme.primary
        Priority.LOW    -> MaterialTheme.colorScheme.outline
    }
    Surface(
        modifier = Modifier
            .width(4.dp)
            .height(48.dp),
        shape = MaterialTheme.shapes.small,
        color = color
    ) {}
}

@Composable
private fun StatusChip(status: TaskStatus) {
    if (status == TaskStatus.PENDING) return
    val label = when (status) {
        TaskStatus.COMPLETED -> "Done"
        TaskStatus.SNOOZED   -> "Snoozed"
        else                  -> return
    }
    AssistChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
    )
}

// ── NoteCard ──────────────────────────────────────────────────────────────────

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.body.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = note.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (onTogglePin != null) {
                IconButton(onClick = onTogglePin) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = if (note.isPinned) "Unpin" else "Pin",
                        tint = if (note.isPinned)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
