package com.nightcheck.ui.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nightcheck.ui.theme.LocalNightcheckColors

/**
 * Compact dialog shown inline when user hits a free tier limit.
 * Tapping "Upgrade" should set showPaywall = true in the caller.
 */
@Composable
fun LimitReachedDialog(
    title: String,
    message: String,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val nc     = LocalNightcheckColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = scheme.surface,
        shape            = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(scheme.primary.copy(alpha = 0.2f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint     = scheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                title,
                style     = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color      = scheme.onSurface
                ),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                message,
                style     = MaterialTheme.typography.bodyMedium.copy(color = nc.textMuted),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onUpgrade,
                shape   = RoundedCornerShape(12.dp),
                colors  = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upgrade to Premium", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick  = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Not now", color = nc.textMuted)
            }
        }
    )
}