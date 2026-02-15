package com.discipl.app.service

import com.discipl.app.data.db.DailyCheckInDao
import com.discipl.app.data.db.JournalEntryDao
import com.discipl.app.data.db.RelapseDao
import com.discipl.app.data.db.StreakDao
import com.discipl.app.data.model.DailyCheckIn
import com.discipl.app.data.model.JournalEntry
import com.discipl.app.data.model.Relapse
import com.discipl.app.data.model.Streak
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakService @Inject constructor(
    private val streakDao: StreakDao,
    private val relapseDao: RelapseDao,
    private val dailyCheckInDao: DailyCheckInDao,
    private val journalEntryDao: JournalEntryDao
) {

    // --- Streak Calculations ---

    suspend fun getCurrentStreak(): Streak? = streakDao.getActiveStreak()

    suspend fun getCurrentStreakDays(): Int = getCurrentStreak()?.durationDays ?: 0

    suspend fun getAllStreaks(): List<Streak> = streakDao.getAll()

    suspend fun getLongestStreakDays(): Int =
        getAllStreaks().maxOfOrNull { it.durationDays } ?: 0

    suspend fun getTotalCleanDays(): Int =
        getAllStreaks().sumOf { it.durationDays }

    suspend fun getAverageStreakDays(): Double {
        val completed = getAllStreaks().filter { it.endDate != null }
        if (completed.isEmpty()) return 0.0
        return completed.sumOf { it.durationDays }.toDouble() / completed.size
    }

    suspend fun getRelapseCount(): Int = relapseDao.getCount()

    // --- Streak Management ---

    suspend fun createInitialStreak(startDate: Long) {
        streakDao.insert(Streak(startDate = startDate))
    }

    suspend fun updateCurrentStreakStartDate(newStartDate: Long) {
        getCurrentStreak()?.let { streak ->
            streakDao.update(streak.copy(startDate = newStartDate))
        }
    }

    suspend fun hasRelapsedToday(): Boolean {
        val (start, end) = todayRange()
        return relapseDao.countForDay(start, end) > 0
    }

    suspend fun recordRelapse(
        timeOfDay: String? = null,
        trigger: String? = null,
        moodBefore: Int? = null,
        notes: String? = null
    ): Boolean {
        val now = System.currentTimeMillis()

        // End the current streak
        getCurrentStreak()?.let { current ->
            streakDao.update(current.copy(endDate = now))
        }

        // Create relapse record
        relapseDao.insert(
            Relapse(
                date = now,
                timeOfDay = timeOfDay,
                trigger = trigger,
                moodBefore = moodBefore,
                notes = notes
            )
        )

        // Create new streak starting now
        streakDao.insert(Streak(startDate = now))
        return true
    }

    // --- Daily Check-In ---

    suspend fun hasCheckedInToday(): Boolean {
        val (start, end) = todayRange()
        return dailyCheckInDao.countForDay(start, end) > 0
    }

    suspend fun recordCheckIn(status: String = DailyCheckIn.STATUS_CLEAN): Boolean {
        if (hasCheckedInToday()) return false
        dailyCheckInDao.insert(DailyCheckIn(date = System.currentTimeMillis(), status = status))
        return true
    }

    suspend fun getCheckIns(month: YearMonth): List<DailyCheckIn> {
        val zone = ZoneId.systemDefault()
        val startOfMonth = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfMonth = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return dailyCheckInDao.getForMonth(startOfMonth, endOfMonth)
    }

    // --- Relapse History ---

    suspend fun getAllRelapses(): List<Relapse> = relapseDao.getAll()

    suspend fun getMostCommonTrigger(): String? {
        val triggers = getAllRelapses().mapNotNull { it.trigger }
        if (triggers.isEmpty()) return null
        return triggers.groupBy { it }.maxByOrNull { it.value.size }?.key
    }

    suspend fun getMostCommonTimeOfDay(): String? {
        val times = getAllRelapses().mapNotNull { it.timeOfDay }
        if (times.isEmpty()) return null
        return times.groupBy { it }.maxByOrNull { it.value.size }?.key
    }

    suspend fun getMostCommonDayOfWeek(): String? {
        val relapses = getAllRelapses()
        if (relapses.isEmpty()) return null
        val calendar = Calendar.getInstance()
        val weekdays = relapses.map { relapse ->
            calendar.timeInMillis = relapse.date
            calendar.get(Calendar.DAY_OF_WEEK)
        }
        val mostCommon = weekdays.groupBy { it }.maxByOrNull { it.value.size }?.key ?: return null
        val dayNames = arrayOf("", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        return dayNames[mostCommon]
    }

    // --- Journal Entries ---

    suspend fun recordJournalEntry(
        mood: Int,
        feelings: String? = null,
        energyLevel: Int? = null,
        notes: String? = null
    ): Boolean {
        journalEntryDao.insert(
            JournalEntry(
                date = System.currentTimeMillis(),
                mood = mood,
                feelings = feelings,
                energyLevel = energyLevel,
                notes = notes
            )
        )
        recordCheckIn(DailyCheckIn.STATUS_CLEAN)
        return true
    }

    suspend fun getAllJournalEntries(): List<JournalEntry> = journalEntryDao.getAll()

    suspend fun getMostCommonFeeling(): String? {
        val allFeelings = getAllJournalEntries()
            .mapNotNull { it.feelings }
            .flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (allFeelings.isEmpty()) return null
        return allFeelings.groupBy { it }.maxByOrNull { it.value.size }?.key
    }

    suspend fun getAverageMood(): Double? {
        val entries = getAllJournalEntries()
        if (entries.isEmpty()) return null
        return entries.sumOf { it.mood }.toDouble() / entries.size
    }

    suspend fun getAverageEnergy(): Double? {
        val energies = getAllJournalEntries().mapNotNull { it.energyLevel }
        if (energies.isEmpty()) return null
        return energies.sum().toDouble() / energies.size
    }

    // --- Helpers ---

    private fun todayRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val startOfToday = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfToday = LocalDate.now().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return startOfToday to endOfToday
    }
}
