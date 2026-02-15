package com.discipl.app.service

import android.content.Context
import com.discipl.app.data.model.DailyTask
import com.discipl.app.data.model.Insight
import com.discipl.app.data.model.InsightsData
import com.discipl.app.data.model.TasksData
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val insights: List<Insight> by lazy { loadInsights() }
    private val tasks: List<DailyTask> by lazy { loadTasks() }

    private fun loadInsights(): List<Insight> {
        return try {
            val json = context.assets.open("insights.json").bufferedReader().use { it.readText() }
            Gson().fromJson(json, InsightsData::class.java).insights
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadTasks(): List<DailyTask> {
        return try {
            val json = context.assets.open("tasks.json").bufferedReader().use { it.readText() }
            Gson().fromJson(json, TasksData::class.java).tasks.sortedBy { it.day }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Returns the daily task for a given streak day (days 0-29).
     * Picks from 5 variants per day using calendar-day rotation,
     * so relapses show different tasks for the same streak day.
     */
    fun getTaskForDay(streakDays: Int): DailyTask? {
        val clamped = streakDays.coerceIn(0, 29)
        val dayTasks = tasks.filter { it.day == clamped }
        if (dayTasks.isEmpty()) return null

        val dayOrdinal = LocalDate.now().toEpochDay()
        val index = (dayOrdinal % dayTasks.size).toInt().let { if (it < 0) it + dayTasks.size else it }
        return dayTasks[index]
    }

    /**
     * Returns a streak-day-aware insight of the day using deterministic daily rotation.
     */
    fun getInsightOfTheDay(streakDays: Int): Insight? {
        val dayOrdinal = LocalDate.now().toEpochDay()

        // For long streaks (366+), rotate through ALL insights
        if (streakDays > 365 && insights.isNotEmpty()) {
            val index = ((dayOrdinal + streakDays) % insights.size).toInt()
                .let { if (it < 0) it + insights.size else it }
            return insights[index]
        }

        val matching = insights.filter { streakDays >= it.minDay && streakDays <= it.maxDay }
        if (matching.isEmpty()) return null

        val index = (dayOrdinal % matching.size).toInt().let { if (it < 0) it + matching.size else it }
        return matching[index]
    }

    fun getInsights(streakDays: Int): List<Insight> =
        insights.filter { streakDays >= it.minDay && streakDays <= it.maxDay }

    val count: Int get() = insights.size
}
