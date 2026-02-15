package com.discipl.app.ui.onboarding

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.discipl.app.ui.components.AnimatedBackground
import com.discipl.app.ui.components.PrimaryButton
import com.discipl.app.ui.paywall.PaywallScreen
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppSpacing
import com.discipl.app.ui.theme.AppTypography
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val view = LocalView.current

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar: back button + progress dots
            TopBar(
                currentStep = state.currentStep,
                totalSteps = state.totalSteps,
                onBack = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    viewModel.goBack()
                }
            )

            // Screen content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (state.currentStep) {
                    0 -> WelcomeScreen(state)
                    1 -> QuitTypeScreen(state, viewModel, view)
                    2 -> DurationScreen(state, viewModel, view)
                    3 -> EscalationScreen(state, viewModel, view)
                    4 -> SymptomsScreen(state, viewModel, view)
                    5 -> GoalsScreen(state, viewModel, view)
                    6 -> ScoreScreen(state, viewModel)
                    7 -> PlanScreen(state, viewModel)
                    8 -> QuitDateScreen(state, viewModel)
                    9 -> DisclaimerScreen(state, viewModel, view)
                }
            }

            // Bottom button
            PrimaryButton(
                text = state.buttonText,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    viewModel.goNext()
                },
                enabled = state.canContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xl.dp)
                    .padding(bottom = AppSpacing.xxl.dp)
            )
        }

        // Quit date confirmation dialog
        if (state.showQuitDateConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissQuitDateConfirmation() },
                title = {
                    Text(
                        if (state.language == "en") "Confirm Your Start Date" else "Confirma tu fecha de inicio",
                        style = AppTypography.body.copy(fontWeight = FontWeight.Bold),
                        color = AppColors.textPrimary
                    )
                },
                text = {
                    Text(
                        if (state.language == "en")
                            "This date is more than a week ago. This app works best when your data reflects reality. Being honest with yourself is the first step toward real change."
                        else
                            "Esta fecha es de hace más de una semana. Esta app funciona mejor cuando tus datos reflejan la realidad. Ser honesto contigo mismo es el primer paso hacia un cambio real.",
                        style = AppTypography.caption,
                        color = AppColors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissQuitDateConfirmation() }) {
                        Text(
                            if (state.language == "en") "Yes, It's Correct" else "Sí, es correcta",
                            color = AppColors.accent
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.resetQuitDate() }) {
                        Text(
                            if (state.language == "en") "Change Date" else "Cambiar fecha",
                            color = AppColors.textSecondary
                        )
                    }
                },
                containerColor = AppColors.surface,
                titleContentColor = AppColors.textPrimary,
                textContentColor = AppColors.textSecondary
            )
        }

        // Paywall as full-screen dialog
        if (state.showPaywall) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = {
                    viewModel.dismissPaywall()
                    viewModel.completeOnboarding(onComplete)
                },
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false
                )
            ) {
                PaywallScreen(
                    onDismiss = {
                        viewModel.dismissPaywall()
                        viewModel.completeOnboarding(onComplete)
                    }
                )
            }
        }
    }
}

// MARK: - Top Bar

@Composable
private fun TopBar(currentStep: Int, totalSteps: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg.dp, vertical = AppSpacing.md.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep > 0) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.textSecondary
                )
            }
        } else {
            Spacer(Modifier.size(48.dp))
        }

        Spacer(Modifier.weight(1f))

        // Progress dots (visible after welcome)
        if (currentStep > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 1 until totalSteps) {
                    Box(
                        modifier = Modifier
                            .size(if (i == currentStep) 10.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    i == currentStep -> AppColors.accent
                                    i < currentStep -> AppColors.accent.copy(alpha = 0.5f)
                                    else -> AppColors.textSecondary.copy(alpha = 0.3f)
                                }
                            )
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.size(48.dp))
    }
}

// MARK: - Screen 0: Welcome

