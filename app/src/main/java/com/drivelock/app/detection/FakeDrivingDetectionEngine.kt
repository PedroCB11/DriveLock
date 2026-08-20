package com.drivelock.app.detection

import com.drivelock.app.domain.model.DriveState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeDrivingDetectionEngine : DrivingDetectionEngine {
    private val mutableDriveState = MutableStateFlow(DriveState.IDLE)
    override val driveState: StateFlow<DriveState> = mutableDriveState.asStateFlow()
    private val mutableMonitoringState = MutableStateFlow(MonitoringState.STOPPED)
    override val monitoringState = mutableMonitoringState.asStateFlow()

    override fun startMonitoring() { mutableMonitoringState.value = MonitoringState.ACTIVE }
    override fun stopMonitoring() { mutableMonitoringState.value = MonitoringState.STOPPED; mutableDriveState.value = DriveState.IDLE }

    fun simulateMovement() { mutableDriveState.value = DriveState.MOVEMENT_DETECTED }
    fun simulateVehicleDetection() { mutableDriveState.value = DriveState.CONFIRMING_DRIVER }
    override fun confirmDriver() { mutableDriveState.value = DriveState.DRIVING }
    override fun markPassenger() { mutableDriveState.value = DriveState.IDLE }
    fun simulateTripEnd() { mutableDriveState.value = DriveState.POSSIBLE_TRIP_END }
    override fun endTrip() = simulateTripEnd()
    override fun reset() { mutableDriveState.value = DriveState.IDLE }
}
