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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
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
import com.discipl.app.data.model.Relapse
import androidx.compose.material.icons.filled.Favorite
import com.discipl.app.ui.components.DangerButton
import com.discipl.app.ui.components.PrimaryButton
import com.discipl.app.ui.components.SecondaryButton
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppSpacing
import com.discipl.app.ui.theme.AppTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RelapseFlow(
    language: String,
    onConfirm: (timeOfDay: String?, trigger: String?, mood: Int?, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedTimeOfDay by remember { mutableStateOf<String?>(null) }
    var selectedTrigger by remember { mutableStateOf<String?>(null) }
    var selectedMood by remember { mutableIntStateOf(0) }
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
            // Step 0: Confirmation
            0 -> {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = AppColors.danger,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(AppSpacing.md.dp))
                Text(
                    text = if (language == "en") "Are you sure?" else "¿Estás seguro?",
                    style = AppTypography.sectionHeader,
                    color = AppColors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(AppSpacing.sm.dp))
                Text(
                    text = if (language == "en") "Your streak will reset." else "Tu racha se reiniciará.",
                    style = AppTypography.body,
                    color = AppColors.textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(AppSpacing.xl.dp))

                DangerButton(
                    text = if (language == "en") "Yes, I relapsed" else "Sí, recaí",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        step = 1
                    }
                )
                Spacer(Modifier.height(AppSpacing.md.dp))
                SecondaryButton(
                    text = if (language == "en") "Cancel" else "Cancelar",
                    onClick = onDismiss
                )
            }

            // Step 1: Trigger logging (all optional)
            1 -> {
                Text(
                    text = if (language == "en") "What happened?" else "¿Qué pasó?",
                    style = AppTypography.sectionHeader,
                    color = AppColors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (language == "en") "All fields are optional" else "Todos los campos son opcionales",
                    style = AppTypography.caption,
                    color = AppColors.textSecondary
                )
                Spacer(Modifier.height(AppSpacing.lg.dp))

                // Time of day
                Text(
                    text = if (language == "en") "Time of day" else "Momento del día",
                    style = AppTypography.body.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.textPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(AppSpacing.sm.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val timeLabels = mapOf(
                        "morning" to (if (language == "en") "Morning" else "Mañana"),
                        "afternoon" to (if (language == "en") "Afternoon" else "Tarde"),
                        "evening" to (if (language == "en") "Evening" else "Noche"),
                        "late_night" to (if (language == "en") "Late night" else "Madrugada")
                    )
                    timeLabels.forEach { (key, label) ->
                        ChipOption(label, selectedTimeOfDay == key) {
                            selectedTimeOfDay = if (selectedTimeOfDay == key) null else key
                        }
                    }
                }

                Spacer(Modifier.height(AppSpacing.lg.dp))

                // Trigger
                Text(
                    text = if (language == "en") "Trigger" else "Detonante",
                    style = AppTypography.body.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.textPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(AppSpacing.sm.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val triggerLabels = mapOf(
                        "boredom" to (if (language == "en") "Boredom" else "Aburrimiento"),
                        "stress" to (if (language == "en") "Stress" else "Estrés"),
                        "loneliness" to (if (language == "en") "Loneliness" else "Soledad"),
                        "late_night" to (if (language == "en") "Late night" else "Madrugada"),
                        "social_media" to (if (language == "en") "Social media" else "Redes sociales"),
                        "alcohol" to "Alcohol",
                        "other" to (if (language == "en") "Other" else "Otro")
                    )
                    triggerLabels.forEach { (key, label) ->
                        ChipOption(label, selectedTrigger == key) {
                            selectedTrigger = if (selectedTrigger == key) null else key
                        }
                    }
                }

                Spacer(Modifier.height(AppSpacing.lg.dp))

                // Mood
                Text(
                    text = if (language == "en") "Mood before (1-5)" else "Ánimo antes (1-5)",
                    style = AppTypography.body.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.textPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(AppSpacing.sm.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { mood ->
                        val moodEmoji = when (mood) {
                            1 -> "😞"; 2 -> "😟"; 3 -> "😐"; 4 -> "🙂"; 5 -> "😊"
                            else -> "😐"
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedMood == mood) AppColors.accent.copy(alpha = 0.2f)
                                    else AppColors.surface
                                )
                                .border(
                                    1.dp,
                                    if (selectedMood == mood) AppColors.accent else AppColors.surface,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMood = if (selectedMood == mood) 0 else mood },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(moodEmoji, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(Modifier.height(AppSpacing.lg.dp))

                // Notes
                Text(
                    text = if (language == "en") "Notes" else "Notas",
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
                            if (language == "en") "How are you feeling?" else "¿Cómo te sientes?",
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
                    onClick = { step = 2 }
                )
                Spacer(Modifier.height(AppSpacing.sm.dp))
                SecondaryButton(
                    text = if (language == "en") "Skip" else "Saltar",
                    onClick = {
                        selectedTimeOfDay = null
                        selectedTrigger = null
                        selectedMood = 0
                        notes = ""
                        step = 2
                    }
                )
            }

            // Step 2: Encouragement
            2 -> {
                Spacer(Modifier.height(AppSpacing.xxl.dp))

                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = AppColors.success,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(Modifier.height(AppSpacing.xl.dp))
                Text(
                    text = if (language == "en") "Every attempt makes you stronger." else "Cada intento te hace más fuerte.",
                    style = AppTypography.sectionHeader,
                    color = AppColors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(AppSpacing.sm.dp))
                Text(
                    text = if (language == "en") "Your new streak starts now." else "Tu nueva racha comienza ahora.",
                    style = AppTypography.body,
                    color = AppColors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(AppSpacing.xxl.dp))

                PrimaryButton(
                    text = if (language == "en") "Continue" else "Continuar",
                    onClick = {
                        onConfirm(
                            selectedTimeOfDay,
                            selectedTrigger,
                            if (selectedMood > 0) selectedMood else null,
                            notes.ifBlank { null }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ChipOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = AppTypography.caption.copy(
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        ),
        color = if (selected) AppColors.textPrimary else AppColors.textSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) AppColors.accent.copy(alpha = 0.2f) else AppColors.surface)
            .border(
                1.dp,
                if (selected) AppColors.accent else AppColors.textSecondary.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}
