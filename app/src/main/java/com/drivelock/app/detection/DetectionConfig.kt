package com.drivelock.app.detection

data class DetectionConfig(
    val minimumVehicleSpeedMetersPerSecond: Float = 5.5556f,
    val maximumLocationAccuracyMeters: Float = 50f,
    val tripEndStationaryDurationMillis: Long = 180_000,
)
