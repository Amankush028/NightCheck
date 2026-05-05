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
import com.nightcheck.ui.theme.LocalNightcheckColors
import com.nightcheck.ui.theme.NightcheckTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.nightcheck.util.PreferencesManager
import javax.inject.Inject
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.lifecycleScope
import com.nightcheck.ads.AdManager
import com.nightcheck.billing.PremiumCache
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EndOfDayReviewActivity : ComponentActivity() {

    private val viewModel: EndOfDayReviewViewModel by viewModels()

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var premiumCache: PremiumCache
    @Inject lateinit var adManager: AdManager

    private var interstitialShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val systemDark  = isSystemInDarkTheme()
            val isDarkTheme = preferencesManager.isDarkTheme
                .collectAsStateWithLifecycle(initialValue = systemDark).value

            NightcheckTheme(darkTheme = isDarkTheme) {
                EndOfDayReviewScreen(
                    viewModel = viewModel,
                    onFinish  = { finish() }
                )
            }
        }
        showEodInterstitialIfNeeded()
    }

    private fun showEodInterstitialIfNeeded() {
        if (interstitialShown) return
        lifecycleScope.launch {
            val isPremium = premiumCache.isCurrentlyPremium()
            if (!isPremium) {
                interstitialShown = true
                adManager.showInterstitialIfReady(this@EndOfDayReviewActivity)
            }
        }
    }
}





@Composable
private fun EndOfDayReviewScreen(
    viewModel: EndOfDayReviewViewModel,
    onFinish: () -> Unit
) {
    val uiState      by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingItems  = remember(uiState.items) { uiState.items.filter { it.action == ReviewAction.NONE } }
    val currentItem   = pendingItems.firstOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        WallpaperBackground()

        Column(
            modifier            = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text          = "NIGHTCHECK",
                style         = MaterialTheme.typography.labelSmall,
                color         = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                letterSpacing = 2.sp,
                modifier      = Modifier.padding(top = 22.dp, bottom = 6.dp)
            )

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier              = Modifier.padding(bottom = 18.dp)
            ) {
                Surface(
                    shape    = RoundedCornerShape(8.dp),
                    color    = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Schedule,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onPrimary,
                        modifier           = Modifier.padding(6.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "End of day",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                )
            }

            when {
                uiState.isLoading  -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                currentItem != null -> {
                    ReviewSheet(
                        pendingCount   = pendingItems.size,
                        currentItem    = currentItem,
                        totalCount     = uiState.totalTodayCount,
                        completedCount = uiState.completedTodayCount,
                        onMarkDone     = { viewModel.markComplete(currentItem.task.id) },
                        onDelay        = { viewModel.snooze(currentItem.task.id) },
                        onSkip         = { viewModel.dismiss(currentItem.task.id) }
                    )
                }
                else               -> AllClearSheet(onFinish = onFinish)
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 16.dp)
                    .width(80.dp)
                    .height(3.dp)
                    .background(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

/**
 * Colors are hoisted out of Canvas as plain [Color] values — reading
 * ColorScheme or CompositionLocals inside DrawScope subscribes the canvas
 * to snapshot state and causes it to redraw every frame.
 */
@Composable
private fun WallpaperBackground() {
    val nc     = LocalNightcheckColors.current
    val scheme = MaterialTheme.colorScheme

    // Hoist colors before entering Canvas
    val bgColor      = nc.reviewBg
    val primaryColor = scheme.primary
    val dimColor     = nc.primaryDim
    val overlayColor = scheme.background.copy(alpha = 0.55f)

    // Remember brushes so they're not reallocated every recomposition
    val brush1 = remember(primaryColor) {
        Brush.radialGradient(
            colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent)
        )
    }
    val brush2 = remember(dimColor) {
        Brush.radialGradient(
            colors = listOf(dimColor.copy(alpha = 0.2f), Color.Transparent)
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush  = brush1,
                center = center.copy(x = size.width * 0.3f, y = size.height * 0.3f),
                radius = size.minDimension * 0.8f
            )
            drawCircle(
                brush  = brush2,
                center = center.copy(x = size.width * 0.75f, y = size.height * 0.7f),
                radius = size.minDimension * 0.7f
            )
        }
        Box(modifier = Modifier.fillMaxSize().background(overlayColor))
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
    val nc     = LocalNightcheckColors.current
    val scheme = MaterialTheme.colorScheme

    // Format date once — not on every recomposition
    val dateText = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")).uppercase()
    }

    val progress = remember(completedCount, totalCount) {
        if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    }

    Card(
        modifier = Modifier.padding(horizontal = 14.dp).fillMaxWidth(),
        shape    = RoundedCornerShape(26.dp),
        colors   = CardDefaults.cardColors(containerColor = nc.reviewSheet),
        border   = BorderStroke(0.5.dp, nc.borderMuted)
    ) {
        Column(modifier = Modifier.padding(top = 20.dp)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(dateText, style = MaterialTheme.typography.labelSmall, color = nc.textMuted, letterSpacing = 1.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = if (pendingCount == 1) "1 task needs\nyour attention"
                    else "$pendingCount tasks need\nyour attention",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    ),
                    color = scheme.onSurface
                )
                Spacer(Modifier.height(3.dp))
                Text("Before you close out the day", style = MaterialTheme.typography.bodySmall, color = nc.textMuted)
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = nc.borderMuted)
            Spacer(Modifier.height(14.dp))

            Row(
                modifier          = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.padding(top = 4.dp).size(8.dp).background(scheme.primary, CircleShape))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text       = currentItem.task.title,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = scheme.onSurface
                    )
                    currentItem.task.description?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = nc.textFaint)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = nc.borderMuted)

            Row(
                modifier          = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$completedCount of $totalCount tasks done today",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = nc.textFaint,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .width(80.dp).height(4.dp)
                        .background(nc.borderMuted, RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress).fillMaxHeight()
                            .background(scheme.primary, RoundedCornerShape(2.dp))
                    )
                }
            }

            Column(
                modifier            = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Button(
                    onClick  = onMarkDone,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Mark as done", fontWeight = FontWeight.SemiBold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedButton(
                        onClick  = onDelay,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(16.dp),
                        border   = BorderStroke(1.dp, nc.borderMuted)
                    ) { Text("Do tomorrow", color = scheme.onSurface.copy(alpha = 0.7f)) }

                    OutlinedButton(
                        onClick  = onSkip,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(16.dp),
                        border   = BorderStroke(1.dp, nc.borderMuted)
                    ) { Text("Skip", color = scheme.onSurface.copy(alpha = 0.7f)) }
                }
            }
        }
    }
}

@Composable
private fun AllClearSheet(onFinish: () -> Unit) {
    val nc     = LocalNightcheckColors.current
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.padding(horizontal = 14.dp).fillMaxWidth(),
        shape    = RoundedCornerShape(26.dp),
        colors   = CardDefaults.cardColors(containerColor = nc.reviewSheet),
        border   = BorderStroke(0.5.dp, nc.borderMuted)
    ) {
        Column(
            modifier            = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape    = CircleShape,
                color    = scheme.primary.copy(alpha = 0.15f)
            ) {
                Icon(
                    Icons.Default.Check, null,
                    modifier = Modifier.padding(16.dp).size(32.dp),
                    tint     = scheme.primary
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "All clear for tonight",
                style     = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold
                ),
                color     = scheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Great job! You've handled all your tasks for today. Rest well.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = nc.textMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick  = onFinish,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = scheme.primary)
            ) { Text("Finish Review", fontWeight = FontWeight.SemiBold) }
        }
    }
}