@Composable
private fun WelcomeScreen(state: OnboardingUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.lg.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.discipl.app.R.drawable.discipl_logo),
            contentDescription = "Discipl",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg.dp)
        )

        Spacer(Modifier.height(AppSpacing.xl.dp))

        Text(
            text = if (state.language == "en") "Your path to discipline starts today"
            else "Tu camino a la disciplina comienza hoy",
            style = AppTypography.body.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// MARK: - Screen 1: Quit Type

@Composable
private fun QuitTypeScreen(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
    view: android.view.View
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.xl.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = if (state.language == "en") "What do you want to quit?" else "¿Qué quieres dejar?",
            style = AppTypography.sectionHeader.copy(fontSize = 28.sp),
            color = AppColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(AppSpacing.xl.dp))

        QuizOptionCard(
            title = if (state.language == "en") "Pornography" else "Pornografía",
            isSelected = state.selectedQuitType == "porn",
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.selectQuitType("porn")
            }
        )
        Spacer(Modifier.height(AppSpacing.md.dp))
        QuizOptionCard(
            title = if (state.language == "en") "Masturbation" else "Masturbación",
            isSelected = state.selectedQuitType == "masturbation",
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.selectQuitType("masturbation")
            }
        )
        Spacer(Modifier.height(AppSpacing.md.dp))
        QuizOptionCard(
            title = if (state.language == "en") "Both" else "Ambos",
            isSelected = state.selectedQuitType == "both",
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.selectQuitType("both")
            }
        )

        Spacer(Modifier.weight(2f))
    }
}

// MARK: - Screen 2: Duration

@Composable
private fun DurationScreen(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
    view: android.view.View
) {
    val durations = listOf(
        "< 1 año" to "< 1 year",
        "1-3 años" to "1-3 years",
        "3-5 años" to "3-5 years",
        "5+ años" to "5+ years"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.xl.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = if (state.language == "en") "How long have you struggled with this?"
            else "¿Hace cuánto luchas con esto?",
            style = AppTypography.sectionHeader.copy(fontSize = 28.sp),
            color = AppColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(AppSpacing.xl.dp))

        durations.forEach { (es, en) ->
            QuizOptionCard(
                title = if (state.language == "en") en else es,
                isSelected = state.selectedDuration == es,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    viewModel.selectDuration(es)
                }
            )
            Spacer(Modifier.height(AppSpacing.md.dp))
        }

        Spacer(Modifier.weight(2f))
    }
}

// MARK: - Screen 3: Escalation

@Composable
private fun EscalationScreen(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
    view: android.view.View
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.xl.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = if (state.language == "en") "Has your usage escalated over time?"
            else "¿Tu consumo ha escalado con el tiempo?",
            style = AppTypography.sectionHeader.copy(fontSize = 28.sp),
            color = AppColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(AppSpacing.sm.dp))

        Text(
            text = if (state.language == "en") "Seeking more extreme content or spending more time"
            else "Buscando contenido más extremo o dedicando más tiempo",
            style = AppTypography.caption,
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(AppSpacing.xl.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md.dp)
        ) {
            QuizOptionCard(
                title = if (state.language == "en") "Yes" else "Sí",
                isSelected = state.hasEscalated == true,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    viewModel.setEscalated(true)
                },
                modifier = Modifier.weight(1f)
            )
            QuizOptionCard(
                title = "No",
                isSelected = state.hasEscalated == false,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    viewModel.setEscalated(false)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.weight(2f))
    }
}

// MARK: - Screen 4: Symptoms

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomsScreen(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
    view: android.view.View
) {
    val symptoms = listOf(
        Triple("low_energy", "Poca energía", "Low energy"),
        Triple("brain_fog", "Niebla mental", "Brain fog"),
        Triple("guilt", "Culpa/Vergüenza", "Guilt/Shame"),
        Triple("relationship", "Problemas de pareja", "Relationship issues"),
        Triple("confidence", "Baja confianza", "Low confidence"),
        Triple("focus", "Dificultad para concentrarse", "Difficulty focusing")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.lg.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = if (state.language == "en") "How does it affect you?" else "¿Cómo te afecta?",
            style = AppTypography.sectionHeader.copy(fontSize = 28.sp),
            color = AppColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(AppSpacing.sm.dp))

        Text(
            text = if (state.language == "en") "Select all that apply" else "Selecciona todo lo que aplique",
            style = AppTypography.caption,
            color = AppColors.textSecondary
        )

        Spacer(Modifier.height(AppSpacing.xl.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md.dp),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md.dp),
            maxItemsInEachRow = 2,
            modifier = Modifier.fillMaxWidth()
        ) {
            symptoms.forEach { (id, es, en) ->
                MultiSelectCard(
                    title = if (state.language == "en") en else es,
                    isSelected = state.selectedSymptoms.contains(id),
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        viewModel.toggleSymptom(id)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.weight(2f))
    }
}

