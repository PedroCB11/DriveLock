package com.drivelock.app.domain.model

data class Trip(
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val durationMillis: Long? = null,
    val distanceMeters: Double = 0.0,
    val averageSpeedKph: Double? = null,
    val maxSpeedKph: Double? = null,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val endLatitude: Double? = null,
    val endLongitude: Double? = null,
)

