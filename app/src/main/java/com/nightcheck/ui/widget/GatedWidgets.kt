package com.nightcheck.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.material3.GlanceTheme
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.nightcheck.billing.PremiumCache
import com.nightcheck.ui.MainActivity
import dagger.hilt.android.EntryPointAccessors

/**
 * Premium-gated widget. If the user isn't premium, shows an upsell cell
 * instead of the task list. This makes the widget itself an acquisition surface.
 */
class TodayTasksWidgetGated : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Check premium status from the local cache (no billing round-trip in widget)
        val premiumCache = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        ).premiumCache()

        val isPremium = premiumCache.isCurrentlyPremium()

        if (!isPremium) {
            provideContent { UpsellWidgetContent() }
            return
        }

        val tasks = TodayTasksWidgetStateHelper.getTodayTasks(context)
        provideContent {
            GlanceTheme {
                TodayTasksWidgetContent(tasks = tasks)
            }
        }
    }
}

@Composable
private fun UpsellWidgetContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF1C1C24)))
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment   = Alignment.CenterVertically
    ) {
        Text(
            "⭐ NightCheck Premium",
            style = TextStyle(
                color      = ColorProvider(Color(0xFFA78BFA)),
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.height(6.dp))
        Text(
            "Unlock widgets & more",
            style = TextStyle(
                color    = ColorProvider(Color(0x99FFFFFF)),
                fontSize = 11.sp
            )
        )
        Spacer(GlanceModifier.height(10.dp))
        Text(
            "Tap to upgrade →",
            style = TextStyle(
                color      = ColorProvider(Color(0xFFA78BFA)),
                fontSize   = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

class TodayTasksWidgetGatedReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayTasksWidgetGated()
}

// ── QuickAdd gated variant ────────────────────────────────────────────────────

class QuickAddWidgetGated : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val premiumCache = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        ).premiumCache()

        val isPremium = premiumCache.isCurrentlyPremium()

        provideContent {
            if (isPremium) QuickAddWidgetContent()
            else QuickAddUpsellContent()
        }
    }
}

@Composable
private fun QuickAddWidgetContent() {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF6650A4)))
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "+ Quick Add Task",
            style = TextStyle(
                color      = ColorProvider(Color.White),
                fontSize   = 15.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun QuickAddUpsellContent() {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF1C1C24)))
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "⭐ Upgrade for widgets",
            style = TextStyle(
                color      = ColorProvider(Color(0xFFA78BFA)),
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

class QuickAddWidgetGatedReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickAddWidgetGated()
}
