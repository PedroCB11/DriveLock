package com.drivelock.app.detection

import com.drivelock.app.detection.location.LocationDataSource
import com.drivelock.app.detection.location.LocationSample
import com.drivelock.app.domain.model.DriveState
import com.drivelock.app.tracking.NoOpTripTrackingController
import com.drivelock.app.tracking.TripTrackingController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RealDrivingDetectionEngine(
    private val monitoringLocationDataSource: LocationDataSource,
    private val scope: CoroutineScope,
    private val config: DetectionConfig = DetectionConfig(),
    private val monitorController: DrivingMonitorController = NoOpDrivingMonitorController,
    private val tripTrackingController: TripTrackingController = NoOpTripTrackingController,
) : DrivingDetectionEngine {
    private val mutableDriveState = MutableStateFlow(DriveState.IDLE)
    override val driveState = mutableDriveState.asStateFlow()
    private val mutableMonitoringState = MutableStateFlow(MonitoringState.STOPPED)
    override val monitoringState = mutableMonitoringState.asStateFlow()
    private val mutableDriverDecision = MutableStateFlow(DriverDecision.UNKNOWN)
    override val driverDecision = mutableDriverDecision.asStateFlow()
    private val mutableDiagnostics = MutableStateFlow(
        DetectionDiagnostics(
            speedThresholdKph = config.minimumVehicleSpeedMetersPerSecond * 3.6,
            stopDelaySeconds = config.tripEndStationaryDurationMillis / 1_000,
        ),
    )
    override val diagnostics = mutableDiagnostics.asStateFlow()
    private var locationJob: Job? = null
    private var lowSpeedJob: Job? = null

    override fun startMonitoring() {
        if (mutableDriveState.value == DriveState.DRIVING || mutableMonitoringState.value == MonitoringState.ACTIVE) return
        if (!monitoringLocationDataSource.hasPreciseLocationPermission()) {
            if (mutableMonitoringState.value !in setOf(
                    MonitoringState.LOCATION_PERMISSION_DENIED,
                    MonitoringState.LOCATION_PERMISSION_PERMANENTLY_DENIED,
                )
            ) mutableMonitoringState.value = MonitoringState.LOCATION_PERMISSION_REQUIRED
            return
        }
        if (!monitoringLocationDataSource.hasBackgroundLocationPermission()) {
            mutableMonitoringState.value = MonitoringState.BACKGROUND_LOCATION_PERMISSION_REQUIRED
            return
        }
        if (locationJob == null) locationJob = scope.launch {
            monitoringLocationDataSource.samples.collect(::onLocationSample)
        }
        mutableMonitoringState.value = MonitoringState.STARTING
        monitorController.start()
            .onSuccess { mutableMonitoringState.value = MonitoringState.ACTIVE }
            .onFailure { onMonitoringUnavailable() }
    }

    override fun onLocationSample(sample: LocationSample) {
        val valid = sample.accuracyMeters in 0f..config.maximumLocationAccuracyMeters && sample.speedMetersPerSecond != null
        mutableDiagnostics.value = mutableDiagnostics.value.copy(
            speedKph = sample.speedMetersPerSecond?.times(3.6),
            accuracyMeters = sample.accuracyMeters,
            lastSampleElapsedRealtimeMillis = sample.elapsedRealtimeMillis,
            acceptedSamples = mutableDiagnostics.value.acceptedSamples + if (valid) 1 else 0,
            rejectedSamples = mutableDiagnostics.value.rejectedSamples + if (valid) 0 else 1,
        )
        if (!valid) return
        val speed = sample.speedMetersPerSecond ?: return
        when {
            mutableDriveState.value == DriveState.DRIVING && speed >= config.minimumVehicleSpeedMetersPerSecond -> cancelLowSpeedCountdown()
            mutableDriveState.value == DriveState.DRIVING -> startTripEndCountdown()
            mutableDriverDecision.value == DriverDecision.PASSENGER && speed >= config.minimumVehicleSpeedMetersPerSecond -> cancelLowSpeedCountdown()
            mutableDriverDecision.value == DriverDecision.PASSENGER -> startPassengerResetCountdown()
            mutableDriverDecision.value == DriverDecision.UNKNOWN && speed >= config.minimumVehicleSpeedMetersPerSecond -> {
                mutableDriveState.value = DriveState.MOVEMENT_DETECTED
                mutableDriveState.value = DriveState.CONFIRMING_DRIVER
            }
        }
    }

    private fun startTripEndCountdown() {
        if (lowSpeedJob != null) return
        mutableDiagnostics.value = mutableDiagnostics.value.copy(lowSpeedCountdownActive = true)
        lowSpeedJob = scope.launch {
            delay(config.tripEndStationaryDurationMillis)
            lowSpeedJob = null
            mutableDiagnostics.value = mutableDiagnostics.value.copy(lowSpeedCountdownActive = false)
            endTrip()
        }
    }

    private fun startPassengerResetCountdown() {
        if (lowSpeedJob != null) return
        mutableDiagnostics.value = mutableDiagnostics.value.copy(lowSpeedCountdownActive = true)
        lowSpeedJob = scope.launch {
            delay(config.tripEndStationaryDurationMillis)
            lowSpeedJob = null
            mutableDiagnostics.value = mutableDiagnostics.value.copy(lowSpeedCountdownActive = false)
            mutableDriverDecision.value = DriverDecision.UNKNOWN
        }
    }

    private fun cancelLowSpeedCountdown() {
        lowSpeedJob?.cancel()
        lowSpeedJob = null
        mutableDiagnostics.value = mutableDiagnostics.value.copy(lowSpeedCountdownActive = false)
    }

    override fun confirmDriver() {
        if (mutableDriveState.value != DriveState.CONFIRMING_DRIVER) return
        monitorController.stop()
        locationJob?.cancel()
        locationJob = null
        tripTrackingController.start()
            .onSuccess {
                mutableDriverDecision.value = DriverDecision.DRIVER
                mutableDriveState.value = DriveState.DRIVING
            }
            .onFailure { onMonitoringUnavailable() }
    }

    override fun markPassenger() {
        if (mutableDriveState.value != DriveState.CONFIRMING_DRIVER) return
        mutableDriverDecision.value = DriverDecision.PASSENGER
        mutableDriveState.value = DriveState.IDLE
    }

    override fun endTrip() {
        if (mutableDriveState.value != DriveState.DRIVING) return
        cancelLowSpeedCountdown()
        tripTrackingController.stop()
        onTrackingStopped()
    }

    override fun onTrackingStopped() {
        cancelLowSpeedCountdown()
        monitorController.stop()
        locationJob?.cancel()
        locationJob = null
        mutableMonitoringState.value = MonitoringState.STOPPED
        if (mutableDriverDecision.value == DriverDecision.DRIVER) mutableDriveState.value = DriveState.POSSIBLE_TRIP_END
    }

    override fun stopMonitoring() {
        cancelLowSpeedCountdown()
        monitorController.stop()
        tripTrackingController.stop()
        locationJob?.cancel()
        locationJob = null
        mutableMonitoringState.value = MonitoringState.STOPPED
        mutableDriveState.value = DriveState.IDLE
        mutableDriverDecision.value = DriverDecision.UNKNOWN
    }

    override fun reset() {
        cancelLowSpeedCountdown()
        mutableDriverDecision.value = DriverDecision.UNKNOWN
        mutableDriveState.value = DriveState.IDLE
    }

    override fun onMonitoringUnavailable() {
        mutableMonitoringState.value = MonitoringState.UNAVAILABLE
    }

    override fun onLocationPermissionDenied(permanently: Boolean) {
        mutableMonitoringState.value = if (permanently) {
            MonitoringState.LOCATION_PERMISSION_PERMANENTLY_DENIED
        } else {
            MonitoringState.LOCATION_PERMISSION_DENIED
        }
    }
}
