package com.drivelock.app.detection

import com.drivelock.app.detection.location.LocationSample
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TripEndDetector(private val config: DetectionConfig = DetectionConfig()) {
    private val mutableProbableTripEnd = MutableStateFlow(false)
    val probableTripEnd = mutableProbableTripEnd.asStateFlow()
    private var vehicleExited = false
    private var lowSpeedSinceMillis: Long? = null
    private var latestSpeedIsLow = false

    fun startSession() {
        vehicleExited = false
        lowSpeedSinceMillis = null
        latestSpeedIsLow = false
        mutableProbableTripEnd.value = false
    }

    fun onVehiclePresenceChanged(inVehicle: Boolean, elapsedRealtimeMillis: Long) {
        if (inVehicle) {
            vehicleExited = false
            lowSpeedSinceMillis = null
            mutableProbableTripEnd.value = false
        } else {
            vehicleExited = true
            if (latestSpeedIsLow && lowSpeedSinceMillis == null) lowSpeedSinceMillis = elapsedRealtimeMillis
            evaluate(elapsedRealtimeMillis)
        }
    }

    fun onLocation(sample: LocationSample) {
        if (sample.accuracyMeters !in 0f..config.maximumLocationAccuracyMeters) return
        val speed = sample.speedMetersPerSecond ?: return
        latestSpeedIsLow = speed <= config.tripEndMaximumSpeedMetersPerSecond
        if (latestSpeedIsLow) {
            if (lowSpeedSinceMillis == null) lowSpeedSinceMillis = sample.elapsedRealtimeMillis
        } else {
            lowSpeedSinceMillis = null
            mutableProbableTripEnd.value = false
        }
        evaluate(sample.elapsedRealtimeMillis)
    }

    fun tick(elapsedRealtimeMillis: Long) {
        if (latestSpeedIsLow) evaluate(elapsedRealtimeMillis)
    }

    fun reset() = startSession()

    private fun evaluate(nowMillis: Long) {
        val lowSince = lowSpeedSinceMillis ?: return
        if (vehicleExited && nowMillis - lowSince >= config.tripEndStationaryDurationMillis) {
            mutableProbableTripEnd.value = true
        }
    }
}
