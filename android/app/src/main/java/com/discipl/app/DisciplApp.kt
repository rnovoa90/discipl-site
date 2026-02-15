package com.discipl.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.discipl.app.service.AnalyticsService
import com.discipl.app.service.PaywallService
import com.posthog.PostHog
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DisciplApp : Application() {

    @Inject lateinit var paywallService: PaywallService
    @Inject lateinit var analyticsService: AnalyticsService

    private var sessionStartTime: Long = 0L

    override fun onCreate() {
        super.onCreate()

        // PostHog analytics
        initPostHog()

        // RevenueCat subscriptions
        paywallService.configure(BuildConfig.REVENUECAT_API_KEY)

        // Notification channel (required Android 8+)
        createNotificationChannel()

        // Session tracking via ProcessLifecycleOwner
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                sessionStartTime = System.currentTimeMillis()
            }

            override fun onStop(owner: LifecycleOwner) {
                if (sessionStartTime > 0) {
                    val durationSeconds = ((System.currentTimeMillis() - sessionStartTime) / 1000).toInt()
                    analyticsService.appBackgrounded(durationSeconds)
                }
            }
        })
    }

    private fun initPostHog() {
        try {
            val config = PostHogAndroidConfig(
                apiKey = BuildConfig.POSTHOG_API_KEY,
                host = BuildConfig.POSTHOG_HOST
            )
            PostHogAndroid.setup(this, config)
        } catch (_: Exception) {
            // PostHog keys not set — ignore in debug
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "discipl_notifications",
            "Discipl",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Motivational reminders and streak milestones"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
