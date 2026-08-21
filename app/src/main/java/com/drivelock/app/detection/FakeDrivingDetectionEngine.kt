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
    private val mutableDriverDecision = MutableStateFlow(DriverDecision.UNKNOWN)
    override val driverDecision = mutableDriverDecision.asStateFlow()

    override fun startMonitoring() { mutableMonitoringState.value = MonitoringState.ACTIVE }
    override fun stopMonitoring() { mutableMonitoringState.value = MonitoringState.STOPPED; reset() }

    fun simulateMovement() { mutableDriveState.value = DriveState.MOVEMENT_DETECTED }
    fun simulateVehicleDetection() { mutableDriveState.value = DriveState.CONFIRMING_DRIVER }
    override fun confirmDriver() {
        if (mutableDriveState.value != DriveState.CONFIRMING_DRIVER) return
        mutableDriverDecision.value = DriverDecision.DRIVER
        mutableDriveState.value = DriveState.DRIVING
    }
    override fun markPassenger() {
        if (mutableDriveState.value != DriveState.CONFIRMING_DRIVER) return
        mutableDriverDecision.value = DriverDecision.PASSENGER
        mutableDriveState.value = DriveState.IDLE
    }
    fun simulateTripEnd() { mutableDriveState.value = DriveState.POSSIBLE_TRIP_END }
    override fun endTrip() = simulateTripEnd()
    override fun onTrackingStopped() = simulateTripEnd()
    override fun reset() { mutableDriverDecision.value = DriverDecision.UNKNOWN; mutableDriveState.value = DriveState.IDLE }
}
