package com.nightcheck.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onToggleStatus: (TaskStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = task.status == TaskStatus.COMPLETED
    val isDark = isSystemInDarkTheme()

    val backgroundColor = if (isDark) Color(0x0DFFFFFF) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0x0FFFFFFF) else Color(0x12000000)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isDone) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .border(
                        width = 1.5.dp,
                        color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
                    .clickable { onToggleStatus(if (isDone) TaskStatus.PENDING else TaskStatus.COMPLETED) },
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 13.sp,
                    color = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    textDecoration = if (isDone) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val timeText = if (!task.description.isNullOrBlank()) task.description else "No time set"
                Text(
                    text = timeText,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            val (tagBg, tagTextCol, tagLabel) = when (task.priority) {
                Priority.HIGH -> Triple(
                    if (isDark) Color(0x1FFBBF24) else Color(0x1FFBBF24),
                    if (isDark) Color(0xFFD4A017) else Color(0xFFB07B00),
                    "Urgent"
                )
                Priority.MEDIUM -> Triple(
                    Color(0x267C6AF5),
                    Color(0xFFA78BFA),
                    "Study"
                )
                Priority.LOW -> Triple(
                    if (isDark) Color(0x1F34D399) else Color(0x1F34D399),
                    if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                    "Health"
                )
            }

            Box(
                modifier = Modifier
                    .background(tagBg, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = tagLabel,
                    color = tagTextCol,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
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
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0x0DFFFFFF) else Color(0x0A000000)
    val borderColor = if (isDark) Color(0x0DFFFFFF) else Color(0x0F000000)
    val textColor = if (isDark) Color(0x80FFFFFF) else Color(0x73000000)

    Card(
        modifier = modifier
            .widthIn(max = 110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Text(
            text = note.title.ifBlank { note.body },
            fontSize = 11.sp,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}