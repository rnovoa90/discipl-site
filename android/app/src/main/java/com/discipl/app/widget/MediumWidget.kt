package com.discipl.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.discipl.app.service.WidgetDataService

/**
 * Medium widget (4x2): Shows streak count + next milestone text.
 * Dark background matching the app theme.
 */
class MediumWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetData = WidgetDataService(context)
        val streakDays = widgetData.currentStreakDays
        val language = widgetData.language
        val nextMilestoneDay = widgetData.nextMilestoneDay
        val nextMilestoneTitle = widgetData.nextMilestoneTitle

        provideContent {
            MediumWidgetContent(
                streakDays = streakDays,
                language = language,
                nextMilestoneDay = nextMilestoneDay,
                nextMilestoneTitle = nextMilestoneTitle
            )
        }
    }
}

@Composable
private fun MediumWidgetContent(
    streakDays: Int,
    language: String,
    nextMilestoneDay: Int?,
    nextMilestoneTitle: String?
) {
    val bgColor = ColorProvider(android.graphics.Color.parseColor("#0D1117"))
    val accentColor = ColorProvider(android.graphics.Color.parseColor("#00B4D8"))
    val textPrimary = ColorProvider(android.graphics.Color.parseColor("#E6EDF3"))
    val textSecondary = ColorProvider(android.graphics.Color.parseColor("#8B949E"))
    val successColor = ColorProvider(android.graphics.Color.parseColor("#06D6A0"))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Streak number
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

            Spacer(modifier = GlanceModifier.width(20.dp))

            // Next milestone
            Column {
                Text(
                    text = "DISCIPL",
                    style = TextStyle(
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                if (nextMilestoneDay != null && nextMilestoneTitle != null) {
                    val daysLeft = nextMilestoneDay - streakDays
                    if (daysLeft > 0) {
                        Text(
                            text = if (language == "en")
                                "Next: $nextMilestoneTitle"
                            else
                                "Siguiente: $nextMilestoneTitle",
                            style = TextStyle(
                                color = successColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = if (language == "en")
                                "$daysLeft days to go"
                            else
                                "$daysLeft días restantes",
                            style = TextStyle(
                                color = textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    } else {
                        Text(
                            text = if (language == "en") "Keep going!" else "¡Sigue adelante!",
                            style = TextStyle(
                                color = successColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                } else {
                    Text(
                        text = if (language == "en") "Stay strong!" else "¡Mantente fuerte!",
                        style = TextStyle(
                            color = successColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}
