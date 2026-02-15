package com.discipl.app.ui.stats

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareCardGenerator {

    fun generateAndShare(
        context: Context,
        streakDays: Int,
        language: String
    ) {
        val bitmap = generateBitmap(context, streakDays, language)
        val uri = saveBitmapToCache(context, bitmap) ?: return

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(
                shareIntent,
                if (language == "en") "Share your progress" else "Comparte tu progreso"
            )
        )
    }

    private fun generateBitmap(context: Context, streakDays: Int, language: String): Bitmap {
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                0xFF0D1117.toInt(), 0xFF161B22.toInt(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Streak number
        val numberPaint = Paint().apply {
            shader = LinearGradient(
                width / 2f - 200f, height / 2f - 100f,
                width / 2f + 200f, height / 2f + 100f,
                0xFF06D6A0.toInt(), 0xFF00B4D8.toInt(),
                Shader.TileMode.CLAMP
            )
            textSize = 200f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("$streakDays", width / 2f, height / 2f + 40f, numberPaint)

        // Label
        val labelPaint = Paint().apply {
            color = 0xFF8B949E.toInt()
            textSize = 48f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val label = if (language == "en") "days of discipline" else "días de disciplina"
        canvas.drawText(label, width / 2f, height / 2f + 110f, labelPaint)

        // Day label
        val dayLabel = if (language == "en") "Day" else "Día"
        val dayPaint = Paint().apply {
            color = 0xFF8B949E.toInt()
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(dayLabel, width / 2f, height / 2f - 100f, dayPaint)

        // Progress bar (90 day journey)
        val barY = height / 2f + 180f
        val barWidth = width * 0.7f
        val barLeft = (width - barWidth) / 2
        val barHeight = 8f
        val progress = (streakDays / 90f).coerceAtMost(1f)

        // Background bar
        val barBgPaint = Paint().apply {
            color = 0xFF161B22.toInt()
            isAntiAlias = true
        }
        canvas.drawRoundRect(barLeft, barY, barLeft + barWidth, barY + barHeight, 4f, 4f, barBgPaint)

        // Progress bar
        val barPaint = Paint().apply {
            shader = LinearGradient(
                barLeft, barY, barLeft + barWidth * progress, barY,
                0xFF00B4D8.toInt(), 0xFF06D6A0.toInt(),
                Shader.TileMode.CLAMP
            )
            isAntiAlias = true
        }
        canvas.drawRoundRect(barLeft, barY, barLeft + barWidth * progress, barY + barHeight, 4f, 4f, barPaint)

        // "discipl" branding at bottom
        val brandPaint = Paint().apply {
            color = 0xFF8B949E.toInt()
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.15f
        }
        canvas.drawText("discipl", width / 2f, height - 80f, brandPaint)

        return bitmap
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val dir = File(context.cacheDir, "share_images").also { it.mkdirs() }
            val file = File(dir, "discipl_streak_card.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }
}
