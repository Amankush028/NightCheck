package com.nightcheck.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.nightcheck.ui.MainActivity
import androidx.glance.action.clickable

/**
 * 4×1 Quick Add widget.
 * Tapping the button opens MainActivity (which can handle a quick-add intent).
 * A dedicated QuickAddActivity could be substituted here for a bottom-sheet experience.
 */
class QuickAddWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            QuickAddWidgetContent()
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

        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "+ Quick Add Task",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

// ── Widget receiver ───────────────────────────────────────────────────────────

class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget()
}
