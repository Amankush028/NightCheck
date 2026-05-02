package com.nightcheck.ui.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcheck.domain.model.Priority
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.ui.components.TaskCard
import com.nightcheck.ui.theme.LocalNightcheckColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onNavigateToAddTask: () -> Unit,
    onNavigateToTask: (Long) -> Unit,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme  = MaterialTheme.colorScheme
    val nc      = LocalNightcheckColors.current

    val totalTasks     = uiState.tasks.size
    val completedTasks = uiState.tasks.count { it.status == TaskStatus.COMPLETED }
    val progress       = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
    val animatedProgress by animateFloatAsState(
        targetValue  = progress,
        animationSpec = tween(durationMillis = 600),
        label        = "progress"
    )

    Scaffold(
        containerColor = scheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick           = onNavigateToAddTask,
                containerColor    = scheme.primary,
                contentColor      = scheme.onPrimary,
                shape             = CircleShape,
                modifier          = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add task", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Header ───────────────────────────────────────────────────────
            item(key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "Tasks",
                        style      = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize   = 28.sp,
                            color      = scheme.primary
                        )
                    )
                }
            }

            // ── Filter tabs ──────────────────────────────────────────────────
            item(key = "tabs") {
                TaskFilterTabRow(
                    selectedFilter = uiState.filter,
                    onFilterChange = viewModel::setFilter
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Progress banner ──────────────────────────────────────────────
            item(key = "progress_banner") {
                ProgressBanner(
                    completed = completedTasks,
                    total     = totalTasks,
                    progress  = animatedProgress
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Task list ────────────────────────────────────────────────────
            if (uiState.tasks.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment  = Alignment.Center
                    ) {
                        Text(
                            text  = "No tasks here yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = nc.textMuted
                        )
                    }
                }
            } else {
                items(uiState.tasks, key = { it.id }) { task ->
                    AnimatedVisibility(
                        visible = true,
                        enter   = fadeIn() + slideInVertically()
                    ) {
                        NightTaskCard(
                            task           = task,
                            onClick        = { onNavigateToTask(task.id) },
                            onToggleStatus = { newStatus -> viewModel.toggleTaskStatus(task, newStatus) }
                        )
                    }
                }
                item(key = "bottom_spacer") { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

// ── Filter Tab Row ────────────────────────────────────────────────────────────

@Composable
private fun TaskFilterTabRow(
    selectedFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val nc     = LocalNightcheckColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(scheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TaskFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            val label = when (filter) {
                TaskFilter.TODAY     -> "Today"
                TaskFilter.UPCOMING  -> "Upcoming"
                TaskFilter.COMPLETED -> "Done"
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) nc.overlay else Color.Transparent
                    )
                    .clickable { onFilterChange(filter) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color      = if (isSelected) scheme.onSurface else nc.textMuted
                    )
                )
            }
        }
    }
}

// ── Progress Banner ───────────────────────────────────────────────────────────

@Composable
private fun ProgressBanner(
    completed: Int,
    total: Int,
    progress: Float
) {
    val scheme = MaterialTheme.colorScheme
    val nc     = LocalNightcheckColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        nc.surfaceHigh,
                        scheme.surfaceVariant
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text  = "CURRENT PROGRESS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    color         = scheme.primary,
                    fontWeight    = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text  = "$completed of $total completed",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color      = scheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress          = { progress },
                modifier          = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color             = scheme.primary,
                trackColor        = nc.borderMuted
            )
        }
    }
}

// ── Task Card ─────────────────────────────────────────────────────────────────

@Composable
private fun NightTaskCard(
    task: Task,
    onClick: () -> Unit,
    onToggleStatus: (TaskStatus) -> Unit
) {
    val scheme      = MaterialTheme.colorScheme
    val nc          = LocalNightcheckColors.current
    val isCompleted = task.status == TaskStatus.COMPLETED

    val newStatus = if (isCompleted) TaskStatus.PENDING else TaskStatus.COMPLETED

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(scheme.surface)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        IconButton(
            onClick  = { onToggleStatus(newStatus) },
            modifier = Modifier.size(36.dp)
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Mark incomplete",
                    tint               = scheme.primary,
                    modifier           = Modifier.size(26.dp)
                )
            } else {
                Icon(
                    Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Mark complete",
                    tint               = nc.textMuted,
                    modifier           = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title + subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text           = task.title,
                style          = MaterialTheme.typography.titleMedium.copy(
                    fontWeight      = FontWeight.SemiBold,
                    color           = if (isCompleted) nc.textMuted else scheme.onSurface,
                    textDecoration  = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                ),
                maxLines       = 1,
                overflow       = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            val subLabel = buildDueSublabel(task)
            if (subLabel != null) {
                Text(
                    text  = subLabel,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = nc.textFaint
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Priority badge
        PriorityBadge(priority = task.priority)
    }
}

@Composable
private fun PriorityBadge(priority: Priority) {
    val scheme = MaterialTheme.colorScheme
    val nc     = LocalNightcheckColors.current

    val (label, bgColor, textColor) = when (priority) {
        Priority.HIGH   -> Triple("HIGH",   scheme.errorContainer,  scheme.error)
        Priority.MEDIUM -> Triple("MEDIUM", nc.overlay,             scheme.primary)
        Priority.LOW    -> Triple("LOW",    scheme.surfaceVariant,   nc.textMuted)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight    = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color         = textColor
            )
        )
    }
}

/** Returns a human-readable due string, or null if neither dueDate nor recurringDays is set. */
private fun buildDueSublabel(task: Task): String? {
    val today    = LocalDate.now()
    val tomorrow = today.plusDays(1)

    return when {
        task.status == TaskStatus.COMPLETED -> {
            "Completed"
        }
        task.dueDate != null -> {
            val prefix = when (task.dueDate) {
                today    -> "Due today"
                tomorrow -> "Due tomorrow"
                else     -> "Due ${task.dueDate.format(DateTimeFormatter.ofPattern("MMM d"))}"
            }
            val timePart = task.reminderTime?.let {
                " at ${it.toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a"))}"
            } ?: ""
            prefix + timePart
        }
        task.recurringDays != null -> "Repeats daily"
        else -> null
    }
}