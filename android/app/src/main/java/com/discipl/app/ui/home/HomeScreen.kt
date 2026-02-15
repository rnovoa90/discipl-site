package com.discipl.app.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.discipl.app.ui.components.AnimatedBackground
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppSpacing
import com.discipl.app.ui.theme.AppTypography
import com.discipl.app.ui.theme.SenFontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToTab: (Int) -> Unit = {},
    onNavigateToBenefitsMilestone: (Int) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showRelapseFlow by remember { mutableStateOf(false) }
    var showJournalFlow by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadData() }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(AppSpacing.xl.dp))

            // Streak counter
            StreakCounter(
                streakDays = state.currentStreakDays,
                language = state.language
            )

            // Streak subtitle
            Text(
                text = streakSubtitle(state.currentStreakDays, state.language),
                style = AppTypography.body.copy(fontSize = 15.sp, letterSpacing = 2.sp),
                color = AppColors.textSecondary
            )

            // Start date
            state.currentStreak?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatStartDate(it.startDate, state.language),
                    style = AppTypography.caption.copy(fontSize = 11.sp),
                    color = AppColors.textSecondary.copy(alpha = 0.5f)
                )
            }

            Spacer(Modifier.height(AppSpacing.lg.dp))

            // Milestone celebration
            state.currentMilestone?.let { milestone ->
                MilestoneCelebrationCard(milestone, state.language) {
                    onNavigateToBenefitsMilestone(milestone.day)
                }
                Spacer(Modifier.height(AppSpacing.lg.dp))
            }

            // Longest streak badge
            LongestStreakBadge(state.longestStreakDays, state.language)

            Spacer(Modifier.height(AppSpacing.lg.dp))

            // Action buttons
            ActionButtons(
                checkedIn = state.hasCheckedInToday,
                language = state.language,
                onCheckIn = { showJournalFlow = true },
                onRelapse = { showRelapseFlow = true }
            )

            Spacer(Modifier.height(AppSpacing.lg.dp))

            // Next milestone teaser
            state.nextMilestone?.let { milestone ->
                NextMilestoneTeaser(milestone, state.language) { onNavigateToTab(1) }
                Spacer(Modifier.height(AppSpacing.lg.dp))
            }

            // Task section (days 0-30)
            if (state.currentStreakDays <= 30) {
                if (state.taskText != null) {
                    TaskCard(state.taskText!!, state.language)
                } else if (!state.isPremium) {
                    BlurredTaskCard(state.language) { showPaywall = true }
                }
                Spacer(Modifier.height(AppSpacing.lg.dp))
            } else if (state.insightText != null) {
                InsightCard(state.insightText!!, state.language)
                Spacer(Modifier.height(AppSpacing.lg.dp))
            } else if (!state.isPremium && state.currentStreakDays > 30) {
                BlurredInsightCard(state.language) { showPaywall = true }
                Spacer(Modifier.height(AppSpacing.lg.dp))
            }

            Spacer(Modifier.height(AppSpacing.xl.dp))
        }
    }

    // Relapse bottom sheet
    if (showRelapseFlow) {
        ModalBottomSheet(
            onDismissRequest = { showRelapseFlow = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = AppColors.surface
        ) {
            RelapseFlow(
                language = state.language,
                onConfirm = { timeOfDay, trigger, mood, notes ->
                    viewModel.recordRelapse(timeOfDay, trigger, mood, notes)
                    showRelapseFlow = false
                },
                onDismiss = { showRelapseFlow = false }
            )
        }
    }

    // Journal bottom sheet
    if (showJournalFlow) {
        ModalBottomSheet(
            onDismissRequest = { showJournalFlow = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = AppColors.surface
        ) {
            JournalFlow(
                language = state.language,
                onComplete = { mood, feelings, energy, notes ->
                    viewModel.recordJournalEntry(mood, feelings, energy, notes)
                    showJournalFlow = false
                },
                onDismiss = { showJournalFlow = false }
            )
        }
    }
}

@Composable
private fun StreakCounter(streakDays: Int, language: String) {
    var displayedNumber by remember { mutableIntStateOf(0) }
    var ringProgress by remember { mutableFloatStateOf(0f) }

    val milestones = listOf(1, 3, 7, 14, 21, 30, 60, 90, 180, 365, 730, 1095)
    val nextMilestoneDay = milestones.firstOrNull { it > streakDays } ?: 1095
    val prevMilestoneDay = milestones.lastOrNull { it <= streakDays } ?: 0
    val milestoneProgress = if (nextMilestoneDay > prevMilestoneDay) {
        (streakDays - prevMilestoneDay).toFloat() / (nextMilestoneDay - prevMilestoneDay)
    } else 1f
    val journeyProgress = min(streakDays / 90f, 1f)

    // Animate ring
    val animatedRing by animateFloatAsState(
        targetValue = milestoneProgress,
        animationSpec = tween(1200),
        label = "ring"
    )

    // Count up animation
    LaunchedEffect(streakDays) {
        displayedNumber = 0
        val steps = min(streakDays, 60)
        if (steps == 0) {
            displayedNumber = 0
            return@LaunchedEffect
        }
        val totalMs = min(1500L, streakDays * 30L)
        val interval = totalMs / steps
        val increment = maxOf(1, streakDays / steps)
        for (i in 0..steps) {
            displayedNumber = min(streakDays, increment * i)
            delay(interval)
        }
        displayedNumber = streakDays
    }

    // Glow pulse
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val ringSize = 180f
    val outerRingSize = ringSize + 28f

    Box(
        modifier = Modifier
            .size(220.dp)
            .drawBehind {
                val center = Offset(size.width / 2, size.height / 2)
                val ringRadius = ringSize / 2 * density
                val outerRadius = outerRingSize / 2 * density

                // Outer journey ring background (dotted)
                drawCircle(
                    color = AppColors.surface.copy(alpha = 0.3f),
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = 2f * density)
                )

                // Outer journey ring progress
                drawArc(
                    color = AppColors.accent.copy(alpha = 0.4f),
                    startAngle = -90f,
                    sweepAngle = journeyProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = 2f * density, cap = StrokeCap.Round),
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2)
                )

                // Inner background ring
                drawCircle(
                    color = AppColors.surface.copy(alpha = 0.5f),
                    radius = ringRadius,
                    center = center,
                    style = Stroke(width = 4f * density)
                )

                // Main progress ring
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(AppColors.accent, AppColors.success, AppColors.accent),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedRing * 360f,
                    useCenter = false,
                    style = Stroke(width = 4f * density, cap = StrokeCap.Round),
                    topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                    size = androidx.compose.ui.geometry.Size(ringRadius * 2, ringRadius * 2)
                )

                // Glow behind number
                drawCircle(
                    color = AppColors.success.copy(alpha = glowAlpha * 0.15f),
                    radius = ringRadius * 0.78f,
                    center = center
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Next milestone day label — positioned at top between inner and outer rings
        if (streakDays < 1095) {
            Text(
                text = "$nextMilestoneDay",
                style = AppTypography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = AppColors.accent.copy(alpha = 0.6f),
                modifier = Modifier.offset(y = -(ringSize / 2 + 7).dp)
            )
        }

        // "90" label on the outer journey ring — shows the 90-day target
        if (streakDays < 90) {
            Text(
                text = "90",
                style = AppTypography.label.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = AppColors.textSecondary.copy(alpha = 0.4f),
                modifier = Modifier.offset(y = -(outerRingSize / 2 + 10).dp)
            )
        }

        val digitCount = displayedNumber.toString().length
        val fontSize = when {
            digitCount >= 5 -> 30.sp
            digitCount == 4 -> 42.sp
            else -> 64.sp
        }

        Text(
            text = "$displayedNumber",
            style = AppTypography.streakNumber.copy(
                fontSize = fontSize,
                fontWeight = FontWeight.Normal,
                brush = Brush.linearGradient(
                    colors = listOf(AppColors.success, AppColors.accent.copy(alpha = 0.8f))
                )
            )
        )
    }
}

