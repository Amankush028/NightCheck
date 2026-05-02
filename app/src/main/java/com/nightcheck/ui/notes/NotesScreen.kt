package com.nightcheck.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcheck.ui.components.NoteCard
import com.nightcheck.ui.theme.LocalNightcheckColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateToAddNote: () -> Unit,
    onNavigateToNote: (Long) -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val notes       by viewModel.notes.collectAsStateWithLifecycle()
    var isGridView  by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val scheme = MaterialTheme.colorScheme
    val nc     = LocalNightcheckColors.current

    val filteredPinned = remember(notes, searchQuery) {
        notes.filter { it.isPinned }.let { list ->
            if (searchQuery.isBlank()) list
            else list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.body.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    val filteredUnpinned = remember(notes, searchQuery) {
        notes.filter { !it.isPinned }.let { list ->
            if (searchQuery.isBlank()) list
            else list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.body.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = scheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onNavigateToAddNote,
                containerColor = scheme.primary,
                contentColor   = scheme.onPrimary,
                shape          = RoundedCornerShape(16.dp),
                modifier       = Modifier.size(50.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add note",
                    modifier           = Modifier.size(22.dp)
                )
            }
        }
    ) { innerPadding ->

        LazyVerticalStaggeredGrid(
            columns               = StaggeredGridCells.Fixed(if (isGridView) 2 else 1),
            modifier              = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding        = PaddingValues(
                start  = 20.dp,
                end    = 20.dp,
                top    = 20.dp,
                bottom = 120.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing   = 10.dp
        ) {

            // ── Header ─────────────────────────────────────────────────────
            item(span = StaggeredGridItemSpan.FullLine, key = "header") {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text  = "Notes",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight    = FontWeight.Bold,
                            fontSize      = 28.sp,
                            letterSpacing = (-0.5).sp,
                            color         = scheme.onBackground
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(scheme.surface)
                            .border(1.dp, nc.borderMuted, RoundedCornerShape(10.dp))
                            .clickable { isGridView = !isGridView },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                            contentDescription = if (isGridView) "List view" else "Grid view",
                            tint               = nc.textMuted,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ── Search bar ─────────────────────────────────────────────────
            item(span = StaggeredGridItemSpan.FullLine, key = "search") {
                BasicTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine    = true,
                    textStyle     = MaterialTheme.typography.bodyMedium.copy(
                        color = scheme.onSurface
                    ),
                    cursorBrush = SolidColor(scheme.primary),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(scheme.surface)
                                .border(1.dp, nc.borderMuted, RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint               = nc.textFaint,
                                modifier           = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            // Box stacks placeholder behind the real field — no overlap
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text  = "Search notes...",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = nc.textFaint
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ── Pinned section ─────────────────────────────────────────────
            if (filteredPinned.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine, key = "pinned_label") {
                    NoteSectionLabel("Pinned")
                }
                items(
                    items = filteredPinned,
                    key   = { "pinned_${it.id}" },
                    span  = { StaggeredGridItemSpan.FullLine }
                ) { note ->
                    NoteCard(
                        note        = note,
                        onClick     = { onNavigateToNote(note.id) },
                        onTogglePin = { viewModel.togglePin(note) }
                    )
                }
                item(span = StaggeredGridItemSpan.FullLine, key = "allnotes_spacer") {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // ── All Notes section ──────────────────────────────────────────
            if (filteredUnpinned.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine, key = "all_label") {
                    NoteSectionLabel("All Notes")
                }
                items(
                    items = filteredUnpinned,
                    key   = { it.id }
                ) { note ->
                    NoteCard(
                        note        = note,
                        onClick     = { onNavigateToNote(note.id) },
                        onTogglePin = { viewModel.togglePin(note) }
                    )
                }
            }

            // ── Empty state (no notes at all) ──────────────────────────────
            if (notes.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine, key = "empty") {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = "No notes yet. Tap + to create one.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = nc.textMuted)
                        )
                    }
                }
            }

            // ── No search results ──────────────────────────────────────────
            if (notes.isNotEmpty() && filteredPinned.isEmpty() && filteredUnpinned.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine, key = "no_results") {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = "No notes match \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium.copy(color = nc.textMuted)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Section label
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoteSectionLabel(text: String) {
    val nc = LocalNightcheckColors.current
    Text(
        text     = text.uppercase(),
        style    = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.2.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = nc.textFaint
        ),
        modifier = Modifier.padding(bottom = 10.dp)
    )
}