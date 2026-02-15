package com.discipl.app.ui.journal

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.discipl.app.data.model.JournalEntry
import com.discipl.app.data.model.Relapse
import com.discipl.app.ui.components.AnimatedBackground
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppSpacing
import com.discipl.app.ui.theme.AppTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalScreen(
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPaywall by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground()

        if (!state.isPremium) {
            // Premium gate
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.xl.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(AppSpacing.md.dp))
                Text(
                    text = "El diario es una función PRO",
                    style = AppTypography.sectionHeader,
                    color = AppColors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(AppSpacing.sm.dp))
                Text(
                    text = "Desbloquea seguimiento detallado de recaídas y reflexiones.",
                    style = AppTypography.body,
                    color = AppColors.textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(AppSpacing.lg.dp))
                com.discipl.app.ui.components.PrimaryButton(
                    text = "Desbloquear PRO",
                    onClick = { showPaywall = true },
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Tab row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = AppColors.surface,
                    contentColor = AppColors.textPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = AppColors.accent
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                if (state.language == "en") "Relapses" else "Recaídas",
                                style = AppTypography.caption.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        selectedContentColor = AppColors.accent,
                        unselectedContentColor = AppColors.textSecondary
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                if (state.language == "en") "Journal" else "Diario",
                                style = AppTypography.caption.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        selectedContentColor = AppColors.accent,
                        unselectedContentColor = AppColors.textSecondary
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(AppSpacing.lg.dp)
                ) {
                    when (selectedTab) {
                        0 -> RelapsesTab(state)
                        1 -> JournalEntriesTab(state)
                    }
                    Spacer(Modifier.height(AppSpacing.xxl.dp))
                }
            }
        }

        // Paywall dialog
        if (showPaywall) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showPaywall = false },
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false
                )
            ) {
                com.discipl.app.ui.paywall.PaywallScreen(
                    onDismiss = { showPaywall = false }
                )
            }
        }
    }
}

