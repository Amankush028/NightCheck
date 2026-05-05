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
import com.nightcheck.ads.AdManager
import com.nightcheck.ads.BannerAdView
import com.nightcheck.billing.PremiumViewModel
import com.nightcheck.ui.components.NoteCard
import com.nightcheck.ui.paywall.LimitReachedDialog
import com.nightcheck.ui.paywall.PaywallReason
import com.nightcheck.ui.paywall.PaywallSheet
import com.nightcheck.ui.theme.LocalNightcheckColors

/**
 * NotesScreen with:
 *  - Banner ad at the bottom (free tier only)
 *  - Note-limit enforcement via [AddEditNoteViewModel]'s showNoteLimitDialog flag
 *    (the limit check happens in the VM when Save is tapped in AddEditNoteScreen;
 *     the limit FAB guard here prevents even navigating to the add screen when full)
 */
@Composable
fun NotesScreen(
    onNavigateToAddNote: () -> Unit,
    onNavigateToNote: (Long) -> Unit,
    viewModel: NotesViewModel = hiltViewModel(),
    premiumViewModel: PremiumViewModel = hiltViewModel(),
    adManager: AdManager
) {
    val notes      by viewModel.notes.collectAsStateWithLifecycle()
    val isPremium  by premiumViewModel.isPremium.collectAsStateWithLifecycle()

    var isGridView  by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showPaywall by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }

    val scheme = MaterialTheme.colorScheme
    val nc     = LocalNightcheckColors.current

    // Guard: block FAB if free and at limit (the VM also enforces this on save)
    val canAddNote = isPremium || notes.size < com.nightcheck.billing.UsageTracker.MAX_FREE_NOTES

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

    // ── Dialogs ────────────────────────────────────────────────────────────
    if (showLimitDialog) {
        LimitReachedDialog(
            title    = "Note limit reached",
            message  = "Free accounts support up to ${com.nightcheck.billing.UsageTracker.MAX_FREE_NOTES} notes. Upgrade to add unlimited notes.",
            onUpgrade = { showLimitDialog = false; showPaywall = true },
            onDismiss = { showLimitDialog = false }
        )
    }
    if (showPaywall) {
        PaywallSheet(
            reason    = PaywallReason.NoteLimit,
            onDismiss = { showPaywall = false }
        )
    }

    Scaffold(
        containerColor = scheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (canAddNote) onNavigateToAddNote()
                    else showLimitDialog = true
                },
                containerColor = scheme.primary,
                contentColor   = scheme.onPrimary,
                shape          = RoundedCornerShape(16.dp),
                modifier       = Modifier.size(50.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add note", modifier = Modifier.size(22.dp))
            }
        },
        // ── Banner ad anchored to bottom (free only) ─────────────────────
        bottomBar = {
            if (!isPremium) {
                BannerAdView(modifier = Modifier.fillMaxWidth().height(50.dp))
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
                bottom = 80.dp  // extra room above banner
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing   = 10.dp
        ) {

            // ── Header ─────────────────────────────────────────────────────
            item(span = StaggeredGridItemSpan.FullLine, key = "header") {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Notes",
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
                            if (isGridView) Icons.Default.List else Icons.Default.GridView,
                            contentDescription = null,
                            tint     = nc.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ── Free tier usage badge ──────────────────────────────────────
            if (!isPremium) {
                item(span = StaggeredGridItemSpan.FullLine, key = "usage_badge") {
                    FreeTierBadge(
                        current = notes.size,
                        max     = com.nightcheck.billing.UsageTracker.MAX_FREE_NOTES,
                        label   = "notes",
                        onUpgrade = { showPaywall = true }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            // ── Search bar ─────────────────────────────────────────────────
            item(span = StaggeredGridItemSpan.FullLine, key = "search") {
                BasicTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine    = true,
                    textStyle     = MaterialTheme.typography.bodyMedium.copy(color = scheme.onSurface),
                    cursorBrush   = SolidColor(scheme.primary),
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
                            Icon(Icons.Default.Search, null, tint = nc.textFaint, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(10.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text("Search notes...", style = MaterialTheme.typography.bodyMedium.copy(color = nc.textFaint))
                                }
                                innerTextField()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
            }

            if (filteredPinned.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine, key = "pinned_label") { NoteSectionLabel("Pinned") }
                items(filteredPinned, key = { "pinned_${it.id}" }, span = { StaggeredGridItemSpan.FullLine }) { note ->
                    NoteCard(note = note, onClick = { onNavigateToNote(note.id) }, onTogglePin = { viewModel.togglePin(note) })
                }
                item(span = StaggeredGridItemSpan.FullLine, key = "allnotes_spacer") { Spacer(Modifier.height(4.dp)) }
            }

            if (filteredUnpinned.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine, key = "all_label") { NoteSectionLabel("All Notes") }
                items(filteredUnpinned, key = { it.id }) { note ->
                    NoteCard(note = note, onClick = { onNavigateToNote(note.id) }, onTogglePin = { viewModel.togglePin(note) })
                }
            }

            if (notes.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine, key = "empty") {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
                        Text("No notes yet. Tap + to create one.", style = MaterialTheme.typography.bodyMedium.copy(color = nc.textMuted))
                    }
                }
            }

            if (notes.isNotEmpty() && filteredPinned.isEmpty() && filteredUnpinned.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine, key = "no_results") {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        Text("No notes match \"$searchQuery\"", style = MaterialTheme.typography.bodyMedium.copy(color = nc.textMuted))
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteSectionLabel(text: String) {
    val nc = LocalNightcheckColors.current
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.2.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = nc.textFaint
        ),
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

/**
 * Small usage bar shown at the top of the notes list for free tier users.
 * At 80%+ capacity the bar turns amber, at 100% it shows red + upgrade nudge.
 */
@Composable
private fun FreeTierBadge(
    current: Int,
    max: Int,
    label: String,
    onUpgrade: () -> Unit
) {
    val scheme   = MaterialTheme.colorScheme
    val nc       = LocalNightcheckColors.current
    val fraction = (current.toFloat() / max).coerceIn(0f, 1f)
    val barColor = when {
        fraction >= 1f   -> MaterialTheme.colorScheme.error
        fraction >= 0.8f -> androidx.compose.ui.graphics.Color(0xFFF59E0B) // amber
        else             -> scheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(scheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "$current / $max $label",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = scheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(5.dp))
            LinearProgressIndicator(
                progress  = { fraction },
                modifier  = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                color     = barColor,
                trackColor = nc.borderMuted
            )
        }
        Spacer(Modifier.width(12.dp))
        if (current >= max) {
            TextButton(
                onClick        = onUpgrade,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    "Upgrade",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color      = scheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}