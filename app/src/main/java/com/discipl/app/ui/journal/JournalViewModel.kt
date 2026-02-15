package com.discipl.app.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.discipl.app.data.db.UserProfileDao
import com.discipl.app.data.model.JournalEntry
import com.discipl.app.data.model.Relapse
import com.discipl.app.service.StreakService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JournalUiState(
    val relapses: List<Relapse> = emptyList(),
    val journalEntries: List<JournalEntry> = emptyList(),
    val mostCommonTrigger: String? = null,
    val mostCommonTimeOfDay: String? = null,
    val mostCommonDayOfWeek: String? = null,
    val mostCommonFeeling: String? = null,
    val averageMood: Double? = null,
    val averageEnergy: Double? = null,
    val isPremium: Boolean = false,
    val language: String = "es"
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val streakService: StreakService,
    private val userProfileDao: UserProfileDao
) : ViewModel() {

    private val _state = MutableStateFlow(JournalUiState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val profile = userProfileDao.get()
            _state.value = JournalUiState(
                relapses = streakService.getAllRelapses(),
                journalEntries = streakService.getAllJournalEntries(),
                mostCommonTrigger = streakService.getMostCommonTrigger(),
                mostCommonTimeOfDay = streakService.getMostCommonTimeOfDay(),
                mostCommonDayOfWeek = streakService.getMostCommonDayOfWeek(),
                mostCommonFeeling = streakService.getMostCommonFeeling(),
                averageMood = streakService.getAverageMood(),
                averageEnergy = streakService.getAverageEnergy(),
                isPremium = profile?.isPremium ?: false,
                language = profile?.language ?: "es"
            )
        }
    }
}
