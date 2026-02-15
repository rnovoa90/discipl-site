package com.discipl.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.discipl.app.data.db.UserProfileDao
import com.discipl.app.data.model.Milestone
import com.discipl.app.data.model.Quote
import com.discipl.app.data.model.Streak
import com.discipl.app.data.model.UserProfile
import com.discipl.app.service.AnalyticsService
import com.discipl.app.service.BenefitsService
import com.discipl.app.service.InsightService
import com.discipl.app.service.QuoteService
import com.discipl.app.service.StreakService
import com.discipl.app.service.WidgetDataService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val profile: UserProfile? = null,
    val currentStreak: Streak? = null,
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val hasCheckedInToday: Boolean = false,
    val quote: Quote? = null,
    val quoteIndex: Int = 0,
    val nextMilestone: Milestone? = null,
    val currentMilestone: Milestone? = null,
    val taskText: String? = null,
    val insightText: String? = null,
    val language: String = "es",
    val isPremium: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val streakService: StreakService,
    private val quoteService: QuoteService,
    private val benefitsService: BenefitsService,
    private val insightService: InsightService,
    private val analyticsService: AnalyticsService,
    private val widgetDataService: WidgetDataService
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val profile = userProfileDao.get()
            val streak = streakService.getCurrentStreak()
            val days = streak?.durationDays ?: 0
            val language = profile?.language ?: "es"
            val isPremium = profile?.isPremium ?: false

            val dayOrdinal = LocalDate.now().toEpochDay().toInt()
            val quote = quoteService.getQuoteByIndex(dayOrdinal)
            val nextMilestone = benefitsService.getNextMilestone(days)
            val currentMilestone = benefitsService.getAllMilestones().firstOrNull { it.day == days }

            val taskText = if (days <= 30 && isPremium && (profile?.dailyTaskEnabled != false)) {
                insightService.getTaskForDay(days)?.text(language)
            } else null

            val insightText = if (days > 30 && isPremium && (profile?.dailyInsightEnabled != false)) {
                insightService.getInsightOfTheDay(days)?.text(language)
            } else null

            _state.value = HomeUiState(
                profile = profile,
                currentStreak = streak,
                currentStreakDays = days,
                longestStreakDays = streakService.getLongestStreakDays(),
                hasCheckedInToday = streakService.hasCheckedInToday(),
                quote = quote,
                quoteIndex = dayOrdinal,
                nextMilestone = nextMilestone,
                currentMilestone = currentMilestone,
                taskText = taskText,
                insightText = insightText,
                language = language,
                isPremium = isPremium
            )

            syncWidgetData(days, streak, nextMilestone, language)
        }
    }

    fun recordCheckIn() {
        viewModelScope.launch {
            val days = streakService.getCurrentStreakDays()
            if (streakService.recordCheckIn()) {
                analyticsService.dailyCheckInCompleted(days)
                loadData()
            }
        }
    }

    fun recordRelapse(
        timeOfDay: String? = null,
        trigger: String? = null,
        moodBefore: Int? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val daysLost = streakService.getCurrentStreakDays()
            streakService.recordRelapse(timeOfDay, trigger, moodBefore, notes)
            analyticsService.relapseLogged(daysLost)
            loadData()
        }
    }

    fun recordJournalEntry(
        mood: Int,
        feelings: String? = null,
        energyLevel: Int? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val days = streakService.getCurrentStreakDays()
            streakService.recordJournalEntry(mood, feelings, energyLevel, notes)
            analyticsService.journalEntryCompleted(days)
            loadData()
        }
    }

    private fun syncWidgetData(days: Int, streak: Streak?, nextMilestone: Milestone?, language: String) {
        widgetDataService.update(
            currentStreakDays = days,
            streakStartDate = streak?.startDate ?: System.currentTimeMillis(),
            nextMilestoneDay = nextMilestone?.day,
            nextMilestoneTitle = nextMilestone?.title(language),
            language = language
        )
    }
}
