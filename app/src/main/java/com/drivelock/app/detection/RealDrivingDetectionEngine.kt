package com.drivelock.app.detection

import com.drivelock.app.detection.activity.ActivityRecognitionDataSource
import com.drivelock.app.detection.activity.RecognizedActivity
import com.drivelock.app.detection.activity.TransitionType
import com.drivelock.app.detection.location.LocationDataSource
import com.drivelock.app.detection.location.VehicleSpeedVerifier
import com.drivelock.app.domain.model.DriveState
import com.drivelock.app.tracking.NoOpTripTrackingController
import com.drivelock.app.tracking.TripTrackingController
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
    private val tripTrackingController: TripTrackingController = NoOpTripTrackingController,
    private val tripEndDetector: TripEndDetector = TripEndDetector(config),
) : DrivingDetectionEngine {
    private val mutableDriveState = MutableStateFlow(DriveState.IDLE)
    override val driveState = mutableDriveState.asStateFlow()
    private val mutableMonitoringState = MutableStateFlow(MonitoringState.STOPPED)
    override val monitoringState = mutableMonitoringState.asStateFlow()
    private val mutableDriverDecision = MutableStateFlow(DriverDecision.UNKNOWN)
    override val driverDecision = mutableDriverDecision.asStateFlow()
    private var signalJob: Job? = null
    private var locationJob: Job? = null
    private var tripEndJob: Job? = null
    private val speedVerifier = VehicleSpeedVerifier(config)

    override fun startMonitoring() {
        if (!dataSource.hasPermission()) {
            mutableMonitoringState.value = MonitoringState.ACTIVITY_PERMISSION_REQUIRED
            return
        }
        if (signalJob == null) signalJob = scope.launch { dataSource.signals.collect(::handleSignal) }
        if (tripEndJob == null) tripEndJob = scope.launch {
            tripEndDetector.probableTripEnd.collect { probable -> if (probable) endTrip() }
        }
        mutableMonitoringState.value = MonitoringState.STARTING
        dataSource.start { result ->
            mutableMonitoringState.value = if (result.isSuccess) MonitoringState.ACTIVE else MonitoringState.UNAVAILABLE
            if (result.isSuccess && mutableDriveState.value == DriveState.MOVEMENT_DETECTED) startLocationVerification()
        }
    }

    override fun stopMonitoring() {
        dataSource.stop()
        stopLocationVerification()
        tripTrackingController.stop()
        signalJob?.cancel()
        signalJob = null
        tripEndJob?.cancel()
        tripEndJob = null
        tripEndDetector.reset()
        mutableMonitoringState.value = MonitoringState.STOPPED
        mutableDriveState.value = DriveState.IDLE
        mutableDriverDecision.value = DriverDecision.UNKNOWN
    }

    internal fun handleSignal(signal: com.drivelock.app.detection.activity.ActivityTransitionSignal) {
        if (signal.activity == RecognizedActivity.IN_VEHICLE && signal.transition == TransitionType.ENTER) {
            tripEndDetector.onVehiclePresenceChanged(true, signal.elapsedRealtimeMillis)
            if (mutableDriverDecision.value == DriverDecision.PASSENGER) return
            if (mutableDriveState.value == DriveState.DRIVING) return
            mutableDriveState.value = DriveState.MOVEMENT_DETECTED
            startLocationVerification()
        } else if (signal.activity == RecognizedActivity.IN_VEHICLE && signal.transition == TransitionType.EXIT) {
            tripEndDetector.onVehiclePresenceChanged(false, signal.elapsedRealtimeMillis)
            if (mutableDriveState.value != DriveState.DRIVING) stopLocationVerification()
            if (mutableDriverDecision.value == DriverDecision.PASSENGER) {
                mutableDriverDecision.value = DriverDecision.UNKNOWN
            }
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

    override fun confirmDriver() {
        if (mutableDriveState.value != DriveState.CONFIRMING_DRIVER) return
        val startResult = tripTrackingController.start()
        if (startResult.isFailure) {
            mutableMonitoringState.value = MonitoringState.UNAVAILABLE
            return
        }
        tripEndDetector.startSession()
        mutableDriverDecision.value = DriverDecision.DRIVER
        mutableDriveState.value = DriveState.DRIVING
    }

    override fun markPassenger() {
        if (mutableDriveState.value != DriveState.CONFIRMING_DRIVER) return
        stopLocationVerification()
        mutableDriverDecision.value = DriverDecision.PASSENGER
        mutableDriveState.value = DriveState.IDLE
    }
    override fun endTrip() {
        if (mutableDriveState.value != DriveState.DRIVING) return
        tripTrackingController.stop()
        onTrackingStopped()
    }

    override fun onTrackingStopped() {
        tripEndDetector.reset()
        if (mutableDriverDecision.value == DriverDecision.DRIVER) mutableDriveState.value = DriveState.POSSIBLE_TRIP_END
    }
    override fun reset() {
        stopLocationVerification()
        tripTrackingController.stop()
        tripEndDetector.reset()
        mutableDriverDecision.value = DriverDecision.UNKNOWN
        mutableDriveState.value = DriveState.IDLE
    }
}
