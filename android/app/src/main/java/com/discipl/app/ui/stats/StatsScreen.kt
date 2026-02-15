package com.discipl.app.ui.stats

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.discipl.app.ui.components.AnimatedBackground
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppSpacing
import com.discipl.app.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showStreakHistory by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadData() }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg.dp)
        ) {
            Spacer(Modifier.height(AppSpacing.xl.dp))

            Text(
                text = if (state.language == "en") "Statistics" else "Estadísticas",
                style = AppTypography.sectionHeader,
                color = AppColors.textPrimary
            )

            Spacer(Modifier.height(AppSpacing.lg.dp))

            // Stat cards grid (2x2)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    icon = Icons.Default.Timer,
                    label = if (state.language == "en") "Current" else "Actual",
                    value = "${state.currentStreakDays}",
                    unit = if (state.language == "en") "days" else "días",
                    color = AppColors.success,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.EmojiEvents,
                    label = if (state.language == "en") "Record" else "Récord",
                    value = "${state.longestStreakDays}",
                    unit = if (state.language == "en") "days" else "días",
                    color = AppColors.accent,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    icon = Icons.Default.CalendarMonth,
                    label = if (state.language == "en") "Total clean" else "Total limpio",
                    value = "${state.totalCleanDays}",
                    unit = if (state.language == "en") "days" else "días",
                    color = AppColors.success,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.Refresh,
                    label = if (state.language == "en") "Relapses" else "Recaídas",
                    value = "${state.relapseCount}",
                    unit = "total",
                    color = AppColors.danger,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(AppSpacing.lg.dp))

            // Average streak + history link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.surface.copy(alpha = 0.6f))
                    .clickable { showStreakHistory = true }
                    .padding(AppSpacing.md.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (state.language == "en") "Average streak:" else "Racha promedio:",
                    style = AppTypography.caption,
                    color = AppColors.textSecondary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", state.averageStreakDays),
                    style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.textPrimary
                )
                Text(
                    text = if (state.language == "en") " days" else " días",
                    style = AppTypography.caption,
                    color = AppColors.textSecondary
                )
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ShowChart, contentDescription = null, tint = AppColors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
            }

            Spacer(Modifier.height(AppSpacing.lg.dp))

            // Calendar heatmap section
            Text(
                text = if (state.language == "en") "Calendar" else "Calendario",
                style = AppTypography.body.copy(fontWeight = FontWeight.Bold),
                color = AppColors.textPrimary
            )
            Spacer(Modifier.height(AppSpacing.sm.dp))

            if (state.isPremium) {
                CalendarHeatmap(
                    checkIns = state.checkIns,
                    relapses = state.relapses,
                    currentMonth = state.currentMonth,
                    profileCreatedAt = state.profileCreatedAt,
                    language = state.language,
                    onMonthChange = { viewModel.changeMonth(it) }
                )
            } else {
                // Blurred premium gate
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Fake calendar grid behind blur
                    CalendarHeatmap(
                        checkIns = emptyList(),
                        relapses = emptyList(),
                        currentMonth = state.currentMonth,
                        profileCreatedAt = state.profileCreatedAt,
                        language = state.language,
                        onMonthChange = {},
                        modifier = Modifier.blur(8.dp)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showPaywall = true }
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Desbloquear PRO",
                            style = AppTypography.label,
                            color = AppColors.accent
                        )
                    }
                }
            }

            Spacer(Modifier.height(AppSpacing.lg.dp))

            // Share card button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.accent.copy(alpha = 0.1f))
                    .border(1.dp, AppColors.accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable {
                        if (state.isPremium) {
                            viewModel.onShareCardCreated()
                            ShareCardGenerator.generateAndShare(
                                context = context,
                                streakDays = state.currentStreakDays,
                                language = state.language
                            )
                        } else {
                            showPaywall = true
                        }
                    }
                    .padding(AppSpacing.md.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (state.language == "en") "Share your progress" else "Comparte tu progreso",
                        style = AppTypography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        color = AppColors.textPrimary
                    )
                    Text(
                        text = if (state.language == "en") "Generate a streak card" else "Genera una tarjeta de racha",
                        style = AppTypography.caption,
                        color = AppColors.textSecondary
                    )
                }
                if (!state.isPremium) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "PRO",
                        style = AppTypography.label.copy(fontSize = 10.sp),
                        color = AppColors.accent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AppColors.accent.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(AppSpacing.xxl.dp))
        }
    }

    if (showStreakHistory) {
        ModalBottomSheet(
            onDismissRequest = { showStreakHistory = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = AppColors.surface
        ) {
            StreakHistory(
                streaks = state.allStreaks,
                language = state.language
            )
        }
    }

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

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface.copy(alpha = 0.8f))
            .padding(AppSpacing.md.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            text = value,
            style = AppTypography.sectionHeader.copy(fontSize = 28.sp),
            color = AppColors.textPrimary
        )
        Text(
            text = unit,
            style = AppTypography.caption,
            color = AppColors.textSecondary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = AppTypography.label.copy(fontSize = 10.sp),
            color = color
        )
    }
}
