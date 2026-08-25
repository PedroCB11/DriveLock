package com.drivelock.app.tracking

import com.drivelock.app.detection.location.LocationSample
import com.drivelock.app.domain.model.Trip
import com.drivelock.app.domain.repository.TripRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompletedTripRecorderTest {
    @Test fun `completed session is converted and persisted exactly once`() = runTest {
        val manager = TripSessionManager()
        val repository = FakeTripRepository()
        val recorderScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val recorder = CompletedTripRecorder(manager, repository, recorderScope)

        manager.start(startTimeMillis = 10_000, elapsedRealtimeMillis = 1_000)
        manager.addLocation(LocationSample(-23.5, -46.6, 10f, 10f, 1_000))
        manager.addLocation(LocationSample(-23.501, -46.601, 20f, 10f, 11_000))
        manager.end(endTimeMillis = 20_000)
        runCurrent()

        assertEquals(null, recorder.persistenceError.value)
        assertEquals(1, repository.inserted.size)
        val trip = repository.inserted.single()
        assertEquals(10_000L, trip.startTime)
        assertEquals(20_000L, trip.endTime)
        assertEquals(10_000L, trip.durationMillis)
        assertEquals(72.0, trip.maxSpeedKph ?: 0.0, 0.001)
        assertEquals(-23.5, trip.startLatitude ?: 0.0, 0.0)
        assertEquals(-23.501, trip.endLatitude ?: 0.0, 0.0)
        assertEquals(42L, recorder.lastCompletedTrip.value?.id)

        manager.end(endTimeMillis = 21_000)
        runCurrent()
        assertEquals(1, repository.inserted.size)
        recorder.stop()
    }

    @Test fun `incomplete session cannot be converted to trip`() {
        assertEquals(null, TripSessionState().toTrip())
        assertNotNull(TripSessionState(startTimeMillis = 1, endTimeMillis = 2).toTrip())
    }
}

private class FakeTripRepository : TripRepository {
    val inserted = mutableListOf<Trip>()
    private val trips = MutableStateFlow<List<Trip>>(emptyList())
    override fun getAllTrips(): Flow<List<Trip>> = trips
    override fun getTripById(id: Long): Flow<Trip?> = MutableStateFlow(trips.value.firstOrNull { it.id == id })
    override suspend fun insertTrip(trip: Trip): Long {
        inserted += trip
        trips.value = listOf(trip.copy(id = 42))
        return 42
    }
    override suspend fun deleteTrip(trip: Trip) = Unit
    override suspend fun deleteAllTrips() = Unit
}
