package com.discipl.app.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.discipl.app.data.model.DailyCheckIn
import com.discipl.app.data.model.Relapse
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppSpacing
import com.discipl.app.ui.theme.AppTypography
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarHeatmap(
    checkIns: List<DailyCheckIn>,
    relapses: List<Relapse>,
    currentMonth: YearMonth,
    profileCreatedAt: Long,
    language: String,
    onMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now()
    val createdDate = Instant.ofEpochMilli(profileCreatedAt).atZone(zone).toLocalDate()

    // Build lookup maps
    val checkInDays = checkIns.map {
        Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate()
    }.toSet()

    val relapseDays = relapses.map {
        Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate()
    }.toSet()

    val locale = if (language == "en") Locale.US else Locale("es", "ES")
    val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { it.titlecase(locale) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface.copy(alpha = 0.6f))
            .padding(AppSpacing.md.dp)
    ) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = AppColors.textSecondary)
            }
            Text(
                text = "$monthName ${currentMonth.year}",
                style = AppTypography.body.copy(fontWeight = FontWeight.Bold),
                color = AppColors.textPrimary
            )
            IconButton(
                onClick = { onMonthChange(currentMonth.plusMonths(1)) },
                enabled = currentMonth.isBefore(YearMonth.now())
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next month",
                    tint = if (currentMonth.isBefore(YearMonth.now())) AppColors.textSecondary
                    else AppColors.textSecondary.copy(alpha = 0.2f)
                )
            }
        }

        // Day of week headers
        val dayHeaders = if (language == "en") {
            listOf("S", "M", "T", "W", "T", "F", "S")
        } else {
            listOf("D", "L", "M", "M", "J", "V", "S")
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEach { day ->
                Text(
                    text = day,
                    style = AppTypography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = AppColors.textSecondary.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Calendar grid
        val firstDayOfMonth = currentMonth.atDay(1)
        val daysInMonth = currentMonth.lengthOfMonth()
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0

        val totalCells = startDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - startDayOfWeek + 1

                    if (dayNum in 1..daysInMonth) {
                        val date = currentMonth.atDay(dayNum)
                        val color = when {
                            date.isAfter(today) -> AppColors.surface // future
                            date.isBefore(createdDate) -> AppColors.textSecondary.copy(alpha = 0.1f) // before install
                            relapseDays.contains(date) -> AppColors.danger
                            checkInDays.contains(date) -> AppColors.success
                            else -> AppColors.textSecondary.copy(alpha = 0.15f) // no data
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayNum",
                                style = AppTypography.caption.copy(fontSize = 10.sp),
                                color = when {
                                    date.isAfter(today) -> AppColors.textSecondary.copy(alpha = 0.3f)
                                    relapseDays.contains(date) || checkInDays.contains(date) -> AppColors.textPrimary
                                    else -> AppColors.textSecondary.copy(alpha = 0.5f)
                                }
                            )
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(AppSpacing.sm.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(AppColors.success, if (language == "en") "Clean" else "Limpio")
            Spacer(Modifier.width(16.dp))
            LegendDot(AppColors.danger, if (language == "en") "Relapse" else "Recaída")
            Spacer(Modifier.width(16.dp))
            LegendDot(AppColors.textSecondary.copy(alpha = 0.15f), if (language == "en") "No data" else "Sin datos")
        }
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = AppTypography.caption.copy(fontSize = 10.sp),
            color = AppColors.textSecondary
        )
    }
}
