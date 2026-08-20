package com.drivelock.app.ui.home

import com.drivelock.app.domain.model.DriveState
import com.drivelock.app.domain.model.Trip
import com.drivelock.app.detection.MonitoringState

data class HomeUiState(
    val driveState: DriveState = DriveState.IDLE,
    val lastTrip: Trip? = null,
    val isLoading: Boolean = true,
    val monitoringState: MonitoringState = MonitoringState.STOPPED,
)
