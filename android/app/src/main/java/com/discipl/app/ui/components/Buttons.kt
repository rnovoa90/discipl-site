package com.discipl.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppTypography

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "scale")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = if (enabled) 1f else 0.4f)
            .shadow(12.dp, RoundedCornerShape(12.dp), ambientColor = AppColors.accent.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(AppColors.accent, AppColors.accent.copy(alpha = 0.7f))
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTypography.button,
            color = AppColors.textPrimary
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "scale")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface.copy(alpha = 0.8f))
            .border(1.dp, AppColors.textSecondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTypography.button,
            color = AppColors.textSecondary
        )
    }
}

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "scale")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface.copy(alpha = 0.8f))
            .border(1.dp, AppColors.danger.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = AppColors.danger.copy(alpha = 0.2f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTypography.button,
            color = AppColors.danger
        )
    }
}
