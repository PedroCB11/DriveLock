package com.drivelock.app.detection

import com.drivelock.app.detection.activity.ActivityRecognitionDataSource
import com.drivelock.app.detection.activity.RecognizedActivity
import com.drivelock.app.detection.activity.TransitionType
import com.drivelock.app.detection.location.LocationDataSource
import com.drivelock.app.detection.location.VehicleSpeedVerifier
import com.drivelock.app.domain.model.DriveState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RealDrivingDetectionEngine(
    private val dataSource: ActivityRecognitionDataSource,
    private val locationDataSource: LocationDataSource,
    private val scope: CoroutineScope,
    private val config: DetectionConfig = DetectionConfig(),
) : DrivingDetectionEngine {
    private val mutableDriveState = MutableStateFlow(DriveState.IDLE)
    override val driveState = mutableDriveState.asStateFlow()
    private val mutableMonitoringState = MutableStateFlow(MonitoringState.STOPPED)
    override val monitoringState = mutableMonitoringState.asStateFlow()
    private var signalJob: Job? = null
    private var locationJob: Job? = null
    private val speedVerifier = VehicleSpeedVerifier(config)

    override fun startMonitoring() {
        if (!dataSource.hasPermission()) {
            mutableMonitoringState.value = MonitoringState.ACTIVITY_PERMISSION_REQUIRED
            return
        }
        if (signalJob == null) signalJob = scope.launch { dataSource.signals.collect(::handleSignal) }
        mutableMonitoringState.value = MonitoringState.STARTING
        dataSource.start { result ->
            mutableMonitoringState.value = if (result.isSuccess) MonitoringState.ACTIVE else MonitoringState.UNAVAILABLE
            if (result.isSuccess && mutableDriveState.value == DriveState.MOVEMENT_DETECTED) startLocationVerification()
        }
    }

    override fun stopMonitoring() {
        dataSource.stop()
        stopLocationVerification()
        signalJob?.cancel()
        signalJob = null
        mutableMonitoringState.value = MonitoringState.STOPPED
        mutableDriveState.value = DriveState.IDLE
    }

    internal fun handleSignal(signal: com.drivelock.app.detection.activity.ActivityTransitionSignal) {
        if (signal.activity == RecognizedActivity.IN_VEHICLE && signal.transition == TransitionType.ENTER) {
            mutableDriveState.value = DriveState.MOVEMENT_DETECTED
            startLocationVerification()
        } else if (signal.activity == RecognizedActivity.IN_VEHICLE && signal.transition == TransitionType.EXIT) {
            stopLocationVerification()
            if (mutableDriveState.value != DriveState.DRIVING) mutableDriveState.value = DriveState.IDLE
        }
    }

    private fun startLocationVerification() {
        if (!locationDataSource.hasPreciseLocationPermission()) {
            mutableMonitoringState.value = MonitoringState.LOCATION_PERMISSION_REQUIRED
            return
        }
        if (locationJob == null) locationJob = scope.launch {
            locationDataSource.samples.collect { sample ->
                if (speedVerifier.add(sample)) {
                    mutableDriveState.value = DriveState.POSSIBLE_VEHICLE
                    mutableDriveState.value = DriveState.CONFIRMING_DRIVER
                    stopLocationVerification()
                }
            }
        }
        mutableMonitoringState.value = MonitoringState.STARTING
        locationDataSource.start { result ->
            mutableMonitoringState.value = if (result.isSuccess) MonitoringState.ACTIVE else MonitoringState.UNAVAILABLE
        }
    }

    private fun stopLocationVerification() {
        locationDataSource.stop()
        locationJob?.cancel()
        locationJob = null
        speedVerifier.reset()
    }

    override fun confirmDriver() { mutableDriveState.value = DriveState.DRIVING }
    override fun markPassenger() { stopLocationVerification(); mutableDriveState.value = DriveState.IDLE }
    override fun endTrip() { mutableDriveState.value = DriveState.POSSIBLE_TRIP_END }
    override fun reset() { stopLocationVerification(); mutableDriveState.value = DriveState.IDLE }
}
