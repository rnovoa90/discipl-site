package com.discipl.app.ui.benefits

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.discipl.app.data.model.Milestone
import com.discipl.app.ui.components.AnimatedBackground
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppSpacing
import com.discipl.app.ui.theme.AppTypography

@Composable
fun BenefitsTimelineScreen(
    modifier: Modifier = Modifier,
    viewModel: BenefitsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

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
                text = if (state.language == "en") "Benefits Timeline" else "Línea de Beneficios",
                style = AppTypography.sectionHeader,
                color = AppColors.textPrimary
            )
            Spacer(Modifier.height(AppSpacing.sm.dp))
            Text(
                text = if (state.language == "en")
                    "Your brain is healing. Here's what to expect."
                else
                    "Tu cerebro se está sanando. Esto es lo que puedes esperar.",
                style = AppTypography.caption,
                color = AppColors.textSecondary
            )

            Spacer(Modifier.height(AppSpacing.lg.dp))

            state.milestones.forEachIndexed { index, milestone ->
                MilestoneCard(
                    milestone = milestone,
                    streakDays = state.currentStreakDays,
                    isPremium = state.isPremium,
                    language = state.language,
                    isLast = index == state.milestones.size - 1,
                    onLockedTap = { viewModel.onPaywallHit() }
                )
            }

            Spacer(Modifier.height(AppSpacing.xxl.dp))
        }
    }
}

@Composable
private fun MilestoneCard(
    milestone: Milestone,
    streakDays: Int,
    isPremium: Boolean,
    language: String,
    isLast: Boolean,
    onLockedTap: () -> Unit
) {
    val isPassed = streakDays >= milestone.day
    val isAccessible = milestone.isFree || isPremium
    var isExpanded by remember { mutableStateOf(false) }
    val view = LocalView.current

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // Timeline line + circle
        Box(
            modifier = Modifier.width(40.dp).fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Continuous connector line behind the circle
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(top = 14.dp)
                        .background(
                            if (isPassed) AppColors.success.copy(alpha = 0.4f)
                            else AppColors.textSecondary.copy(alpha = 0.15f)
                        )
                )
            }
            // Circle on top of the line
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isPassed -> AppColors.success
                            else -> AppColors.surface
                        }
                    )
                    .border(
                        2.dp,
                        when {
                            isPassed -> AppColors.success
                            else -> AppColors.textSecondary.copy(alpha = 0.3f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPassed) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = AppColors.background,
                        modifier = Modifier.size(14.dp)
                    )
                } else if (!isAccessible) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = AppColors.textSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                } else {
                    Text(
                        milestone.shortLabel,
                        style = AppTypography.caption.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = AppColors.textSecondary
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        // Card content
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.surface.copy(alpha = 0.8f))
                .border(
                    1.dp,
                    when {
                        isPassed -> AppColors.success.copy(alpha = 0.3f)
                        else -> AppColors.textSecondary.copy(alpha = 0.1f)
                    },
                    RoundedCornerShape(12.dp)
                )
                .clickable {
                    if (isAccessible) {
                        isExpanded = !isExpanded
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } else {
                        onLockedTap()
                    }
                }
                .padding(AppSpacing.md.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = milestone.dayLabel(language),
                    style = AppTypography.label.copy(fontSize = 11.sp),
                    color = if (isPassed) AppColors.success else AppColors.accent
                )
                Spacer(Modifier.weight(1f))
                if (isPassed) {
                    Text(
                        text = "✓",
                        style = AppTypography.label,
                        color = AppColors.success
                    )
                } else if (!isAccessible) {
                    Text(
                        text = "PRO",
                        style = AppTypography.label.copy(fontSize = 10.sp),
                        color = AppColors.accent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AppColors.accent.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                } else {
                    Text(
                        text = if (language == "en") "SOON" else "PRONTO",
                        style = AppTypography.label.copy(fontSize = 10.sp),
                        color = AppColors.textSecondary
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = milestone.title(language),
                style = AppTypography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                color = AppColors.textPrimary,
                modifier = if (!isPassed) Modifier.blur(6.dp) else Modifier
            )

            // Detail text (blurred if locked)
            if (isAccessible) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        Spacer(Modifier.height(AppSpacing.sm.dp))
                        Text(
                            text = milestone.detail(language),
                            style = AppTypography.caption.copy(fontSize = 13.sp),
                            color = AppColors.textSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(Modifier.height(AppSpacing.md.dp))
                        Text(
                            text = if (language == "en") "Why does this happen?" else "¿Por qué pasa esto?",
                            style = AppTypography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                            color = AppColors.accent
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = milestone.why(language),
                            style = AppTypography.caption.copy(fontSize = 12.sp),
                            color = AppColors.textSecondary,
                            lineHeight = 16.sp
                        )

                        val signs = milestone.signs(language)
                        if (signs.isNotEmpty()) {
                            Spacer(Modifier.height(AppSpacing.md.dp))
                            Text(
                                text = if (language == "en") "How you'll notice it:" else "Cómo lo notarás:",
                                style = AppTypography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                color = AppColors.success
                            )
                            signs.forEach { sign ->
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "• $sign",
                                    style = AppTypography.caption.copy(fontSize = 12.sp),
                                    color = AppColors.textSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(Modifier.height(AppSpacing.sm.dp))
                Text(
                    text = milestone.detail(language),
                    style = AppTypography.caption.copy(fontSize = 13.sp),
                    color = AppColors.textSecondary,
                    modifier = Modifier.blur(6.dp),
                    maxLines = 2
                )
            }
        }
    }

    if (!isLast) {
        Spacer(Modifier.height(4.dp))
    }
}
