package com.drivelock.app.detection

data class DetectionConfig(
    val minimumVehicleSpeedMetersPerSecond: Float = 5.5f,
    val minimumVehicleSpeedDurationMillis: Long = 10_000,
    val minimumValidSpeedSamples: Int = 3,
    val maximumLocationAccuracyMeters: Float = 50f,
    val tripEndMaximumSpeedMetersPerSecond: Float = 1.5f,
    val tripEndStationaryDurationMillis: Long = 60_000,
)
