package com.drivelock.app.detection.location

import com.drivelock.app.detection.DetectionConfig

class VehicleSpeedVerifier(private val config: DetectionConfig) {
    private var firstValidTimestamp: Long? = null
    private var validSampleCount = 0
    private var lastTimestamp: Long? = null

    fun add(sample: LocationSample): Boolean {
        val timestampIsValid = lastTimestamp?.let { sample.elapsedRealtimeMillis > it } ?: true
        lastTimestamp = sample.elapsedRealtimeMillis
        val isValid = timestampIsValid &&
            sample.accuracyMeters in 0f..config.maximumLocationAccuracyMeters &&
            sample.speedMetersPerSecond?.let { it >= config.minimumVehicleSpeedMetersPerSecond } == true
        if (!isValid) {
            resetSpeedWindow()
            return false
        }

        validSampleCount += 1
        val start = firstValidTimestamp ?: sample.elapsedRealtimeMillis.also { firstValidTimestamp = it }
        return validSampleCount >= config.minimumValidSpeedSamples &&
            sample.elapsedRealtimeMillis - start >= config.minimumVehicleSpeedDurationMillis
    }

    fun reset() {
        lastTimestamp = null
        resetSpeedWindow()
    }

    private fun resetSpeedWindow() {
        firstValidTimestamp = null
        validSampleCount = 0
    }
}
