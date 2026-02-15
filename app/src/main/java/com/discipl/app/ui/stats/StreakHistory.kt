package com.discipl.app.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.discipl.app.data.model.Streak
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppSpacing
import com.discipl.app.ui.theme.AppTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StreakHistory(
    streaks: List<Streak>,
    language: String
) {
    val maxDays = streaks.maxOfOrNull { it.durationDays }?.toFloat()?.coerceAtLeast(1f) ?: 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.lg.dp)
            .padding(bottom = AppSpacing.xxl.dp)
    ) {
        Text(
            text = if (language == "en") "Streak History" else "Historial de Rachas",
            style = AppTypography.sectionHeader,
            color = AppColors.textPrimary
        )

        Spacer(Modifier.height(AppSpacing.lg.dp))

        if (streaks.isEmpty()) {
            Text(
                text = if (language == "en") "No streaks recorded yet." else "Aún no hay rachas registradas.",
                style = AppTypography.body,
                color = AppColors.textSecondary
            )
        } else {
            streaks.forEachIndexed { index, streak ->
                val locale = if (language == "en") Locale.US else Locale("es", "ES")
                val dateFormat = SimpleDateFormat("d MMM yyyy", locale)
                val startLabel = dateFormat.format(Date(streak.startDate))
                val endLabel = if (streak.isActive) {
                    if (language == "en") "Active" else "Activa"
                } else {
                    dateFormat.format(Date(streak.endDate!!))
                }
                val barWidth = streak.durationDays / maxDays

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Duration label
                    Text(
                        text = "${streak.durationDays}",
                        style = AppTypography.body.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = if (streak.isActive) AppColors.success else AppColors.textPrimary,
                        modifier = Modifier.width(40.dp)
                    )

                    // Bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(barWidth)
                                .height(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (streak.isActive) AppColors.success.copy(alpha = 0.6f)
                                    else AppColors.accent.copy(alpha = 0.4f)
                                )
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // Date range
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = startLabel,
                            style = AppTypography.caption.copy(fontSize = 10.sp),
                            color = AppColors.textSecondary
                        )
                        Text(
                            text = endLabel,
                            style = AppTypography.caption.copy(
                                fontSize = 10.sp,
                                fontWeight = if (streak.isActive) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (streak.isActive) AppColors.success else AppColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