@Composable
private fun MilestoneCelebrationCard(milestone: com.discipl.app.data.model.Milestone, language: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.success.copy(alpha = 0.1f))
            .border(1.dp, AppColors.success.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(AppSpacing.md.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🎉", fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (language == "en") "MILESTONE REACHED!" else "¡META ALCANZADA!",
                style = AppTypography.label,
                color = AppColors.success
            )
        }
        Spacer(Modifier.height(AppSpacing.sm.dp))
        Text(
            text = milestone.dayLabel(language),
            style = AppTypography.body.copy(fontWeight = FontWeight.Bold),
            color = AppColors.textPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = milestone.title(language),
            style = AppTypography.caption.copy(fontSize = 14.sp),
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LongestStreakBadge(longest: Int, language: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.surface.copy(alpha = 0.8f))
            .padding(horizontal = AppSpacing.md.dp, vertical = AppSpacing.sm.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = AppColors.accent,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(6.dp))
        val dayWord = if (longest == 1) {
            if (language == "en") "day" else "día"
        } else {
            if (language == "en") "days" else "días"
        }
        Text(
            text = if (language == "en") "Record: $longest $dayWord" else "Récord: $longest $dayWord",
            style = AppTypography.label.copy(fontSize = 12.sp),
            color = AppColors.textSecondary
        )
    }
}

