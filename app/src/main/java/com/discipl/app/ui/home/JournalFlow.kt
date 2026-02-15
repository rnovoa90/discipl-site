package com.discipl.app.ui.home

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.discipl.app.data.model.JournalEntry
import com.discipl.app.ui.components.PrimaryButton
import com.discipl.app.ui.components.SecondaryButton
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppSpacing
import com.discipl.app.ui.theme.AppTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalFlow(
    language: String,
    onComplete: (mood: Int, feelings: String?, energyLevel: Int?, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedMood by remember { mutableIntStateOf(3) }
    var selectedFeelings by remember { mutableStateOf(setOf<String>()) }
    var selectedEnergy by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf("") }
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.lg.dp)
            .padding(bottom = AppSpacing.xxl.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (step) {
            // Step 0: Journal entry
            0 -> {
                Text(
                    text = if (language == "en") "How are you feeling?" else "¿Cómo te sientes?",
                    style = AppTypography.sectionHeader,
                    color = AppColors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(AppSpacing.lg.dp))

                // Mood picker
                Text(
                    text = if (language == "en") "Mood" else "Ánimo",
                    style = AppTypography.body.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.textPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(AppSpacing.sm.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val moods = listOf("😞" to 1, "😟" to 2, "😐" to 3, "🙂" to 4, "😊" to 5)
                    moods.forEach { (emoji, value) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedMood == value) AppColors.accent.copy(alpha = 0.2f)
                                    else AppColors.surface
                                )
                                .border(
                                    1.dp,
                                    if (selectedMood == value) AppColors.accent else AppColors.surface,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedMood = value
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 24.sp)
                        }
                    }
                }

                Spacer(Modifier.height(AppSpacing.lg.dp))

                // Feelings multi-select
                Text(
                    text = if (language == "en") "Feelings (select all that apply)" else "Sentimientos (selecciona todo lo que aplique)",
                    style = AppTypography.body.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.textPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(AppSpacing.sm.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val feelingLabels = mapOf(
                        "anxious" to (if (language == "en") "Anxious" else "Ansioso"),
                        "calm" to (if (language == "en") "Calm" else "Tranquilo"),
                        "motivated" to (if (language == "en") "Motivated" else "Motivado"),
                        "lonely" to (if (language == "en") "Lonely" else "Solo"),
                        "grateful" to (if (language == "en") "Grateful" else "Agradecido"),
                        "stressed" to (if (language == "en") "Stressed" else "Estresado"),
                        "happy" to (if (language == "en") "Happy" else "Feliz"),
                        "frustrated" to (if (language == "en") "Frustrated" else "Frustrado"),
                        "bored" to (if (language == "en") "Bored" else "Aburrido"),
                        "confident" to (if (language == "en") "Confident" else "Confiado")
                    )
                    feelingLabels.forEach { (key, label) ->
                        ChipOption(label, selectedFeelings.contains(key)) {
                            selectedFeelings = if (selectedFeelings.contains(key)) {
                                selectedFeelings - key
                            } else {
                                selectedFeelings + key
                            }
                        }
                    }
                }

                Spacer(Modifier.height(AppSpacing.lg.dp))

                // Energy level
                Text(
                    text = if (language == "en") "Energy level" else "Nivel de energía",
                    style = AppTypography.body.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.textPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(AppSpacing.sm.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val energyLabels = if (language == "en") {
                        listOf("Very low", "Low", "Medium", "High", "Very high")
                    } else {
                        listOf("Muy baja", "Baja", "Media", "Alta", "Muy alta")
                    }
                    energyLabels.forEachIndexed { index, label ->
                        val level = index + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedEnergy == level) AppColors.success.copy(alpha = 0.2f)
                                    else AppColors.surface
                                )
                                .border(
                                    1.dp,
                                    if (selectedEnergy == level) AppColors.success else AppColors.surface,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedEnergy = if (selectedEnergy == level) 0 else level }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$level",
                                style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                                color = if (selectedEnergy == level) AppColors.success else AppColors.textSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(AppSpacing.lg.dp))

                // Notes
                Text(
                    text = if (language == "en") "Notes (optional)" else "Notas (opcional)",
                    style = AppTypography.body.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.textPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(AppSpacing.sm.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            if (language == "en") "Anything on your mind..." else "Lo que tengas en mente...",
                            color = AppColors.textSecondary.copy(alpha = 0.5f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.accent,
                        unfocusedBorderColor = AppColors.textSecondary.copy(alpha = 0.3f),
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary,
                        cursorColor = AppColors.accent
                    ),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(Modifier.height(AppSpacing.xl.dp))

                PrimaryButton(
                    text = if (language == "en") "Save" else "Guardar",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        step = 1
                    }
                )
                Spacer(Modifier.height(AppSpacing.sm.dp))
                SecondaryButton(
                    text = if (language == "en") "Cancel" else "Cancelar",
                    onClick = onDismiss
                )
            }

            // Step 1: Completion
            1 -> {
                Spacer(Modifier.height(AppSpacing.xl.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AppColors.success,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(AppSpacing.md.dp))
                Text(
                    text = if (language == "en") "Great job!" else "¡Buen trabajo!",
                    style = AppTypography.sectionHeader,
                    color = AppColors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(AppSpacing.sm.dp))
                Text(
                    text = if (language == "en") "Your check-in has been recorded." else "Tu check-in ha sido registrado.",
                    style = AppTypography.body,
                    color = AppColors.textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(AppSpacing.xl.dp))

                PrimaryButton(
                    text = if (language == "en") "Done" else "Listo",
                    onClick = {
                        onComplete(
                            selectedMood,
                            selectedFeelings.takeIf { it.isNotEmpty() }?.joinToString(","),
                            if (selectedEnergy > 0) selectedEnergy else null,
                            notes.ifBlank { null }
                        )
                    }
                )
            }
        }
    }
}
