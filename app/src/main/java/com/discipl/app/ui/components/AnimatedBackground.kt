package com.discipl.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.discipl.app.ui.theme.AppColors
import kotlin.math.sin
import kotlin.random.Random

private data class Orb(
    val baseX: Float,
    val baseY: Float,
    val size: Float,
    val opacity: Float,
    val color: Color,
    val offsetX: Float,
    val offsetY: Float,
    val speed: Float // multiplier for animation phase
)

@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val orbs = remember {
        val colors = listOf(
            AppColors.accent.copy(alpha = 0.6f),
            AppColors.success.copy(alpha = 0.5f),
            Color(0xFF800080).copy(alpha = 0.5f), // purple
            Color(0xFF0000FF).copy(alpha = 0.4f), // blue
            AppColors.accent.copy(alpha = 0.35f)
        )
        val random = Random(42) // deterministic for consistency

        val regular = (0 until 12).map {
            Orb(
                baseX = random.nextFloat(),
                baseY = random.nextFloat(),
                size = random.nextFloat() * 140f + 80f,
                opacity = random.nextFloat() * 0.3f + 0.5f,
                color = colors[random.nextInt(colors.size)],
                offsetX = random.nextFloat() * 50f + 30f,
                offsetY = random.nextFloat() * 50f + 30f,
                speed = random.nextFloat() * 0.6f + 0.7f
            )
        }

        val heroes = listOf(
            Orb(
                baseX = random.nextFloat() * 0.6f + 0.2f,
                baseY = random.nextFloat() * 0.4f + 0.2f,
                size = random.nextFloat() * 80f + 260f,
                opacity = random.nextFloat() * 0.25f + 0.6f,
                color = AppColors.accent.copy(alpha = 0.7f),
                offsetX = random.nextFloat() * 50f + 40f,
                offsetY = random.nextFloat() * 50f + 40f,
                speed = random.nextFloat() * 0.6f + 0.8f
            ),
            Orb(
                baseX = random.nextFloat() * 0.6f + 0.2f,
                baseY = random.nextFloat() * 0.4f + 0.2f,
                size = random.nextFloat() * 80f + 260f,
                opacity = random.nextFloat() * 0.25f + 0.6f,
                color = AppColors.success.copy(alpha = 0.6f),
                offsetX = random.nextFloat() * 50f + 40f,
                offsetY = random.nextFloat() * 50f + 40f,
                speed = random.nextFloat() * 0.6f + 0.8f
            )
        )

        regular + heroes
    }

    Box(modifier = modifier.fillMaxSize().background(AppColors.background)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            for (orb in orbs) {
                val animPhase = sin(phase * 2 * Math.PI * orb.speed).toFloat()
                val cx = orb.baseX * w + animPhase * orb.offsetX
                val cy = orb.baseY * h + animPhase * orb.offsetY
                val radius = orb.size / 2f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orb.color, Color.Transparent),
                        center = Offset(cx, cy),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(cx, cy),
                    alpha = orb.opacity
                )
            }
        }
    }
}
