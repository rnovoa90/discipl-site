package com.discipl.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.discipl.app.data.db.UserProfileDao
import com.discipl.app.service.AnalyticsService
import com.discipl.app.service.StreakService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0,
    val language: String = "es",

    // Screen 1: Quit type
    val selectedQuitType: String? = null,

    // Screen 2: Duration
    val selectedDuration: String? = null,

    // Screen 3: Escalation
    val hasEscalated: Boolean? = null,

    // Screen 4: Symptoms (multi-select)
    val selectedSymptoms: Set<String> = emptySet(),

    // Screen 5: Goals (multi-select)
    val selectedGoals: Set<String> = emptySet(),

    // Screen 6: Score
    val animatedScore: Int = 0,
    val scoreAnimationDone: Boolean = false,

    // Screen 7: Plan
    val planProgress: Float = 0f,
    val planReady: Boolean = false,

    // Screen 8: Quit date
    val selectedQuitDateMillis: Long = System.currentTimeMillis(),
    val showQuitDateConfirmation: Boolean = false,

    // Screen 9: Disclaimer
    val disclaimerAccepted: Boolean = false,

    // Paywall
    val showPaywall: Boolean = false
) {
    val totalSteps: Int get() = 10

    val dependencyScore: Int
        get() {
            var score = 0

            // Duration: 0-40 points
            when (selectedDuration) {
                "< 1 año" -> score += 10
                "1-3 años" -> score += 20
                "3-5 años" -> score += 30
                "5+ años" -> score += 40
            }

            // Escalation: +15
            if (hasEscalated == true) score += 15

            // Symptoms: up to 30 (5 per symptom, max 6)
            score += selectedSymptoms.size * 5

            // Goals: up to 15 (3 per goal, max 5)
            score += minOf(selectedGoals.size * 3, 15)

            return minOf(score, 100)
        }

    val canContinue: Boolean
        get() = when (currentStep) {
            0 -> true // welcome
            1 -> selectedQuitType != null
            2 -> selectedDuration != null
            3 -> hasEscalated != null
            4 -> selectedSymptoms.isNotEmpty()
            5 -> selectedGoals.isNotEmpty()
            6 -> scoreAnimationDone
            7 -> planReady
            8 -> true
            9 -> disclaimerAccepted
            else -> false
        }

    val buttonText: String
        get() = when {
            currentStep == 0 -> if (language == "en") "Begin" else "Empezar"
            currentStep == totalSteps - 1 -> if (language == "en") "Start" else "Comenzar"
            currentStep == totalSteps - 2 -> if (language == "en") "I Accept" else "Acepto"
            else -> if (language == "en") "Continue" else "Continuar"
        }

    val planBulletPoints: List<String>
        get() {
            val points = mutableListOf<String>()

            points.add(if (language == "en") "Daily streak tracking" else "Seguimiento diario de racha")

            val goalTexts = mapOf(
                "energy" to Pair("Plan para recuperar tu energía", "Plan to restore your energy"),
                "clarity" to Pair("Ejercicios de claridad mental", "Mental clarity exercises"),
                "relationships" to Pair("Guía para mejorar tus relaciones", "Guide to improve your relationships"),
                "confidence" to Pair("Ruta para más confianza", "Path to more confidence"),
                "focus" to Pair("Técnicas de enfoque y concentración", "Focus and concentration techniques"),
                "self_respect" to Pair("Camino al respeto propio", "Path to self-respect")
            )

            selectedGoals.take(3).forEach { goal ->
                goalTexts[goal]?.let { (es, en) ->
                    points.add(if (language == "en") en else es)
                }
            }

            if (points.size < 4) {
                points.add(if (language == "en") "Benefits timeline for your journey" else "Línea de beneficios para tu camino")
            }

            return points
        }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val streakService: StreakService,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = userProfileDao.get()
            _state.value = _state.value.copy(language = profile?.language ?: "es")
            analyticsService.onboardingStarted()
            analyticsService.onboardingSlideViewed(0)
        }
    }

    fun selectQuitType(type: String) {
        _state.value = _state.value.copy(selectedQuitType = type)
    }

    fun selectDuration(duration: String) {
        _state.value = _state.value.copy(selectedDuration = duration)
    }

    fun setEscalated(escalated: Boolean) {
        _state.value = _state.value.copy(hasEscalated = escalated)
    }

    fun toggleSymptom(symptom: String) {
        val current = _state.value.selectedSymptoms
        val updated = if (current.contains(symptom)) current - symptom else current + symptom
        _state.value = _state.value.copy(selectedSymptoms = updated)
    }

    fun toggleGoal(goal: String) {
        val current = _state.value.selectedGoals
        val updated = if (current.contains(goal)) current - goal else current + goal
        _state.value = _state.value.copy(selectedGoals = updated)
    }

    fun updateAnimatedScore(score: Int) {
        _state.value = _state.value.copy(animatedScore = score)
    }

    fun setScoreAnimationDone() {
        _state.value = _state.value.copy(scoreAnimationDone = true)
        val s = _state.value
        analyticsService.quizScoreCalculated(s.dependencyScore, s.selectedQuitType, s.selectedDuration)
    }

    fun updatePlanProgress(progress: Float) {
        _state.value = _state.value.copy(planProgress = progress)
    }

    fun setPlanReady() {
        _state.value = _state.value.copy(planReady = true)
    }

    fun selectQuitDate(millis: Long) {
        val daysDiff = (System.currentTimeMillis() - millis) / (1000 * 60 * 60 * 24)
        _state.value = _state.value.copy(
            selectedQuitDateMillis = millis,
            showQuitDateConfirmation = daysDiff >= 7
        )
    }

    fun dismissQuitDateConfirmation() {
        _state.value = _state.value.copy(showQuitDateConfirmation = false)
    }

    fun resetQuitDate() {
        _state.value = _state.value.copy(
            selectedQuitDateMillis = System.currentTimeMillis(),
            showQuitDateConfirmation = false
        )
    }

    fun toggleDisclaimer() {
        _state.value = _state.value.copy(disclaimerAccepted = !_state.value.disclaimerAccepted)
    }

    fun goNext() {
        val current = _state.value.currentStep
        if (current == 8) {
            // Save quit date before advancing to disclaimer
            viewModelScope.launch {
                val profile = userProfileDao.get() ?: return@launch
                val updated = profile.copy(
                    quitDate = _state.value.selectedQuitDateMillis,
                    quitType = _state.value.selectedQuitType ?: "both"
                )
                userProfileDao.update(updated)
            }
        }

        if (current < _state.value.totalSteps - 1) {
            val next = current + 1
            _state.value = _state.value.copy(currentStep = next)
            analyticsService.onboardingSlideViewed(next)
        } else {
            // Last step: show paywall
            analyticsService.paywallPresented(placement = "onboarding_end")
            _state.value = _state.value.copy(showPaywall = true)
        }
    }

    fun goBack() {
        val current = _state.value.currentStep
        if (current > 0) {
            _state.value = _state.value.copy(currentStep = current - 1)
        }
    }

    fun dismissPaywall() {
        _state.value = _state.value.copy(showPaywall = false)
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            val profile = userProfileDao.get() ?: return@launch
            val updated = profile.copy(
                hasCompletedOnboarding = true,
                quitDate = _state.value.selectedQuitDateMillis,
                quitType = _state.value.selectedQuitType ?: "both"
            )
            userProfileDao.update(updated)
            analyticsService.onboardingCompleted(isPremium = updated.isPremium)

            // Create initial streak
            streakService.createInitialStreak(_state.value.selectedQuitDateMillis)

            onComplete()
        }
    }
}
