package com.nightcheck.ui.addeditnote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    noteId: Long?,
    onNavigateUp: () -> Unit,
    viewModel: AddEditNoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditing = noteId != null

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onNavigateUp() }

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
                title = { Text(if (isEditing) "Edit Note" else "New Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Pin toggle
                    IconButton(onClick = viewModel::togglePin) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = if (uiState.isPinned) "Unpin" else "Pin",
                            tint = if (uiState.isPinned)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (isEditing) {
                        IconButton(onClick = viewModel::delete) {
                            Icon(Icons.Default.Delete, "Delete note")
                        }
                    }
                    TextButton(onClick = viewModel::save) { Text("Save") }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.body,
                onValueChange = viewModel::onBodyChange,
                label = { Text("Note") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                maxLines = Int.MAX_VALUE
            )
        }
    }
}
