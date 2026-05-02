@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
package com.nightcheck.ui.addedittask

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcheck.domain.model.Priority
import com.nightcheck.ui.theme.LocalNightcheckColors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    taskId: Long?,
    onNavigateUp: () -> Unit,
    viewModel: AddEditTaskViewModel = hiltViewModel()
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditing  = taskId != null
    val nc         = LocalNightcheckColors.current
    val scheme     = MaterialTheme.colorScheme

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateUp()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text  = { Text("Are you sure you want to delete this task? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.delete() }) {
                    Text("Delete", color = nc.destructive)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditing) "Edit Task" else "New Task",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = scheme.onBackground
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(
                            onClick  = { showDeleteDialog = true },
                            enabled  = !uiState.isLoading
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete task",
                                tint = nc.destructive
                            )
                        }
                    }
                    TextButton(onClick = viewModel::save, enabled = !uiState.isLoading) {
                        Text(
                            "Save",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = scheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor    = scheme.background,
                    titleContentColor = scheme.onBackground
                )
            )
        },
        snackbarHost    = { SnackbarHost(snackbarHostState) },
        containerColor  = scheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Column {
                SectionLabel("TITLE *")
                TransparentTextField(
                    value         = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder   = "What needs to be done?",
                    singleLine    = true
                )
            }

            Column {
                SectionLabel("DESCRIPTION")
                TransparentTextField(
                    value         = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder   = "Add more details...",
                    minLines      = 1
                )
            }

            ScheduleSection(
                dueDate              = uiState.dueDate,
                recurringTime        = uiState.recurringTime,
                onScheduleModeChange = viewModel::onScheduleModeChange,
                onDateSelected       = viewModel::onDueDateChange,
                onRecurringTimeChange = viewModel::onRecurringTimeChange
            )

            PrioritySection(
                selectedPriority  = uiState.priority,
                onPrioritySelected = viewModel::onPriorityChange
            )

            ReminderSection(
                reminderTime  = uiState.reminderTime,
                onTimeSelected = viewModel::onReminderTimeChange
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier   = Modifier.fillMaxWidth().height(2.dp),
                    color      = scheme.primary,
                    trackColor = scheme.outline
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val nc = LocalNightcheckColors.current
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.2.sp,
            fontWeight    = FontWeight.Bold
        ),
        color    = nc.textFaint,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun TransparentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = false,
    minLines: Int = 1
) {
    val nc     = LocalNightcheckColors.current
    val scheme = MaterialTheme.colorScheme
    TextField(
        value         = value,
        onValueChange = onValueChange,
        placeholder   = {
            Text(
                placeholder,
                color = nc.textFaint,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        modifier   = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines   = minLines,
        colors     = TextFieldDefaults.colors(
            focusedTextColor        = scheme.onBackground,
            unfocusedTextColor      = scheme.onBackground,
            focusedContainerColor   = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
            disabledContainerColor  = androidx.compose.ui.graphics.Color.Transparent,
            cursorColor             = scheme.primary,
            focusedIndicatorColor   = scheme.outline,
            unfocusedIndicatorColor = scheme.outline
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
    )
}

@Composable
private fun ScheduleSection(
    dueDate: LocalDate?,
    recurringTime: LocalTime?,
    onScheduleModeChange: (Boolean) -> Unit,
    onDateSelected: (LocalDate?) -> Unit,
    onRecurringTimeChange: (LocalTime?) -> Unit
) {
    val isRecurring = recurringTime != null
    val nc          = LocalNightcheckColors.current
    val scheme      = MaterialTheme.colorScheme

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDate
            ?.atStartOfDay(ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli()
    )
    val timePickerState = rememberTimePickerState(
        initialHour   = recurringTime?.hour ?: 9,
        initialMinute = recurringTime?.minute ?: 0
    )

    Column {
        SectionLabel("SCHEDULE")

        CustomToggleRow(
            options         = listOf("One-time", "Recurring"),
            selectedIndex   = if (isRecurring) 1 else 0,
            onOptionSelected = { index -> onScheduleModeChange(index == 1) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { if (isRecurring) showTimePicker = true else showDatePicker = true },
            color = scheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isRecurring) {
                    Column {
                        Text(
                            "Repeats daily at",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                            color = nc.textFaint
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            recurringTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "Set time",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = scheme.onSurface
                        )
                    }
                } else {
                    val today    = LocalDate.now()
                    val tomorrow = today.plusDays(1)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text  = dueDate?.dayOfMonth?.toString() ?: "--",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = scheme.primary
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            val label = when (dueDate) {
                                today    -> "Today"
                                tomorrow -> "Tomorrow"
                                else     -> dueDate?.format(DateTimeFormatter.ofPattern("EEEE")) ?: "Pick a date"
                            }
                            Text(
                                text  = label,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = scheme.onSurface
                            )
                            Text(
                                text  = dueDate?.format(DateTimeFormatter.ofPattern("MMMM yyyy")) ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = nc.textFaint
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(nc.overlay)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Change",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = scheme.primary
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        )
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onRecurringTimeChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showTimePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

@Composable
private fun PrioritySection(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    Column {
        SectionLabel("PRIORITY")
        CustomToggleRow(
            options          = Priority.entries.map { it.label },
            selectedIndex    = selectedPriority.ordinal,
            onOptionSelected = { index -> onPrioritySelected(Priority.entries[index]) }
        )
    }
}

@Composable
private fun CustomToggleRow(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    val nc     = LocalNightcheckColors.current
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(scheme.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        options.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isSelected) nc.overlay else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onOptionSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint     = scheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text  = title,
                        color = if (isSelected) scheme.onSurface else nc.textMuted,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderSection(
    reminderTime: LocalDateTime?,
    onTimeSelected: (LocalDateTime?) -> Unit
) {
    val nc     = LocalNightcheckColors.current
    val scheme = MaterialTheme.colorScheme

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempDate by remember { mutableStateOf<LocalDate?>(null) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { showDatePicker = true },
        color = scheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(nc.surfaceHigh, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = scheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Set Reminder",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = scheme.onSurface
                )
                Text(
                    reminderTime?.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
                        ?: "Tap to set reminder",
                    style = MaterialTheme.typography.bodySmall,
                    color = nc.textFaint
                )
            }

            if (reminderTime != null) {
                TextButton(onClick = { onTimeSelected(null) }) {
                    Text("Clear", color = nc.textMuted)
                }
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = nc.textFaint
                )
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        tempDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        showDatePicker = false
                        showTimePicker = true
                    }
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = tempDate
                    if (date != null) {
                        val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        onTimeSelected(LocalDateTime.of(date, time))
                    }
                    showTimePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}