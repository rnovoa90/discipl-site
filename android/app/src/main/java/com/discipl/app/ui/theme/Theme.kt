package com.discipl.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.accent,
    onPrimary = AppColors.textPrimary,
    secondary = AppColors.success,
    onSecondary = AppColors.textPrimary,
    tertiary = AppColors.danger,
    background = AppColors.background,
    onBackground = AppColors.textPrimary,
    surface = AppColors.surface,
    onSurface = AppColors.textPrimary,
    surfaceVariant = AppColors.surface,
    onSurfaceVariant = AppColors.textSecondary,
    outline = AppColors.textSecondary.copy(alpha = 0.3f),
    error = AppColors.danger,
    onError = AppColors.textPrimary
)

@Composable
fun DisciplTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
