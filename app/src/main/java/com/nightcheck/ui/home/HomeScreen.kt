package com.nightcheck.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.ui.components.TaskCard
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.ui.res.painterResource
import com.nightcheck.R // Make sure this matches your package
import com.nightcheck.ui.theme.LocalIsDarkTheme
import com.nightcheck.ui.theme.LocalThemeToggle

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

    val isDarkTheme = LocalIsDarkTheme.current
    val onThemeToggle = LocalThemeToggle.current

    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    val completedTasksCount = todayTasks.count { it.status == TaskStatus.COMPLETED }
    val totalTasks = todayTasks.size
    val progress = if (totalTasks > 0) completedTasksCount.toFloat() / totalTasks else 0f

    // Hoist color lookups out of DrawScope
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Replaced the CircleShape Surface with the Nightcheck Logo
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Swap with your exact logo drawable if different
                            contentDescription = "NightCheck Logo",
                            tint = primaryColor,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "NIGHTCHECK",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onBackground // Changed from Color.White to be theme-aware
                        )
                    }

                    // Replaced Notifications Icon with a Theme Toggle Button
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Greeting and Date
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text(
                        text = greeting.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = primaryColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            // Progress Ring
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                ) {
                    val ringSize = 200.dp
                    val strokeWidth = 14.dp
                    
                    // Background Circle
                    Canvas(modifier = Modifier.size(ringSize)) {
                        drawArc(
                            color = Color(0xFF1A1A1A),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                        )
                        
                        // Progress Arc with Gradient and Glow effect
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(primaryColor, Color(0xFF8B5CF6), primaryColor)
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (totalTasks > 0) "$completedTasksCount/$totalTasks" else "0/0",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "DONE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Today's Tasks Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S TASKS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "VIEW ALL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            if (todayTasks.isEmpty()) {
                item {
                    Text(
                        text = "No tasks for today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                items(todayTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onNavigateToTask(task.id) },
                        onToggleStatus = { newStatus -> viewModel.toggleTaskStatus(task, newStatus) },
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                    )
                }
            }

            // Pinned Notes Header
            // Pinned Notes Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp, bottom = 16.dp), // <-- Split into two calls!
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PINNED NOTES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "VIEW ALL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            // Pinned Notes Grid (2 items per row)
            if (pinnedNotes.isEmpty()) {
                item {
                    Text(
                        text = "No pinned notes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                // .chunked(2) splits the list into pairs so we can render two cards per row
                items(pinnedNotes.chunked(2)) { rowNotes ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (note in rowNotes) {
                            // Note Card
                            Card(
                                onClick = { onNavigateToNote(note.id) },
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Adjust 'title' and 'content' based on your actual Note model
                                    Text(
                                        text = note.title ?: "Untitled",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = note.body ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                        maxLines = 3
                                    )
                                }
                            }
                        }

                        // If there is an odd number of notes, this fills the remaining space so the last card doesn't stretch
                        if (rowNotes.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onNavigateToAddTask,
            containerColor = primaryColor,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(28.dp))
        }
    }
}
