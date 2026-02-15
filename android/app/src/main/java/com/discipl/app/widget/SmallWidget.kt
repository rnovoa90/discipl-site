package com.discipl.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.discipl.app.service.WidgetDataService

/**
 * Small widget (2x2): Shows "Día N" + "días" label.
 * Dark background matching the app theme.
 */
class SmallWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetData = WidgetDataService(context)
        val streakDays = widgetData.currentStreakDays
        val language = widgetData.language

        provideContent {
            SmallWidgetContent(streakDays = streakDays, language = language)
        }
    }
}

@Composable
private fun SmallWidgetContent(streakDays: Int, language: String) {
    val bgColor = ColorProvider(android.graphics.Color.parseColor("#0D1117"))
    val accentColor = ColorProvider(android.graphics.Color.parseColor("#00B4D8"))
    val textSecondary = ColorProvider(android.graphics.Color.parseColor("#8B949E"))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (language == "en") "Day" else "Día",
                style = TextStyle(
                    color = textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Text(
                text = "$streakDays",
                style = TextStyle(
                    color = accentColor,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = if (language == "en") {
                    if (streakDays == 1) "day" else "days"
                } else {
                    if (streakDays == 1) "día" else "días"
                },
                style = TextStyle(
                    color = textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}
