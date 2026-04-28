package com.nightcheck.ui.review

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcheck.ui.theme.NightcheckTheme
import dagger.hilt.android.AndroidEntryPoint
import android.os.Build
import android.view.WindowManager

@AndroidEntryPoint
class EndOfDayReviewActivity : ComponentActivity() {

    private val viewModel: EndOfDayReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This makes the Activity show over the lock screen and wakes the screen up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        enableEdgeToEdge()
        setContent {
            NightcheckTheme {
                EndOfDayReviewContent(
                    viewModel = viewModel,
                    onFinish = { finish() }
                )
            }
        }
    }
}

@Composable
private fun EndOfDayReviewContent(
    viewModel: EndOfDayReviewViewModel,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSummaryDialog by remember { mutableStateOf(false) }

    // Summary dialog
    if (showSummaryDialog) {
        SummaryDialog(
            completed = uiState.completedCount,
            total = uiState.totalCount,
            onDismiss = { showSummaryDialog = false; onFinish() }
        )
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "End of Day",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Review today's tasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = { showSummaryDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp)
                ) {
                    Text("Done for the day")
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", style = MaterialTheme.typography.displayLarge)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "All clear! No pending tasks.",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "${uiState.totalCount} pending task(s)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                }

                items(uiState.items, key = { it.task.id }) { item ->
                    ReviewTaskCard(
                        item = item,
                        onMarkComplete = { viewModel.markComplete(item.task.id) },
                        onSnooze = { viewModel.snooze(item.task.id) },
                        onDismiss = { viewModel.dismiss(item.task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewTaskCard(
    item: ReviewTaskItem,
    onMarkComplete: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    val actioned = item.action != ReviewAction.NONE

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (item.action) {
                ReviewAction.COMPLETED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ReviewAction.SNOOZED  -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ReviewAction.DISMISSED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ReviewAction.NONE     -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Task title + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (item.action == ReviewAction.COMPLETED)
                        TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )
                if (actioned) {
                    ActionBadge(item.action)
                }
            }

            item.task.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action buttons — only shown when not yet actioned
            AnimatedVisibility(visible = !actioned) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mark Complete
                    FilledTonalButton(
                        onClick = onMarkComplete,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp), // Reduces inner margins
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Done", style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false)
                    }

                    // Snooze to tomorrow
                    FilledTonalButton(
                        onClick = onSnooze,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Delay", style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false)
                    }

                    // Dismiss
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Skip", style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionBadge(action: ReviewAction) {
    val (label, color) = when (action) {
        ReviewAction.COMPLETED -> "Completed" to MaterialTheme.colorScheme.primary
        ReviewAction.SNOOZED  -> "Tomorrow"  to MaterialTheme.colorScheme.tertiary
        ReviewAction.DISMISSED -> "Dismissed" to MaterialTheme.colorScheme.outline
        ReviewAction.NONE     -> return
    }
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun SummaryDialog(
    completed: Int,
    total: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Night check complete") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "$completed of $total tasks completed",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                val remaining = total - completed
                if (remaining > 0) {
                    Text(
                        "$remaining task(s) moved to tomorrow or dismissed.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text("All tasks handled — great work! 🌙", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}
