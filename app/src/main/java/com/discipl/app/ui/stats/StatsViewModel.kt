package com.discipl.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.discipl.app.data.db.UserProfileDao
import com.discipl.app.data.model.DailyCheckIn
import com.discipl.app.data.model.Relapse
import com.discipl.app.data.model.Streak
import com.discipl.app.service.AnalyticsService
import com.discipl.app.service.StreakService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class StatsUiState(
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val totalCleanDays: Int = 0,
    val relapseCount: Int = 0,
    val averageStreakDays: Double = 0.0,
    val allStreaks: List<Streak> = emptyList(),
    val checkIns: List<DailyCheckIn> = emptyList(),
    val relapses: List<Relapse> = emptyList(),
    val currentMonth: YearMonth = YearMonth.now(),
    val profileCreatedAt: Long = 0L,
    val isPremium: Boolean = false,
    val language: String = "es"
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val streakService: StreakService,
    private val userProfileDao: UserProfileDao,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val profile = userProfileDao.get()
            val month = _state.value.currentMonth
            analyticsService.statsViewed()
            _state.value = StatsUiState(
                currentStreakDays = streakService.getCurrentStreakDays(),
                longestStreakDays = streakService.getLongestStreakDays(),
                totalCleanDays = streakService.getTotalCleanDays(),
                relapseCount = streakService.getRelapseCount(),
                averageStreakDays = streakService.getAverageStreakDays(),
                allStreaks = streakService.getAllStreaks(),
                checkIns = streakService.getCheckIns(month),
                relapses = streakService.getAllRelapses(),
                currentMonth = month,
                profileCreatedAt = profile?.createdAt ?: 0L,
                isPremium = profile?.isPremium ?: false,
                language = profile?.language ?: "es"
            )
        }
    }

    fun changeMonth(month: YearMonth) {
        viewModelScope.launch {
            val checkIns = streakService.getCheckIns(month)
            _state.value = _state.value.copy(currentMonth = month, checkIns = checkIns)
        }
    }

    fun onShareCardCreated() = analyticsService.shareCardCreated()
    fun onShareCardShared() = analyticsService.shareCardShared()
}
