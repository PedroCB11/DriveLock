package com.drivelock.app.navigation

sealed class Route(val path: String) {
    data object Home : Route("home")
    data object DrivingConfirmation : Route("driving_confirmation")
    data object ActiveDrive : Route("active_drive")
    data object TripSummary : Route("trip_summary")
    data object History : Route("history")
    data object Settings : Route("settings")
}

