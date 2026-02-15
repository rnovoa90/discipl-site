package com.discipl.app.ui.benefits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.discipl.app.data.db.UserProfileDao
import com.discipl.app.data.model.Milestone
import com.discipl.app.service.AnalyticsService
import com.discipl.app.service.BenefitsService
import com.discipl.app.service.StreakService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BenefitsUiState(
    val milestones: List<Milestone> = emptyList(),
    val currentStreakDays: Int = 0,
    val isPremium: Boolean = false,
    val language: String = "es"
)

@HiltViewModel
class BenefitsViewModel @Inject constructor(
    private val benefitsService: BenefitsService,
    private val streakService: StreakService,
    private val userProfileDao: UserProfileDao,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow(BenefitsUiState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val profile = userProfileDao.get()
            val days = streakService.getCurrentStreakDays()
            analyticsService.benefitsTimelineViewed()
            _state.value = BenefitsUiState(
                milestones = benefitsService.getAllMilestones(),
                currentStreakDays = days,
                isPremium = profile?.isPremium ?: false,
                language = profile?.language ?: "es"
            )
        }
    }

    fun onPaywallHit() {
        analyticsService.benefitsPaywallHit()
    }
}
