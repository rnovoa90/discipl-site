package com.discipl.app.service

import android.content.Context
import android.util.Log
import com.discipl.app.BuildConfig
import com.posthog.PostHog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PostHog analytics wrapper. Same event names as iOS.
 * Privacy: relapse_logged sends ONLY streak_days_lost. No PII is sent.
 */
@Singleton
class AnalyticsService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "Analytics"
    }

    private fun track(event: String, properties: Map<String, Any>? = null) {
        try {
            if (properties != null) {
                PostHog.capture(event, properties = properties)
            } else {
                PostHog.capture(event)
            }
        } catch (_: Exception) {
            // PostHog not configured yet — ignore
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "[$event] ${properties ?: emptyMap<String, Any>()}")
        }
    }

    // --- Onboarding ---
    fun onboardingStarted() = track("onboarding_started")
    fun onboardingSlideViewed(slideNumber: Int) = track("onboarding_slide_viewed", mapOf("slide_number" to slideNumber))
    fun paywallPresented(placement: String) = track("paywall_presented", mapOf("placement" to placement))
    fun paywallDismissed(placement: String) = track("paywall_dismissed", mapOf("placement" to placement))
    fun subscriptionStarted(plan: String, trial: Boolean) = track("subscription_started", mapOf("plan" to plan, "trial" to trial))
    fun onboardingCompleted(isPremium: Boolean) = track("onboarding_completed", mapOf("is_premium" to isPremium))
    fun quizScoreCalculated(score: Int) {
        track("quiz_score_calculated", mapOf("score" to score))
    }

    // --- Core Engagement ---
    fun appOpened(streakDays: Int) = track("app_opened", mapOf("streak_days" to streakDays))
    fun dailyCheckInCompleted(streakDays: Int) = track("daily_checkin_completed", mapOf("streak_days" to streakDays))
    fun relapseLogged(streakDaysLost: Int) = track("relapse_logged", mapOf("streak_days_lost" to streakDaysLost))
    fun streakMilestoneReached(milestone: Int) = track("streak_milestone_reached", mapOf("milestone" to milestone))
    fun benefitsTimelineViewed() = track("benefits_timeline_viewed")
    fun benefitsPaywallHit() = track("benefits_paywall_hit")
    fun statsViewed() = track("stats_viewed")
    fun shareCardCreated() = track("share_card_created")
    fun shareCardShared() = track("share_card_shared")
    fun journalEntryCompleted(streakDays: Int) = track("journal_entry_completed", mapOf("streak_days" to streakDays))

    // --- Widget ---
    fun widgetTapped() = track("widget_tapped")

    // --- Notifications ---
    fun notificationPermissionGranted() = track("notification_permission_granted")
    fun notificationPermissionDenied() = track("notification_permission_denied")
    fun notificationOpened(type: String) = track("notification_opened", mapOf("type" to type))

    // --- Session ---
    fun appBackgrounded(sessionDurationSeconds: Int) = track("app_backgrounded", mapOf("session_duration_seconds" to sessionDurationSeconds))
    fun settingsOpened() = track("settings_opened")
    fun languageChanged(from: String, to: String) = track("language_changed", mapOf("from" to from, "to" to to))
}
