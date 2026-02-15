package com.discipl.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.discipl.app.data.db.UserProfileDao
import com.discipl.app.data.model.UserProfile
import com.discipl.app.service.AnalyticsService
import com.discipl.app.service.StreakService
import com.discipl.app.ui.onboarding.OnboardingScreen
import com.discipl.app.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class Screen(val route: String) {
    data object Loading : Screen("loading")
    data object Onboarding : Screen("onboarding")
    data object Main : Screen("main")
}

@HiltViewModel
class AppNavViewModel @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val streakService: StreakService,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow<AppNavState>(AppNavState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = userProfileDao.get()
            if (profile == null) {
                userProfileDao.insert(UserProfile())
                _state.value = AppNavState.Onboarding
            } else if (!profile.hasCompletedOnboarding) {
                _state.value = AppNavState.Onboarding
            } else {
                if (streakService.getCurrentStreak() == null) {
                    streakService.createInitialStreak(profile.quitDate)
                }
                analyticsService.appOpened(streakService.getCurrentStreakDays())
                _state.value = AppNavState.Main
            }
        }
    }

    fun onOnboardingComplete() {
        _state.value = AppNavState.Main
    }
}

sealed class AppNavState {
    data object Loading : AppNavState()
    data object Onboarding : AppNavState()
    data object Main : AppNavState()
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val viewModel: AppNavViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val navController = rememberNavController()

    LaunchedEffect(state) {
        val route = when (state) {
            AppNavState.Loading -> Screen.Loading.route
            AppNavState.Onboarding -> Screen.Onboarding.route
            AppNavState.Main -> Screen.Main.route
        }
        if (navController.currentDestination?.route != route) {
            navController.navigate(route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Loading.route,
        modifier = modifier
    ) {
        composable(Screen.Loading.route) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.background)
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(onComplete = { viewModel.onOnboardingComplete() })
        }

        composable(Screen.Main.route) {
            MainNavigation()
        }
    }
}
