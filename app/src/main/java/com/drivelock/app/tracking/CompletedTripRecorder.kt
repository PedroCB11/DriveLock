package com.drivelock.app.tracking

import com.drivelock.app.domain.model.Trip
import com.drivelock.app.domain.repository.TripRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompletedTripRecorder(
    sessionManager: TripSessionManager,
    private val repository: TripRepository,
    scope: CoroutineScope,
) {
    private val mutableLastCompletedTrip = MutableStateFlow<Trip?>(null)
    val lastCompletedTrip = mutableLastCompletedTrip.asStateFlow()
    private val mutablePersistenceError = MutableStateFlow<Throwable?>(null)
    val persistenceError = mutablePersistenceError.asStateFlow()

    private val recordingJob = scope.launch {
            sessionManager.completedSessions.collect { state ->
                state.toTrip()?.let { trip ->
                    runCatching { repository.insertTrip(trip) }
                        .onSuccess { id -> mutableLastCompletedTrip.value = trip.copy(id = id) }
                        .onFailure { error -> mutablePersistenceError.value = error }
                }
            }
    }

    fun stop() = recordingJob.cancel()
}

fun TripSessionState.toTrip(): Trip? {
    val start = startTimeMillis ?: return null
    val end = endTimeMillis ?: return null
    return Trip(
        startTime = start,
        endTime = end,
        durationMillis = elapsedMillis,
        distanceMeters = distanceMeters,
        averageSpeedKph = averageSpeedMetersPerSecond * METERS_PER_SECOND_TO_KPH,
        maxSpeedKph = maximumSpeedMetersPerSecond * METERS_PER_SECOND_TO_KPH,
        startLatitude = startLatitude,
        startLongitude = startLongitude,
        endLatitude = endLatitude,
        endLongitude = endLongitude,
    )
}

private const val METERS_PER_SECOND_TO_KPH = 3.6
