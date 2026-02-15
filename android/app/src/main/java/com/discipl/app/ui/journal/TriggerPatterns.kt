package com.discipl.app.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppSpacing
import com.discipl.app.ui.theme.AppTypography

@Composable
fun TriggerPatterns(
    mostCommonTrigger: String?,
    mostCommonTimeOfDay: String?,
    mostCommonDayOfWeek: String?,
    language: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.danger.copy(alpha = 0.08f))
            .border(1.dp, AppColors.danger.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(AppSpacing.md.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Insights, contentDescription = null, tint = AppColors.danger, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (language == "en") "TRIGGER PATTERNS" else "PATRONES DE RECAÍDA",
                style = AppTypography.label,
                color = AppColors.danger
            )
        }

        Spacer(Modifier.height(AppSpacing.sm.dp))

        val parts = mutableListOf<String>()
        mostCommonTimeOfDay?.let {
            val time = localizeTimeOfDayForPattern(it, language)
            parts.add(time)
        }
        mostCommonTrigger?.let {
            val trigger = localizeTriggerForPattern(it, language)
            parts.add(trigger)
        }

        if (parts.isNotEmpty()) {
            val text = if (language == "en") {
                "Your relapses tend to happen ${parts.joinToString(" when you feel ")}"
            } else {
                "Tus recaídas suelen ocurrir ${parts.joinToString(" cuando sientes ")}"
            }
            Text(
                text = text,
                style = AppTypography.body.copy(fontSize = 14.sp),
                color = AppColors.textPrimary,
                lineHeight = 20.sp
            )
        }

        mostCommonDayOfWeek?.let { day ->
            val localizedDay = localizeDayOfWeek(day, language)
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (language == "en") "Most common day: $localizedDay" else "Día más común: $localizedDay",
                style = AppTypography.caption,
                color = AppColors.textSecondary
            )
        }
    }
}

private fun localizeTimeOfDayForPattern(key: String, language: String): String = when (key) {
    "morning" -> if (language == "en") "in the morning" else "en la mañana"
    "afternoon" -> if (language == "en") "in the afternoon" else "en la tarde"
    "evening" -> if (language == "en") "in the evening" else "en la noche"
    "late_night" -> if (language == "en") "late at night" else "en la madrugada"
    else -> key
}

private fun localizeDayOfWeek(day: String, language: String): String {
    if (language == "en") return day
    return when (day) {
        "Monday" -> "Lunes"
        "Tuesday" -> "Martes"
        "Wednesday" -> "Miércoles"
        "Thursday" -> "Jueves"
        "Friday" -> "Viernes"
        "Saturday" -> "Sábado"
        "Sunday" -> "Domingo"
        else -> day
    }
}

private fun localizeTriggerForPattern(key: String, language: String): String = when (key) {
    "boredom" -> if (language == "en") "boredom" else "aburrimiento"
    "stress" -> if (language == "en") "stress" else "estrés"
    "loneliness" -> if (language == "en") "loneliness" else "soledad"
    "late_night" -> if (language == "en") "being up late" else "estar despierto tarde"
    "social_media" -> if (language == "en") "social media" else "redes sociales"
    "alcohol" -> "alcohol"
    else -> key
}
