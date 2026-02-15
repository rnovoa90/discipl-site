package com.discipl.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.discipl.app.ui.theme.AppColors

fun Modifier.cardStyle(): Modifier = this
    .clip(RoundedCornerShape(12.dp))
    .background(AppColors.surface)
    .padding(16.dp)
