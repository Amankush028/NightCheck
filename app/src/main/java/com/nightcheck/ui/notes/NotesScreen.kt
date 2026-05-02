package com.nightcheck.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcheck.ui.components.NoteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateToAddNote: () -> Unit,
    onNavigateToNote: (Long) -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val notes       by viewModel.notes.collectAsStateWithLifecycle()
    var isGridView  by remember { mutableStateOf(true) }

    // Remember column count so it doesn't recalculate on every recomposition
    val columns = remember(isGridView) {
        StaggeredGridCells.Fixed(if (isGridView) 2 else 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title   = { Text("Notes") },
                actions = {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector        = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                            contentDescription = if (isGridView) "List view" else "Grid view"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddNote) {
                Icon(Icons.Default.Add, contentDescription = "Add note")
            }
        }
    ) { innerPadding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            ) {
                Text(
                    "No notes yet. Tap + to create one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns             = columns,
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding      = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp
            ) {
                // Stable key — only changed notes recompose
                items(notes, key = { it.id }) { note ->
                    NoteCard(
                        note         = note,
                        onClick      = { onNavigateToNote(note.id) },
                        onTogglePin  = { viewModel.togglePin(note) }
                    )
                }
            }
        }
    }
}