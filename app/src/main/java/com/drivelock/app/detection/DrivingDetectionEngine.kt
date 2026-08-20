package com.drivelock.app.detection

import com.drivelock.app.domain.model.DriveState
import kotlinx.coroutines.flow.StateFlow

interface DrivingDetectionEngine {
    val driveState: StateFlow<DriveState>
    val monitoringState: StateFlow<MonitoringState>
    fun startMonitoring()
    fun stopMonitoring()
    fun confirmDriver()
    fun markPassenger()
    fun endTrip()
    fun reset()
}

enum class MonitoringState {
    STOPPED,
    ACTIVITY_PERMISSION_REQUIRED,
    LOCATION_PERMISSION_REQUIRED,
    STARTING,
    ACTIVE,
    UNAVAILABLE,
}
