package com.drivelock.app.tracking

import com.drivelock.app.detection.location.LocationSample
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class TripSessionState(
    val isActive: Boolean = false,
    val startTimeMillis: Long? = null,
    val endTimeMillis: Long? = null,
    val elapsedMillis: Long = 0,
    val distanceMeters: Double = 0.0,
    val currentSpeedMetersPerSecond: Float = 0f,
    val averageSpeedMetersPerSecond: Double = 0.0,
    val maximumSpeedMetersPerSecond: Float = 0f,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val endLatitude: Double? = null,
    val endLongitude: Double? = null,
)

class TripSessionManager {
    private val mutableState = MutableStateFlow(TripSessionState())
    val state = mutableState.asStateFlow()
    private val completedSessionChannel = Channel<TripSessionState>(Channel.UNLIMITED)
    val completedSessions = completedSessionChannel.receiveAsFlow()
    private var startElapsedRealtimeMillis = 0L
    private var lastSample: LocationSample? = null

    fun start(startTimeMillis: Long, elapsedRealtimeMillis: Long) {
        startElapsedRealtimeMillis = elapsedRealtimeMillis
        lastSample = null
        mutableState.value = TripSessionState(isActive = true, startTimeMillis = startTimeMillis)
    }

    fun updateElapsed(elapsedRealtimeMillis: Long) {
        if (!mutableState.value.isActive) return
        val elapsed = (elapsedRealtimeMillis - startElapsedRealtimeMillis).coerceAtLeast(0)
        updateMetrics(elapsedMillis = elapsed)
    }

    fun addLocation(sample: LocationSample) {
        val current = mutableState.value
        if (!current.isActive || sample.accuracyMeters !in 0f..MAX_ACCURACY_METERS) return
        val previous = lastSample
        if (previous != null && sample.elapsedRealtimeMillis <= previous.elapsedRealtimeMillis) return

        var distance = current.distanceMeters
        if (previous != null) {
            val segment = distanceMeters(previous, sample)
            val seconds = (sample.elapsedRealtimeMillis - previous.elapsedRealtimeMillis) / 1_000.0
            if (segment / seconds > MAX_PLAUSIBLE_SPEED_METERS_PER_SECOND) return
            distance += segment
        }
        lastSample = sample
        val elapsed = (sample.elapsedRealtimeMillis - startElapsedRealtimeMillis).coerceAtLeast(current.elapsedMillis)
        val speed = sample.speedMetersPerSecond?.coerceAtLeast(0f) ?: 0f
        updateMetrics(
            elapsedMillis = elapsed,
            distanceMeters = distance,
            currentSpeed = speed,
            maximumSpeed = maxOf(current.maximumSpeedMetersPerSecond, speed),
            sample = sample,
        )
    }

    fun end(endTimeMillis: Long = System.currentTimeMillis()): TripSessionState {
        val current = mutableState.value
        val finalState = current.copy(
            isActive = false,
            endTimeMillis = endTimeMillis,
            currentSpeedMetersPerSecond = 0f,
        )
        mutableState.value = finalState
        if (current.isActive) completedSessionChannel.trySend(finalState)
        lastSample = null
        return finalState
    }

    private fun updateMetrics(
        elapsedMillis: Long = mutableState.value.elapsedMillis,
        distanceMeters: Double = mutableState.value.distanceMeters,
        currentSpeed: Float = mutableState.value.currentSpeedMetersPerSecond,
        maximumSpeed: Float = mutableState.value.maximumSpeedMetersPerSecond,
        sample: LocationSample? = null,
    ) {
        val average = if (elapsedMillis > 0) distanceMeters / (elapsedMillis / 1_000.0) else 0.0
        mutableState.value = mutableState.value.copy(
            elapsedMillis = elapsedMillis,
            distanceMeters = distanceMeters,
            currentSpeedMetersPerSecond = currentSpeed,
            averageSpeedMetersPerSecond = average,
            maximumSpeedMetersPerSecond = maximumSpeed,
            startLatitude = mutableState.value.startLatitude ?: sample?.latitude,
            startLongitude = mutableState.value.startLongitude ?: sample?.longitude,
            endLatitude = sample?.latitude ?: mutableState.value.endLatitude,
            endLongitude = sample?.longitude ?: mutableState.value.endLongitude,
        )
    }

    private fun distanceMeters(first: LocationSample, second: LocationSample): Double {
        val lat1 = Math.toRadians(first.latitude)
        val lat2 = Math.toRadians(second.latitude)
        val deltaLat = lat2 - lat1
        val deltaLon = Math.toRadians(second.longitude - first.longitude)
        val a = sin(deltaLat / 2) * sin(deltaLat / 2) + cos(lat1) * cos(lat2) * sin(deltaLon / 2) * sin(deltaLon / 2)
        return 2 * EARTH_RADIUS_METERS * asin(sqrt(a))
    }

    private companion object {
        const val EARTH_RADIUS_METERS = 6_371_000.0
        const val MAX_ACCURACY_METERS = 50f
        const val MAX_PLAUSIBLE_SPEED_METERS_PER_SECOND = 80.0
    }
}
