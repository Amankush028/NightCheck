package com.nightcheck.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcheck.R
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.ui.components.TaskCard
import com.nightcheck.ui.theme.LocalIsDarkTheme
import com.nightcheck.ui.theme.LocalNightcheckColors
import com.nightcheck.ui.theme.LocalThemeToggle
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onNavigateToAddTask: () -> Unit,
    onNavigateToAddNote: () -> Unit,
    onNavigateToTask: (Long) -> Unit,
    onNavigateToNote: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val todayTasks  by viewModel.todayTasks.collectAsStateWithLifecycle()
    val pinnedNotes by viewModel.pinnedNotes.collectAsStateWithLifecycle()

    val isDarkTheme   = LocalIsDarkTheme.current
    val onThemeToggle = LocalThemeToggle.current
    val nc            = LocalNightcheckColors.current
    val scheme        = MaterialTheme.colorScheme

    // ── Stable derived state ─────────────────────────────────────────────────
    // derivedStateOf ensures these only recompute when their inputs actually change,
    // not on every recomposition of HomeScreen.
    val greeting by remember {
        derivedStateOf {
            when (LocalTime.now().hour) {
                in 0..11  -> "Good morning"
                in 12..16 -> "Good afternoon"
                else      -> "Good evening"
            }
        }
    }

    // Format date once per composition, not per-frame
    val dateLabel = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    }

    val completedCount by remember(todayTasks) {
        derivedStateOf { todayTasks.count { it.status == TaskStatus.COMPLETED } }
    }
    val totalCount by remember(todayTasks) {
        derivedStateOf { todayTasks.size }
    }
    val rawProgress by remember(completedCount, totalCount) {
        derivedStateOf { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f }
    }

    // Smooth animated ring — avoids a jarring jump on task completion
    val animatedProgress by animateFloatAsState(
        targetValue   = rawProgress,
        animationSpec = tween(durationMillis = 500),
        label         = "ring_progress"
    )

    // ── Hoist Colors out of Canvas ───────────────────────────────────────────
    // Reading ColorScheme INSIDE a Canvas DrawScope causes the canvas to
    // subscribe to snapshot changes and redraw every frame. Cache them here.
    val primaryColor    = scheme.primary
    val primaryDimColor = nc.primaryDim
    val trackColor      = scheme.surfaceVariant

    // Brush allocation is expensive; remember so it's only recreated on color change
    val progressBrush = remember(primaryColor, primaryDimColor) {
        Brush.sweepGradient(listOf(primaryColor, primaryDimColor, primaryColor))
    }

    // Pre-chunk notes so the chunked() call isn't repeated on every recomposition
    val chunkedNotes = remember(pinnedNotes) { pinnedNotes.chunked(2) }

    Box(modifier = Modifier.fillMaxSize().background(scheme.background)) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),

            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item(key = "header") {
                HomeHeader(
                    isDarkTheme   = isDarkTheme,
                    onThemeToggle = onThemeToggle,
                    primaryColor  = primaryColor,
                    onBackground  = scheme.onBackground
                )
            }

            item(key = "greeting") {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text(
                        text          = greeting.uppercase(),
                        style         = MaterialTheme.typography.labelSmall,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color         = primaryColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = dateLabel,
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.Medium,
                        color      = scheme.onBackground
                    )
                }
            }

            // Extracted subcomposable — only redraws when progress or colors change
            item(key = "progress_ring") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                ) {
                    ProgressRing(
                        progress      = animatedProgress,
                        progressBrush = progressBrush,
                        trackColor    = trackColor,
                        completed     = completedCount,
                        total         = totalCount,
                        textColor     = scheme.onBackground,
                        subTextColor  = nc.textMuted
                    )
                }
            }

            item(key = "tasks_header") {
                SectionHeader(title = "TODAY'S TASKS", labelColor = nc.textMuted)
            }

            if (todayTasks.isEmpty()) {
                item(key = "tasks_empty") {
                    Text(
                        text     = "No tasks for today",
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = nc.textFaint,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                // Stable `key` means only changed items recompose, not the whole list
                items(todayTasks, key = { it.id }) { task ->
                    TaskCard(
                        task           = task,
                        onClick        = { onNavigateToTask(task.id) },
                        onToggleStatus = { newStatus -> viewModel.toggleTaskStatus(task, newStatus) },
                        modifier       = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                    )
                }
            }

            item(key = "notes_header") {
                SectionHeader(
                    title      = "PINNED NOTES",
                    labelColor = nc.textMuted,
                    topPad     = 24.dp
                )
            }

            if (pinnedNotes.isEmpty()) {
                item(key = "notes_empty") {
                    Text(
                        text     = "No pinned notes",
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = nc.textFaint,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                items(chunkedNotes, key = { row -> row.first().id }) { rowNotes ->
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (note in rowNotes) {
                            Card(
                                onClick  = { onNavigateToNote(note.id) },
                                modifier = Modifier.weight(1f),
                                colors   = CardDefaults.cardColors(containerColor = scheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                ) {
                                    Text(
                                        text     = note.title.ifBlank { "Untitled" },
                                        style    = MaterialTheme.typography.titleSmall,
                                        color    = scheme.onBackground,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text     = note.body,
                                        style    = MaterialTheme.typography.bodySmall,
                                        color    = scheme.onSurfaceVariant,
                                        maxLines = 3
                                    )
                                }
                            }
                        }
                        if (rowNotes.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick        = onNavigateToAddTask,
            containerColor = primaryDimColor,
            contentColor   = scheme.onPrimary,
            shape          = CircleShape,
            modifier       = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(28.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stable sub-composables — splitting large composables limits recomposition
// scope: only the leaf that changed re-executes.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    primaryColor: Color,
    onBackground: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter           = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "NightCheck Logo",
                tint              = primaryColor,
                modifier          = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text          = "NIGHTCHECK",
                style         = MaterialTheme.typography.titleMedium,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.sp,
                color         = onBackground
            )
        }
        IconButton(onClick = onThemeToggle) {
            Icon(
                imageVector        = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle Theme",
                tint               = onBackground.copy(alpha = 0.7f),
                modifier           = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Isolated Canvas composable. Because colors are passed as plain [Color] values
 * (not read from LocalCompositionLocals inside DrawScope), this only redraws
 * when [progress] or colors actually change.
 */
@Composable
private fun ProgressRing(
    progress: Float,
    progressBrush: Brush,
    trackColor: Color,
    completed: Int,
    total: Int,
    textColor: Color,
    subTextColor: Color,
) {
    Box(contentAlignment = Alignment.Center) {
        val strokeWidthDp = 14.dp
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokePx = strokeWidthDp.toPx()
            drawArc(
                color      = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter  = false,
                style      = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            if (progress > 0f) {
                drawArc(
                    brush      = progressBrush,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter  = false,
                    style      = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = if (total > 0) "$completed/$total" else "0/0",
                fontSize   = 48.sp,
                fontWeight = FontWeight.Bold,
                color      = textColor
            )
            Text(
                text          = "DONE",
                fontSize      = 12.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 1.sp,
                color         = subTextColor
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    labelColor: Color,
    topPad: Dp = 0.dp,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = if (topPad > 0.dp) topPad else 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text          = title,
            style         = MaterialTheme.typography.labelMedium,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 1.sp,
            color         = labelColor
        )
        Text(
            text       = "VIEW ALL",
            fontSize   = 10.sp,
            fontWeight = FontWeight.Bold,
            color      = labelColor
        )
    }
}