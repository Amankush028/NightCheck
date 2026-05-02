@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
package com.nightcheck.ui.addedittask

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcheck.domain.model.Priority
import com.nightcheck.ui.theme.LocalNightcheckColors
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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
            containerColor   = scheme.surfaceVariant,
            title = {
                Text(
                    "Delete Task",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = scheme.onSurface
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete this task? This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = nc.textMuted
                )
            },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.delete() }) {
                    Text("Delete", color = nc.destructive, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = nc.textMuted)
                }
            }
        )
    }

    Scaffold(
        containerColor  = scheme.background,
        snackbarHost    = { SnackbarHost(snackbarHostState) },
        topBar = {
            // ── Custom top bar ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint               = scheme.onBackground
                    )
                }

                // Title
                Text(
                    text      = if (isEditing) "Edit Task" else "New Task",
                    style     = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 20.sp
                    ),
                    color     = scheme.onBackground,
                    modifier  = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                // Delete (edit mode only)
                if (isEditing) {
                    IconButton(
                        onClick  = { showDeleteDialog = true },
                        enabled  = !uiState.isLoading
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete task",
                            tint               = nc.destructive
                        )
                    }
                } else {
                    // Spacer to balance the layout
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        },
        bottomBar = {
            // ── Save button pinned at bottom ───────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick        = viewModel::save,
                    enabled        = !uiState.isLoading,
                    modifier       = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape          = RoundedCornerShape(16.dp),
                    colors         = ButtonDefaults.buttonColors(
                        containerColor = scheme.primary,
                        contentColor   = scheme.onPrimary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier  = Modifier.size(20.dp),
                            color     = scheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text       = if (isEditing) "Save Changes" else "Create Task",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Title field ────────────────────────────────────────────────
            NightSection(label = "TITLE") {
                NightTextField(
                    value         = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder   = "What needs to be done?",
                    singleLine    = true,
                    fontSize      = 18
                )
            }

            // ── Description field ──────────────────────────────────────────
            NightSection(label = "DESCRIPTION") {
                NightTextField(
                    value         = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder   = "Add more details...",
                    minLines      = 2
                )
            }

            // ── Schedule ───────────────────────────────────────────────────
            NightSection(label = "SCHEDULE") {
                NightScheduleSection(
                    dueDate               = uiState.dueDate,
                    recurringTime         = uiState.recurringTime,
                    onScheduleModeChange  = viewModel::onScheduleModeChange,
                    onDateSelected        = viewModel::onDueDateChange,
                    onRecurringTimeChange = viewModel::onRecurringTimeChange
                )
            }

            // ── Priority ───────────────────────────────────────────────────
            NightSection(label = "PRIORITY") {
                NightPriorityRow(
                    selectedPriority   = uiState.priority,
                    onPrioritySelected = viewModel::onPriorityChange
                )
            }

            // ── Reminder ───────────────────────────────────────────────────
            NightSection(label = "REMINDER") {
                NightReminderRow(
                    reminderTime   = uiState.reminderTime,
                    onTimeSelected = viewModel::onReminderTimeChange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Section wrapper
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NightSection(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val nc = LocalNightcheckColors.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.4.sp,
                fontWeight    = FontWeight.Bold,
                color         = nc.textFaint
            )
        )
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Text field
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NightTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = false,
    minLines: Int = 1,
    fontSize: Int = 16
) {
    val nc     = LocalNightcheckColors.current
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(scheme.surface)
    ) {
        TextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = {
                Text(
                    placeholder,
                    color = nc.textFaint,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp)
                )
            },
            modifier   = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines   = minLines,
            colors     = TextFieldDefaults.colors(
                focusedTextColor        = scheme.onSurface,
                unfocusedTextColor      = scheme.onSurface,
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor  = Color.Transparent,
                cursorColor             = scheme.primary,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Schedule section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NightScheduleSection(
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

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        // One-time / Recurring toggle
        NightToggleRow(
            options          = listOf("One-time", "Recurring"),
            selectedIndex    = if (isRecurring) 1 else 0,
            onOptionSelected = { onScheduleModeChange(it == 1) }
        )

        // Date / time picker card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(scheme.surface)
                .clickable { if (isRecurring) showTimePicker = true else showDatePicker = true }
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                verticalAlignment      = Alignment.CenterVertically,
                horizontalArrangement  = Arrangement.SpaceBetween,
                modifier               = Modifier.fillMaxWidth()
            ) {
                if (isRecurring) {
                    Column {
                        Text(
                            "Repeats daily at",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.8.sp,
                                color         = nc.textFaint
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            recurringTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "Set time",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color      = scheme.onSurface
                            )
                        )
                    }
                } else {
                    val today    = LocalDate.now()
                    val tomorrow = today.plusDays(1)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text  = dueDate?.dayOfMonth?.toString() ?: "--",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color      = scheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            val label = when (dueDate) {
                                today    -> "Today"
                                tomorrow -> "Tomorrow"
                                else     -> dueDate?.format(DateTimeFormatter.ofPattern("EEEE"))
                                    ?: "Pick a date"
                            }
                            Text(
                                text  = label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color      = scheme.onSurface
                                )
                            )
                            Text(
                                text  = dueDate?.format(DateTimeFormatter.ofPattern("MMMM yyyy")) ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(color = nc.textFaint)
                            )
                        }
                    }
                }

                // "Change" pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(nc.overlay)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Change",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color      = scheme.primary
                        )
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
            containerColor   = MaterialTheme.colorScheme.surfaceVariant,
            confirmButton = {
                TextButton(onClick = {
                    onRecurringTimeChange(
                        LocalTime.of(timePickerState.hour, timePickerState.minute)
                    )
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

// ─────────────────────────────────────────────────────────────────────────────
//  Priority row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NightPriorityRow(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val nc     = LocalNightcheckColors.current

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Priority.entries.forEach { priority ->
            val isSelected = priority == selectedPriority

            val (accentColor, bgColor) = when (priority) {
                Priority.HIGH   -> scheme.error to scheme.errorContainer
                Priority.MEDIUM -> scheme.primary to nc.overlay
                Priority.LOW    -> nc.textMuted to scheme.surfaceVariant
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) bgColor else scheme.surface)
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) accentColor else nc.borderMuted,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onPrioritySelected(priority) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint     = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text  = priority.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color      = if (isSelected) accentColor else nc.textMuted
                        )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Reminder row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NightReminderRow(
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.surface)
            .clickable { showDatePicker = true }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bell icon box
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(nc.surfaceHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint               = scheme.primary,
                modifier           = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Set Reminder",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color      = scheme.onSurface
                )
            )
            Text(
                reminderTime?.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
                    ?: "No reminder set",
                style = MaterialTheme.typography.bodySmall.copy(color = nc.textFaint)
            )
        }

        if (reminderTime != null) {
            TextButton(
                onClick      = { onTimeSelected(null) },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text("Clear", color = nc.textMuted, fontSize = 12.sp)
            }
        } else {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint     = nc.textFaint,
                modifier = Modifier.size(20.dp)
            )
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
            containerColor   = scheme.surfaceVariant,
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

// ─────────────────────────────────────────────────────────────────────────────
//  Generic toggle row (One-time/Recurring, etc.)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NightToggleRow(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    val nc     = LocalNightcheckColors.current
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(scheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) nc.overlay else Color.Transparent)
                    .clickable { onOptionSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint     = scheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text  = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color      = if (isSelected) scheme.onSurface else nc.textMuted
                        )
                    )
                }
            }
        }
    }
}