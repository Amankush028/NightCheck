package com.nightcheck.ui.addeditnote

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
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

    val wordCount = uiState.body.trim().split("\\s+".toRegex()).count { it.isNotEmpty() }
    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) }

    var showColorPicker by remember { mutableStateOf(false) }

    val noteBackground = uiState.colorHex?.let { Color(it.toColorInt()) } ?: MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = noteBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = viewModel::togglePin) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = if (uiState.isPinned) "Unpin" else "Pin",
                        tint = if (uiState.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    if (showColorPicker) {
                        ColorPickerRow(onColorSelected = { 
                            viewModel.onColorChange(it)
                            showColorPicker = false
                        })
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = viewModel::toggleBold) { 
                            Icon(
                                Icons.Default.FormatBold, 
                                contentDescription = "Bold", 
                                tint = if (uiState.isBold) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            ) 
                        }
                        IconButton(onClick = viewModel::toggleItalic) { 
                            Icon(
                                Icons.Default.FormatItalic, 
                                contentDescription = "Italic", 
                                tint = if (uiState.isItalic) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            ) 
                        }
                        IconButton(onClick = { showColorPicker = !showColorPicker }) { 
                            Icon(Icons.Default.Palette, contentDescription = "Color") 
                        }
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
        ) {
            TextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                placeholder = { Text("Title", fontSize = 26.sp, fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(currentDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(3.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text("$wordCount words", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            TextField(
                value = uiState.body,
                onValueChange = viewModel::onBodyChange,
                placeholder = { Text("Start writing…") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 400.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = if (uiState.isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (uiState.isItalic) FontStyle.Italic else FontStyle.Normal,
                    color = MaterialTheme.colorScheme.onSurface
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

@Composable
fun ColorPickerRow(onColorSelected: (String?) -> Unit) {
    val colors = listOf(
        null, // Default
        "#FFF8E1", // Amber
        "#E8F5E9", // Green
        "#E3F2FD", // Blue
        "#F3E5F5", // Purple
        "#FFEBEE"  // Red
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        colors.forEach { colorHex ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colorHex?.let { Color(it.toColorInt()) } ?: MaterialTheme.colorScheme.surface)
                    .border(1.dp, Color.Gray, CircleShape)
                    .clickable { onColorSelected(colorHex) }
            )
        }
    }
}
