package com.discipl.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.discipl.app.data.db.UserProfileDao
import com.discipl.app.ui.benefits.BenefitsTimelineScreen
import com.discipl.app.ui.home.HomeScreen
import com.discipl.app.ui.journal.JournalScreen
import com.discipl.app.ui.settings.SettingsScreen
import com.discipl.app.ui.stats.StatsScreen
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppTypography

data class TabItem(
    val titleEs: String,
    val titleEn: String,
    val icon: ImageVector
)

val tabs = listOf(
    TabItem("Inicio", "Home", Icons.Default.Home),
    TabItem("Beneficios", "Benefits", Icons.Default.Timeline),
    TabItem("Progreso", "Stats", Icons.Default.BarChart),
    TabItem("Diario", "Journal", Icons.Default.Book),
    TabItem("Ajustes", "Settings", Icons.Default.Settings)
)

@Composable
fun MainNavigation() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var expandMilestoneDay by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        containerColor = AppColors.background,
        bottomBar = {
            NavigationBar(
                containerColor = AppColors.surface,
                contentColor = AppColors.textPrimary
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.titleEn) },
                        label = {
                            Text(
                                text = tab.titleEs, // Will be dynamic based on language
                                style = AppTypography.caption
                            )
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppColors.accent,
                            selectedTextColor = AppColors.accent,
                            unselectedIconColor = AppColors.textSecondary,
                            unselectedTextColor = AppColors.textSecondary,
                            indicatorColor = AppColors.accent.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                onNavigateToTab = { selectedTab = it },
                onNavigateToBenefitsMilestone = { day ->
                    expandMilestoneDay = day
                    selectedTab = 1
                }
            )
            1 -> BenefitsTimelineScreen(
                modifier = Modifier.padding(innerPadding),
                expandMilestoneDay = expandMilestoneDay,
                onMilestoneExpanded = { expandMilestoneDay = null }
            )
            2 -> StatsScreen(modifier = Modifier.padding(innerPadding))
            3 -> JournalScreen(modifier = Modifier.padding(innerPadding))
            4 -> SettingsScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}
