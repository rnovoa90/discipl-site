package com.discipl.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current


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
                    onToggle = { viewModel.toggleDailyTask(it) },
                    isPremium = !state.isPremium
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
                text = if (state.isPremium) "v1.0.0 (PRO)" else "v1.0.0",
                style = AppTypography.caption,
                color = AppColors.textSecondary.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (com.discipl.app.BuildConfig.DEBUG) {
                            Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    viewModel.toggleDebugPremium()
                                    Toast.makeText(
                                        context,
                                        if (!state.isPremium) "Premium enabled" else "Premium disabled",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        } else Modifier
                    )
            )

            Spacer(Modifier.height(AppSpacing.xxl.dp))
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