@Composable
private fun ActionButtons(
    checkedIn: Boolean,
    language: String,
    onCheckIn: () -> Unit,
    onRelapse: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl.dp)) {
        // Check-in button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(AppColors.surface)
                    .border(1.5.dp, AppColors.success.copy(alpha = if (checkedIn) 0.2f else 0.4f), CircleShape)
                    .clickable(enabled = !checkedIn, onClick = onCheckIn),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Check-in",
                    tint = AppColors.success,
                    modifier = Modifier
                        .size(20.dp)
                        .then(if (checkedIn) Modifier.blur(1.dp) else Modifier)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (checkedIn) {
                    if (language == "en") "Done" else "Hecho"
                } else "Check-in",
                style = AppTypography.caption.copy(fontSize = 11.sp),
                color = AppColors.textSecondary.copy(alpha = if (checkedIn) 0.4f else 1f)
            )
        }

        // Relapse button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(AppColors.surface)
                    .border(1.5.dp, AppColors.danger.copy(alpha = 0.4f), CircleShape)
                    .clickable(onClick = onRelapse),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Relapse",
                    tint = AppColors.danger,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (language == "en") "Relapse" else "Recaída",
                style = AppTypography.caption.copy(fontSize = 11.sp),
                color = AppColors.textSecondary
            )
        }
    }
}

@Composable
private fun NextMilestoneTeaser(
    milestone: com.discipl.app.data.model.Milestone,
    language: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface.copy(alpha = 0.6f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Star, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (language == "en") "Next goal:" else "Próxima meta:",
            style = AppTypography.caption.copy(fontSize = 13.sp),
            color = AppColors.textSecondary
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = milestone.title(language),
            style = AppTypography.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            color = AppColors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(11.dp))
    }
}

@Composable
private fun TaskCard(taskText: String, language: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface.copy(alpha = 0.6f))
            .padding(AppSpacing.md.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.TaskAlt, contentDescription = null, tint = AppColors.success, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (language == "en") "TASK OF THE DAY" else "TAREA DEL DÍA",
                style = AppTypography.label,
                color = AppColors.success
            )
        }
        Spacer(Modifier.height(AppSpacing.sm.dp))
        Text(
            text = taskText,
            style = AppTypography.caption.copy(fontSize = 14.sp),
            color = AppColors.textSecondary,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun BlurredTaskCard(language: String, onTap: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface.copy(alpha = 0.6f))
            .clickable(onClick = onTap)
            .padding(AppSpacing.md.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.TaskAlt, contentDescription = null, tint = AppColors.success, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (language == "en") "TASK OF THE DAY" else "TAREA DEL DÍA",
                style = AppTypography.label,
                color = AppColors.success
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "PRO",
                style = AppTypography.label.copy(fontSize = 11.sp),
                color = AppColors.accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppColors.accent.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(AppSpacing.sm.dp))
        Text(
            text = if (language == "en")
                "Unlock daily tasks to build discipline during your first 30 days."
            else
                "Desbloquea tareas diarias para construir disciplina en tus primeros 30 días.",
            style = AppTypography.caption.copy(fontSize = 14.sp),
            color = AppColors.textSecondary,
            lineHeight = 20.sp,
            modifier = Modifier.blur(4.dp)
        )
    }
}

@Composable
private fun InsightCard(insightText: String, language: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface.copy(alpha = 0.6f))
            .padding(AppSpacing.md.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (language == "en") "DAILY INSIGHT" else "REFLEXIÓN DEL DÍA",
                style = AppTypography.label,
                color = AppColors.accent
            )
        }
        Spacer(Modifier.height(AppSpacing.sm.dp))
        Text(
            text = insightText,
            style = AppTypography.caption.copy(fontSize = 14.sp),
            color = AppColors.textSecondary,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun BlurredInsightCard(language: String, onTap: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface.copy(alpha = 0.6f))
            .clickable(onClick = onTap)
            .padding(AppSpacing.md.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (language == "en") "DAILY INSIGHT" else "REFLEXIÓN DEL DÍA",
                style = AppTypography.label,
                color = AppColors.accent
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "PRO",
                style = AppTypography.label.copy(fontSize = 11.sp),
                color = AppColors.accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppColors.accent.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(AppSpacing.sm.dp))
        Text(
            text = if (language == "en")
                "Unlock daily science-based reflections tailored to your streak progress."
            else
                "Desbloquea reflexiones diarias basadas en ciencia según tu progreso.",
            style = AppTypography.caption.copy(fontSize = 14.sp),
            color = AppColors.textSecondary,
            lineHeight = 20.sp,
            modifier = Modifier.blur(4.dp)
        )
    }
}

// --- Helpers ---

private fun streakSubtitle(days: Int, language: String): String = when {
    days == 0 -> if (language == "en") "Your journey begins" else "Tu camino comienza"
    days == 1 -> if (language == "en") "Day of self-control" else "Día de autocontrol"
    else -> if (language == "en") "Days of self-control" else "Días de autocontrol"
}

private fun formatStartDate(epochMillis: Long, language: String): String {
    val locale = if (language == "en") Locale.US else Locale("es", "ES")
    val formatter = SimpleDateFormat("d MMM yyyy", locale)
    val prefix = if (language == "en") "Since" else "Desde"
    return "$prefix ${formatter.format(Date(epochMillis))}"
}
