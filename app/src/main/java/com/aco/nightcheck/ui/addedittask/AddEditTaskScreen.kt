package com.nightcheck.ui.addedittask

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcheck.domain.model.Priority
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    taskId: Long?,
    onNavigateUp: () -> Unit,
    viewModel: AddEditTaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditing = taskId != null

    // Navigate back when saved
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateUp()
    }

    // Error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Task" else "New Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = viewModel::delete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete task")
                        }
                    }
                    TextButton(onClick = viewModel::save, enabled = !uiState.isLoading) {
                        Text("Save")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )

            // Due date (basic text field – a date picker dialog would replace this in V1)
            DueDateField(
                selectedDate = uiState.dueDate,
                onDateSelected = viewModel::onDueDateChange
            )

            // Priority selector
            PrioritySelector(
                selectedPriority = uiState.priority,
                onPrioritySelected = viewModel::onPriorityChange
            )

            // Reminder time
            ReminderTimeField(
                reminderTime = uiState.reminderTime,
                onTimeSelected = viewModel::onReminderTimeChange
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun DueDateField(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit
) {
    // Placeholder: In a real implementation wire up DatePickerDialog here
    OutlinedTextField(
        value = selectedDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "",
        onValueChange = { /* handled by picker */ },
        label = { Text("Due Date (YYYY-MM-DD)") },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("No due date") },
        readOnly = true,
        trailingIcon = {
            if (selectedDate != null) {
                TextButton(onClick = { onDateSelected(null) }) { Text("Clear") }
            }
        }
    )
}

@Composable
private fun PrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    Column {
        Text("Priority", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            Priority.entries.forEachIndexed { index, priority ->
                SegmentedButton(
                    selected = selectedPriority == priority,
                    onClick = { onPrioritySelected(priority) },
                    shape = SegmentedButtonDefaults.itemShape(index, Priority.entries.size),
                    label = { Text(priority.label) }
                )
            }
        }
    }
}

@Composable
private fun ReminderTimeField(
    reminderTime: LocalDateTime?,
    onTimeSelected: (LocalDateTime?) -> Unit
) {
    OutlinedTextField(
        value = reminderTime?.format(DateTimeFormatter.ofPattern("MMM d, h:mm a")) ?: "",
        onValueChange = {},
        label = { Text("Reminder") },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("No reminder") },
        readOnly = true,
        trailingIcon = {
            if (reminderTime != null) {
                TextButton(onClick = { onTimeSelected(null) }) { Text("Clear") }
            }
        }
    )
}