// MARK: - Screen 5: Goals

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalsScreen(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
    view: android.view.View
) {
    val goals = listOf(
        Triple("energy", "Más energía", "More energy"),
        Triple("clarity", "Claridad mental", "Mental clarity"),
        Triple("relationships", "Mejores relaciones", "Better relationships"),
        Triple("confidence", "Más confianza", "More confidence"),
        Triple("focus", "Mejor enfoque", "Better focus"),
        Triple("self_respect", "Respeto propio", "Self-respect")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.lg.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = if (state.language == "en") "What do you want to achieve?" else "¿Qué quieres lograr?",
            style = AppTypography.sectionHeader.copy(fontSize = 28.sp),
            color = AppColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(AppSpacing.sm.dp))

        Text(
            text = if (state.language == "en") "Select all that apply" else "Selecciona todo lo que aplique",
            style = AppTypography.caption,
            color = AppColors.textSecondary
        )

        Spacer(Modifier.height(AppSpacing.xl.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md.dp),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md.dp),
            maxItemsInEachRow = 2,
            modifier = Modifier.fillMaxWidth()
        ) {
            goals.forEach { (id, es, en) ->
                MultiSelectCard(
                    title = if (state.language == "en") en else es,
                    isSelected = state.selectedGoals.contains(id),
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        viewModel.toggleGoal(id)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.weight(2f))
    }
}

// MARK: - Screen 6: Dependency Score

@Composable
private fun ScoreScreen(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    val targetScore = state.dependencyScore
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(targetScore) {
        animProgress.animateTo(0f, tween(0))
        // Animate score count-up over 1.5s
        for (i in 1..maxOf(targetScore, 1)) {
            delay((1500L / maxOf(targetScore, 1)))
            viewModel.updateAnimatedScore(minOf(i, targetScore))
        }
        delay(300)
        viewModel.setScoreAnimationDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.xl.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        // Circular progress with score
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            // Background ring
            androidx.compose.foundation.Canvas(modifier = Modifier.size(200.dp)) {
                drawCircle(
                    color = AppColors.surface,
                    style = Stroke(width = 12.dp.toPx())
                )
            }

            // Progress ring — transitions from accent (teal) to danger (red) as score increases
            androidx.compose.foundation.Canvas(modifier = Modifier.size(200.dp)) {
                val sweep = 360f * state.animatedScore / 100f
                val fraction = state.animatedScore / 100f
                // Lerp from accent to danger based on score
                val ringColor = androidx.compose.ui.graphics.lerp(AppColors.accent, AppColors.danger, fraction)
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Score number — regular weight to match iOS
            Text(
                text = "${state.animatedScore}",
                style = AppTypography.streakNumber.copy(
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = AppColors.textPrimary
            )
        }

        Spacer(Modifier.height(AppSpacing.lg.dp))

        Text(
            text = if (state.language == "en") "OVERDEPENDENCY LEVEL" else "NIVEL DE SOBREDEPENDENCIA",
            style = AppTypography.label.copy(letterSpacing = 1.5.sp),
            color = AppColors.textSecondary
        )

        AnimatedVisibility(
            visible = state.scoreAnimationDone,
            enter = fadeIn() + slideInVertically { it / 2 }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = AppSpacing.lg.dp)
            ) {
                Text(
                    text = if (state.language == "en")
                        "Your overdependency level is ${state.dependencyScore} out of 100"
                    else
                        "Tu nivel de sobredependencia está en ${state.dependencyScore} de 100",
                    style = AppTypography.body,
                    color = AppColors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(AppSpacing.sm.dp))
                Text(
                    text = if (state.language == "en")
                        "But we have good news... Discipl can help you"
                    else
                        "Pero tenemos buenas noticias... Discipl puede ayudarte",
                    style = AppTypography.body.copy(fontSize = 15.sp),
                    color = AppColors.accent,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.weight(2f))
    }
}

// MARK: - Screen 7: Plan

@Composable
private fun PlanScreen(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    LaunchedEffect(Unit) {
        if (!state.planReady) {
            // Animate progress over 2 seconds
            val steps = 50
            for (i in 1..steps) {
                delay(2000L / steps)
                viewModel.updatePlanProgress(i.toFloat() / steps)
            }
            delay(200)
            viewModel.setPlanReady()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.xl.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        if (!state.planReady) {
            // Loading state
            Text(
                text = if (state.language == "en") "Creating your personalized plan..."
                else "Creando tu plan personalizado...",
                style = AppTypography.sectionHeader,
                color = AppColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(AppSpacing.lg.dp))

            LinearProgressIndicator(
                progress = { state.planProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = AppColors.accent,
                trackColor = AppColors.surface
            )
        } else {
            // Plan ready
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.discipl.app.R.drawable.discipl_logo),
                contentDescription = "Discipl",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xl.dp)
            )

            Spacer(Modifier.height(AppSpacing.lg.dp))

            Text(
                text = if (state.language == "en") "Your personalized plan is ready"
                else "Tu plan personalizado está listo",
                style = AppTypography.sectionHeader.copy(fontSize = 24.sp),
                color = AppColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(AppSpacing.sm.dp))

            Text(
                text = if (state.language == "en") "Based on your answers, your plan includes:"
                else "Basado en tus respuestas, tu plan incluye:",
                style = AppTypography.body.copy(fontSize = 15.sp),
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(AppSpacing.lg.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.surface)
                    .padding(AppSpacing.lg.dp),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md.dp)
            ) {
                state.planBulletPoints.forEach { point ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AppColors.success,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = point,
                            style = AppTypography.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                            color = AppColors.textPrimary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(2f))
    }
}

// MARK: - Screen 8: Quit Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuitDateScreen(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    // DatePicker works in UTC — compute today as UTC midnight for the constraint
    val todayUtcMillis = remember {
        java.time.LocalDate.now()
            .atStartOfDay(java.time.ZoneOffset.UTC)
            .toInstant().toEpochMilli()
    }
    // Convert stored local millis to UTC midnight for initial selection
    val initialDateUtcMillis = remember(state.selectedQuitDateMillis) {
        val localDate = java.time.Instant.ofEpochMilli(state.selectedQuitDateMillis)
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

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { utcMillis ->
            // Convert UTC midnight back to local midnight
            val localDate = java.time.Instant.ofEpochMilli(utcMillis)
                .atZone(java.time.ZoneOffset.UTC)
                .toLocalDate()
            val localMillis = localDate
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli()
            viewModel.selectQuitDate(localMillis)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.lg.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(AppSpacing.md.dp))

        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = AppColors.accent,
            modifier = Modifier.size(56.dp)
        )

        Spacer(Modifier.height(AppSpacing.md.dp))

        Text(
            text = if (state.language == "en") "When did you start?" else "¿Cuándo comenzaste?",
            style = AppTypography.sectionHeader.copy(fontSize = 28.sp),
            color = AppColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(AppSpacing.sm.dp))

        Text(
            text = if (state.language == "en") "Select today or the date you started your journey"
            else "Selecciona hoy o la fecha en que comenzaste tu camino",
            style = AppTypography.body.copy(fontSize = 15.sp),
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(AppSpacing.md.dp))

        DatePicker(
            state = datePickerState,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(AppColors.surface),
            title = null,
            headline = null,
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor = AppColors.surface,
                selectedDayContainerColor = AppColors.accent,
                todayDateBorderColor = AppColors.accent,
                todayContentColor = AppColors.accent
            )
        )

        Spacer(Modifier.height(AppSpacing.lg.dp))
    }
}

