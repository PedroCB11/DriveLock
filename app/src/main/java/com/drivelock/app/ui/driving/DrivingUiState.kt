package com.drivelock.app.ui.driving

import com.drivelock.app.domain.model.DriveState

data class DrivingUiState(
    val driveState: DriveState = DriveState.IDLE,
    val elapsedMinutes: Int = 0,
    val distanceKm: Double = 0.0,
    val speedKph: Double = 0.0,
)

