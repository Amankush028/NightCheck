package com.nightcheck.ui.addeditnote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    // Dynamic word count mimicking the HTML logic
    val wordCount = uiState.body.trim().split("\\s+".toRegex()).count { it.isNotEmpty() }
    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Custom Top Toolbar matching your HTML design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = viewModel::togglePin) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = if (uiState.isPinned) "Unpin" else "Pin",
                        tint = if (uiState.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { /* TODO: Remind */ }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Remind", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { /* TODO: Archive */ }) {
                    Icon(Icons.Default.Archive, contentDescription = "Archive", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isEditing) {
                    IconButton(onClick = viewModel::delete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
                Button(
                    onClick = viewModel::save,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp).padding(start = 4.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        bottomBar = {
            // Bottom formatting bar matching HTML design
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.Checklist, "Checklist") }
                    IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.FormatBold, "Bold") }
                    IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.FormatItalic, "Italic") }
                    IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.FormatUnderlined, "Underline") }
                    IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.Palette, "Color") }
                    IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.MoreVert, "More") }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Title Input (Borderless, Large Text)
            TextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                placeholder = {
                    Text("Title", fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface // Fixes Dark Mode issue!
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Meta Details Row
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(currentDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(3.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text("$wordCount words", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(3.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Just now", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            // Body Input (Borderless)
            TextField(
                value = uiState.body,
                onValueChange = viewModel::onBodyChange,
                placeholder = {
                    Text("Start writing…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface // Fixes Dark Mode issue!
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }
}