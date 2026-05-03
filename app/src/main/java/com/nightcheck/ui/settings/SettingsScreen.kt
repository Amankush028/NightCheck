package com.nightcheck.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcheck.ui.theme.LocalIsDarkTheme
import com.nightcheck.ui.theme.LocalNightcheckColors
import com.nightcheck.ui.theme.LocalThemeToggle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState       by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme        = MaterialTheme.colorScheme
    val nc            = LocalNightcheckColors.current
    val isDarkTheme   = LocalIsDarkTheme.current
    val onThemeToggle = LocalThemeToggle.current

    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour   = uiState.endOfDayHour,
        initialMinute = uiState.endOfDayMinute,
        is24Hour      = false
    )

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor   = scheme.surfaceVariant,
            title            = { Text("Set review time", fontWeight = FontWeight.Bold) },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateEndOfDayTime(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = nc.textMuted)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ─────────────────────────────────────────────────────────
        Text(
            text       = "Profile",
            style      = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize   = 28.sp,
                color      = scheme.onBackground
            ),
            modifier   = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
        )

        // ── Avatar card ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surface)
                .border(1.dp, nc.borderMuted, RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(scheme.primary.copy(alpha = 0.15f))
                        .border(2.dp, scheme.primary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint     = scheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "NightCheck User",
                    style      = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color      = scheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Staying on top of things",
                    style = MaterialTheme.typography.bodySmall,
                    color = nc.textMuted
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Appearance section ─────────────────────────────────────────────
        SettingsSectionLabel("APPEARANCE")

        SettingsRow(
            icon        = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            label       = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
            sublabel    = if (isDarkTheme) "Currently dark" else "Currently light",
            onClick     = onThemeToggle
        )

        Spacer(Modifier.height(16.dp))

        // ── Notifications section ──────────────────────────────────────────
        SettingsSectionLabel("NOTIFICATIONS")

        SettingsRow(
            icon     = Icons.Default.Schedule,
            label    = "End-of-Day Review",
            sublabel = uiState.formattedTime,
            onClick  = { showTimePicker = true },
            trailing = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(scheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        uiState.formattedTime,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color      = scheme.primary
                        )
                    )
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        // ── About section ──────────────────────────────────────────────────
        SettingsSectionLabel("ABOUT")

        SettingsRow(
            icon     = Icons.Default.Info,
            label    = "NightCheck",
            sublabel = "Version 1.0.0",
            onClick  = {}
        )

        SettingsRow(
            icon     = Icons.Default.StarBorder,
            label    = "Rate the app",
            sublabel = "Enjoying NightCheck?",
            onClick  = {}
        )

        Spacer(Modifier.height(32.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionLabel(text: String) {
    val nc = LocalNightcheckColors.current
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.4.sp,
            fontWeight    = FontWeight.Bold,
            color         = nc.textFaint
        ),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    sublabel: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val nc     = LocalNightcheckColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.surface)
            .border(1.dp, nc.borderMuted, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(nc.surfaceHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint     = scheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color      = scheme.onSurface
                )
            )
            Text(
                sublabel,
                style = MaterialTheme.typography.bodySmall.copy(color = nc.textFaint)
            )
        }

        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        } else {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint     = nc.textFaint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}