package com.nightcheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nightcheck.domain.model.Note
import com.nightcheck.domain.model.Priority
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.ui.theme.LocalNightcheckColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────────────────────────────────────
//  TaskCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onToggleStatus: (TaskStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = task.status == TaskStatus.COMPLETED
    val nc     = LocalNightcheckColors.current
    val scheme = MaterialTheme.colorScheme

    val onCheckboxClick = remember(task.id, isDone) {
        { onToggleStatus(if (isDone) TaskStatus.PENDING else TaskStatus.COMPLETED) }
    }

    val tagLabel = remember(task.priority) {
        when (task.priority) {
            Priority.HIGH   -> "URGENT"
            Priority.MEDIUM -> "MEDIUM"
            Priority.LOW    -> "LOW"
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        color = scheme.surface,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isDone) scheme.primary else Color.Transparent)
                    .border(2.dp, if (isDone) scheme.primary else scheme.outline, CircleShape)
                    .clickable(onClick = onCheckboxClick),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector        = Icons.Default.Check,
                        contentDescription = "Done",
                        tint               = scheme.onPrimary,
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text           = task.title,
                    fontSize       = 16.sp,
                    fontWeight     = FontWeight.Bold,
                    color          = if (isDone) nc.textFaint else scheme.onSurface,
                    textDecoration = if (isDone) TextDecoration.LineThrough else null,
                    maxLines       = 1,
                    overflow       = TextOverflow.Ellipsis
                )
                if (!isDone && !task.description.isNullOrBlank()) {
                    Text(
                        text     = task.description,
                        fontSize = 12.sp,
                        color    = nc.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .background(
                        scheme.primary.copy(alpha = if (isDone) 0.08f else 0.15f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text          = tagLabel,
                    color         = scheme.primary.copy(alpha = if (isDone) 0.4f else 1f),
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  NoteCard — dispatches to Pinned or Regular variant
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (note.isPinned) {
        PinnedNoteCard(
            note        = note,
            onClick     = onClick,
            onTogglePin = onTogglePin,
            modifier    = modifier
        )
    } else {
        RegularNoteCard(
            note        = note,
            onClick     = onClick,
            onTogglePin = onTogglePin,
            modifier    = modifier
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Pinned note — full-width, purple gradient top accent line
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PinnedNoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme      = MaterialTheme.colorScheme
    val nc          = LocalNightcheckColors.current
    val accentBrush = Brush.horizontalGradient(listOf(scheme.primary, nc.primaryDim))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surface)
            .border(1.dp, nc.borderMuted, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .drawBehind {
                drawRect(
                    brush   = accentBrush,
                    size    = Size(size.width, 2.dp.toPx()),
                    topLeft = Offset.Zero
                )
            }
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            // Title + unpin button
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Text(
                    text     = note.title.ifBlank { "Untitled" },
                    style    = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color      = scheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(scheme.primaryContainer.copy(alpha = 0.4f))
                        .clickable { onTogglePin() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Unpin",
                        tint               = scheme.primary,
                        modifier           = Modifier.size(13.dp)
                    )
                }
            }

            // Body preview
            if (note.body.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text     = note.body,
                    style    = MaterialTheme.typography.bodySmall.copy(
                        color      = nc.textMuted,
                        lineHeight = 20.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Date + tag
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = formatNoteDate(note),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color      = nc.textFaint,
                        fontWeight = FontWeight.Medium
                    )
                )
                if (note.colorHex != null) {
                    NoteTagChip(label = "Pinned")
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Regular note — compact grid/list card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RegularNoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val nc     = LocalNightcheckColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(scheme.surface)
            .border(1.dp, nc.borderMuted, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text     = note.title.ifBlank { "Untitled" },
            style    = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color      = scheme.onSurface,
                fontSize   = 14.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Render multiline body as individual lines (list-style), single body as a block
        val bodyLines = note.body.split("\n").filter { it.isNotBlank() }.take(4)
        if (bodyLines.size > 1) {
            bodyLines.forEach { line ->
                Text(
                    text     = line,
                    style    = MaterialTheme.typography.bodySmall.copy(
                        color      = nc.textMuted,
                        lineHeight = 22.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Text(
                text     = note.body,
                style    = MaterialTheme.typography.bodySmall.copy(
                    color      = nc.textMuted,
                    lineHeight = 20.sp
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = formatNoteDate(note),
                style = MaterialTheme.typography.labelSmall.copy(
                    color      = nc.textFaint,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Icon(
                Icons.Default.PushPin,
                contentDescription = "Pin note",
                tint               = nc.textFaint,
                modifier           = Modifier
                    .size(14.dp)
                    .clickable { onTogglePin() }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Tag chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoteTagChip(label: String) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(scheme.primaryContainer.copy(alpha = 0.3f))
            .border(1.dp, scheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight    = FontWeight.SemiBold,
                fontSize      = 10.sp,
                letterSpacing = 0.5.sp,
                color         = scheme.primary
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Date formatter — wire up note.updatedAt / createdAt when available
// ─────────────────────────────────────────────────────────────────────────────
private fun formatNoteDate(note: Note): String {
    val today     = LocalDate.now()
    val yesterday = today.minusDays(1)
    val noteDate  = note.updatedAt.toLocalDate()

    return when (noteDate) {
        today     -> "Today"
        yesterday -> "Yesterday"
        else      -> noteDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}