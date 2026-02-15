package com.discipl.app.service

import android.content.Context
import com.discipl.app.data.model.Milestone
import com.discipl.app.data.model.MilestonesData
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BenefitsService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val milestones: List<Milestone> by lazy { loadMilestones() }

    private fun loadMilestones(): List<Milestone> {
        return try {
            val json = context.assets.open("benefits.json").bufferedReader().use { it.readText() }
            Gson().fromJson(json, MilestonesData::class.java).milestones.sortedBy { it.day }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAllMilestones(): List<Milestone> = milestones

    fun getNextMilestone(currentStreakDays: Int): Milestone? =
        milestones.firstOrNull { it.day > currentStreakDays }

    fun daysUntilNextMilestone(currentStreakDays: Int): Int? =
        getNextMilestone(currentStreakDays)?.let { it.day - currentStreakDays }

    fun getPassedMilestones(currentStreakDays: Int): List<Milestone> =
        milestones.filter { it.day <= currentStreakDays }

    fun getUpcomingMilestones(currentStreakDays: Int): List<Milestone> =
        milestones.filter { it.day > currentStreakDays }

    fun isMilestoneAccessible(milestone: Milestone, isPremium: Boolean): Boolean =
        milestone.isFree || isPremium
}
