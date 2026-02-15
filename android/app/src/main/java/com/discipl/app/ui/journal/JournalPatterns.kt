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
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppSpacing
import com.discipl.app.ui.theme.AppTypography

@Composable
fun JournalPatterns(
    mostCommonFeeling: String?,
    averageMood: Double?,
    averageEnergy: Double?,
    language: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.success.copy(alpha = 0.08f))
            .border(1.dp, AppColors.success.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(AppSpacing.md.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Psychology, contentDescription = null, tint = AppColors.success, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (language == "en") "JOURNAL INSIGHTS" else "REFLEXIONES DEL DIARIO",
                style = AppTypography.label,
                color = AppColors.success
            )
        }

        Spacer(Modifier.height(AppSpacing.sm.dp))

        averageMood?.let { mood ->
            val moodEmoji = when {
                mood < 1.5 -> "😞"; mood < 2.5 -> "😟"; mood < 3.5 -> "😐"; mood < 4.5 -> "🙂"; else -> "😊"
            }
            Text(
                text = if (language == "en")
                    "$moodEmoji Average mood: ${String.format("%.1f", mood)}/5"
                else
                    "$moodEmoji Ánimo promedio: ${String.format("%.1f", mood)}/5",
                style = AppTypography.body.copy(fontSize = 14.sp),
                color = AppColors.textPrimary
            )
            Spacer(Modifier.height(6.dp))
        }

        averageEnergy?.let { energy ->
            Text(
                text = if (language == "en")
                    "⚡ Average energy: ${String.format("%.1f", energy)}/5"
                else
                    "⚡ Energía promedio: ${String.format("%.1f", energy)}/5",
                style = AppTypography.body.copy(fontSize = 14.sp),
                color = AppColors.textPrimary
            )
            Spacer(Modifier.height(6.dp))
        }

        mostCommonFeeling?.let { feeling ->
            val localizedFeeling = when (feeling) {
                "anxious" -> if (language == "en") "anxious" else "ansioso"
                "calm" -> if (language == "en") "calm" else "tranquilo"
                "motivated" -> if (language == "en") "motivated" else "motivado"
                "lonely" -> if (language == "en") "lonely" else "solo"
                "grateful" -> if (language == "en") "grateful" else "agradecido"
                "stressed" -> if (language == "en") "stressed" else "estresado"
                "happy" -> if (language == "en") "happy" else "feliz"
                "frustrated" -> if (language == "en") "frustrated" else "frustrado"
                "bored" -> if (language == "en") "bored" else "aburrido"
                "confident" -> if (language == "en") "confident" else "confiado"
                else -> feeling
            }
            Text(
                text = if (language == "en")
                    "Most common feeling: $localizedFeeling"
                else
                    "Sentimiento más común: $localizedFeeling",
                style = AppTypography.caption,
                color = AppColors.textSecondary
            )
        }
    }
}
