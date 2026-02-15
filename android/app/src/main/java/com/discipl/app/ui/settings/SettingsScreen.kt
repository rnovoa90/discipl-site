package com.discipl.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.discipl.app.ui.components.AnimatedBackground
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppSpacing
import com.discipl.app.ui.theme.AppTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

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
                text = "Ajustes",
                style = AppTypography.sectionHeader,
                color = AppColors.textPrimary
            )

            Spacer(Modifier.height(AppSpacing.lg.dp))

            // Quit date
            SettingsSection("FECHA DE INICIO") {
                val dateFormat = SimpleDateFormat("d MMM yyyy", Locale("es", "ES"))
                val dateStr = state.profile?.let { dateFormat.format(Date(it.quitDate)) } ?: "—"
                SettingsRow(
                    icon = Icons.Default.CalendarMonth,
                    title = "Fecha de inicio",
                    subtitle = dateStr,
                    onClick = { showDatePicker = true }
                )
            }

            Spacer(Modifier.height(AppSpacing.lg.dp))

            // Notifications
            SettingsSection("NOTIFICACIONES") {
                SettingsToggle(
                    title = "Motivación diaria",
                    enabled = state.morningMotivationEnabled,
                    onToggle = { viewModel.toggleMorningMotivation(it) }
                )
                SettingsToggle(
                    title = "Celebración de metas",
                    enabled = state.milestoneNotificationsEnabled,
                    onToggle = { viewModel.toggleMilestoneNotifications(it) },
                    isPremium = !state.isPremium
                )
                SettingsToggle(
                    title = "Check-in diario",
                    enabled = state.eveningCheckInEnabled,
                    onToggle = { viewModel.toggleEveningCheckIn(it) },
                    isPremium = !state.isPremium
                )
                SettingsToggle(
                    title = "Re-enganche",
                    enabled = state.reengagementEnabled,
                    onToggle = { viewModel.toggleReengagement(it) },
                    isPremium = !state.isPremium
                )
            }

            Spacer(Modifier.height(AppSpacing.lg.dp))

            // Features
            SettingsSection("FUNCIONES") {
                SettingsToggle(
                    title = "Contenido diario",
                    enabled = state.dailyTaskEnabled,
                    onToggle = { viewModel.toggleDailyTask(it) }
                )
            }

            Spacer(Modifier.height(AppSpacing.lg.dp))

            // Links
            SettingsSection("MÁS") {
                SettingsRow(
                    icon = Icons.Default.Subscriptions,
                    title = "Administrar suscripción",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions"))
                        context.startActivity(intent)
                    }
                )
                SettingsRow(
                    icon = Icons.Default.Star,
                    title = "Califica la app",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.discipl.app"))
                        context.startActivity(intent)
                    }
                )
                SettingsRow(
                    icon = Icons.Default.Email,
                    title = "Contáctanos",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:info@discipl.win"))
                        context.startActivity(intent)
                    }
                )
                SettingsRow(
                    icon = Icons.Default.Policy,
                    title = "Política de privacidad",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discipl.win/privacy"))
                        context.startActivity(intent)
                    }
                )
                SettingsRow(
                    icon = Icons.Default.Policy,
                    title = "Términos de servicio",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discipl.win/terms"))
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(Modifier.height(AppSpacing.xl.dp))

            // Version
            Text(
                text = "v1.0.0",
                style = AppTypography.caption,
                color = AppColors.textSecondary.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(AppSpacing.xxl.dp))
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        // DatePicker works in UTC. We need today's date as UTC midnight for the constraint.
        val todayUtcMillis = remember {
            java.time.LocalDate.now()
                .atStartOfDay(java.time.ZoneOffset.UTC)
                .toInstant().toEpochMilli()
        }
        // Convert the stored local quit date to UTC midnight for the initial selection
        val initialDateUtcMillis = remember(state.profile?.quitDate) {
            val storedMillis = state.profile?.quitDate ?: System.currentTimeMillis()
            val localDate = java.time.Instant.ofEpochMilli(storedMillis)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            localDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateUtcMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= todayUtcMillis
                }
                override fun isSelectableYear(year: Int): Boolean {
                    return year <= java.time.LocalDate.now().year
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        // Convert UTC midnight back to local midnight
                        val localDate = java.time.Instant.ofEpochMilli(utcMillis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                        val localMillis = localDate
                            .atStartOfDay(java.time.ZoneId.systemDefault())
                            .toInstant().toEpochMilli()
                        viewModel.updateQuitDate(localMillis)
                    }
                    showDatePicker = false
                }) {
                    Text("Confirmar", color = AppColors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = AppColors.textSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = AppColors.surface)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = AppColors.surface,
                    selectedDayContainerColor = AppColors.accent,
                    todayDateBorderColor = AppColors.accent,
                    todayContentColor = AppColors.accent
                )
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Text(
        text = title,
        style = AppTypography.label.copy(fontSize = 11.sp),
        color = AppColors.textSecondary
    )
    Spacer(Modifier.height(AppSpacing.sm.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface.copy(alpha = 0.8f))
    ) {
        content()
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(AppSpacing.md.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = AppTypography.body.copy(fontSize = 15.sp), color = AppColors.textPrimary)
            subtitle?.let {
                Text(it, style = AppTypography.caption, color = AppColors.textSecondary)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    isPremium: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = AppTypography.body.copy(fontSize = 15.sp),
            color = if (isPremium) AppColors.textSecondary.copy(alpha = 0.5f) else AppColors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        if (isPremium) {
            Text(
                text = "PRO",
                style = AppTypography.label.copy(fontSize = 10.sp),
                color = AppColors.accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppColors.accent.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Switch(
            checked = enabled,
            onCheckedChange = { if (!isPremium) onToggle(it) },
            enabled = !isPremium,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.accent,
                checkedTrackColor = AppColors.accent.copy(alpha = 0.3f),
                uncheckedThumbColor = AppColors.textSecondary,
                uncheckedTrackColor = AppColors.surface
            )
        )
    }
}
