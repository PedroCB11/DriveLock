package com.drivelock.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.drivelock.app.ui.settings.SettingsScreen
import com.drivelock.app.ui.summary.TripSummaryScreen

@Composable
fun DriveLockNavHost(navController: NavHostController, container: AppContainer) {
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(container.tripRepository, container.detectionEngine))
    val drivingViewModel: DrivingViewModel = viewModel(factory = DrivingViewModel.Factory(container.detectionEngine))
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val drivingState by drivingViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(homeState.driveState) {
        when (homeState.driveState) {
            DriveState.CONFIRMING_DRIVER -> navController.navigate(Route.DrivingConfirmation.path) { launchSingleTop = true }
            DriveState.DRIVING -> navController.navigate(Route.ActiveDrive.path) { launchSingleTop = true }
            DriveState.POSSIBLE_TRIP_END -> navController.navigate(Route.TripSummary.path) { launchSingleTop = true }
            else -> Unit
        }
    }

    NavHost(navController, startDestination = Route.Home.path) {
        composable(Route.Home.path) { HomeScreen(homeState, { navController.navigate(Route.History.path) }, { navController.navigate(Route.Settings.path) }, homeViewModel::simulateMovement, homeViewModel::simulateVehicleDetection, homeViewModel::reset) }
        composable(Route.DrivingConfirmation.path) { DrivingConfirmationScreen(drivingViewModel::confirmDriver) { drivingViewModel.markPassenger(); navController.popBackStack(Route.Home.path, false) } }
        composable(Route.ActiveDrive.path) { ActiveDriveScreen(drivingState, drivingViewModel::endTrip) }
        composable(Route.TripSummary.path) { TripSummaryScreen { drivingViewModel.reset(); navController.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } } } }
        composable(Route.History.path) {
            val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(container.tripRepository))
            val trips by historyViewModel.trips.collectAsStateWithLifecycle()
            HistoryScreen(trips)
        }
        composable(Route.Settings.path) { SettingsScreen() }
    }
}