@Composable
private fun RelapsesTab(state: JournalUiState) {
    // Trigger patterns card (shown when 3+ relapses)
    if (state.relapses.size >= 3) {
        TriggerPatterns(
            mostCommonTrigger = state.mostCommonTrigger,
            mostCommonTimeOfDay = state.mostCommonTimeOfDay,
            mostCommonDayOfWeek = state.mostCommonDayOfWeek,
            language = state.language
        )
        Spacer(Modifier.height(AppSpacing.lg.dp))
    }

    if (state.relapses.isEmpty()) {
        Spacer(Modifier.height(AppSpacing.xl.dp))
        Text(
            text = if (state.language == "en") "No relapses recorded." else "No hay recaídas registradas.",
            style = AppTypography.body,
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        state.relapses.forEach { relapse ->
            RelapseCard(relapse, state.language)
            Spacer(Modifier.height(AppSpacing.sm.dp))
        }
    }
}

@Composable
private fun JournalEntriesTab(state: JournalUiState) {
    // Journal patterns card
    if (state.journalEntries.size >= 3) {
        JournalPatterns(
            mostCommonFeeling = state.mostCommonFeeling,
            averageMood = state.averageMood,
            averageEnergy = state.averageEnergy,
            language = state.language
        )
        Spacer(Modifier.height(AppSpacing.lg.dp))
    }

    if (state.journalEntries.isEmpty()) {
        Spacer(Modifier.height(AppSpacing.xl.dp))
        Text(
            text = if (state.language == "en") "No journal entries yet." else "Aún no hay entradas de diario.",
            style = AppTypography.body,
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        state.journalEntries.forEach { entry ->
            JournalEntryCard(entry, state.language)
            Spacer(Modifier.height(AppSpacing.sm.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelapseCard(relapse: Relapse, language: String) {
    val locale = if (language == "en") Locale.US else Locale("es", "ES")
    val dateFormat = SimpleDateFormat("d MMM yyyy, HH:mm", locale)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface.copy(alpha = 0.8f))
            .border(1.dp, AppColors.danger.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(AppSpacing.md.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = AppColors.danger, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = dateFormat.format(Date(relapse.date)),
                style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                color = AppColors.textPrimary
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            relapse.timeOfDay?.let { SmallChip(localizeTimeOfDay(it, language)) }
            relapse.trigger?.let { SmallChip(localizeTrigger(it, language)) }
            relapse.moodBefore?.let {
                val emoji = when (it) { 1 -> "😞"; 2 -> "😟"; 3 -> "😐"; 4 -> "🙂"; 5 -> "😊"; else -> "😐" }
                SmallChip("$emoji $it/5")
            }
        }

        relapse.notes?.let { notes ->
            if (notes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = notes,
                    style = AppTypography.caption,
                    color = AppColors.textSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JournalEntryCard(entry: JournalEntry, language: String) {
    val locale = if (language == "en") Locale.US else Locale("es", "ES")
    val dateFormat = SimpleDateFormat("d MMM yyyy", locale)
    val moodEmoji = when (entry.mood) { 1 -> "😞"; 2 -> "😟"; 3 -> "😐"; 4 -> "🙂"; 5 -> "😊"; else -> "😐" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface.copy(alpha = 0.8f))
            .border(1.dp, AppColors.success.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(AppSpacing.md.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Book, contentDescription = null, tint = AppColors.success, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = dateFormat.format(Date(entry.date)),
                style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                color = AppColors.textPrimary
            )
            Spacer(Modifier.weight(1f))
            Text("$moodEmoji ${entry.mood}/5", style = AppTypography.caption, color = AppColors.textSecondary)
        }

        entry.feelings?.let { feelings ->
            if (feelings.isNotBlank()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    feelings.split(",").forEach { feeling ->
                        SmallChip(localizeFeeling(feeling.trim(), language))
                    }
                }
            }
        }

        entry.energyLevel?.let { energy ->
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${if (language == "en") "Energy" else "Energía"}: $energy/5",
                style = AppTypography.caption,
                color = AppColors.textSecondary
            )
        }

        entry.notes?.let { notes ->
            if (notes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(text = notes, style = AppTypography.caption, color = AppColors.textSecondary, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun SmallChip(text: String) {
    Text(
        text = text,
        style = AppTypography.caption.copy(fontSize = 11.sp),
        color = AppColors.textSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.background.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

// --- Localization helpers ---

private fun localizeTimeOfDay(key: String, language: String): String = when (key) {
    "morning" -> if (language == "en") "Morning" else "Mañana"
    "afternoon" -> if (language == "en") "Afternoon" else "Tarde"
    "evening" -> if (language == "en") "Evening" else "Noche"
    "late_night" -> if (language == "en") "Late night" else "Madrugada"
    else -> key
}

private fun localizeTrigger(key: String, language: String): String = when (key) {
    "boredom" -> if (language == "en") "Boredom" else "Aburrimiento"
    "stress" -> if (language == "en") "Stress" else "Estrés"
    "loneliness" -> if (language == "en") "Loneliness" else "Soledad"
    "late_night" -> if (language == "en") "Late night" else "Madrugada"
    "social_media" -> if (language == "en") "Social media" else "Redes sociales"
    "alcohol" -> "Alcohol"
    "other" -> if (language == "en") "Other" else "Otro"
    else -> key
}

private fun localizeFeeling(key: String, language: String): String = when (key) {
    "anxious" -> if (language == "en") "Anxious" else "Ansioso"
    "calm" -> if (language == "en") "Calm" else "Tranquilo"
    "motivated" -> if (language == "en") "Motivated" else "Motivado"
    "lonely" -> if (language == "en") "Lonely" else "Solo"
    "grateful" -> if (language == "en") "Grateful" else "Agradecido"
    "stressed" -> if (language == "en") "Stressed" else "Estresado"
    "happy" -> if (language == "en") "Happy" else "Feliz"
    "frustrated" -> if (language == "en") "Frustrated" else "Frustrado"
    "bored" -> if (language == "en") "Bored" else "Aburrido"
    "confident" -> if (language == "en") "Confident" else "Confiado"
    else -> key
}
