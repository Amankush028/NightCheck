package com.nightcheck.ui.review

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcheck.ui.theme.NightcheckTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class EndOfDayReviewActivity : ComponentActivity() {

    private val viewModel: EndOfDayReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This makes the Activity show over the lock screen and wakes the screen up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        enableEdgeToEdge()
        setContent {
            NightcheckTheme {
                EndOfDayReviewScreen(
                    viewModel = viewModel,
                    onFinish = { finish() }
                )
            }
        }
    }
}

@Composable
private fun EndOfDayReviewScreen(
    viewModel: EndOfDayReviewViewModel,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Filter items to find what's left to action
    val pendingItems = uiState.items.filter { it.action == ReviewAction.NONE }
    val currentItem = pendingItems.firstOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        WallpaperBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "NIGHTCHECK" eyebrow
            Text(
                text = "NIGHTCHECK",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.45f),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 22.dp, bottom = 6.dp)
            )

            // App row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 18.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF7C6AF5),
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "End of day",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

            // The Sheet
            if (uiState.isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (currentItem != null) {
                ReviewSheet(
                    pendingCount = pendingItems.size,
                    currentItem = currentItem,
                    totalCount = uiState.totalTodayCount,
                    completedCount = uiState.completedTodayCount,
                    onMarkDone = { viewModel.markComplete(currentItem.task.id) },
                    onDelay = { viewModel.snooze(currentItem.task.id) },
                    onSkip = { viewModel.dismiss(currentItem.task.id) }
                )
            } else {
                // All clear
                AllClearSheet(onFinish = onFinish)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Handle
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 16.dp)
                    .width(80.dp)
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun WallpaperBackground() {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF1A1235) else Color(0xFFC9C4E0)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF7C6AF5).copy(alpha = 0.3f), Color.Transparent),
                    center = center.copy(x = size.width * 0.3f, y = size.height * 0.3f),
                    radius = size.minDimension * 0.8f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF5032B4).copy(alpha = 0.2f), Color.Transparent),
                    center = center.copy(x = size.width * 0.75f, y = size.height * 0.7f),
                    radius = size.minDimension * 0.7f
                )
            )
        }
        // Dim overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color.Black.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.35f))
        )
    }
}

@Composable
private fun ReviewSheet(
    pendingCount: Int,
    currentItem: ReviewTaskItem,
    totalCount: Int,
    completedCount: Int,
    onMarkDone: () -> Unit,
    onDelay: () -> Unit,
    onSkip: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val sheetBg = if (isDark) Color(0xEB161226) else Color(0xF0FFFFFF)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)

    Card(
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = sheetBg),
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(top = 20.dp)) {
            // Header
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                val today = LocalDate.now()
                val dateText = today.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
                Text(
                    text = dateText.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.35f),
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (pendingCount == 1) "1 task needs\nyour attention" else "$pendingCount tasks need\nyour attention",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    ),
                    color = if (isDark) Color(0xFFF0EDE8) else Color(0xFF1A1917)
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Before you close out the day",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.35f)
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = borderColor)
            Spacer(Modifier.height(14.dp))

            // Tasks (showing current task)
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(8.dp)
                        .background(Color(0xFF7C6AF5), CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = currentItem.task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.82f)
                    )
                    currentItem.task.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color.White.copy(alpha = 0.32f) else Color.Black.copy(alpha = 0.35f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = borderColor)

            // Progress
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$completedCount of $totalCount tasks done today",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.32f),
                    modifier = Modifier.weight(1f)
                )
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(4.dp)
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.1f),
                            RoundedCornerShape(2.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(Color(0xFF7C6AF5), RoundedCornerShape(2.dp))
                    )
                }
            }

            // Actions
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Button(
                    onClick = onMarkDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C6AF5))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Mark as done", fontWeight = FontWeight.SemiBold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedButton(
                        onClick = onDelay,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Text(
                            "Do tomorrow",
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                        )
                    }

                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Text(
                            "Skip",
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AllClearSheet(onFinish: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val sheetBg = if (isDark) Color(0xEB161226) else Color(0xF0FFFFFF)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)

    Card(
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = sheetBg),
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = Color(0xFF7C6AF5).copy(alpha = 0.15f)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(32.dp),
                    tint = Color(0xFF7C6AF5)
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = "All clear for tonight",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isDark) Color.White else Color.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = "Great job! You've handled all your tasks for today. Rest well.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Color.White.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C6AF5))
            ) {
                Text("Finish Review", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
