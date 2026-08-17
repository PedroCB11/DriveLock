package com.drivelock.app.detection

import com.drivelock.app.domain.model.DriveState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeDrivingDetectionEngine : DrivingDetectionEngine {
    private val mutableDriveState = MutableStateFlow(DriveState.IDLE)
    override val driveState: StateFlow<DriveState> = mutableDriveState.asStateFlow()

    override fun startMonitoring() { mutableDriveState.value = DriveState.IDLE }
    override fun stopMonitoring() { mutableDriveState.value = DriveState.IDLE }

    fun simulateMovement() { mutableDriveState.value = DriveState.MOVEMENT_DETECTED }
    fun simulateVehicleDetection() { mutableDriveState.value = DriveState.CONFIRMING_DRIVER }
    fun confirmDriver() { mutableDriveState.value = DriveState.DRIVING }
    fun markPassenger() { mutableDriveState.value = DriveState.IDLE }
    fun simulateTripEnd() { mutableDriveState.value = DriveState.POSSIBLE_TRIP_END }
    fun reset() { mutableDriveState.value = DriveState.IDLE }
}

