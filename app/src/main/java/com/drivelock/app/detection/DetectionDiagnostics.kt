package com.drivelock.app.detection

data class DetectionDiagnostics(
    val speedKph: Double? = null,
    val accuracyMeters: Float? = null,
    val lastSampleElapsedRealtimeMillis: Long? = null,
    val acceptedSamples: Long = 0,
    val rejectedSamples: Long = 0,
    val lowSpeedCountdownActive: Boolean = false,
    val speedThresholdKph: Double = 20.0,
    val stopDelaySeconds: Long = 180,
)
