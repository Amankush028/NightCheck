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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.nightcheck.domain.model.Note
import com.nightcheck.domain.model.Priority
import com.nightcheck.domain.model.Task
import com.nightcheck.domain.model.TaskStatus
import com.nightcheck.ui.theme.LocalNightcheckColors

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

    // Stable lambda — only recreated when task.id or isDone changes,
    // not on every parent recomposition. Avoids unnecessary child redraws.
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
            modifier          = Modifier.fillMaxWidth().padding(20.dp),
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

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    // Parse hex only when colorHex changes — not every recomposition
    val resolvedBg = remember(note.colorHex) {
        note.colorHex?.let { Color(it.toColorInt()) }
    }

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = resolvedBg ?: scheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = note.title.ifBlank { "Untitled" },
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = scheme.onSurface,
                    modifier   = Modifier.weight(1f),
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                if (note.isPinned) {
                    Icon(
                        imageVector        = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint               = scheme.primary,
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }
            if (note.body.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text       = note.body,
                    fontSize   = 13.sp,
                    color      = scheme.onSurfaceVariant,
                    fontWeight = if (note.isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle  = if (note.isItalic) FontStyle.Italic else FontStyle.Normal,
                    maxLines   = 3,
                    overflow   = TextOverflow.Ellipsis
                )
            }
        }
    }
}