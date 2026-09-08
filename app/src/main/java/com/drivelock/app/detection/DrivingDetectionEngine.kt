package com.drivelock.app.detection

import com.drivelock.app.domain.model.DriveState
import com.drivelock.app.detection.location.LocationSample
import kotlinx.coroutines.flow.StateFlow

interface DrivingDetectionEngine {
    val driveState: StateFlow<DriveState>
    val monitoringState: StateFlow<MonitoringState>
    val driverDecision: StateFlow<DriverDecision>
    fun startMonitoring()
    fun stopMonitoring()
    fun confirmDriver()
    fun markPassenger()
    fun endTrip()
    fun onTrackingStopped()
    fun reset()
    fun onLocationSample(sample: LocationSample)
    fun onMonitoringUnavailable()
}

enum class DriverDecision { UNKNOWN, DRIVER, PASSENGER }

enum class MonitoringState {
    STOPPED,
    LOCATION_PERMISSION_REQUIRED,
    BACKGROUND_LOCATION_PERMISSION_REQUIRED,
    STARTING,
    ACTIVE,
    UNAVAILABLE,
}
