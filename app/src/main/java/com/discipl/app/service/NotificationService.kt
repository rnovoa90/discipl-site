package com.discipl.app.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.discipl.app.MainActivity
import com.discipl.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "discipl_notifications"
        const val CHANNEL_NAME = "Discipl"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Discipl daily notifications"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    /**
     * Reschedules all notifications based on current user preferences.
     */
    fun rescheduleAll(
        morningEnabled: Boolean,
        morningTime: LocalTime,
        milestoneEnabled: Boolean,
        eveningEnabled: Boolean,
        eveningTime: LocalTime,
        reengagementEnabled: Boolean,
        currentStreakDays: Int,
        language: String
    ) {
        val workManager = WorkManager.getInstance(context)

        // Cancel all existing
        workManager.cancelAllWorkByTag("discipl_notification")

        if (morningEnabled) {
            scheduleMorningMotivation(morningTime, language)
        }
        if (milestoneEnabled) {
            scheduleMilestoneNotifications(currentStreakDays, language)
        }
        if (eveningEnabled) {
            scheduleEveningCheckIn(eveningTime, language)
        }
        if (reengagementEnabled) {
            scheduleReengagement(language)
        }
    }

    private fun scheduleMorningMotivation(time: LocalTime, language: String) {
        val messages = morningMessages(language)
        val dayOrdinal = LocalDate.now().toEpochDay().toInt()

        for (dayOffset in 0..6) {
            val targetDate = LocalDate.now().plusDays(dayOffset.toLong())
            val targetDateTime = LocalDateTime.of(targetDate, time)
            val now = LocalDateTime.now()
            if (targetDateTime.isBefore(now)) continue

            val delayMs = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                    System.currentTimeMillis()
            if (delayMs <= 0) continue

            val message = messages[(dayOrdinal + dayOffset) % messages.size]

            val data = Data.Builder()
                .putString("title", "discipl")
                .putString("body", message)
                .putInt("notificationId", 1000 + dayOffset)
                .build()

            val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("discipl_notification")
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }

    private fun scheduleMilestoneNotifications(currentStreakDays: Int, language: String) {
        val milestoneDays = listOf(1, 3, 7, 14, 21, 30, 60, 90, 180, 365, 730, 1095)

        for (day in milestoneDays) {
            if (day <= currentStreakDays) continue

            val daysUntil = (day - currentStreakDays).toLong()
            val targetDate = LocalDate.now().plusDays(daysUntil)
            val targetDateTime = LocalDateTime.of(targetDate, LocalTime.of(10, 0))
            val delayMs = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                    System.currentTimeMillis()
            if (delayMs <= 0) continue

            val message = milestoneMessage(day, language)

            val data = Data.Builder()
                .putString("title", "discipl")
                .putString("body", message)
                .putInt("notificationId", 2000 + day)
                .build()

            val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("discipl_notification")
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }

    private fun scheduleEveningCheckIn(time: LocalTime, language: String) {
        val body = if (language == "en") "How are you feeling today? Log your journal."
        else "¿Cómo te sientes hoy? Registra tu diario."

        // Schedule for next 7 days
        for (dayOffset in 0..6) {
            val targetDate = LocalDate.now().plusDays(dayOffset.toLong())
            val targetDateTime = LocalDateTime.of(targetDate, time)
            val delayMs = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                    System.currentTimeMillis()
            if (delayMs <= 0) continue

            val data = Data.Builder()
                .putString("title", "discipl")
                .putString("body", body)
                .putInt("notificationId", 3000 + dayOffset)
                .build()

            val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("discipl_notification")
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }

    private fun scheduleReengagement(language: String) {
        val messages = if (language == "en") listOf(
            "Your progress is waiting. Open the app.",
            "Just a quick check-in. It takes a second.",
            "Keep the momentum going. Open the app."
        ) else listOf(
            "Tu progreso te espera. Abre la app.",
            "Solo un check-in rápido. Toma un segundo.",
            "Mantén el impulso. Abre la app."
        )

        val daysOut = listOf(3, 5, 7)
        for ((index, days) in daysOut.withIndex()) {
            val data = Data.Builder()
                .putString("title", "discipl")
                .putString("body", messages[index % messages.size])
                .putInt("notificationId", 4000 + days)
                .build()

            val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(days.toLong(), TimeUnit.DAYS)
                .setInputData(data)
                .addTag("discipl_notification")
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }

    fun resetReengagementTimer(enabled: Boolean, language: String) {
        val workManager = WorkManager.getInstance(context)
        // Cancel existing re-engagement work
        workManager.cancelUniqueWork("reengagement_3d")
        workManager.cancelUniqueWork("reengagement_5d")
        workManager.cancelUniqueWork("reengagement_7d")

        if (enabled) {
            scheduleReengagement(language)
        }
    }

    // --- Message Content ---

    private fun morningMessages(language: String): List<String> {
        return if (language == "en") listOf(
            "New day, new opportunity. Make it count.",
            "Small steps every day lead to big results.",
            "You're building something great. Keep going.",
            "Progress is progress, no matter how small.",
            "Today is yours. Own it.",
            "The best version of you is being built right now.",
            "Consistency is your superpower.",
            "You woke up with purpose. Let's go.",
            "Every day is a chance to grow.",
            "You're stronger than you think.",
            "Trust the process. It's working.",
            "Think about how far you've come.",
            "Another day, another step forward.",
            "Your future self will thank you for today.",
            "Keep the momentum going.",
            "You're doing better than you realize.",
            "Focus on what matters today.",
            "One day at a time. You've got this.",
            "Good things are happening. Stay the course.",
            "Champions show up every day. So do you.",
            "The work you put in today pays off tomorrow.",
            "Write a good chapter today.",
            "Growth happens in the quiet moments.",
            "You're on the right path. Keep walking.",
            "Your consistency speaks louder than words.",
            "Day by day, you're becoming who you want to be.",
            "Show up for yourself today.",
            "The journey is the reward.",
            "You chose this. And you're crushing it.",
            "One more day of showing up. Let's go."
        ) else listOf(
            "Nuevo día, nueva oportunidad. Haz que cuente.",
            "Pequeños pasos cada día llevan a grandes resultados.",
            "Estás construyendo algo grande. Sigue así.",
            "Progreso es progreso, sin importar lo pequeño.",
            "Hoy es tuyo. Aprovéchalo.",
            "La mejor versión de ti se está construyendo ahora.",
            "Tu constancia es tu superpoder.",
            "Te despertaste con propósito. Vamos.",
            "Cada día es una oportunidad de crecer.",
            "Eres más fuerte de lo que piensas.",
            "Confía en el proceso. Está funcionando.",
            "Piensa en lo lejos que llegaste.",
            "Otro día, otro paso adelante.",
            "Tu yo del futuro te va a agradecer por hoy.",
            "Mantén el impulso.",
            "Lo estás haciendo mejor de lo que crees.",
            "Enfócate en lo que importa hoy.",
            "Un día a la vez. Tú puedes.",
            "Cosas buenas están pasando. Mantén el rumbo.",
            "Los campeones aparecen cada día. Tú también.",
            "El trabajo de hoy rinde frutos mañana.",
            "Escribe un buen capítulo hoy.",
            "El crecimiento pasa en los momentos tranquilos.",
            "Vas por buen camino. Sigue caminando.",
            "Tu constancia habla más fuerte que las palabras.",
            "Día a día, te estás convirtiendo en quien quieres ser.",
            "Muestra por ti hoy.",
            "El camino es la recompensa.",
            "Tú elegiste esto. Y la estás rompiendo.",
            "Un día más de aparecer. Vamos."
        )
    }

    private fun milestoneMessage(day: Int, language: String): String {
        return if (language == "en") when (day) {
            1 -> "Day 1 complete. You're on your way."
            3 -> "3 days in. Great start."
            7 -> "One week! You're building momentum."
            14 -> "Two weeks. You're proving yourself."
            21 -> "21 days. The habit is forming."
            30 -> "One month! Incredible progress."
            60 -> "60 days. You're in a new league."
            90 -> "90 days. A true transformation."
            180 -> "Half a year. Remarkable."
            365 -> "One full year. You did it."
            else -> "$day days. Keep going."
        } else when (day) {
            1 -> "Día 1 completo. Vas por buen camino."
            3 -> "3 días. Gran comienzo."
            7 -> "¡Una semana! Estás ganando impulso."
            14 -> "Dos semanas. Te estás demostrando."
            21 -> "21 días. El hábito se está formando."
            30 -> "¡Un mes! Progreso increíble."
            60 -> "60 días. Estás en otro nivel."
            90 -> "90 días. Una verdadera transformación."
            180 -> "Medio año. Impresionante."
            365 -> "Un año completo. Lo lograste."
            else -> "$day días. Sigue así."
        }
    }
}

/**
 * Worker that posts a notification. Used by WorkManager for scheduled notifications.
 */
class NotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "discipl"
        val body = inputData.getString("body") ?: return Result.failure()
        val notificationId = inputData.getInt("notificationId", 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.failure()
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationService.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
        return Result.success()
    }
}
