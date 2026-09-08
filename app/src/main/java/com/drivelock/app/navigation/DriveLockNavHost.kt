package com.drivelock.app.navigation

import android.Manifest
import android.os.Build
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.drivelock.app.AppContainer
import com.drivelock.app.domain.model.DriveState
import com.drivelock.app.ui.driving.ActiveDriveScreen
import com.drivelock.app.ui.driving.DrivingConfirmationScreen
import com.drivelock.app.ui.driving.DrivingViewModel
import com.drivelock.app.ui.history.HistoryScreen
import com.drivelock.app.ui.history.HistoryViewModel
import com.drivelock.app.ui.home.HomeScreen
import com.drivelock.app.ui.home.HomeViewModel
import com.drivelock.app.ui.onboarding.HowItWorksScreen
import com.drivelock.app.ui.onboarding.PrivacyScreen
import com.drivelock.app.ui.onboarding.WelcomeScreen
import com.drivelock.app.ui.settings.SettingsScreen
import com.drivelock.app.ui.settings.SettingsViewModel
import com.drivelock.app.ui.settings.NotificationAppsScreen
import com.drivelock.app.ui.summary.TripSummaryScreen

@Composable
fun DriveLockNavHost(navController: NavHostController, container: AppContainer) {
    val context = LocalContext.current
    var onboardingComplete by rememberSaveable { mutableStateOf(container.onboardingPreferences.isComplete()) }
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(container.tripRepository, container.detectionEngine))
    val drivingViewModel: DrivingViewModel = viewModel(
        factory = DrivingViewModel.Factory(container.detectionEngine, container.tripSessionManager),
    )
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val drivingState by drivingViewModel.uiState.collectAsStateWithLifecycle()
    val activityPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        homeViewModel.startMonitoring()
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        homeViewModel.startMonitoring()
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        drivingViewModel.confirmDriver()
    }

    LaunchedEffect(onboardingComplete) {
        if (onboardingComplete) homeViewModel.startMonitoring()
    }

    LaunchedEffect(homeState.driveState, onboardingComplete) {
        if (!onboardingComplete) return@LaunchedEffect
        when (homeState.driveState) {
            DriveState.CONFIRMING_DRIVER -> navController.navigate(Route.DrivingConfirmation.path) { launchSingleTop = true }
            DriveState.DRIVING -> navController.navigate(Route.ActiveDrive.path) { launchSingleTop = true }
            DriveState.POSSIBLE_TRIP_END -> navController.navigate(Route.TripSummary.path) { launchSingleTop = true }
            else -> Unit
        }
    }

    NavHost(
        navController = navController,
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        startDestination = if (onboardingComplete) Route.Home.path else Route.OnboardingWelcome.path,
        enterTransition = { forwardEnterTransition() },
        exitTransition = { forwardExitTransition() },
        popEnterTransition = { backwardEnterTransition() },
        popExitTransition = { backwardExitTransition() },
    ) {
        composable(Route.OnboardingWelcome.path) {
            WelcomeScreen { navController.navigate(Route.OnboardingHowItWorks.path) }
        }
        composable(Route.OnboardingHowItWorks.path) {
            HowItWorksScreen { navController.navigate(Route.OnboardingPrivacy.path) }
        }
        composable(Route.OnboardingPrivacy.path) {
            PrivacyScreen {
                container.onboardingPreferences.markComplete()
                onboardingComplete = true
                navController.navigate(Route.Home.path) {
                    popUpTo(Route.OnboardingWelcome.path) { inclusive = true }
                }
            }
        }
        composable(Route.Home.path) {
            HomeScreen(
                homeState,
                { navController.navigate(Route.History.path) },
                { navController.navigate(Route.Settings.path) },
                { navController.navigate(Route.NotificationApps.path) },
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    } else {
                        homeViewModel.startMonitoring()
                    }
                },
                {
                    locationPermissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                    )
                },
                homeViewModel::reset,
            )
        }
        composable(Route.DrivingConfirmation.path) {
            DrivingConfirmationScreen(
                onDriver = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        drivingViewModel.confirmDriver()
                    }
                },
                onPassenger = { drivingViewModel.markPassenger(); navController.popBackStack(Route.Home.path, false) },
            )
        }
        composable(Route.ActiveDrive.path) {
            ActiveDriveScreen(drivingState, drivingViewModel::endTrip) { navController.navigate(Route.Home.path) { launchSingleTop = true } }
        }
        composable(Route.TripSummary.path) {
            TripSummaryScreen(drivingState) {
                drivingViewModel.reset()
                navController.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } }
            }
        }
        composable(Route.History.path) {
            val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(container.tripRepository))
            val trips by historyViewModel.trips.collectAsStateWithLifecycle()
            HistoryScreen(trips) { navController.popBackStack() }
        }
        composable(Route.Settings.path) {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(container.tripRepository))
            val themeMode by container.settingsPreferences.themeMode.collectAsStateWithLifecycle()
            val historyCleared by settingsViewModel.historyCleared.collectAsStateWithLifecycle()
            SettingsScreen(
                themeMode = themeMode,
                historyCleared = historyCleared,
                onThemeModeChange = container.settingsPreferences::setThemeMode,
                onNotificationApps = { navController.navigate(Route.NotificationApps.path) },
                onBack = { navController.popBackStack() },
                onOpenPermissions = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                    )
                },
                onReviewOnboarding = {
                    container.onboardingPreferences.reset()
                    onboardingComplete = false
                    navController.navigate(Route.OnboardingWelcome.path) {
                        popUpTo(Route.Home.path) { inclusive = true }
                    }
                },
                onClearHistory = settingsViewModel::clearHistory,
                onHistoryClearedConsumed = settingsViewModel::consumeHistoryCleared,
            )
        }
        composable(Route.NotificationApps.path) {
            val selectedPackages by container.notificationControlPreferences.selectedPackages.collectAsStateWithLifecycle()
            NotificationAppsScreen(
                context = context,
                selectedPackages = selectedPackages,
                onToggle = container.notificationControlPreferences::toggle,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private const val TRANSITION_DURATION_MILLIS = 380

private fun forwardEnterTransition(): EnterTransition =
    slideInHorizontally(tween(TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing)) { it }

private fun forwardExitTransition(): ExitTransition =
    slideOutHorizontally(tween(TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing)) { -it / 4 }

private fun backwardEnterTransition(): EnterTransition =
    slideInHorizontally(tween(TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing)) { -it / 4 }

private fun backwardExitTransition(): ExitTransition =
    slideOutHorizontally(tween(TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing)) { it }
