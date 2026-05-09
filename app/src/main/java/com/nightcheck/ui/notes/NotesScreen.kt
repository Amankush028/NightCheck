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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
 * NotesScreen redesigned to match HTML reference:
 *  - Header with note count badge
 *  - Pinned notes rendered as full-width cards with primary-color top border accent
 *  - Banner ad inline after pinned section, styled like a pinned note card (free tier only)
 *  - Unpinned notes in staggered 2-col grid
 *  - No bottomBar ad — ad lives inline in the list
 */
@Composable
fun NotesScreen(
    onNavigateToAddNote: () -> Unit,
    onNavigateToNote: (Long) -> Unit,
    viewModel: NotesViewModel = hiltViewModel(),
    premiumViewModel: PremiumViewModel = hiltViewModel(),
    adManager: AdManager
) {
    val notes     by viewModel.notes.collectAsStateWithLifecycle()
    val isPremium by premiumViewModel.isPremium.collectAsStateWithLifecycle()

    var isGridView      by remember { mutableStateOf(true) }
    var searchQuery     by remember { mutableStateOf("") }
    var showPaywall     by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }

    val scheme = MaterialTheme.colorScheme
    val nc     = LocalNightcheckColors.current

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
            title     = "Note limit reached",
            message   = "Free accounts support up to ${com.nightcheck.billing.UsageTracker.MAX_FREE_NOTES} notes. Upgrade to add unlimited notes.",
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
        // No bottomBar — ad is inline in the list now
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
                bottom = 100.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing   = 10.dp
        ) {

            // ── Header with note count ──────────────────────────────────────
            item(span = StaggeredGridItemSpan.FullLine, key = "header") {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        // Note count badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(scheme.surfaceVariant)
                                .border(1.dp, nc.borderMuted, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = "${notes.size}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color      = nc.textMuted,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 12.sp
                                )
                            )
                        }
                    }
                    // Grid/List toggle
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
                            if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
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
                        current   = notes.size,
                        max       = com.nightcheck.billing.UsageTracker.MAX_FREE_NOTES,
                        label     = "notes",
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
                                    Text(
                                        "Search notes...",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = nc.textFaint)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
            }

            // ── Pinned section ─────────────────────────────────────────────
            if (filteredPinned.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine, key = "pinned_label") {
                    NoteSectionLabel("Pinned")
                }
                // Pinned notes as full-width cards with accent top border
                items(filteredPinned, key = { "pinned_${it.id}" }, span = { StaggeredGridItemSpan.FullLine }) { note ->
                    NoteCard(
                        note         = note,
                        onClick      = { onNavigateToNote(note.id) },
                        onTogglePin  = { viewModel.togglePin(note) },
                        modifier     = Modifier.animateItem()
                    )
                }

                // ── Inline banner ad — after pinned notes, styled as pinned card ──
                if (!isPremium) {
                    item(span = StaggeredGridItemSpan.FullLine, key = "inline_ad") {
                        InlineAdCard(adManager = adManager)
                    }
                }

                item(span = StaggeredGridItemSpan.FullLine, key = "pinned_bottom_spacer") {
                    Spacer(Modifier.height(4.dp))
                }
            }

            // ── All Notes section ──────────────────────────────────────────
            if (filteredUnpinned.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine, key = "all_label") {
                    NoteSectionLabel("All Notes")
                }
                items(filteredUnpinned, key = { it.id }) { note ->
                    NoteCard(
                        note        = note,
                        onClick     = { onNavigateToNote(note.id) },
                        onTogglePin = { viewModel.togglePin(note) },
                        modifier    = Modifier.animateItem()
                    )
                }
            }

            // ── Empty state ────────────────────────────────────────────────
            if (notes.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine, key = "empty") {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No notes yet. Tap + to create one.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = nc.textMuted)
                        )
                    }
                }
            }

            // ── No search results ──────────────────────────────────────────
            if (notes.isNotEmpty() && filteredPinned.isEmpty() && filteredUnpinned.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine, key = "no_results") {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No notes match \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium.copy(color = nc.textMuted)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Banner ad wrapped in a card that mimics the pinned note style —
 * same surface background, border, rounded corners, and a primary-color
 * top-edge accent line — so it blends naturally into the pinned section.
 */
@Composable
private fun InlineAdCard(adManager: AdManager) {
    val scheme  = MaterialTheme.colorScheme
    val nc      = LocalNightcheckColors.current
    val primary = scheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surface)
            .border(1.dp, nc.borderMuted, RoundedCornerShape(20.dp))
            // Draw a 2dp primary-color gradient line along the top edge,
            // identical to how the HTML reference styles pinned note cards.
            .drawBehind {
                drawLine(
                    brush       = Brush.horizontalGradient(
                        colors      = listOf(primary, primary.copy(alpha = 0.5f)),
                        startX      = 0f,
                        endX        = size.width
                    ),
                    start       = Offset(0f, 1f),
                    end         = Offset(size.width, 1f),
                    strokeWidth = 4f
                )
            }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        BannerAdView(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}

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

/**
 * Free-tier usage progress bar shown below the header.
 * Turns amber at 80 % capacity, red at 100 %.
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
        fraction >= 1f   -> scheme.error
        fraction >= 0.8f -> androidx.compose.ui.graphics.Color(0xFFF59E0B)
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
                    color      = scheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(5.dp))
            LinearProgressIndicator(
                progress   = { fraction },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color      = barColor,
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
