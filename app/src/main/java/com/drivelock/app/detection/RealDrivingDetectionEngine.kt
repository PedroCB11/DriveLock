package com.drivelock.app.detection

import com.drivelock.app.detection.activity.ActivityRecognitionDataSource
import com.drivelock.app.detection.activity.RecognizedActivity
import com.drivelock.app.detection.activity.TransitionType
import com.drivelock.app.domain.model.DriveState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RealDrivingDetectionEngine(
    private val dataSource: ActivityRecognitionDataSource,
    private val scope: CoroutineScope,
    private val config: DetectionConfig = DetectionConfig(),
) : DrivingDetectionEngine {
    private val mutableDriveState = MutableStateFlow(DriveState.IDLE)
    override val driveState = mutableDriveState.asStateFlow()
    private val mutableMonitoringState = MutableStateFlow(MonitoringState.STOPPED)
    override val monitoringState = mutableMonitoringState.asStateFlow()
    private var signalJob: Job? = null
    private var confirmationJob: Job? = null

    override fun startMonitoring() {
        if (!dataSource.hasPermission()) {
            mutableMonitoringState.value = MonitoringState.PERMISSION_REQUIRED
            return
        }
        if (signalJob == null) signalJob = scope.launch { dataSource.signals.collect(::handleSignal) }
        mutableMonitoringState.value = MonitoringState.STARTING
        dataSource.start { result ->
            mutableMonitoringState.value = if (result.isSuccess) MonitoringState.ACTIVE else MonitoringState.UNAVAILABLE
        }
    }

    override fun stopMonitoring() {
        dataSource.stop()
        confirmationJob?.cancel()
        signalJob?.cancel()
        signalJob = null
        mutableMonitoringState.value = MonitoringState.STOPPED
        mutableDriveState.value = DriveState.IDLE
    }

    internal fun handleSignal(signal: com.drivelock.app.detection.activity.ActivityTransitionSignal) {
        if (signal.activity == RecognizedActivity.IN_VEHICLE && signal.transition == TransitionType.ENTER) {
            confirmationJob?.cancel()
            mutableDriveState.value = DriveState.MOVEMENT_DETECTED
            confirmationJob = scope.launch {
                delay(config.vehicleConfirmationDelayMillis)
                mutableDriveState.value = DriveState.POSSIBLE_VEHICLE
                mutableDriveState.value = DriveState.CONFIRMING_DRIVER
            }
        } else if (signal.activity == RecognizedActivity.IN_VEHICLE && signal.transition == TransitionType.EXIT) {
            confirmationJob?.cancel()
            if (mutableDriveState.value != DriveState.DRIVING) mutableDriveState.value = DriveState.IDLE
        }
    }

    override fun confirmDriver() { mutableDriveState.value = DriveState.DRIVING }
    override fun markPassenger() { confirmationJob?.cancel(); mutableDriveState.value = DriveState.IDLE }
    override fun endTrip() { mutableDriveState.value = DriveState.POSSIBLE_TRIP_END }
    override fun reset() { confirmationJob?.cancel(); mutableDriveState.value = DriveState.IDLE }
}