// MARK: - Screen 9: Disclaimer

@Composable
private fun DisclaimerScreen(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
    view: android.view.View
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.lg.dp)
    ) {
        Spacer(Modifier.height(AppSpacing.md.dp))

        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = AppColors.accent,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(AppSpacing.sm.dp))
            Text(
                text = if (state.language == "en") "Before You Begin" else "Antes de comenzar",
                style = AppTypography.sectionHeader.copy(fontSize = 24.sp),
                color = AppColors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (state.language == "en") "Please read and accept the following terms."
                else "Por favor lee y acepta los siguientes términos.",
                style = AppTypography.caption,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(AppSpacing.lg.dp))

        // Disclaimer sections
        DisclaimerSection(
            title = if (state.language == "en") "Health & Medical Disclaimer" else "Aviso de salud",
            body = if (state.language == "en")
                "Discipl is a self-tracking tool, not a medical device or therapy. It does not diagnose, treat, or cure any condition. If you are experiencing mental health issues, addiction, or emotional distress, please consult a qualified healthcare professional."
            else
                "Discipl es una herramienta de seguimiento personal, no un dispositivo médico ni terapia. No diagnostica, trata ni cura ninguna condición. Si experimentas problemas de salud mental, adicción o angustia emocional, consulta a un profesional de salud calificado."
        )
        Spacer(Modifier.height(AppSpacing.sm.dp))

        DisclaimerSection(
            title = if (state.language == "en") "No Guarantee of Results" else "Sin garantía de resultados",
            body = if (state.language == "en")
                "The milestones and benefits shown in the app are based on general neuroscience research and are for informational purposes only. Individual results vary. Progress depends on many personal factors beyond what this app can measure."
            else
                "Las metas y beneficios mostrados en la app están basados en investigación neurocientífica general y son solo informativos. Los resultados individuales varían. El progreso depende de muchos factores personales fuera del alcance de esta app."
        )
        Spacer(Modifier.height(AppSpacing.sm.dp))

        DisclaimerSection(
            title = if (state.language == "en") "Privacy & Data Handling" else "Privacidad y datos",
            body = if (state.language == "en")
                "All your data is stored locally on your device. Discipl does not collect, transmit, or store personal data on any server. If you delete the app, all your data is permanently erased. Anonymous usage analytics are collected to improve the app experience."
            else
                "Todos tus datos se almacenan localmente en tu dispositivo. Discipl no recopila, transmite ni almacena datos personales en ningún servidor. Si eliminas la app, todos tus datos se borran permanentemente. Se recopilan análisis de uso anónimos para mejorar la experiencia."
        )
        Spacer(Modifier.height(AppSpacing.sm.dp))

        DisclaimerSection(
            title = if (state.language == "en") "Subscription Terms" else "Términos de suscripción",
            body = if (state.language == "en")
                "Discipl offers optional paid subscriptions billed through Google Play. Subscriptions auto-renew unless canceled at least 24 hours before the end of the current period. You can manage or cancel your subscription in Google Play Store > Subscriptions."
            else
                "Discipl ofrece suscripciones opcionales cobradas a través de Google Play. Las suscripciones se renuevan automáticamente a menos que se cancelen al menos 24 horas antes del fin del periodo actual. Puedes administrar o cancelar tu suscripción en Google Play Store > Suscripciones."
        )
        Spacer(Modifier.height(AppSpacing.sm.dp))

        DisclaimerSection(
            title = if (state.language == "en") "Age Restriction & Terms of Use" else "Restricción de edad y términos de uso",
            body = if (state.language == "en")
                "You must be at least 17 years old to use Discipl. By continuing, you agree to our Terms of Service and Privacy Policy. You acknowledge that this app addresses sensitive topics related to adult content and self-improvement."
            else
                "Debes tener al menos 17 años para usar Discipl. Al continuar, aceptas nuestros Términos de Servicio y Política de Privacidad. Reconoces que esta app aborda temas sensibles relacionados con contenido adulto y superación personal."
        )
        Spacer(Modifier.height(AppSpacing.sm.dp))

        DisclaimerSection(
            title = if (state.language == "en") "Content Attribution" else "Atribución de contenido",
            body = if (state.language == "en")
                "Motivational quotes and benefit descriptions are based on publicly available research and educational material. They are not original clinical findings. Sources include published neuroscience and psychology literature on habit formation and behavioral recovery."
            else
                "Las frases motivacionales y descripciones de beneficios están basadas en investigación y material educativo de acceso público. No son hallazgos clínicos originales. Las fuentes incluyen literatura publicada de neurociencia y psicología sobre formación de hábitos y recuperación conductual."
        )

        Spacer(Modifier.height(AppSpacing.lg.dp))

        // Accept toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    viewModel.toggleDisclaimer()
                }
                .padding(vertical = AppSpacing.sm.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (state.disclaimerAccepted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (state.disclaimerAccepted) AppColors.accent else AppColors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (state.language == "en") "I have read and accept these terms"
                else "He leído y acepto estos términos",
                style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                color = AppColors.textPrimary
            )
        }

        Spacer(Modifier.height(AppSpacing.xl.dp))
    }
}

// MARK: - Reusable Components

@Composable
private fun QuizOptionCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        if (isSelected) AppColors.accent else Color.Transparent,
        label = "border"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(AppSpacing.md.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTypography.body.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) AppColors.textPrimary else AppColors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) AppColors.accent else AppColors.textSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun MultiSelectCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        if (isSelected) AppColors.accent else Color.Transparent,
        label = "border"
    )

    Column(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(AppSpacing.sm.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = AppColors.accent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
        }
        Text(
            text = title,
            style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) AppColors.textPrimary else AppColors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun DisclaimerSection(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface)
            .padding(AppSpacing.md.dp)
    ) {
        Text(
            text = title,
            style = AppTypography.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
            color = AppColors.textPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = AppTypography.caption,
            color = AppColors.textSecondary,
            lineHeight = 18.sp
        )
    }
}
