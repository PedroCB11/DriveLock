package com.drivelock.app.detection.location

data class LocationSample(
    val latitude: Double,
    val longitude: Double,
    val speedMetersPerSecond: Float?,
    val accuracyMeters: Float,
    val elapsedRealtimeMillis: Long,
)
