package com.nightcheck.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }

    // Material3 TimePickerState
    val timePickerState = rememberTimePickerState(
        initialHour = uiState.endOfDayHour,
        initialMinute = uiState.endOfDayMinute,
        is24Hour = false
    )

    if (showTimePicker) {
        TimePickerDialog(
            state = timePickerState,
            onDismiss = { showTimePicker = false },
            onConfirm = {
                viewModel.updateEndOfDayTime(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                "Notifications",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // End-of-Day time setting
            ListItem(
                headlineContent = { Text("End-of-Day Review time") },
                supportingContent = { Text(uiState.formattedTime) },
                trailingContent = {
                    TextButton(onClick = { showTimePicker = true }) {
                        Text("Change")
                    }
                }
            )

            HorizontalDivider()

            Text(
                "About",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            ListItem(
                headlineContent = { Text("Nightcheck") },
                supportingContent = { Text("Version 1.0.0") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    state: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set review time") },
        text = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                TimePicker(state = state)
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("OK") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
