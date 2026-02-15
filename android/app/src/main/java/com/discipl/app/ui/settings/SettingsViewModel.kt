package com.discipl.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.discipl.app.data.db.UserProfileDao
import com.discipl.app.data.model.UserProfile
import com.discipl.app.service.AnalyticsService
import com.discipl.app.service.NotificationService
import com.discipl.app.service.StreakService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

data class SettingsUiState(
    val profile: UserProfile? = null,
    val language: String = "es",
    val isPremium: Boolean = false,
    val morningMotivationEnabled: Boolean = true,
    val milestoneNotificationsEnabled: Boolean = true,
    val eveningCheckInEnabled: Boolean = false,
    val reengagementEnabled: Boolean = true,
    val dailyTaskEnabled: Boolean = true,
    val dailyInsightEnabled: Boolean = true,
    val hasNotificationPermission: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val streakService: StreakService,
    private val notificationService: NotificationService,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val profile = userProfileDao.get()
            analyticsService.settingsOpened()
            _state.value = SettingsUiState(
                profile = profile,
                language = profile?.language ?: "es",
                isPremium = profile?.isPremium ?: false,
                morningMotivationEnabled = profile?.morningMotivationEnabled ?: true,
                milestoneNotificationsEnabled = profile?.milestoneNotificationsEnabled ?: true,
                eveningCheckInEnabled = profile?.eveningCheckInEnabled ?: false,
                reengagementEnabled = profile?.reengagementEnabled ?: true,
                dailyTaskEnabled = profile?.dailyTaskEnabled ?: true,
                dailyInsightEnabled = profile?.dailyInsightEnabled ?: true,
                hasNotificationPermission = notificationService.hasNotificationPermission()
            )
        }
    }

    fun updateQuitDate(dateMillis: Long) {
        viewModelScope.launch {
            val profile = _state.value.profile ?: return@launch
            val updated = profile.copy(quitDate = dateMillis)
            userProfileDao.update(updated)
            // Update the active streak's start date to match the new quit date
            streakService.updateCurrentStreakStartDate(dateMillis)
            loadData()
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val profile = _state.value.profile ?: return@launch
            val oldLang = profile.language
            val newLang = if (oldLang == "es") "en" else "es"
            val updated = profile.copy(language = newLang)
            userProfileDao.update(updated)
            analyticsService.languageChanged(oldLang, newLang)
            loadData()
            rescheduleNotifications(updated)
        }
    }

    fun toggleMorningMotivation(enabled: Boolean) {
        updateProfileField { it.copy(morningMotivationEnabled = enabled) }
    }

    fun toggleMilestoneNotifications(enabled: Boolean) {
        updateProfileField { it.copy(milestoneNotificationsEnabled = enabled) }
    }

    fun toggleEveningCheckIn(enabled: Boolean) {
        updateProfileField { it.copy(eveningCheckInEnabled = enabled) }
    }

    fun toggleReengagement(enabled: Boolean) {
        updateProfileField { it.copy(reengagementEnabled = enabled) }
    }

    fun toggleDailyTask(enabled: Boolean) {
        updateProfileField { it.copy(dailyTaskEnabled = enabled) }
    }

    fun toggleDailyInsight(enabled: Boolean) {
        updateProfileField { it.copy(dailyInsightEnabled = enabled) }
    }

    fun toggleDebugPremium() {
        updateProfileField { it.copy(isPremium = !it.isPremium) }
    }

    private fun updateProfileField(transform: (UserProfile) -> UserProfile) {
        viewModelScope.launch {
            val profile = _state.value.profile ?: return@launch
            val updated = transform(profile)
            userProfileDao.update(updated)
            loadData()
            rescheduleNotifications(updated)
        }
    }

    private fun rescheduleNotifications(profile: UserProfile) {
        viewModelScope.launch {
            val streakDays = streakService.getCurrentStreakDays()
            notificationService.rescheduleAll(
                morningEnabled = profile.morningMotivationEnabled,
                morningTime = LocalTime.of(
                    (profile.morningMotivationTime / 3600000).toInt(),
                    ((profile.morningMotivationTime % 3600000) / 60000).toInt()
                ),
                milestoneEnabled = profile.milestoneNotificationsEnabled && profile.isPremium,
                eveningEnabled = profile.eveningCheckInEnabled && profile.isPremium,
                eveningTime = LocalTime.of(
                    (profile.eveningCheckInTime / 3600000).toInt(),
                    ((profile.eveningCheckInTime % 3600000) / 60000).toInt()
                ),
                reengagementEnabled = profile.reengagementEnabled && profile.isPremium,
                currentStreakDays = streakDays,
                language = profile.language
            )
        }
    }
}
